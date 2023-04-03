package ru.netology.cloudservice.repository;

import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.netology.cloudservice.entity.FileInfo;
import ru.netology.cloudservice.entity.UserEntity;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import static java.time.Instant.now;
import static java.util.function.Predicate.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.instancio.Select.field;

@DataJpaTest
class FileInfoRepositoryTest {
    private static final String TEST_USER_NAME = "test_user";
    private static final String FIRST_FILE_NAME = "testfile1.dat";
    private static final String SECOND_FILE_NAME = "testfile2.dat";
    private static final int FIRST_FILE_EXIST_COUNT = 2;
    private static final int FIRST_FILE_DELETED_COUNT = 2;
    private static final int SECOND_FILE_EXIST_COUNT = 1;
    private static final int EXIST_FILE_LIST_SIZE = FIRST_FILE_EXIST_COUNT + SECOND_FILE_EXIST_COUNT;
    private static final int DELETED_FILE_LIST_SIZE = FIRST_FILE_DELETED_COUNT;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileInfoRepository sut;

    @BeforeEach
    void setUp() {
        UserEntity testUser = userRepository.save(new UserEntity(0, TEST_USER_NAME, "{noop}123", true, Set.of("ROLE_USER")));
        sut.saveAll(Instancio.ofList(FileInfo.class)
                .size(FIRST_FILE_EXIST_COUNT)
                .set(field(FileInfo::getFilename), FIRST_FILE_NAME)
                .set(field(FileInfo::getOwner), testUser)
                .set(field(FileInfo::getCreatedAt), now())
                .set(field(FileInfo::getDeletedAt), null)
                .create());
        sut.saveAll(Instancio.ofList(FileInfo.class)
                .size(SECOND_FILE_EXIST_COUNT)
                .set(field(FileInfo::getFilename), SECOND_FILE_NAME)
                .set(field(FileInfo::getOwner), testUser)
                .set(field(FileInfo::getCreatedAt), now())
                .set(field(FileInfo::getDeletedAt), null)
                .create());
        sut.saveAll(Instancio.ofList(FileInfo.class)
                .size(FIRST_FILE_DELETED_COUNT)
                .set(field(FileInfo::getFilename), FIRST_FILE_NAME)
                .set(field(FileInfo::getOwner), testUser)
                .set(field(FileInfo::getCreatedAt), now())
                .set(field(FileInfo::getDeletedAt), now().plusSeconds(1))
                .create());
    }

    @AfterEach
    void tearDown() {
        sut.deleteAll();
        userRepository.deleteAll();
    }

    private <T> int countMatches(Collection<T> collection, Predicate<T> predicate) {
        return (int) collection.stream()
                .filter(predicate)
                .count();
    }

    @Test
    @DisplayName("Получение списка существующих версий одного файла по имени")
    void find_exist_file_list_by_filename_success() {
        var resultList = sut.findAllByOwnerUsernameAndFilenameAndDeletedAtIsNull(TEST_USER_NAME, FIRST_FILE_NAME);
        assertThat(resultList, hasSize(FIRST_FILE_EXIST_COUNT));
        resultList.forEach(fileInfo -> {
            assertThat(fileInfo.getFilename(), equalTo(FIRST_FILE_NAME));
            assertThat(fileInfo.getOwner().getUsername(), equalTo(TEST_USER_NAME));
            assertThat(fileInfo.isDeleted(), is(false));
        });
    }

    @Test
    @DisplayName("Получение существующего файла по имени")
    void find_exist_file_by_filename_success() {
        var fileInfo = sut.findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(TEST_USER_NAME, FIRST_FILE_NAME)
                .orElseThrow();
        assertThat(fileInfo.getFilename(), equalTo(FIRST_FILE_NAME));
        assertThat(fileInfo.getOwner().getUsername(), equalTo(TEST_USER_NAME));
        assertThat(fileInfo.isDeleted(), is(false));
    }

    @Test
    @DisplayName("Получение списка всех существующих и удаленных файлов")
    void find_all_exist_files_and_deleted_files_success() {
        var expectedResultListSize = EXIST_FILE_LIST_SIZE + DELETED_FILE_LIST_SIZE;
        var resultList = sut.findAllByOwnerUsername(TEST_USER_NAME);
        assertThat(resultList, hasSize(expectedResultListSize));
        assertThat(countMatches(resultList, FileInfo::isDeleted), is(DELETED_FILE_LIST_SIZE));
        assertThat(countMatches(resultList, not(FileInfo::isDeleted)), is(EXIST_FILE_LIST_SIZE));
        resultList.forEach(fileInfo -> {
            assertThat(fileInfo.getOwner().getUsername(), equalTo(TEST_USER_NAME));
        });
    }

    @Test
    @DisplayName("Получение списка всех существующих файлов")
    void find_exist_files_success() {
        var resultList = sut.findAllByOwnerUsernameAndDeletedAtIsNull(TEST_USER_NAME);
        assertThat(resultList, hasSize(EXIST_FILE_LIST_SIZE));
        resultList.forEach(fileInfo -> {
            assertThat(fileInfo.getOwner().getUsername(), equalTo(TEST_USER_NAME));
            assertThat(fileInfo.isDeleted(), is(false));
        });
    }
}