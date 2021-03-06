package com.example.myapplication;

public class Message {
    private String text;
    private long timestamp;
    private String fromId;
    private String toId;
    private boolean isAudio = false;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public boolean getIsAudio() {
        return isAudio;
    }

    public void setIsAudio(boolean isAudio) {
        this.isAudio = isAudio;
    }
}