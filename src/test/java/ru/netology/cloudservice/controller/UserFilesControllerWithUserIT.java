package ru.netology.cloudservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.netology.cloudservice.model.dto.FileContentDto;
import ru.netology.cloudservice.model.dto.FileInfoDto;
import ru.netology.cloudservice.model.request.RenameRequest;
import ru.netology.cloudservice.service.UserFilesService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.netology.cloudservice.controller.UserFilesControllerWithUserIT.USER_NAME;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@WithMockUser(username = USER_NAME)
class UserFilesControllerWithUserIT {
    private static final Random RANDOM = new Random();
    private static final byte[] TEST_BYTES_ARRAY = "Hello World!".getBytes();

    public static final String USER_NAME = "user";
    private static final String FILE_NAME = "FILENAME";
    private static final String BAD_FILE_NAME = "??FILE/../NAME??.*";
    private static final String FILE_NAME_QUERY_PARAM = "filename";
    private static final String LIMIT_QUERY_PARAM = "limit";

    @MockBean
    private UserFilesService userFilesService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /list возвращает список файлов")
    public void getFilesList_success() throws Exception {
        int filesListLimit = 3;
        String path = "/list";

        when(userFilesService.findFilesList(USER_NAME, filesListLimit))
                .thenReturn(createRandomFileInfoResponseList(filesListLimit));

        mockMvc.perform(get(path)
                        .queryParam(LIMIT_QUERY_PARAM, String.valueOf(filesListLimit)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(APPLICATION_JSON),
                        jsonPath("$[0].filename", containsString(FILE_NAME)),
                        jsonPath("$[0].size", greaterThanOrEqualTo(0))
                ).andDo(print());

        verify(userFilesService, times(1)).findFilesList(USER_NAME, filesListLimit);
    }

    private List<FileInfoDto> createRandomFileInfoResponseList(int maxSize) {
        final int maxFileSize = 10_000_000;
        return IntStream.range(0, maxSize)
                .mapToObj(i -> new FileInfoDto(FILE_NAME + i, RANDOM.nextInt(maxFileSize), ""))
                .collect(Collectors.toList());
    }


    @Test
    @DisplayName("DELETE /file успешный вызов сервиса")
    public void deleteFile_success() throws Exception {
        String path = "/file";

        mockMvc.perform(delete(path)
                        .queryParam(FILE_NAME_QUERY_PARAM, FILE_NAME))
                .andExpect(status().isOk())
                .andDo(print());

        verify(userFilesService, times(1)).deleteFile(USER_NAME, FILE_NAME);
    }

    @Test
    @DisplayName("DELETE /file ошибка ввода-вывода 500")
    public void deleteFile_IOException_error() throws Exception {
        String path = "/file";

        doThrow(new IOException("Test IO Error"))
                .when(userFilesService).deleteFile(USER_NAME, FILE_NAME);

        mockMvc.perform(delete(path)
                        .queryParam(FILE_NAME_QUERY_PARAM, FILE_NAME))
                .andExpect(status().isInternalServerError())
                .andDo(print());

        verify(userFilesService, times(1)).deleteFile(USER_NAME, FILE_NAME);
    }

    @Test
    @DisplayName("DELETE /file ошибка параметра запроса filename 400")
    public void deleteFile_bad_filename_error() throws Exception {
        String path = "/file";

        mockMvc.perform(delete(path)
                        .queryParam(FILE_NAME_QUERY_PARAM, BAD_FILE_NAME))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verifyNoInteractions(userFilesService);
    }


    @Test
    @DisplayName("PUT /file успешный вызов сервиса")
    public void renameFile_success() throws Exception {
        String path = "/file";
        String newFileName = "NEW_FILE_NAME";
        String jsonBody = objectMapper.writeValueAsString(new RenameRequest(newFileName));

        mockMvc.perform(put(path)
                        .queryParam(FILE_NAME_QUERY_PARAM, FILE_NAME)
                        .contentType(APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andDo(print());

        verify(userFilesService, times(1)).renameFile(USER_NAME, FILE_NAME, newFileName);
    }

    @Test
    @DisplayName("PUT /file ошибка параметра запроса filename 400")
    public void renameFile_bad_source_filename_error() throws Exception {
        String path = "/file";
        String jsonBody = objectMapper.writeValueAsString(new RenameRequest(FILE_NAME));

        mockMvc.perform(put(path)
                        .queryParam(FILE_NAME_QUERY_PARAM, BAD_FILE_NAME)
                        .contentType(APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verifyNoInteractions(userFilesService);
    }

    @Test
    @DisplayName("PUT /file ошибка параметра объекта filename 400")
    public void renameFile_bad_target_filename_error() throws Exception {
        String path = "/file";
        String jsonBody = objectMapper.writeValueAsString(new RenameRequest(BAD_FILE_NAME));

        mockMvc.perform(put(path)
                        .queryParam(FILE_NAME_QUERY_PARAM, FILE_NAME)
                        .contentType(APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verifyNoInteractions(userFilesService);
    }

    @Test
    @DisplayName("GET /file успешный возврат Multipart данных")
    public void downloadFile_success() throws Exception {
        String path = "/file";
        InputStream fileDataInputStream = new ByteArrayInputStream(TEST_BYTES_ARRAY);
        FileContentDto fileContentDto = new FileContentDto(fileDataInputStream, "1234");

        when(userFilesService.findFileInfo(USER_NAME, FILE_NAME))
                .thenReturn(Optional.of(new FileInfoDto(FILE_NAME, TEST_BYTES_ARRAY.length, "")));

        when(userFilesService.openFile(USER_NAME, FILE_NAME))
                .thenReturn(fileContentDto);

        MvcResult asyncResult = mockMvc.perform(get(path)
                        .queryParam(FILE_NAME_QUERY_PARAM, FILE_NAME))
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MULTIPART_FORM_DATA_VALUE),
                        content().string(containsString("name=\"hash\"")),
                        content().string(containsString("name=\"file\"")))
                .andDo(print());
    }

    @Test
    @DisplayName("GET /file закрывает прочитанный ресурс")
    public void downloadFile_stream_closing_success() throws Exception {
        String path = "/file";
        InputStream fileDataInputStream = new ByteArrayInputStream(TEST_BYTES_ARRAY);
        InputStream fileDataInputStreamSpy = spy(fileDataInputStream);
        FileContentDto fileContentDto = new FileContentDto(fileDataInputStreamSpy, "1234");

        when(userFilesService.findFileInfo(USER_NAME, FILE_NAME))
                .thenReturn(Optional.of(new FileInfoDto(FILE_NAME, TEST_BYTES_ARRAY.length, "")));

        when(userFilesService.openFile(USER_NAME, FILE_NAME))
                .thenReturn(fileContentDto);

        MvcResult asyncResult = mockMvc.perform(get(path)
                        .queryParam(FILE_NAME_QUERY_PARAM, FILE_NAME))
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andDo(print());

        verify(fileDataInputStreamSpy, atLeastOnce()).close();
    }

    @Test
    @DisplayName("GET /file ошибка параметра запроса filename 400")
    public void downloadFile_bad_filename_error() throws Exception {
        String path = "/file";
        mockMvc.perform(get(path)
                        .queryParam(FILE_NAME_QUERY_PARAM, BAD_FILE_NAME))
                .andExpect(status().isBadRequest())
                .andDo(print());
        verifyNoInteractions(userFilesService);
    }

    @Test
    @DisplayName("POST /file успешно принимает Multipart file")
    public void uploadFile_success() throws Exception {
        String path = "/file";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", FILE_NAME, APPLICATION_OCTET_STREAM_VALUE, TEST_BYTES_ARRAY);

        String testHash = "123456abcdef";

        mockMvc.perform(multipart(path)
                        .file(multipartFile)
                        .param("hash", testHash)
                        .queryParam(FILE_NAME_QUERY_PARAM, FILE_NAME))
                .andExpect(status().isOk())
                .andDo(print());

        verify(userFilesService, times(1)).saveFile(eq(USER_NAME), eq(FILE_NAME), eq(testHash),
                org.mockito.ArgumentMatchers.any(InputStream.class));
    }

    @Test
    @DisplayName("POST /file ошибка параметра запроса filename 400")
    public void uploadFile_bad_filename_error() throws Exception {
        String path = "/file";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", FILE_NAME, APPLICATION_OCTET_STREAM_VALUE, TEST_BYTES_ARRAY);

        mockMvc.perform(multipart(path)
                        .file(multipartFile)
                        .queryParam(FILE_NAME_QUERY_PARAM, BAD_FILE_NAME))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verifyNoInteractions(userFilesService);
    }

    @Test
    @DisplayName("POST /file ошибка параметра объекта file 400")
    public void uploadFile_file_part_not_found_error() throws Exception {
        String path = "/file";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "wrong_file_part_name", FILE_NAME, APPLICATION_OCTET_STREAM_VALUE, TEST_BYTES_ARRAY);

        mockMvc.perform(multipart(path)
                        .file(multipartFile)
                        .queryParam(FILE_NAME_QUERY_PARAM, FILE_NAME))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verifyNoInteractions(userFilesService);
    }
}