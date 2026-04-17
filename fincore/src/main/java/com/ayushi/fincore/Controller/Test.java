package com.ayushi.fincore.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
public class Test {
    @GetMapping("/test")
    public String test() {
        return "Protected API working";
    }
}
