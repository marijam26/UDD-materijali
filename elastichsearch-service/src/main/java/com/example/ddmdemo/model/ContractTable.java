package com.example.ddmdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contract_table")
public class ContractTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "server_filename")
    private String serverFilename;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;
}
