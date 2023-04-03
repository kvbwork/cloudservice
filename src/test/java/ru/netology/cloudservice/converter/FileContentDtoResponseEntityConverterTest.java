package ru.netology.cloudservice.converter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.netology.cloudservice.model.dto.FileContentDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class FileContentDtoResponseEntityConverterTest {
    private static final String TEST_CONTENT = "test_content_bytes";
    private static final String TEST_HASH = "test_hash";
    private static FileContentDtoResponseEntityConverter sut;

    @BeforeAll
    static void beforeAll() {
        sut = new FileContentDtoResponseEntityConverter();
    }

    @Test
    @DisplayName("from(FileContentDto) конвертация успешна")
    void from_filecontentdto_success() throws IOException {
        var outputStream = new ByteArrayOutputStream();
        var fileContentDto = new FileContentDto(new ByteArrayInputStream(TEST_CONTENT.getBytes()), TEST_HASH);
        var resultEntity = sut.from(fileContentDto);
        inspectHttpEntity(resultEntity);
    }

    @Test
    @DisplayName("from(InputStream,hash) конвертация успешна")
    void from_inputstream_success() throws IOException {
        var resultEntity = sut.from(new ByteArrayInputStream(TEST_CONTENT.getBytes()), TEST_HASH);
        inspectHttpEntity(resultEntity);
    }

    private void inspectHttpEntity(ResponseEntity<StreamingResponseBody> resultEntity) throws IOException {
        var contentType = resultEntity.getHeaders().getContentType();
        var outputStream = new ByteArrayOutputStream();
        resultEntity.getBody().writeTo(outputStream);
        var actualBody = new String(outputStream.toByteArray());

        assertThat(resultEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(contentType.isCompatibleWith(MediaType.MULTIPART_FORM_DATA), is(true));
        assertThat(contentType.getParameter("boundary"), not(blankOrNullString()));
        assertThat(actualBody, containsString("name=\"file\""));
        assertThat(actualBody, containsString("name=\"hash\""));
        assertThat(actualBody, containsString(TEST_CONTENT));
        assertThat(actualBody, containsString(TEST_HASH));
    }
}