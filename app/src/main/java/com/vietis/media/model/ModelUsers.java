package com.vietis.media.model;

public class ModelUsers {
    private String uid, name, email, onlineStatus, phone, image, cover, typingTo;
    private boolean isBlocked;

    public ModelUsers() {
    }

    public ModelUsers(String uid, String name, String email, String onlineStatus, String phone, String image, String cover, String typingTo, boolean isBlocked) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.onlineStatus = onlineStatus;
        this.phone = phone;
        this.image = image;
        this.cover = cover;
        this.typingTo = typingTo;
        this.isBlocked = isBlocked;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
