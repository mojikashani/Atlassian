package moji.test.hipchat.modules;

/**
 * Created by mojtaba on 16/04/2016.
 */
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleExtractor {
    private static final Pattern TITLE_TAG =
            Pattern.compile("\\<title>(.*)\\</title>", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);

    public static String getPageTitle(String url){
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();

            // ContentType is an inner class defined below
            ContentType contentType = getContentTypeHeader(conn);
            if (contentType == null || !contentType.contentType.equals("text/html"))
                return "Error"; // don't continue if not HTML
            else {
                // determine the charset, or use the default
                Charset charset = getCharset(contentType);
                if (charset == null)
                    charset = Charset.defaultCharset();

                // read the response body, using BufferedReader for performance
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
                int n = 0, totalRead = 0;
                char[] buf = new char[1024];
                StringBuilder content = new StringBuilder();

                // read until EOF or first 8192 characters
                while (totalRead < 8192 && (n = reader.read(buf, 0, buf.length)) != -1) {
                    content.append(buf, 0, n);
                    totalRead += n;
                }
                reader.close();

                // asyncExtract the title
                Matcher matcher = TITLE_TAG.matcher(content);
                if (matcher.find()) {
                /* replace any occurrences of whitespace (which may
                 * include line feeds and other uglies) as well
                 * as HTML brackets with a space */
                    return matcher.group(1).replaceAll("[\\s\\<>]+", " ").trim();
                } else
                    return "Error";
            }
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return "Error: " + ex.getMessage();
        }
    }

    private static ContentType getContentTypeHeader(URLConnection conn) {
        int i = 0;
        boolean moreHeaders = true;
        do {
            String headerName = conn.getHeaderFieldKey(i);
            String headerValue = conn.getHeaderField(i);
            if (headerName != null && headerName.equals("Content-Type"))
                return new ContentType(headerValue);

            i++;
            moreHeaders = headerName != null || headerValue != null;
        }
        while (moreHeaders);

        return null;
    }

    private static Charset getCharset(ContentType contentType) {
        if (contentType != null && contentType.charsetName != null && Charset.isSupported(contentType.charsetName))
            return Charset.forName(contentType.charsetName);
        else
            return null;
    }

    private static final class ContentType {
        private static final Pattern CHARSET_HEADER = Pattern.compile("charset=([-_a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);

        private String contentType;
        private String charsetName;
        private ContentType(String headerValue) {
            if (headerValue == null)
                throw new IllegalArgumentException("ContentType must be constructed with a not-null headerValue");
            int n = headerValue.indexOf(";");
            if (n != -1) {
                contentType = headerValue.substring(0, n);
                Matcher matcher = CHARSET_HEADER.matcher(headerValue);
                if (matcher.find())
                    charsetName = matcher.group(1);
            }
            else
                contentType = headerValue;
        }
    }
}
