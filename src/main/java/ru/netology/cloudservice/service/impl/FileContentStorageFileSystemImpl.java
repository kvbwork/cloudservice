package ru.netology.cloudservice.service.impl;

import ru.netology.cloudservice.service.FileContentStorage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileContentStorageFileSystemImpl implements FileContentStorage {

    private final Path rootPath;

    public FileContentStorageFileSystemImpl(Path rootPath) throws IOException {
        this.rootPath = rootPath;
        if (!Files.exists(rootPath)) Files.createDirectories(rootPath);
    }

    @Override
    public boolean contains(String uid) {
        return Files.exists(rootPath.resolve(uid));
    }

    @Override
    public InputStream get(String uid) throws IOException {
        return Files.newInputStream(rootPath.resolve(uid));
    }

    @Override
    public long put(String uid, InputStream inputStream) throws IOException {
        return Files.copy(inputStream, rootPath.resolve(uid), REPLACE_EXISTING);
    }

    @Override
    public void remove(String uid) throws IOException {
        Files.delete(rootPath.resolve(uid));
    }
}
