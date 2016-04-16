package moji.test.hipchat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import moji.test.hipchat.models.IExtractCallback;
import moji.test.hipchat.modules.ContentExtractor;

public class MainActivity extends AppCompatActivity {
    private EditText etJson;
    private EditText etMessage;
    private ContentExtractor extractor = new ContentExtractor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getViews();
    }

    private void getViews() {
        etJson = (EditText)findViewById(R.id.etJson);
        etMessage = (EditText)findViewById(R.id.etMessage);
    }

    public void onExtractButtonClick(View view)
    {
        final ProgressDialog dlg = ProgressDialog.show(MainActivity.this, getString(R.string.please_wait), getString(R.string.retrieve_web_title), true);
        extractor.asyncExtract(etMessage.getText(), new IExtractCallback() {
            @Override
            public void onCallback(String jsonContent) {
                dlg.dismiss();
                etJson.setText(jsonContent);
            }
        });

    }
}
