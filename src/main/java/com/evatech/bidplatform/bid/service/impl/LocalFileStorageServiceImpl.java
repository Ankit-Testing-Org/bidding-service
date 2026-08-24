package com.evatech.bidplatform.bid.service.impl;

import com.evatech.bidplatform.FileStorageProperties;
import com.evatech.bidplatform.bid.exception.FileStorageException;
import com.evatech.bidplatform.bid.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        return targetPath.toString();
    }

    @Override
    public Resource loadContract(String fileName) {

        try {
            Path contractDirectory = getBasePath().resolve("contracts");
            Path uploadDir = contractDirectory.resolve(fileName);
            Path target = uploadDir.resolve(fileName);

            return new UrlResource(target.toUri());
        } catch (MalformedURLException ex) {

            throw new FileStorageException("Failed to load file", ex);
        }
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
            throw new IllegalStateException("Could not create temp directory: " + tempDirectory, exception);
        }

        return tempDirectory;
    }


    @Override
    public String storeTemplate(String fileName, MultipartFile file) {
        try {
            Path contractDirectory = getBasePath().resolve("templates");
            Path uploadDir = contractDirectory.resolve(fileName);

            Files.createDirectories(uploadDir);

            Path target = uploadDir.resolve(fileName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return target.toString();

        } catch (IOException ex) {

            throw new FileStorageException("Failed to store file", ex);
        }
    }

    @Override
    public String storeTemplate(String fileName, XWPFDocument document) {
        try {
            Path uploadPath = Paths.get("generated-documents");
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            try (FileOutputStream out = new FileOutputStream(filePath.toFile())) {
                document.write(out);
            }
            return filePath.toString();
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file", ex);
        }
    }

    @Override
    public Resource loadTemplate(String fileName) {
        try {
            Path contractDirectory = getBasePath().resolve("templates");
            Path uploadDir = contractDirectory.resolve(fileName);
            Path target = uploadDir.resolve(fileName);
            return new UrlResource(target.toUri());
        } catch (MalformedURLException ex) {

            throw new FileStorageException("Failed to load file", ex);
        }
    }

    @Override
    public void deleteTemplate(String fileName) {
        try {
            Path contractDirectory = getBasePath().resolve("templates");
            Path uploadDir = contractDirectory.resolve(fileName);
            Path target = uploadDir.resolve(fileName);
            Files.deleteIfExists(target);
        } catch (IOException ex) {

            throw new FileStorageException("Failed to delete file", ex);
        }
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
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store file: " + targetPath, exception);
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