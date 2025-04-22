package com.webproject.jandi_ide_backend.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(force = true)
public class ReqTechStackDTO {
    List<String> techStackNameList = new ArrayList<>();
}
