package com.doowzs.mirai.report.controllers;

import com.doowzs.mirai.report.mirai.MiraiConfig;
import com.doowzs.mirai.report.models.Report;
import com.doowzs.mirai.report.repositories.ReportRepository;
import com.doowzs.mirai.report.models.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@Controller
public class MainController {

    private final MiraiConfig config;
    private final ReportRepository repository;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MainController(MiraiConfig config, ReportRepository repository) {
        this.config = config;
        this.repository = repository;
    }

    @GetMapping("/")
    public String viewCommands() {
        return "commands";
    }

    @GetMapping("/{day}")
    public String viewReport(@PathVariable String day, @RequestParam String token, Model model) {
        Report report = repository.findOne(Example.of(new Report(day)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!Objects.equals(token, report.getToken())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        List<User> users = config.getUsers();
        users.removeIf(u -> report.getPosts().containsKey(u.getId()));
        model.addAttribute("report", report);
        model.addAttribute("users", users);

        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(format.parse(day));
            calendar1.add(Calendar.DATE, -1);
            String yesterday = format.format(calendar1.getTime());
            Report previous = repository.findOne(Example.of(new Report(yesterday))).orElse(null);
            model.addAttribute("previous", previous);

            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(format.parse(day));
            calendar2.add(Calendar.DATE, 1);
            String tomorrow = format.format(calendar2.getTime());
            Report next = repository.findOne(Example.of(new Report(tomorrow))).orElse(null);
            model.addAttribute("next", next);
        } catch (Exception ignored) {
        }

        return "report";
    }

}
