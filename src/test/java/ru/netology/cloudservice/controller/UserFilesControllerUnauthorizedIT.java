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
import org.springframework.test.web.servlet.MockMvc;
import ru.netology.cloudservice.model.request.RenameRequest;
import ru.netology.cloudservice.service.UserFilesService;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class UserFilesControllerUnauthorizedIT {
    private static final byte[] TEST_BYTES_ARRAY = "Hello World!".getBytes();
    private static final String FILE_NAME = "FILENAME";
    private static final String FILE_NAME_QUERY_PARAM = "filename";
    private static final String LIMIT_QUERY_PARAM = "limit";

    @MockBean
    private UserFilesService userFilesService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /list не авторизован возвращает ошибку 401")
    public void getFilesList_unauthorized() throws Exception {
        int filesListLimit = 3;
        String path = "/list";
        mockMvc.perform(get(path)
                        .queryParam(LIMIT_QUERY_PARAM, String.valueOf(filesListLimit)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
        verifyNoInteractions(userFilesService);
    }

    @Test
    @DisplayName("DELETE /file не авторизован возвращает ошибку 401")
    public void deleteFile_unauthorized() throws Exception {
        String path = "/file";
        mockMvc.perform(delete(path)
                        .queryParam(FILE_NAME_QUERY_PARAM, FILE_NAME))
                .andExpect(status().isUnauthorized())
                .andDo(print());
        verifyNoInteractions(userFilesService);
    }

    @Test
    @DisplayName("PUT /file не авторизован возвращает ошибку 401")
    public void renameFile_unauthorized() throws Exception {
        String path = "/file";
        String newFileName = "NEW_FILE_NAME";
        String jsonBody = objectMapper.writeValueAsString(new RenameRequest(newFileName));
        mockMvc.perform(put(path)
                        .queryParam(FILE_NAME_QUERY_PARAM, FILE_NAME)
                        .contentType(APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized())
                .andDo(print());
        verifyNoInteractions(userFilesService);
    }

    @Test
    @DisplayName("GET /file не авторизован возвращает ошибку 401")
    public void downloadFile_unauthorized() throws Exception {
        String path = "/file";
        mockMvc.perform(get(path)
                        .queryParam(FILE_NAME_QUERY_PARAM, FILE_NAME))
                .andExpect(status().isUnauthorized())
                .andDo(print());
        verifyNoInteractions(userFilesService);
    }

    @Test
    @DisplayName("POST /file не авторизован возвращает ошибку 401")
    public void uploadFile_unauthorized() throws Exception {
        String path = "/file";
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", FILE_NAME, APPLICATION_OCTET_STREAM_VALUE, TEST_BYTES_ARRAY);

        mockMvc.perform(multipart(path)
                        .file(multipartFile)
                        .queryParam(FILE_NAME_QUERY_PARAM, FILE_NAME))
                .andExpect(status().isUnauthorized())
                .andDo(print());

        verifyNoInteractions(userFilesService);
    }

}