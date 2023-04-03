package ru.netology.cloudservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.netology.cloudservice.model.dto.FileInfoDto;
import ru.netology.cloudservice.model.request.LoginRequest;
import ru.netology.cloudservice.model.request.RenameRequest;
import ru.netology.cloudservice.model.response.LoginResponse;

import java.nio.file.Path;
import java.util.List;

import static org.apache.http.entity.ContentType.DEFAULT_BINARY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CloudServiceSuccessCaseIT {
    private static final String TEST_USER = "user1";
    private static final String TEST_PASSWORD = "123";
    private static final String TEST_FILE_NAME = "test_file.dat";
    private static final String TEST_FILE_CONTENT = "Test file content";
    private static final int DEFAULT_FILE_LIST_LIMIT = 10;

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2");

    @TempDir
    private static Path userFilesPath;

    private static String appUrl;
    private static String tokenBearer;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    private static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("application.userfiles.root-path", userFilesPath::toString);
    }

    @BeforeEach
    public void setUp() throws Exception {
        if (appUrl == null) appUrl = "http://localhost:" + port;
        if (tokenBearer == null) post_login_token_success();
    }

    @Test
    @Order(10)
    public void contextLoads() {
        System.out.println("INTEGRATION TEST");
    }

    @Test
    @Order(20)
    @DisplayName("POST /login успешно вернул токен")
    public void post_login_token_success() throws Exception {
        var url = appUrl + "/login";
        var request = RequestEntity.post(url)
                .accept(APPLICATION_JSON)
                .body(new LoginRequest(TEST_USER, TEST_PASSWORD));

        var response = restTemplate.exchange(request, String.class);
        var loginResponse = objectMapper.readValue(response.getBody(), LoginResponse.class);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(loginResponse.getAuthToken().length(), greaterThan(0));

        tokenBearer = "Bearer " + loginResponse.getAuthToken();
    }

    @Test
    @Order(30)
    @DisplayName("GET /list успешно вернул пустой список файлов")
    public void get_list_empty_success() throws Exception {
        var url = appUrl + "/list?limit=" + DEFAULT_FILE_LIST_LIMIT;
        var request = RequestEntity.get(url)
                .header("auth-token", tokenBearer)
                .accept(APPLICATION_JSON)
                .build();

        var response = restTemplate.exchange(request, String.class);
        var fileInfoList = objectMapper.readValue(response.getBody(),
                new TypeReference<List<FileInfoDto>>() {
                });

        assertThat(response.getStatusCode(), is(OK));
        assertThat(response.getHeaders().getContentType().isCompatibleWith(APPLICATION_JSON), is(true));
        assertThat(fileInfoList, empty());
    }

    private List<FileInfoDto> requestFilesList() throws Exception {
        var url = appUrl + "/list?limit=" + DEFAULT_FILE_LIST_LIMIT;
        var request = RequestEntity.get(url)
                .header("auth-token", tokenBearer)
                .accept(APPLICATION_JSON)
                .build();

        var response = restTemplate.exchange(request, String.class);
        return objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
    }

    private boolean singleFileExistsOnServer(String fileName, long fileSize) throws Exception {
        var filesList = requestFilesList();
        return filesList.size() == 1
                && filesList.get(0).getFilename().equals(fileName)
                && filesList.get(0).getSize() == fileSize;
    }

    @Test
    @Order(40)
    @DisplayName("POST /file успешно загружает Multipart файл")
    public void post_file_save_success() throws Exception {
        var url = appUrl + "/file?filename=" + TEST_FILE_NAME;
        var fileContent = TEST_FILE_CONTENT.getBytes();
        var fileSize = fileContent.length;

        var multipartEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", fileContent, DEFAULT_BINARY, TEST_FILE_NAME)
                .addTextBody("hash", "")
                .build();

        var requestEntity = RequestEntity.post(url)
                .header("auth-token", tokenBearer)
                .contentType(MediaType.parseMediaType(multipartEntity.getContentType().getValue()))
                .body(multipartEntity.getContent().readAllBytes());

        var response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(singleFileExistsOnServer(TEST_FILE_NAME, fileSize), is(true));
    }

    @Test
    @Order(50)
    @DisplayName("POST /file успешно перезаписывает файл")
    public void put_file_overwrite_success() throws Exception {
        FileInfoDto oldFileInfoDto = requestFilesList().get(0);

        var url = appUrl + "/file?filename=" + oldFileInfoDto.getFilename();
        var fileContent = TEST_FILE_CONTENT.repeat(10).getBytes();
        var newFileSize = fileContent.length;

        var multipartEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", fileContent, DEFAULT_BINARY, TEST_FILE_NAME)
                .addTextBody("hash", "")
                .build();

        var requestEntity = RequestEntity.post(url)
                .header("auth-token", tokenBearer)
                .contentType(MediaType.parseMediaType(multipartEntity.getContentType().getValue()))
                .body(multipartEntity.getContent().readAllBytes());

        var response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(singleFileExistsOnServer(TEST_FILE_NAME, newFileSize), is(true));
    }

    @Test
    @Order(60)
    @DisplayName("PUT /file успешно переименовывает файл")
    public void put_file_rename_success() throws Exception {
        FileInfoDto oldFileInfoDto = requestFilesList().get(0);

        var url = appUrl + "/file?filename=" + oldFileInfoDto.getFilename();
        var newFileName = "NEW_FILE_NAME";

        var requestEntity = RequestEntity.put(url)
                .header("auth-token", tokenBearer)
                .contentType(APPLICATION_JSON)
                .body(new RenameRequest(newFileName));

        var response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(singleFileExistsOnServer(newFileName, oldFileInfoDto.getSize()), is(true));
    }

    @Test
    @Order(70)
    @DisplayName("DELETE /file успешно удаляет файл")
    public void delete_file_remove_success() throws Exception {
        FileInfoDto oldFileInfoDto = requestFilesList().get(0);

        var url = appUrl + "/file?filename=" + oldFileInfoDto.getFilename();

        var requestEntity = RequestEntity.delete(url)
                .header("auth-token", tokenBearer)
                .build();

        var response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode(), is(OK));
        assertThat(singleFileExistsOnServer(oldFileInfoDto.getFilename(), oldFileInfoDto.getSize()), is(false));
    }

    @Test
    @Order(80)
    @DisplayName("POST /logout успешно выполнен")
    public void post_logout_token_success() throws Exception {
        var url = appUrl + "/logout";
        var request = RequestEntity.post(url)
                .header("auth-token", tokenBearer)
                .build();
        var response = restTemplate.exchange(request, String.class);

        assertThat(response.getStatusCode(), is(OK));
    }

    @Test
    @Order(90)
    @DisplayName("POST /logout после деактивации не принимает токен")
    public void post_logout_twice_failure() throws Exception {
        var url = appUrl + "/logout";
        var request = RequestEntity.post(url)
                .header("auth-token", tokenBearer)
                .build();
        var response = restTemplate.exchange(request, String.class);

        assertThat(response.getStatusCode(), is(UNAUTHORIZED));
    }

}
