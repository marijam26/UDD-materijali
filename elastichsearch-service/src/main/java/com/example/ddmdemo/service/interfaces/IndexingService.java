package com.example.ddmdemo.service.interfaces;

import com.example.ddmdemo.dto.AgencyContractDTO;
import com.example.ddmdemo.dto.AgencyContractValuesDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface IndexingService {

    String indexDocument(AgencyContractDTO documentFile);
    AgencyContractValuesDTO parseDocumentContent(MultipartFile multipartPdfFile);
}
