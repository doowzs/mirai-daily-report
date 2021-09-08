package com.doowzs.mirai.report.mirai;

import com.doowzs.mirai.report.models.Report;
import com.doowzs.mirai.report.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MiraiHandler implements WebSocketHandler {

    private final MiraiConfig config;
    private final MiraiService service;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MiraiHandler(MiraiConfig config, MiraiService service) {
        this.config = config;
        this.service = service;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("Connected to mirai-api-http-ws");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        logger.info("Disconnected from mirai-api-http-ws");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        logger.debug(String.format("Received %s", message.getPayload()));
        try {
            MiraiEvent event = mapper.readValue((String) message.getPayload(), MiraiEvent.class);
            String report; // Java sucks, I don't want to use lambda and write hundreds of try ... catch :(
            List<String> commands;
            if ((report = service.parseReportEvent(event)) != null) {
                handleReportEvent(event, report);
            } else if ((commands = service.parseCommandEvent(event)) != null) {
                handleCommandEvent(event, commands);
            }
        } catch (Exception e) {
            logger.error(String.format("%s %s", e.getClass().getName(), e.getMessage()), e);
            try {
                service.sendGroupMessage(String.format("野生的%s出现了！请查看日志！", e.getClass().getName()));
            } catch (Exception ignored) {
            }
        }
    }

    private void handleReportEvent(MiraiEvent event, String content) {
        User user = service.getEventUser(event);
        if (user.getName() == null) {
            service.sendGroupMessage(String.format("提交日报失败：用户%d不在提交日报的列表中！", user.getId()));
            logger.info(String.format("UnknownUser %d", user.getId()));
        } else {
            String reply;
            if (content.isEmpty()) {
                reply = "日报内容不能为空！";
            } else {
                Report report = service.updateReport(user, content);
                logger.info(String.format("UpdateReport %d %s %s", user.getId(), user.getName(), content));
                reply = String.format(" 提交日报成功，访问%s/%s?token=%s查看。",
                        config.getWebUrl(), report.getDate(), report.getToken());
            }

            MiraiMessage message1 = new MiraiMessage();
            MiraiMessage message2 = new MiraiMessage();
            message1.setType("At");
            message1.setTarget(user.getId());
            message2.setType("Plain");
            message2.setText(reply);
            service.sendGroupMessage(message1, message2);
        }
    }

    protected void handleCommandEvent(MiraiEvent event, List<String> commands) {
        if (commands.isEmpty()) {
            service.sendGroupMessage(String.format("请输入指令名称！指令大全请查看%s。", config.getWebUrl()));
        } else if (commands.get(0).equals("帮助")) {
            service.sendGroupMessage(String.format("%s", config.getWebUrl()));
        } else if (commands.get(0).equals("查看")) {
            if (commands.size() < 2) {
                service.sendGroupMessage("请输入查看日期，格式YYYY-MM-DD！");
                return;
            }
            Optional<Report> optionalReport = service.getReportOfDay(commands.get(1));
            if (optionalReport.isEmpty()) {
                service.sendGroupMessage(String.format("没有日期为%s的日报！", commands.get(1)));
            } else {
                Report report = optionalReport.get();
                service.sendGroupMessage(String.format("%s/%s?token=%s",
                        config.getWebUrl(), report.getDate(), report.getToken()));
            }
        } else if (commands.get(0).equals("用户列表")) {
            List<User> users = config.getUsers();
            service.sendGroupMessage(users.stream()
                    .map(u -> u.getId() + " " + u.getName())
                    .collect(Collectors.joining("\n")));
        } else if (commands.get(0).equals("今日总结")) {
            service.sendSummaryOfDay();
        } else {
            service.sendGroupMessage(String.format("无法处理指令%s！指令大全请查看%s。", commands.get(0), config.getWebUrl()));
        }
    }

}
