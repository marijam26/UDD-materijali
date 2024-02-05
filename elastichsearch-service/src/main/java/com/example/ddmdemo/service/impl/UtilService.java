package com.example.ddmdemo.service.impl;

import com.example.ddmdemo.dto.AgencyContractValuesDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UtilService {

    public GeoPoint getLocationFromAddress(String strAddress){
        try{
            strAddress = strAddress.replaceAll(" ", "%20");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.geoapify.com/v1/geocode/search?text="+strAddress+"&apiKey=edff0ba2d6d545279a82d4d37402a851"))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(response.body());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            JsonNode featureNode = jsonNode.path("features").path(0);
            double latitude = featureNode.path("geometry").path("coordinates").get(1).asDouble();
            double longitude = featureNode.path("geometry").path("coordinates").get(0).asDouble();

            System.out.println("Latitude: " + latitude);
            System.out.println("Longitude: " + longitude);
            return new GeoPoint(latitude, longitude);
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }


    public String saveFile(MultipartFile file) throws IOException {
        String fileName = null;
        if (! file.isEmpty()) {
            fileName = UUID.randomUUID() + ".pdf";
            byte[] bytes = file.getBytes();
            Path path = Paths.get("src/main/resources/files" + File.separator + fileName);
            Files.write(path, bytes);
        }
        return fileName;
    }

    public Resource download(String filename) throws Exception {
        try {
            Path path = Paths.get("src/main/resources/files/" + filename);
            return new ByteArrayResource(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new Exception("Error while reading file!");
        }
    }

    public AgencyContractValuesDTO extractData(String text) {
        AgencyContractValuesDTO values = new AgencyContractValuesDTO();

        String governmentNameRegex = "Uprava\\s+za\\s+(.*)";
        Pattern pattern = Pattern.compile(governmentNameRegex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String name = matcher.group().split("Uprava za")[1];
            values.setGovernmentName(name.trim());
        }

        String levelRegex = "Nivo\\s+uprave:\\s+(.*)";
        pattern = Pattern.compile(levelRegex);
        matcher = pattern.matcher(text);

        if (matcher.find()) {
            String name = matcher.group().split("Nivo uprave:")[1];
            values.setLevelOfAdministration(name.trim());

        }

        String adressRegex = values.getLevelOfAdministration()+",\\s+(.*)\\(format";
        pattern = Pattern.compile(adressRegex);
        matcher = pattern.matcher(text);

        if (matcher.find()) {
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

        String[] names = lines[3].split("  ");
        values.setEmployeeName(names[4].split(" ")[1]);
        values.setEmployeeSurname(names[4].split(" ")[2]);
        return values;
    }

}
