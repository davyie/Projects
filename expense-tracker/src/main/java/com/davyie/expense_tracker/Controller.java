package com.davyie.expense_tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Controller {

    private Logger LOG = LoggerFactory.getLogger(Controller.class);

    @GetMapping("/")
    public String hello() {
        return "Hello World!";
    }
}
