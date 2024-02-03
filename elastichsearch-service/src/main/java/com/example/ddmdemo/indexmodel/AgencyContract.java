package com.example.ddmdemo.indexmodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Getter
@Setter
@NoArgsConstructor
@Document(indexName = "contracts")
public class AgencyContract {

    @Id
    private String id;

    @Field(type = FieldType.Text, store = true, name = "employeeName")
    private String employeeName;

    @Field(type = FieldType.Text, store = true, name = "employeeSurname")
    private String employeeSurname;

    @Field(type = FieldType.Text, store = true, name = "governmentName")
    private String governmentName;

    @Field(type = FieldType.Text, store = true, name = "levelOfAdministration")
    private String levelOfAdministration;

    @Field(type = FieldType.Text, store = true, name = "content")
    private String content;

    @Field(type = FieldType.Text, store = true, name = "path")
    private String path;

    @Field(type = FieldType.Text, store = true, name = "address")
    private String address;

    @GeoPointField
    @Field(store = true, name = "location")
    private GeoPoint location;

    @Field(type = FieldType.Integer, store = true, name = "databaseId")
    private Integer databaseId;

}