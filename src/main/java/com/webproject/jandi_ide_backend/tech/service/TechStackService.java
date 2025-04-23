package com.webproject.jandi_ide_backend.tech.service;

import com.webproject.jandi_ide_backend.tech.dto.RespTechStackDTO;
import com.webproject.jandi_ide_backend.tech.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechStackService {
    private final TechStackRepository techStackRepository;

    public List<RespTechStackDTO> getTechStack() {
        return techStackRepository.findAll()
                .stream().map(RespTechStackDTO::new).toList();
    }
}
