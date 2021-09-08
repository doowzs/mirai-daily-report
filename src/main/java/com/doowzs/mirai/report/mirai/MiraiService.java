package com.doowzs.mirai.report.mirai;

import com.doowzs.mirai.report.models.Post;
import com.doowzs.mirai.report.models.Report;
import com.doowzs.mirai.report.models.User;
import com.doowzs.mirai.report.repositories.ReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MiraiService {

    private final MiraiConfig config;
    private final MiraiSession session;
    private final ReportRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MiraiService(MiraiConfig config, ReportRepository repository) {
        this.config = config;
        this.session = new MiraiSession(config, new MiraiHandler(config, this));
        this.repository = repository;
    }

    public boolean isFromGroup(MiraiEvent event) {
        return event.getData() != null && Objects.equals(event.getData().getType(), "GroupMessage")
                && event.getData().getSender() != null
                && event.getData().getSender().getGroup() != null
                && Objects.equals(event.getData().getSender().getGroup().getId(), config.getGroupId());
    }

    public String parseReportEvent(MiraiEvent event) {
        if (!isFromGroup(event)) {
            return null;
        }

        List<MiraiMessage> messages = event.getData().getMessageChain();
        boolean isAtReport = messages.size() >= 3 && messages.get(1) != null && messages.get(2) != null
                && Objects.equals(messages.get(1).getType(), "At")
                && Objects.equals(messages.get(1).getTarget(), config.getBotId())
                && Objects.equals(messages.get(2).getType(), "Plain");
        if (isAtReport) {
            return messages.get(2).getText().stripLeading();
        }

        String plainReportPrefix = "@日报";
        boolean isPlainReport = messages.size() >= 2 && messages.get(1) != null
                && Objects.equals(messages.get(1).getType(), "Plain")
                && messages.get(1).getText().startsWith(plainReportPrefix);
        return isPlainReport ? messages.get(1).getText().substring(plainReportPrefix.length()).stripLeading() : null;
    }

    public List<String> parseCommandEvent(MiraiEvent event) {
        if (!isFromGroup(event)) {
            return null;
        }

        List<MiraiMessage> messages = event.getData().getMessageChain();
        boolean isCommandEvent = messages.size() >= 2 && messages.get(1) != null
                && Objects.equals(messages.get(1).getType(), "Plain")
                && messages.get(1).getText().startsWith("/日报");
        return isCommandEvent
                ? Arrays.stream(messages.get(1).getText().split(" ")).skip(1).collect(Collectors.toList())
                : null;
    }

    public User getEventUser(MiraiEvent event) {
        Long id = event.getData().getSender().getId();
        config.getUsers().forEach(u -> logger.debug(String.format("Available user %d %s", u.getId(), u.getName())));
        return config.getUsers().stream()
                .filter(u -> Objects.equals(u.getId(), id))
                .findFirst().orElse(new User(id));
    }

    public Report getReportOfDay() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return getReportOfDay(format.format(new Date())).orElse(createReportOfDay());
    }

    public Optional<Report> getReportOfDay(String day) {
        return repository.findOne(Example.of(new Report(day)));
    }

    public Report createReportOfDay() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Report report = new Report(format.format(new Date()), UUID.randomUUID().toString());
        repository.save(report);
        return report;
    }

    public Report updateReport(User user, String content) {
        Report report = getReportOfDay();
        Post.Single single = new Post.Single(content, new Date());
        if (!report.getPosts().containsKey(user.getId())) {
            report.getPosts().put(user.getId(), new Post(user.getName()));
        }
        report.getPosts().get(user.getId()).getList().add(single);
        repository.save(report);
        return report;
    }

    public void sendSummaryOfDay() {
        sendSummaryOfDay(false);
    }

    public void sendSummaryOfDay(boolean atUsers) {
        Report report = getReportOfDay();
        List<User> users = config.getUsers();
        users.removeIf(u -> report.getPosts().containsKey(u.getId()));

        DateFormat format = new SimpleDateFormat("hh:mm");
        StringBuilder builder = new StringBuilder();
        builder.append(users.size() == 0 ? "今天大家都发了日报！"
                : String.format("今天%s没有发日报！",
                users.stream().map(User::getName).collect(Collectors.joining("、"))));
        report.getPosts().forEach((number, post) -> {
            builder.append(String.format("\n\n%s：\n", post.getName()));
            post.getList().forEach(single ->
                    builder.append(String.format("- [%s] %s\n", format.format(single.getDate()), single.getContent())));
        });
        builder.append(String.format("\n\n访问%s/%s?token=%s查看今天的日报。",
                config.getWebUrl(), report.getDate(), report.getToken()));
        MiraiMessage message = MiraiMessage.Plain(builder.toString());

        List<MiraiMessage> messages = new ArrayList<>(List.of(message));
        if (atUsers) {
            users.forEach(user -> messages.add(MiraiMessage.At(user.getId())));
        }
        sendGroupMessage(messages.toArray(new MiraiMessage[0]));
    }

    public void sendGroupMessage(String message) {
        MiraiMessage msg = new MiraiMessage();
        msg.setType("Plain");
        msg.setText(message);
        sendGroupMessage(msg);
    }

    public void sendGroupMessage(MiraiMessage... messages) {
        MiraiCommand.Content content = new MiraiCommand.Content();
        content.setTarget(config.getGroupId());
        content.setMessageChain(Arrays.stream(messages).collect(Collectors.toList()));
        sendCommand(-1L, "sendGroupMessage", null, content);
    }

    public void sendCommand(Long syncId, String command, String subCommand, MiraiCommand.Content content) {
        MiraiCommand cmd = new MiraiCommand();
        cmd.setSyncId(syncId);
        cmd.setCommand(command);
        cmd.setSubCommand(subCommand);
        cmd.setContent(content);
        try {
            session.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(cmd)));
        } catch (IOException e) {
            logger.error(String.format("Cannot send command: %s", e.getMessage()), e);
        }
    }

}
