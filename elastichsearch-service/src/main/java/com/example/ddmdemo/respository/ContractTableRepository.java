package com.example.ddmdemo.respository;

import com.example.ddmdemo.model.ContractTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractTableRepository extends JpaRepository<ContractTable,Integer> {
}
