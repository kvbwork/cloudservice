package ru.netology.cloudservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloudservice.entity.FileInfo;
import ru.netology.cloudservice.entity.UserEntity;
import ru.netology.cloudservice.model.dto.FileContentDto;
import ru.netology.cloudservice.model.dto.FileInfoDto;
import ru.netology.cloudservice.repository.FileInfoRepository;
import ru.netology.cloudservice.repository.UserRepository;
import ru.netology.cloudservice.service.FileContentStorage;
import ru.netology.cloudservice.service.UserFilesService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

@RequiredArgsConstructor
public class UserFilesServiceJpaImpl implements UserFilesService {

    private final UserRepository userRepository;
    private final FileInfoRepository fileInfoRepository;
    private final FileContentStorage fileContentStorage;

    @Override
    public List<FileInfoDto> findFilesList(String userName, int limit) {
        return fileInfoRepository.findAllByOwnerUsernameAndDeletedAtIsNull(userName)
                .stream()
                .limit(limit)
                .map(fileInfo -> new FileInfoDto(fileInfo.getFilename(), fileInfo.getFilesize(), fileInfo.getHash()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FileInfoDto> findFileInfo(String userName, String fileName) {
        return fileInfoRepository.findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(userName, fileName)
                .map(fileInfo -> new FileInfoDto(fileInfo.getFilename(), fileInfo.getFilesize(), fileInfo.getHash()));
    }

    private FileInfo findFileOrElseThrow(String userName, String fileName) throws FileNotFoundException {
        return fileInfoRepository.findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(userName, fileName)
                .orElseThrow(() -> new FileNotFoundException(fileName));
    }

    @Override
    @Transactional
    public void deleteFile(String userName, String fileName) throws IOException {
        ru.netology.cloudservice.entity.FileInfo fileInfo = findFileOrElseThrow(userName, fileName);
        fileInfo.setDeleted(true);
    }

    @Override
    @Transactional
    public void renameFile(String userName, String oldFileName, String newFileName) throws IOException {
        FileInfo fileInfo = findFileOrElseThrow(userName, oldFileName);
        if (fileInfoRepository.findFirstByOwnerUsernameAndFilenameAndDeletedAtIsNull(userName, newFileName).isPresent()) {
            throw new FileAlreadyExistsException(newFileName);
        }
        fileInfo.setFilename(newFileName);
    }

    @Override
    public FileContentDto openFile(String userName, String fileName) throws IOException {
        FileInfo fileInfo = findFileOrElseThrow(userName, fileName);
        String uid = fileInfo.getContentUid();
        return new FileContentDto(fileContentStorage.get(uid), fileInfo.getHash());
    }

    @Override
    @Transactional
    public void saveFile(String userName, String fileName, String hash, InputStream inputStream) throws IOException {
        Long ownerId = userRepository.findIdByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException(userName));
        UserEntity owner = userRepository.getReferenceById(ownerId);

        fileInfoRepository.findAllByOwnerUsernameAndFilenameAndDeletedAtIsNull(userName, fileName)
                .forEach(oldFile -> oldFile.setDeleted(true));

        String uid = UUID.randomUUID().toString();
        long fileSize;
        String calculatedHash;

        try (CheckedInputStream checkedInputStream = new CheckedInputStream(inputStream, new CRC32())) {
            fileSize = fileContentStorage.put(uid, checkedInputStream);
            calculatedHash = Long.toHexString(checkedInputStream.getChecksum().getValue());
        }

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilename(fileName);
        fileInfo.setOwner(owner);
        fileInfo.setFilesize(fileSize);
        fileInfo.setContentUid(uid);

        fileInfo.setHash(hash == null || hash.isBlank()
                ? calculatedHash
                : hash);

        fileInfoRepository.save(fileInfo);
    }
}
