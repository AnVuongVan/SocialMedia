package com.vietis.media.model;

import com.google.firebase.database.PropertyName;

public class ModelChat {
    private String message, receiver, sender, timestamp, type;
    private boolean checked;

    public ModelChat() {}

    public ModelChat(String message, String receiver, String sender, String timestamp, String type, boolean checked) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timestamp = timestamp;
        this.type = type;
        this.checked = checked;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @PropertyName("checked")
    public boolean isChecked() {
        return checked;
    }

    @PropertyName("checked")
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

}
