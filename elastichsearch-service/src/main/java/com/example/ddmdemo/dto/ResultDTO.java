package com.example.ddmdemo.dto;

import com.example.ddmdemo.indexmodel.AgencyContract;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultDTO {

    public Page<AgencyContract> pages;
    public List<String> highlighters;


}
