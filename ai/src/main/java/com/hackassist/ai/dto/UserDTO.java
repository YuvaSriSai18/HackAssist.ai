package com.hackassist.ai.dto;

public class UserDTO {
    private String uid;
    private String name;
    private String photoUrl;
    private String email;

    public UserDTO() {}

    public UserDTO(String uid, String name, String photoUrl, String email) {
        this.uid = uid;
        this.name = name;
        this.photoUrl = photoUrl;
        this.email = email;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
