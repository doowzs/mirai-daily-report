package com.doowzs.mirai.report.mirai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MiraiMessage {

    private String type;
    private Long target;
    private String text;
    private String display;

    public MiraiMessage() {
    }

    public static MiraiMessage At(Long number) {
        MiraiMessage message = new MiraiMessage();
        message.setType("At");
        message.setTarget(number);
        return message;
    }

    public static MiraiMessage Plain(String text) {
        MiraiMessage message = new MiraiMessage();
        message.setType("Plain");
        message.setText(text);
        return message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTarget() {
        return target;
    }

    public void setTarget(Long target) {
        this.target = target;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

}