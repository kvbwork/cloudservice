package ru.netology.cloudservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.netology.cloudservice.converter.FileContentDtoResponseEntityConverter;
import ru.netology.cloudservice.model.dto.FileContentDto;
import ru.netology.cloudservice.model.dto.FileInfoDto;
import ru.netology.cloudservice.model.request.RenameRequest;
import ru.netology.cloudservice.service.UserFilesService;
import ru.netology.cloudservice.validation.ValidFileName;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
public class UserFilesController {
    private static final String DEFAULT_FILES_LIST_LIMIT = "100";

    private final UserFilesService userFilesService;
    private final FileContentDtoResponseEntityConverter fileContentDtoResponseEntityConverter;

    @GetMapping("/list")
    public List<FileInfoDto> getFilesList(
            Principal principal,
            @Positive @RequestParam(name = "limit", defaultValue = DEFAULT_FILES_LIST_LIMIT) int limit
    ) throws IOException {
        return userFilesService.findFilesList(principal.getName(), limit);
    }

    @DeleteMapping("/file")
    public void deleteFile(
            Principal principal,
            @ValidFileName @RequestParam String filename
    ) throws IOException {
        userFilesService.deleteFile(principal.getName(), filename);
    }

    @PutMapping("/file")
    public void renameFile(
            Principal principal,
            @ValidFileName @RequestParam String filename,
            @Valid @RequestBody RenameRequest renameRequest
    ) throws IOException {
        userFilesService.renameFile(principal.getName(), filename, renameRequest.getFilename());
    }

    @GetMapping(value = "/file")
    public ResponseEntity<StreamingResponseBody> downloadFile(
            Principal principal,
            @ValidFileName @RequestParam String filename
    ) throws IOException {
        FileContentDto fileContentDto = userFilesService.openFile(principal.getName(), filename);
        return fileContentDtoResponseEntityConverter.from(fileContentDto);
    }

    @PostMapping(value = "/file")
    public void uploadFile(
            Principal principal,
            @ValidFileName @RequestParam String filename,
            @RequestPart MultipartFile file,
            @RequestParam(defaultValue = "") String hash
    ) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            userFilesService.saveFile(principal.getName(), filename, hash, inputStream);
        }
    }

}
