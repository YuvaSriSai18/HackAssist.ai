package com.hackassist.ai.models;
/*
    Here we actually writing all the getters and setters but if we use lombok dependency we can just write
    ```
        @Data -> Generates getters, setters, toString, equals, hashCode
        @NoArgsConstructor -> Default constructor
        @AllArgsConstructor -> Constructor with all fields
        public class User {
            private String uid;
            private String name;
            private String photoUrl ;
            private String email ;
        }
    ```
    This will automatically initialize all the getters and setters
*/
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    private String uid;

    @Column(nullable = false)
    private String name;

    private String photoUrl;

    @Column(nullable = false, unique = true)
    private String email;

    public User() {}

    public User(String uid, String name, String photoUrl, String email) {
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