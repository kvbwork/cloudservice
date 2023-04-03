package ru.netology.cloudservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.InputStream;

@Getter
@AllArgsConstructor
public class FileContentDto {

    private final InputStream inputStream;
    private final String hash;

}
