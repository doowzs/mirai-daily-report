package com.doowzs.mirai.report.models;

import org.springframework.data.annotation.Id;

import java.util.HashMap;

public class Report {

    @Id
    private String id;
    private String date;
    private String token;
    private HashMap<Long, Post> posts;

    public Report() {}

    public Report(String date) {
        this.date = date;
    }

    public Report(String date, String token) {
        this.date = date;
        this.token = token;
        this.posts = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public HashMap<Long, Post> getPosts() {
        return posts;
    }

    public void setPosts(HashMap<Long, Post> posts) {
        this.posts = posts;
    }

}
