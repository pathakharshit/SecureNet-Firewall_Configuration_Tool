package com.example.firewall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.firewall.service.NetworkUtils;

@Controller

public class WebServerController {

    @Autowired
    private NetworkUtils networkUtils;

    @GetMapping("/site1")
    public String getSite1(Model model) {
        if (networkUtils.isPortAllowed(8081)) {
            return "site1"; // templates/site1.html page return
        } else {
            return "deny_page";        }
    }

    @GetMapping("/site2")
    public String getSite2(Model model) {
        if (networkUtils.isPortAllowed(8082)) {
            return "site2";
        } else {
            return "deny_page";        }
    }

    @GetMapping("/site3")
    public String getSite3(Model model) {
        if (networkUtils.isPortAllowed(8083)) {
            return "site3";
        } else {
            return "deny_page";
        }
    }

    @GetMapping("/site4")
    public String getSite4(Model model) {
        if (networkUtils.isPortAllowed(8084)) {
            return "site4";
        } else {
            return "deny_page";
        }
    }
}
