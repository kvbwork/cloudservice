package ru.netology.cloudservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.netology.cloudservice.entity.FileInfo;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    List<FileInfo> findAllByOwnerUsernameAndFilenameAndDeletedAtIsNull(String username, String filename);

    Optional<FileInfo> findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(String username, String filename);

    List<FileInfo> findAllByOwnerUsername(String username);

    List<FileInfo> findAllByOwnerUsernameAndDeletedAtIsNull(String username);

}
