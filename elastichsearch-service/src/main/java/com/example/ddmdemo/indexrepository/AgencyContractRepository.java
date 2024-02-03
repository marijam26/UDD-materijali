package com.example.ddmdemo.indexrepository;

import com.example.ddmdemo.indexmodel.AgencyContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgencyContractRepository extends ElasticsearchRepository<AgencyContract, String> {
    @Query("{\"bool\" : " +
            "{\"should\" : [ " +
            "{\"field\" : {\"employeeName\" : \"?\"}}, " +
            "{\"field\" : {\"employeeSurname\" : \"?\"}}, " +
            "{\"field\" : {\"governmentName\" : \"?\"}} " +
            "{\"field\" : {\"levelOfAdministration\" : \"?\"}} " +
            "{\"field\" : {\"content\" : \"?\"}} " +
            "]}}")
    Page<AgencyContract> findByEmployeeNameOrEmployeeSurnameOrGovernmentNameOrLevelOfAdministration(String employeeName, String employeeSurname, String governmentName, String levelOfAdministration,String content, Pageable pageable);
}
