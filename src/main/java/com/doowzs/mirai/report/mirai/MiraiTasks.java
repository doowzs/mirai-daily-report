package com.doowzs.mirai.report.mirai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.doowzs.mirai.report.models.Report;
import com.doowzs.mirai.report.models.User;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MiraiTasks {

    private final MiraiConfig config;
    private final MiraiService service;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    MiraiTasks(MiraiConfig config, MiraiService service) {
        this.config = config;
        this.service = service;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void createReport() {
        service.createReportOfDay();
    }

    @Scheduled(cron = "0 30 8 * * MON-FRI")
    public void sendReportCall() throws IOException {
        service.sendGroupMessage("早上好！请大家 @日报 提交今天的日报！");
    }

    @Scheduled(cron = "0 30 8 * * SAT-SUN")
    public void sendWeekendCall() throws IOException {
        service.sendGroupMessage("早上好！今天是快乐的周末！");
    }

    @Scheduled(cron = "0 30 10 * * MON-FRI")
    public void sendReportSummary() throws IOException {
        Report report = service.getReportOfDay();
        List<User> users = config.getUsers();
        users.removeIf(u -> report.getPosts().containsKey(u.getId()));

        String msg1 = users.size() == 0 ? "今天大家都提交了日报！"
                : String.format("今天%s没有提交日报！",
                users.stream().map(User::getName).collect(Collectors.joining("、")));
        String msg2 = String.format("访问%s/%s?token=%s查看今天的日报。",
                config.getWebUrl(), report.getDate(), report.getToken());
        service.sendGroupMessage(msg1 + msg2);
    }

}
