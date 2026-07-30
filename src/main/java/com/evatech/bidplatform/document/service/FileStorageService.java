package com.evatech.bidplatform.document.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {

    String storeContract(MultipartFile file);

    String storeTemplate(MultipartFile file, String templateCode);

    String storeGeneratedDocument(byte[] content, String fileName);

    Path resolvePath(String relativePath);

    Path getTempDirectory();
}