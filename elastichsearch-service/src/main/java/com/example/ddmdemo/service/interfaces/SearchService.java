package com.example.ddmdemo.service.interfaces;

import com.example.ddmdemo.indexmodel.AgencyContract;
import com.example.ddmdemo.indexmodel.DummyIndex;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface SearchService {

    Page<AgencyContract> simpleSearch(List<String> keywords, Pageable pageable);

    Page<AgencyContract> advancedSearch(List<String> expression, Pageable pageable);
}
