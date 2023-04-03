package ru.netology.cloudservice.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.netology.cloudservice.entity.FileInfo;
import ru.netology.cloudservice.entity.UserEntity;
import ru.netology.cloudservice.model.dto.FileInfoDto;
import ru.netology.cloudservice.repository.FileInfoRepository;
import ru.netology.cloudservice.repository.UserRepository;
import ru.netology.cloudservice.service.FileContentStorage;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserFilesServiceJpaImplTest {
    private static final long TEST_USER_ID = 1L;
    private static final String TEST_USER_NAME = "test_user";
    private static final String TEST_FILE_NAME = "test_file.dat";
    private static final String TEST_NEW_FILE_NAME = "new_test_file.dat";
    private static final String TEST_FILE_HASH = "1234";
    private static final String TEST_FILE_UID = "test_file_uid";
    private static final byte[] TEST_FILE_CONTENT = "test_file_content".getBytes();
    private static final int TEST_FILE_CONTENT_SIZE = TEST_FILE_CONTENT.length;
    private static final int TEST_FILES_LIMIT = 3;
    private static final int DEFAULT_FILE_LIST_SIZE = 10;
    private static final FileInfo TEST_FILE_INFO = new FileInfo(
            1L, mock(UserEntity.class), TEST_FILE_NAME, TEST_FILE_CONTENT_SIZE, TEST_FILE_HASH,
            TEST_FILE_UID, now(), now().plus(24, HOURS));

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileInfoRepository fileInfoRepository;

    @Mock
    private FileContentStorage fileContentStorage;

    @Captor
    private ArgumentCaptor<FileInfo> fileInfoArgumentCaptor;

    @InjectMocks
    private UserFilesServiceJpaImpl sut;

    @Test
    @DisplayName("findFilesList() успешный возврат пустого списка")
    void findFilesList_empty_success() {
        when(fileInfoRepository.findAllByOwnerUsernameAndDeletedAtIsNull(TEST_USER_NAME))
                .thenReturn(Collections.emptyList());
        var result = sut.findFilesList(TEST_USER_NAME, TEST_FILES_LIMIT);
        assertThat(result, emptyCollectionOf(FileInfoDto.class));
    }

    @Test
    @DisplayName("findFilesList() успешный возврат списка файлов")
    void findFilesList_success() {
        var testFilesList = Instancio.ofList(FileInfo.class)
                .size(DEFAULT_FILE_LIST_SIZE)
                .set(field(FileInfo::getDeletedAt), null)
                .create();
        when(fileInfoRepository.findAllByOwnerUsernameAndDeletedAtIsNull(TEST_USER_NAME))
                .thenReturn(testFilesList);
        var result = sut.findFilesList(TEST_USER_NAME, TEST_FILES_LIMIT);
        assertThat(result, hasSize(3));
    }

    @Test
    @DisplayName("findFileInfo() ошибка. Файл не найден")
    void findFileInfo_not_found_failure() {
        when(fileInfoRepository.findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(TEST_USER_NAME, TEST_FILE_NAME))
                .thenReturn(Optional.empty());
        var result = sut.findFileInfo(TEST_USER_NAME, TEST_FILE_NAME);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    @DisplayName("findFileInfo() успешный возврат файла")
    void findFileInfo_success() {
        when(fileInfoRepository.findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(TEST_USER_NAME, TEST_FILE_NAME))
                .thenReturn(Optional.of(TEST_FILE_INFO));
        var result = sut.findFileInfo(TEST_USER_NAME, TEST_FILE_NAME).orElseThrow();
        assertThat(result.getFilename(), equalTo(TEST_FILE_NAME));
        assertThat(result.getHash(), equalTo(TEST_FILE_HASH));
        assertThat(result.getSize(), is((long) TEST_FILE_CONTENT_SIZE));
    }

    @Test
    @DisplayName("deleteFile() ошибка. Файл не найден")
    void deleteFile_not_found_failure() {
        assertThrows(FileNotFoundException.class, () -> sut.deleteFile(TEST_USER_NAME, TEST_FILE_NAME));
    }

    @Test
    @DisplayName("deleteFile() успешное удаление")
    void deleteFile_success() throws IOException {
        var fileInfoEntityMock = mock(FileInfo.class);
        when(fileInfoRepository.findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(TEST_USER_NAME, TEST_FILE_NAME))
                .thenReturn(Optional.of(fileInfoEntityMock));
        sut.deleteFile(TEST_USER_NAME, TEST_FILE_NAME);
        verify(fileInfoEntityMock).setDeleted(true);
    }

    @Test
    @DisplayName("renameFile() ошибка. Файл не найден")
    void renameFile_not_found_failure() {
        assertThrows(FileNotFoundException.class, () ->
                sut.renameFile(TEST_USER_NAME, TEST_FILE_NAME, TEST_NEW_FILE_NAME));
    }

    @Test
    @DisplayName("renameFile() ошибка. Файл уже существует")
    void renameFile_already_exist_failure() {
        when(fileInfoRepository.findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(TEST_USER_NAME, TEST_FILE_NAME))
                .thenReturn(Optional.of(TEST_FILE_INFO));
        when(fileInfoRepository.findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(TEST_USER_NAME, TEST_NEW_FILE_NAME))
                .thenReturn(Optional.of(mock(FileInfo.class)));
        assertThrows(FileAlreadyExistsException.class, () ->
                sut.renameFile(TEST_USER_NAME, TEST_FILE_NAME, TEST_NEW_FILE_NAME));
    }

    @Test
    @DisplayName("renameFile() успешное переименование")
    void renameFile_success() throws IOException {
        var fileInfoEntityMock = mock(FileInfo.class);
        when(fileInfoRepository.findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(TEST_USER_NAME, TEST_FILE_NAME))
                .thenReturn(Optional.of(fileInfoEntityMock));
        when(fileInfoRepository.findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(TEST_USER_NAME, TEST_NEW_FILE_NAME))
                .thenReturn(Optional.empty());
        sut.renameFile(TEST_USER_NAME, TEST_FILE_NAME, TEST_NEW_FILE_NAME);
        verify(fileInfoEntityMock).setFilename(TEST_NEW_FILE_NAME);
    }

    @Test
    @DisplayName("openFile() ошибка. Файл не найден")
    void openFile_not_found_failure() {
        assertThrows(FileNotFoundException.class, () -> sut.openFile(TEST_USER_NAME, TEST_FILE_NAME));
    }

    @Test
    @DisplayName("openFile() успешный возврат файла")
    void openFile_success() throws IOException {
        var fileInfoEntityMock = mock(FileInfo.class);
        when(fileInfoEntityMock.getContentUid()).thenReturn(TEST_FILE_UID);
        when(fileInfoEntityMock.getHash()).thenReturn(TEST_FILE_HASH);
        when(fileInfoRepository.findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(TEST_USER_NAME, TEST_FILE_NAME))
                .thenReturn(Optional.of(fileInfoEntityMock));
        when(fileContentStorage.get(TEST_FILE_UID)).thenReturn(new ByteArrayInputStream(TEST_FILE_CONTENT));

        var result = sut.openFile(TEST_USER_NAME, TEST_FILE_NAME);

        assertThat(Arrays.equals(result.getInputStream().readAllBytes(), TEST_FILE_CONTENT), is(true));
        assertThat(result.getHash(), equalTo(TEST_FILE_HASH));
    }

    @Test
    @DisplayName("saveFile() ошибка. Пользователь не найден.")
    void saveFile_user_not_found_failure() {
        assertThrows(UsernameNotFoundException.class, () ->
                sut.saveFile(TEST_USER_NAME, TEST_FILE_NAME, TEST_FILE_HASH,
                        new ByteArrayInputStream(TEST_FILE_CONTENT)
                )
        );
    }

    @Test
    @DisplayName("saveFile() успешное сохранение")
    void saveFile_success() throws IOException {
        var contentInputStream = new ByteArrayInputStream(TEST_FILE_CONTENT);

        when(userRepository.findIdByUsername(TEST_USER_NAME)).thenReturn(Optional.of(TEST_USER_ID));
        when(fileInfoRepository.findAllByOwnerUsernameAndFilenameAndDeletedAtIsNull(TEST_USER_NAME, TEST_FILE_NAME))
                .thenReturn(Collections.emptyList());
        when(fileContentStorage.put(anyString(), Mockito.any(InputStream.class)))
                .thenReturn((long) TEST_FILE_CONTENT_SIZE);

        sut.saveFile(TEST_USER_NAME, TEST_FILE_NAME, TEST_FILE_HASH, contentInputStream);

        verify(fileInfoRepository, times(1)).save(fileInfoArgumentCaptor.capture());
        var capturedFileInfo = fileInfoArgumentCaptor.getValue();

        assertThat(capturedFileInfo.getFilename(), equalTo(TEST_FILE_NAME));
        assertThat(capturedFileInfo.getFilesize(), is((long) TEST_FILE_CONTENT_SIZE));
        assertThat(capturedFileInfo.getHash(), equalTo(TEST_FILE_HASH));
        assertThat(capturedFileInfo.getCreatedAt().isBefore(Instant.now()), is(true));
        assertThat(capturedFileInfo.getContentUid(), not(blankOrNullString()));
    }

    @Test
    @DisplayName("saveFile() успешная перезапись")
    void saveFile_overwrite_success() throws IOException {
        var contentInputStream = new ByteArrayInputStream(TEST_FILE_CONTENT);
        var oldFileInfoMock = mock(FileInfo.class);

        when(userRepository.findIdByUsername(TEST_USER_NAME)).thenReturn(Optional.of(TEST_USER_ID));
        when(fileInfoRepository.findAllByOwnerUsernameAndFilenameAndDeletedAtIsNull(TEST_USER_NAME, TEST_FILE_NAME))
                .thenReturn(List.of(oldFileInfoMock));
        when(fileContentStorage.put(anyString(), Mockito.any(InputStream.class)))
                .thenReturn((long) TEST_FILE_CONTENT_SIZE);

        sut.saveFile(TEST_USER_NAME, TEST_FILE_NAME, TEST_FILE_HASH, contentInputStream);

        verify(oldFileInfoMock, times(1)).setDeleted(true);
        verify(fileInfoRepository, times(1)).save(fileInfoArgumentCaptor.capture());
        var capturedFileInfo = fileInfoArgumentCaptor.getValue();

        assertThat(capturedFileInfo.getFilename(), equalTo(TEST_FILE_NAME));
        assertThat(capturedFileInfo.getFilesize(), is((long) TEST_FILE_CONTENT_SIZE));
        assertThat(capturedFileInfo.getHash(), equalTo(TEST_FILE_HASH));
        assertThat(capturedFileInfo.getCreatedAt().isBefore(Instant.now()), is(true));
        assertThat(capturedFileInfo.getContentUid(), not(blankOrNullString()));
    }


}