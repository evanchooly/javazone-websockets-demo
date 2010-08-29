package com.antwerkz.stickies;

public class Note {
    private String id = String.valueOf(StickiesApplication.ids.incrementAndGet());
    private String text = "";
    private String timestamp = String.valueOf(System.currentTimeMillis());
    private String left = "300";
    private String top = "300";
    private String zIndex = "0";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTop() {
        return top;
    }

    public void setTop(String top) {
        this.top = top;
    }

    public String getzIndex() {
        return zIndex;
    }

    public void setzIndex(String zIndex) {
        this.zIndex = zIndex;
    }

    @Override
    public String toString() {
        return "var noteArray = {" +
            "id:'" + id + '\'' +
            ", text:'" + text + '\'' +
            ", timestamp:'" + timestamp + '\'' +
            ", left:'" + left + '\'' +
            ", top:'" + top + '\'' +
            ", zIndex:'" + zIndex + '\'' +
            '}';
    }

}
