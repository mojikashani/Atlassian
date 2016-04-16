package moji.test.hipchat.modules;

import android.os.Handler;
import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import moji.test.hipchat.models.IExtractCallback;
import moji.test.hipchat.models.JsonContent;
import moji.test.hipchat.models.URLContent;

/**
 * Created by mojtaba on 16/04/2016.
 */
public class ContentExtractor {
    private Handler handler;
    private Thread thread;
    private static final Pattern PATTERN_MENTION =
            Pattern.compile("\\@[a-z]+", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);

    private static final Pattern PATTERN_EMOTION =
            Pattern.compile("\\([a-z]{1,15}\\)", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);

    private static final Pattern PATTERN_URL = Patterns.WEB_URL;

    public ContentExtractor()
    {
        handler = new Handler();
    }

    public void asyncExtract(final CharSequence message, final IExtractCallback callback)
    {
        thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                final String jsonContent = extract(message);

                handler.post(new Runnable()
                {
                    @Override
                    public void run() {
                        callback.onCallback(jsonContent);
                    }
                });
            }
        });
        thread.start();
    }

    private String extract(final CharSequence message)
    {
        JsonContent content = new JsonContent();

        Matcher matcher = PATTERN_MENTION.matcher(message);
        while (matcher.find()) {
            String mention = matcher.group();
            content.mentions.add(mention.substring(1));
        }

        matcher = PATTERN_EMOTION.matcher(message);
        while (matcher.find()) {
            String emotion = matcher.group();
            content.emotions.add(emotion.substring(1, emotion.length()-1));
        }

        matcher = PATTERN_URL.matcher(message);
        while (matcher.find()) {
            String url = matcher.group();
            content.links.add(new URLContent(url, TitleExtractor.getPageTitle(url)));
        }

        return generateJsonString(content);
    }

    private String generateJsonString(JsonContent content)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("{");

        for(int i=0; i<content.mentions.size();i++){
            if(i == 0)
            {
                builder.append("\"mentions\": [\""+content.mentions.get(i)+"\"");
            }else {
                builder.append(",\""+content.mentions.get(i)+"\"");
            }
        }
        if(content.mentions.size()>0)
        {
            builder.append("]");
        }

        for(int i=0; i<content.emotions.size();i++){
            if(i == 0)
            {
                if(builder.length()>1)
                    builder.append(",");

                builder.append("\"emotions\": [\""+content.emotions.get(i)+"\"");
            }else {
                builder.append(",\""+content.emotions.get(i)+"\"");
            }
        }
        if(content.emotions.size()>0)
        {
            builder.append("]");
        }

        for(int i=0; i<content.links.size();i++){
            if(i == 0)
            {
                if(builder.length()>1)
                    builder.append(",");

                builder.append("\"links\": [{\"url\": \""+content.links.get(i).url+"\""
                +",\"title\": \""+content.links.get(i).pageTitle+"\"}");
            }else {
                builder.append(",{\"url\": \""+content.links.get(i).url+"\""
                        +",\"title\": \""+content.links.get(i).pageTitle+"\"}");
            }
        }
        if(content.links.size()>0)
        {
            builder.append("]");
        }

        builder.append("}");

        return builder.toString();
    }

}
