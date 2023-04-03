package ru.netology.cloudservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileContentStorageFileSystemImplTest {
    private static final String TEST_FILE_CONTENT_UID = "test_file_content_uid";
    private static final byte[] TEST_FILE_CONTENT = "test_file_content_bytes".getBytes();

    @TempDir
    private Path rootPath;

    FileContentStorageFileSystemImpl sut;

    @BeforeEach
    void setUp() throws IOException {
        sut = new FileContentStorageFileSystemImpl(rootPath);
    }

    @Test
    @DisplayName("contains() файл существует")
    void contains_success() throws IOException {
        sut.put(TEST_FILE_CONTENT_UID, new ByteArrayInputStream(TEST_FILE_CONTENT));
        var result = sut.contains(TEST_FILE_CONTENT_UID);
        assertThat(result, is(true));
    }

    @Test
    @DisplayName("contains() файл не существует")
    void contains_failure() {
        var result = sut.contains(TEST_FILE_CONTENT_UID);
        assertThat(result, is(false));
    }

    @Test
    @DisplayName("get() успешное чтение файла")
    void get_success() throws IOException {
        sut.put(TEST_FILE_CONTENT_UID, new ByteArrayInputStream(TEST_FILE_CONTENT));
        try (var resultInputStream = sut.get(TEST_FILE_CONTENT_UID)) {
            byte[] actualContent = resultInputStream.readAllBytes();
            assertThat(Arrays.equals(actualContent, TEST_FILE_CONTENT), is(true));
        }
    }

    @Test
    @DisplayName("get() ошибка файл не найден IOException")
    void get_not_found_failure() {
        assertThrows(IOException.class, () -> sut.get("NOT_EXIST_UID"));
    }

    @Test
    @DisplayName("put() успешно сохраняет файл")
    void put_success() throws IOException {
        var contentSize = sut.put(TEST_FILE_CONTENT_UID, new ByteArrayInputStream(TEST_FILE_CONTENT));
        assertThat((int) contentSize, is(TEST_FILE_CONTENT.length));
    }

    @Test
    @DisplayName("put() успешно перезаписывает файл")
    void put_overwrite_success() throws IOException {
        sut.put(TEST_FILE_CONTENT_UID, new ByteArrayInputStream("TEMPORALCONTENT".getBytes()));
        var fileExistBefore = sut.contains(TEST_FILE_CONTENT_UID);
        var contentSize = sut.put(TEST_FILE_CONTENT_UID, new ByteArrayInputStream(TEST_FILE_CONTENT));
        assertThat(fileExistBefore, is(true));
        assertThat((int) contentSize, is(TEST_FILE_CONTENT.length));
    }

    @Test
    @DisplayName("remove() успешно удаляет файл")
    void remove_success() throws IOException {
        sut.put(TEST_FILE_CONTENT_UID, new ByteArrayInputStream(TEST_FILE_CONTENT));
        var fileExistBefore = sut.contains(TEST_FILE_CONTENT_UID);
        sut.remove(TEST_FILE_CONTENT_UID);
        var fileExistAfter = sut.contains(TEST_FILE_CONTENT_UID);
        assertThat(fileExistBefore, is(true));
        assertThat(fileExistAfter, is(false));
    }

    @Test
    @DisplayName("remove() ошибка файл не найден")
    void remove_not_found_failure() {
        assertThrows(IOException.class, () -> sut.remove(TEST_FILE_CONTENT_UID));
    }
}