package com.main.farforlow.entity;

import com.opencsv.bean.CsvBindByName;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "aiportindex")
public class Airport {
    @Id
    @CsvBindByName(column = "ident")
    private String id;

    @Field(type = FieldType.Text, name = "city")
    @CsvBindByName(column = "municipality")
    private String city;

    @Field(type = FieldType.Keyword, name = "countryCode")
    @CsvBindByName(column = "iso_country")
    private String countryCode;

    @Field(type = FieldType.Keyword, name = "codeIata")
    @CsvBindByName(column = "iata_code")
    private String codeIata;

    @Field(type = FieldType.Text, name = "name")
    @CsvBindByName(column = "name")
    private String name;

    @Field(type = FieldType.Keyword, name = "type")
    @CsvBindByName(column = "type")
    private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCodeIata() {
        return codeIata;
    }

    public void setCodeIata(String codeIata) {
        this.codeIata = codeIata;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Airport{" +
                "id='" + id + '\'' +
                ", city='" + city + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", codeIata='" + codeIata + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
