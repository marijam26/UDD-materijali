package com.example.ddmdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgencyContractValuesDTO {

    private String employeeName;

    private String employeeSurname;

    private String governmentName;

    private String levelOfAdministration;

    private String content;

    private String address;
}
