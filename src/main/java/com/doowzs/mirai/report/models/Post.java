package com.doowzs.mirai.report.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post {

    private String name;
    private List<Single> list;

    public Post() {
    }

    public Post(String name) {
        this.name = name;
        this.list = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Single> getList() {
        return list;
    }

    public void setList(List<Single> list) {
        this.list = list;
    }

    public static class Single {

        private String content;
        private Date date;

        public Single() {
        }

        public Single(String content, Date date) {
            this.content = content;
            this.date = date;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

    }

}
