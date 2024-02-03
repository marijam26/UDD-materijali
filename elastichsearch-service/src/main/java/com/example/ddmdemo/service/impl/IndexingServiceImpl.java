package com.example.ddmdemo.service.impl;

import com.example.ddmdemo.exceptionhandling.exception.LoadingException;
import com.example.ddmdemo.exceptionhandling.exception.StorageException;
import com.example.ddmdemo.indexmodel.AgencyContract;
import com.example.ddmdemo.indexmodel.DummyIndex;
import com.example.ddmdemo.indexrepository.AgencyContractRepository;
import com.example.ddmdemo.indexrepository.DummyIndexRepository;
import com.example.ddmdemo.model.ContractTable;
import com.example.ddmdemo.model.DummyTable;
import com.example.ddmdemo.respository.ContractTableRepository;
import com.example.ddmdemo.respository.DummyRepository;
import com.example.ddmdemo.service.interfaces.FileService;
import com.example.ddmdemo.service.interfaces.IndexingService;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.language.detect.LanguageDetector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final AgencyContractRepository dummyIndexRepository;

    private final ContractTableRepository dummyRepository;

    private final FileService fileService;

    private final LanguageDetector languageDetector;


    @Override
    @Transactional
    public String indexDocument(MultipartFile documentFile) {
        var newEntity = new ContractTable();
        var newIndex = new AgencyContract();

        var title = Objects.requireNonNull(documentFile.getOriginalFilename()).split("\\.")[0];
        // newIndex.setTitle(title);
        newEntity.setTitle(title);

        var documentContent = extractDocumentContent(documentFile);
        newIndex.setContent(documentContent);
//        if (detectLanguage(documentContent).equals("SR")) {
//            newIndex.setContentSr(documentContent);
//        } else {
//            newIndex.setContentEn(documentContent);
//        }
        newEntity.setTitle(title);

        var serverFilename = fileService.store(documentFile, UUID.randomUUID().toString());
        //newIndex.setServerFilename(serverFilename);
        newEntity.setServerFilename(serverFilename);

        newEntity.setMimeType(detectMimeType(documentFile));
        var savedEntity = dummyRepository.save(newEntity);

        newIndex.setDatabaseId(savedEntity.getId());
        dummyIndexRepository.save(newIndex);

        return serverFilename;
    }

    @Override
    public String parseDocumentContent(MultipartFile multipartPdfFile) {
        String documentContent;
        try (var pdfFile = multipartPdfFile.getInputStream()) {
            var pdDocument = PDDocument.load(pdfFile);
            var textStripper = new PDFTextStripper();
            documentContent = textStripper.getText(pdDocument);
            System.out.println(documentContent);
            pdDocument.close();
        } catch (IOException e) {
            throw new LoadingException("Error while trying to load PDF file content.");
        }

        return documentContent;
    }

    private String extractDocumentContent(MultipartFile multipartPdfFile) {
        String documentContent;
        try (var pdfFile = multipartPdfFile.getInputStream()) {
            var pdDocument = PDDocument.load(pdfFile);
            var textStripper = new PDFTextStripper();
            documentContent = textStripper.getText(pdDocument);
            pdDocument.close();
        } catch (IOException e) {
            throw new LoadingException("Error while trying to load PDF file content.");
        }

        return documentContent;
    }

    private String detectLanguage(String text) {
        var detectedLanguage = languageDetector.detect(text).getLanguage().toUpperCase();
        if (detectedLanguage.equals("HR")) {
            detectedLanguage = "SR";
        }

        return detectedLanguage;
    }

    private String detectMimeType(MultipartFile file) {
        var contentAnalyzer = new Tika();

        String trueMimeType;
        String specifiedMimeType;
        try {
            trueMimeType = contentAnalyzer.detect(file.getBytes());
            specifiedMimeType =
                Files.probeContentType(Path.of(Objects.requireNonNull(file.getOriginalFilename())));
        } catch (IOException e) {
            throw new StorageException("Failed to detect mime type for file.");
        }

        if (!trueMimeType.equals(specifiedMimeType) &&
            !(trueMimeType.contains("zip") && specifiedMimeType.contains("zip"))) {
            throw new StorageException("True mime type is different from specified one, aborting.");
        }

        return trueMimeType;
    }
}