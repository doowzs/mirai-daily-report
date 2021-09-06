package com.doowzs.mirai.report.mirai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MiraiEvent {

    private Long syncId;
    private Data data;

    public Long getSyncId() {
        return syncId;
    }

    public void setSyncId(Long syncId) {
        this.syncId = syncId;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {

        private String type;
        private Long target;
        private Sender sender;
        private List<MiraiMessage> messageChain;

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

        public Sender getSender() {
            return sender;
        }

        public void setSender(Sender sender) {
            this.sender = sender;
        }

        public List<MiraiMessage> getMessageChain() {
            return messageChain;
        }

        public void setMessageChain(List<MiraiMessage> messageChain) {
            this.messageChain = messageChain;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sender {

        private Long id;
        private Sender group;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Sender getGroup() {
            return group;
        }

        public void setGroup(Sender group) {
            this.group = group;
        }

    }

}
