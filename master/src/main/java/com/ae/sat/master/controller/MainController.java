package com.ae.sat.master.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by ae on 23-11-16.
 */
@Controller
public class MainController {

    @RequestMapping("/")
    public String index() {
        return "index.html";
    }
}
