package com.local.bookmark.bean;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class Bookmark {
    private String seq;
    private int pageIndex = -1;
    private String title;
    private List<Bookmark> subBookMarks = new ArrayList<>();

    public Bookmark(String title, int pageIndex) {
        this.pageIndex = pageIndex;
        this.title = title;
    }

    public Bookmark(String seq, String title, int pageIndex) {
        this.pageIndex = pageIndex;
        this.title = title;
        this.seq = seq;
    }

    public Bookmark(String title) {
        this.title = title;
    }


    public void addSubBookMark(Bookmark kid) {
        subBookMarks.add(kid);
    }

    public void addSubBookMarkBySeq(Bookmark kid) {

        for (Bookmark bookmark : subBookMarks) {
            if (kid.getSeq().startsWith(bookmark.getSeq() + ".")) {
                bookmark.addSubBookMarkBySeq(kid);
                return;
            }
        }
        subBookMarks.add(kid);
    }


    public HashMap<String, Object> outlines() {
        HashMap<String, Object> root = new HashMap<String, Object>();
        root.put("Title", (getSeq() != null ? getSeq() + " " : "") + getTitle());
        root.put("Action", "GoTo");
        if (pageIndex >= 0)
            root.put("Page", String.format("%d Fit", pageIndex));
        ArrayList<HashMap<String, Object>> kids = new ArrayList<HashMap<String, Object>>();
        if (subBookMarks != null && !subBookMarks.isEmpty()) {
            for (Bookmark bookmark : subBookMarks) {
                kids.add(bookmark.outlines());
            }
            root.put("Kids", kids);
        }

        return root;
    }

    @Override
    public String toString() {
        String indent = "- ";
        StringBuffer sb = new StringBuffer();
        if (getSeq() != null) {
            sb.append(getSeq());
            sb.append(" ");
        }

        sb.append(getTitle());

        if (getSubBookMarks() != null && !getSubBookMarks().isEmpty()) {
            for (Bookmark bookmark : getSubBookMarks()) {
                sb.append("\n");
                sb.append(indent);
                sb.append(bookmark.toString().replaceAll(indent, indent + indent));
            }
        }

        return sb.toString();
    }
}
