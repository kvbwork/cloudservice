package ru.netology.cloudservice.converter;

import lombok.NoArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.netology.cloudservice.model.dto.FileContentDto;

import java.io.InputStream;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Component
@NoArgsConstructor
public class FileContentDtoResponseEntityConverter {

    public ResponseEntity<StreamingResponseBody> from(FileContentDto fileContentDto) {
        return from(fileContentDto.getInputStream(), fileContentDto.getHash());
    }

    public ResponseEntity<StreamingResponseBody> from(InputStream inputStream, String hash) {
        HttpEntity httpEntity = buildHttpEntity(inputStream, hash);
        return ResponseEntity.ok()
                .header(CONTENT_TYPE, httpEntity.getContentType().getValue())
                .body(httpEntity::writeTo);
    }

    private static HttpEntity buildHttpEntity(InputStream inputStream, String hash) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", inputStream);
        builder.addTextBody("hash", hash);
        return builder.build();
    }

}
