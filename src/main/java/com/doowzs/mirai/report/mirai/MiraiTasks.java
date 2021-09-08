package com.doowzs.mirai.report.mirai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MiraiTasks {

    private final MiraiService service;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    MiraiTasks(MiraiService service) {
        this.service = service;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void createReport() {
        service.createReportOfDay();
    }

    @Scheduled(cron = "0 30 8 * * MON-FRI")
    public void sendReportCall() {
        service.sendGroupMessage("早上好！今天又是新的一天！\n"
                + "请大家 @日报 总结昨天的工作或者今天做了什么。");
    }

    @Scheduled(cron = "0 30 8 * * SAT-SUN")
    public void sendWeekendCall() {
        service.sendGroupMessage("早上好！今天是快乐的周末！");
    }

    @Scheduled(cron = "0 00 23 * * MON-FRI")
    public void sendReportSummary() {
        service.sendSummaryOfDay(true);
    }

}
