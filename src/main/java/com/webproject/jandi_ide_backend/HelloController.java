package com.webproject.jandi_ide_backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("")
    public String hello() {
        return "메인 브랜치 최종 업데이트 250411";
    }
}
