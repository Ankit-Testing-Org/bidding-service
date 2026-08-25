package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.contract.entity.ContractAssignmentStatus;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractStatus;
import com.evatech.bidplatform.contract.repository.ContractDocumentRepository;
import com.evatech.bidplatform.contract.service.ContractAccessService;
import com.evatech.bidplatform.user.dto.RoleType;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractAccessServiceImpl implements ContractAccessService {

    private final ContractDocumentRepository contractDocumentRepository;

    @Override
    public ContractDocument getAccessibleContract(
            Long contractId,
            User user,
            List<String> roles
    ) {
        validateContractId(contractId);
        validateUser(user);

        ContractDocument contractDocument = contractDocumentRepository
                .findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contract not found with id: " + contractId
                ));

        validateAccess(contractDocument, user, roles);

        return contractDocument;
    }

    @Override
    public void validateAccess(
            ContractDocument contractDocument,
            User user,
            List<String> roles
    ) {
        if (contractDocument == null) {
            throw new IllegalArgumentException(
                    "Contract document must not be null"
            );
        }

        validateUser(user);

        List<String> effectiveRoles = roles == null
                ? Collections.emptyList()
                : roles;

        boolean administrator = effectiveRoles.stream()
                .filter(Objects::nonNull)
                .anyMatch(role -> RoleType.ADMIN.name().equalsIgnoreCase(role));

        if (administrator) {
            return;
        }

        boolean uploadedAndUnassigned =
                ContractStatus.UPLOADED.equals(contractDocument.getStatus())
                        && ContractAssignmentStatus.UNASSIGNED.equals(
                        contractDocument.getAssignmentStatus()
                );

        boolean assignedToCurrentUser =
                contractDocument.getAssignedTo() != null
                        && contractDocument.getAssignedTo()
                        .equalsIgnoreCase(user.getEmail());

        boolean uploadedByCurrentUser =
                contractDocument.getUploadedBy() != null
                        && contractDocument.getUploadedBy()
                        .equalsIgnoreCase(user.getEmail());

        if (!uploadedAndUnassigned
                && !assignedToCurrentUser
                && !uploadedByCurrentUser) {
            throw new AccessDeniedException(
                    "You are not allowed to access this contract"
            );
        }
    }

    private void validateContractId(Long contractId) {
        if (contractId == null) {
            throw new IllegalArgumentException(
                    "Contract id must not be null"
            );
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "User must not be null"
            );
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "User email must not be empty"
            );
        }
    }
}