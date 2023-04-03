package ru.netology.cloudservice.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileInfoDto {

    private final String filename;

    private final long size;

    @JsonIgnore
    private final String hash;

}
