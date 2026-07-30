package com.evatech.bidplatform.document.service.impl;

import com.evatech.bidplatform.FileStorageProperties;
import com.evatech.bidplatform.document.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public String storeContract(MultipartFile file) {
        String originalFileName = cleanFileName(file.getOriginalFilename());
        String fileName = System.currentTimeMillis() + "_" + originalFileName;

        Path contractDirectory = getBasePath().resolve("contracts");
        Path targetPath = contractDirectory.resolve(fileName);

        createDirectory(contractDirectory);
        copyFile(file, targetPath);

        return "contracts/" + fileName;
    }

    @Override
    public String storeTemplate(MultipartFile file, String templateCode) {
        String originalFileName = cleanFileName(file.getOriginalFilename());
        String fileName = System.currentTimeMillis() + "_" + originalFileName;

        Path templateDirectory = getBasePath()
                .resolve("templates")
                .resolve(templateCode);

        Path targetPath = templateDirectory.resolve(fileName);

        createDirectory(templateDirectory);
        copyFile(file, targetPath);

        return targetPath.toString();
    }

    @Override
    public String storeGeneratedDocument(byte[] content, String fileName) {
        String cleanedFileName = cleanFileName(fileName);

        Path generatedDirectory = getBasePath().resolve("generated");
        Path targetPath = generatedDirectory.resolve(cleanedFileName);

        createDirectory(generatedDirectory);
        writeBytes(content, targetPath);

        return targetPath.toString();
    }

    @Override
    public Path resolvePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("Storage path must not be empty");
        }

        Path basePath = getBasePath();
        Path resolvedPath = basePath.resolve(relativePath).normalize();

        if (!resolvedPath.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid storage path: " + relativePath);
        }

        return resolvedPath;
    }

    @Override
    public Path getTempDirectory() {
        Path tempDirectory = getBasePath().resolve("temp").normalize();

        try {
            Files.createDirectories(tempDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not create temp directory: " + tempDirectory,
                    exception
            );
        }

        return tempDirectory;
    }

    private Path getBasePath() {
        return Path.of(fileStorageProperties.getBasePath()).toAbsolutePath().normalize();
    }

    private void createDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create storage directory: " + directory, exception);
        }
    }

    private void copyFile(MultipartFile file, Path targetPath) {
        try {
            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store file: " + targetPath, exception);
        }
    }

    private void writeBytes(byte[] content, Path targetPath) {
        try {
            Files.write(targetPath, content);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store generated document: " + targetPath, exception);
        }
    }

    private String cleanFileName(String fileName) {
        String cleanedFileName = StringUtils.cleanPath(fileName == null ? "uploaded-file" : fileName);

        if (cleanedFileName.contains("..")) {
            throw new IllegalArgumentException("Invalid file name: " + cleanedFileName);
        }

        return cleanedFileName;
    }
}