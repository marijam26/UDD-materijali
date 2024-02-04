package com.example.ddmdemo.controller;

import com.example.ddmdemo.dto.SearchQueryDTO;
import com.example.ddmdemo.indexmodel.AgencyContract;
import com.example.ddmdemo.indexmodel.DummyIndex;
import com.example.ddmdemo.service.interfaces.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/simple")
    public Page<AgencyContract> simpleSearch(@RequestBody SearchQueryDTO simpleSearchQuery,
                                             Pageable pageable) {
        return searchService.simpleSearch(simpleSearchQuery.getKeywords(), pageable);
    }

    @PostMapping("/advanced")
    public Page<AgencyContract> advancedSearch(@RequestBody SearchQueryDTO advancedSearchQuery,
                                           Pageable pageable) {
        return searchService.advancedSearch(advancedSearchQuery.getKeywords(), pageable);
    }
}
