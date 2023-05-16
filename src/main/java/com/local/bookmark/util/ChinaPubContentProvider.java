package com.local.bookmark.util;

import com.local.bookmark.action.ContentsProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class ChinaPubContentProvider implements ContentsProvider {
    @Override
    public String getContentsByUrl(String url) {
        String contents = null;
        try {
            Document doc = Jsoup.connect(url).get();
            String contentsHtml = doc.select("#ml + div").first().html().replaceAll("<br>", "###");
            contents = Jsoup.parse(contentsHtml).text().replaceAll("###", "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return contents;
    }
}
