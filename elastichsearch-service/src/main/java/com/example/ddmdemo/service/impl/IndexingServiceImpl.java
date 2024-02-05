package com.example.ddmdemo.service.impl;

import com.example.ddmdemo.dto.AgencyContractDTO;
import com.example.ddmdemo.dto.AgencyContractValuesDTO;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.language.detect.LanguageDetector;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final AgencyContractRepository dummyIndexRepository;

    private final ContractTableRepository dummyRepository;

    private final FileService fileService;

    private final UtilService utilService;


    @Override
    @Transactional
    public String indexDocument(AgencyContractDTO document) {
        var documentFile = document.file();
        var newEntity = new ContractTable();
        var newIndex = new AgencyContract();

        try {
            String path = utilService.saveFile(documentFile);
            newIndex.setPath(path);
        } catch (IOException e) {
            e.printStackTrace();
        }


        if(document.type().equals("ugovor")){
            AgencyContractValuesDTO valuesDTO = parseDocumentContent(documentFile);
            newIndex.setEmployeeName(valuesDTO.getEmployeeName());
            newIndex.setEmployeeSurname(valuesDTO.getEmployeeSurname());
            newIndex.setGovernmentName(valuesDTO.getGovernmentName());
            newIndex.setAddress(valuesDTO.getAddress());
            newIndex.setLocation(utilService.getLocationFromAddress(valuesDTO.getAddress()));
            newIndex.setLevelOfAdministration(valuesDTO.getLevelOfAdministration());
            newIndex.setContent(valuesDTO.getContent());
        }


        var title = Objects.requireNonNull(documentFile.getOriginalFilename()).split("\\.")[0];
        newEntity.setTitle(title);
        newIndex.setTitle(title);

        var documentContent = extractDocumentContent(documentFile);
        newIndex.setFullContent(documentContent);

        var serverFilename = fileService.store(documentFile, UUID.randomUUID().toString());
        newEntity.setServerFilename(serverFilename);

        newEntity.setMimeType(detectMimeType(documentFile));
        var savedEntity = dummyRepository.save(newEntity);

        newIndex.setDatabaseId(savedEntity.getId());
        dummyIndexRepository.save(newIndex);

        return serverFilename;
    }

    @Override
    public AgencyContractValuesDTO parseDocumentContent(MultipartFile multipartPdfFile) {
        String documentContent;
        AgencyContractValuesDTO values;
        try (var pdfFile = multipartPdfFile.getInputStream()) {
            var pdDocument = PDDocument.load(pdfFile);
            var textStripper = new PDFTextStripper();
            documentContent = textStripper.getText(pdDocument);
            values = utilService.extractData(documentContent);
            pdDocument.close();
        } catch (IOException e) {
            throw new LoadingException("Error while trying to load PDF file content.");
        }

        return values;
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