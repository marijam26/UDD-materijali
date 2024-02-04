package com.example.ddmdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

public record AgencyContractDTO(MultipartFile file) {
}
