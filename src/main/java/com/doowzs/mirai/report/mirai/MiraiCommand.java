package com.doowzs.mirai.report.mirai;

import java.util.List;

public class MiraiCommand {

    private Long syncId;
    private String command;
    private String subCommand;
    private Content content;

    public Long getSyncId() {
        return syncId;
    }

    public void setSyncId(Long syncId) {
        this.syncId = syncId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getSubCommand() {
        return subCommand;
    }

    public void setSubCommand(String subCommand) {
        this.subCommand = subCommand;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public static class Content {

        private Long target;
        private List<MiraiMessage> messageChain;

        public Long getTarget() {
            return target;
        }

        public void setTarget(Long target) {
            this.target = target;
        }

        public List<MiraiMessage> getMessageChain() {
            return messageChain;
        }

        public void setMessageChain(List<MiraiMessage> messageChain) {
            this.messageChain = messageChain;
        }

    }

}
