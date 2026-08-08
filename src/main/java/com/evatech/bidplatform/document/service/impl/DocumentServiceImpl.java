package com.evatech.bidplatform.document.service.impl;

import com.evatech.bidplatform.bid.entity.Bid;
import com.evatech.bidplatform.bid.entity.BidStatus;
import com.evatech.bidplatform.bid.repository.BidRepository;
import com.evatech.bidplatform.document.entity.DocumentTemplate;
import com.evatech.bidplatform.document.entity.DocumentType;
import com.evatech.bidplatform.document.entity.GeneratedDocument;
import com.evatech.bidplatform.document.repository.DocumentTemplateRepository;
import com.evatech.bidplatform.document.repository.GeneratedDocumentRepository;
import com.evatech.bidplatform.document.service.DocumentService;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final GeneratedDocumentRepository generatedDocumentRepository;
    private final DocumentTemplateRepository documentTemplateRepository;
    private final BidRepository bidRepository;

    @Override
    public GeneratedDocument generateBidDocument(Long bidId, User user) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new IllegalArgumentException("Bid not found with id: " + bidId));

        if (bid.getStatus() != BidStatus.APPROVED) {
            throw new IllegalStateException("Only approved bid can generate document");
        }

        DocumentTemplate template = documentTemplateRepository
                .findByTemplateCodeAndActiveTrue("DEFAULT_BID_TEMPLATE")
                .orElseThrow(() -> new IllegalStateException("Active bid template not found"));

        String generatedFileName = "bid-" + bid.getId() + ".docx";
        String generatedStoragePath = "generated/" + generatedFileName;

        GeneratedDocument generatedDocument = GeneratedDocument.builder()
                .bid(bid)
                .fileName(generatedFileName)
                .storagePath(generatedStoragePath)
                .documentType(DocumentType.BID_DOCX)
                .generatedBy(user.getEmail())
                .generatedAt(LocalDateTime.now())
                .build();

        GeneratedDocument savedDocument = generatedDocumentRepository.save(generatedDocument);

        bid.setStatus(BidStatus.DOCUMENT_GENERATED);
        bidRepository.save(bid);

        return savedDocument;
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadDocument(Long documentId, User user) {
        GeneratedDocument generatedDocument = getDocument(documentId, user);

        throw new UnsupportedOperationException(
                "File loading from storage path not implemented yet: " + generatedDocument.getStoragePath()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GeneratedDocument getDocument(Long documentId, User user) {
        return generatedDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Generated document not found with id: " + documentId));
    }
}
