package ru.netology.cloudservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.netology.cloudservice.repository.FileInfoRepository;
import ru.netology.cloudservice.repository.UserRepository;
import ru.netology.cloudservice.service.FileContentStorage;
import ru.netology.cloudservice.service.UserFilesService;
import ru.netology.cloudservice.service.impl.FileContentStorageFileSystemImpl;
import ru.netology.cloudservice.service.impl.UserFilesServiceJpaImpl;

import java.io.IOException;
import java.nio.file.Path;

@Configuration
public class UserFilesConfig {

    @Value("${application.userfiles.root-path}")
    String userFilesRoot;

    @Bean
    public FileContentStorage fileContentStorage() throws IOException {
        return new FileContentStorageFileSystemImpl(Path.of(userFilesRoot));
    }

    @Bean
    public UserFilesService userFilesService(
            UserRepository userRepository,
            FileInfoRepository fileInfoRepository
    ) throws IOException {
        return new UserFilesServiceJpaImpl(userRepository, fileInfoRepository, fileContentStorage());
    }

}
