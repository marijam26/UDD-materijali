package com.example.ddmdemo.controller;

import com.example.ddmdemo.dto.AgencyContractDTO;
import com.example.ddmdemo.dto.DummyDocumentFileDTO;
import com.example.ddmdemo.dto.DummyDocumentFileResponseDTO;
import com.example.ddmdemo.service.interfaces.IndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/index")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class IndexController {

    private final IndexingService indexingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DummyDocumentFileResponseDTO addDocumentFile(
        @ModelAttribute AgencyContractDTO documentFile) {
        var serverFilename = indexingService.indexDocument(documentFile.file());
        return new DummyDocumentFileResponseDTO(serverFilename);
    }
}
