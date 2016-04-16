package moji.test.hipchat.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mojtaba on 16/04/2016.
 */
public class JsonContent {
    public List<String> mentions;
    public List<String> emotions;
    public List<URLContent> links;

    public JsonContent()
    {
        mentions = new ArrayList<>();
        emotions = new ArrayList<>();
        links = new ArrayList<>();
    }
}
