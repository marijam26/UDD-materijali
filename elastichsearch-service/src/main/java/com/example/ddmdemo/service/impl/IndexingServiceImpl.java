package com.example.ddmdemo.service.impl;

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
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        AgencyContractValuesDTO valuesDTO = parseDocumentContent(documentFile);
        newIndex.setEmployeeName(valuesDTO.getEmployeeName());
        newIndex.setEmployeeSurname(valuesDTO.getEmployeeSurname());
        newIndex.setGovernmentName(valuesDTO.getGovernmentName());
        newIndex.setAddress(valuesDTO.getAddress());
        newIndex.setLevelOfAdministration(valuesDTO.getLevelOfAdministration());
        newIndex.setContent(valuesDTO.getContent());

        var title = Objects.requireNonNull(documentFile.getOriginalFilename()).split("\\.")[0];
        newEntity.setTitle(title);

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
            values = extractData(documentContent);
            pdDocument.close();
        } catch (IOException e) {
            throw new LoadingException("Error while trying to load PDF file content.");
        }

        return values;
    }

    private AgencyContractValuesDTO extractData(String text) {
        AgencyContractValuesDTO values = new AgencyContractValuesDTO();

        String governmentNameRegex = "Uprava\\s+za\\s+(.*)";
        Pattern pattern = Pattern.compile(governmentNameRegex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            System.out.println(matcher.group());
            String name = matcher.group().split("Uprava za")[1];
            values.setGovernmentName(name.trim());
        }

        String levelRegex = "Nivo\\s+uprave:\\s+(.*)";
        pattern = Pattern.compile(levelRegex);
        matcher = pattern.matcher(text);

        if (matcher.find()) {
            System.out.println(matcher.group());
            String name = matcher.group().split("Nivo uprave:")[1];
            values.setLevelOfAdministration(name.trim());

        }

        String adressRegex = values.getLevelOfAdministration()+",\\s+(.*)\\(format";
        pattern = Pattern.compile(adressRegex);
        matcher = pattern.matcher(text);

        if (matcher.find()) {
            System.out.println(matcher.group());
            String name = "";
            if(matcher.group().contains(")")){
                name = matcher.group().split("\\)")[1];
            }else{
                name = matcher.group().split(",")[1];
            }

            values.setAddress(name.split("\\(")[0].trim());
        }

        String[] lines = text.split("\r\n \r\n");
        values.setContent(lines[2].trim());

//        String[] words = values.getContent().split(" ");
//        String lastWord = words[words.length-1];
//        String nameRegex = lastWord+"\r\n\\s+(.*)_______________________";
//        pattern = Pattern.compile(nameRegex);
//        matcher = pattern.matcher(text);
//
//        if (matcher.find()) {
//            System.out.println(matcher.group());
//            //String name = matcher.group().split(",")[1];
//            values.setEmployeeName(matcher.group());
//            values.setEmployeeSurname("p");
//        }
        String[] names = lines[3].split("  ");
        values.setEmployeeName(names[4].split(" ")[1]);
        values.setEmployeeSurname(names[4].split(" ")[2]);
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