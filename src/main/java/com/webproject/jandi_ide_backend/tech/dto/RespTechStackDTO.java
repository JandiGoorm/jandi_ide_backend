package com.webproject.jandi_ide_backend.tech.dto;

import com.webproject.jandi_ide_backend.tech.entity.TechStack;
import lombok.Data;

@Data
public class RespTechStackDTO {
    private final Integer id;
    private final String techStack;

    public RespTechStackDTO(TechStack techStack) {
        this.id = techStack.getId();
        this.techStack = techStack.getName();
    }
}
