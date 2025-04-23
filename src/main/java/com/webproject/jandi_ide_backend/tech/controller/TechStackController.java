package com.webproject.jandi_ide_backend.tech.controller;

import com.webproject.jandi_ide_backend.tech.dto.RespTechStackDTO;
import com.webproject.jandi_ide_backend.tech.service.TechStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tech-stack")
public class TechStackController {
    private final TechStackService techStackService;

    @GetMapping("")
    public List<RespTechStackDTO> getTechStack() {
        return techStackService.getTechStack();
    }
}
