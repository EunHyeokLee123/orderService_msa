package com.playdata.firstservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/first-service")
@Slf4j
public class FirstController {

    @GetMapping("/welcome")
    public String welcome() {
        log.info("/welcome: GET으로 들어옴. firstservice");
        return "Welcome to First Service";
    }

}
