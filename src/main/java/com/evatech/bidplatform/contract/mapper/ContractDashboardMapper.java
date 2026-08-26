package com.evatech.bidplatform.contract.mapper;

import com.evatech.bidplatform.contract.dto.response.contract.ContractListItemResponse;
import com.evatech.bidplatform.contract.dto.response.contract.ContractsSummariesResponse;
import com.evatech.bidplatform.contract.entity.ContractAssignmentStatus;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractStatus;
import com.evatech.bidplatform.contract.entity.LotQualificationStatus;
import com.evatech.bidplatform.contract.entity.analysis.ContractLot;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


@Mapper(componentModel = "spring")
public interface ContractDashboardMapper {

    @Mapping(target = "contractId", source = "id")
    @Mapping(target = "contractName", source = "originalFileName")
    @Mapping(target = "clientName", source = "clientName")
    @Mapping(target = "status", source = "status", qualifiedByName = "enumToString")
    @Mapping(target = "assignmentStatus", expression = "java(resolveUiAssignmentStatus(contractDocument, currentUser))")
    @Mapping(target = "assignedTo", source = "assignedTo")
    @Mapping(target = "assignedToName", expression = "java(resolveAssignedToName(contractDocument))")
    @Mapping(target = "stage", expression = "java(resolveStage(contractDocument))")
    @Mapping(target = "valuation", source = "contractValue")
    @Mapping(target = "currency", expression = "java(resolveCurrency(contractDocument))")
    @Mapping(target = "lotCount", expression = "java(countLots(contractDocument))")
    @Mapping(target = "qualifiedLotCount", expression = "java(countQualifiedLots(contractDocument))")
    @Mapping(target = "uploadedAt", source = "uploadedAt")
    @Mapping(target = "pdfUrl", expression = "java(resolvePdfUrl(contractDocument))")
    ContractListItemResponse toListItem(
            ContractDocument contractDocument,
            @Context String currentUser
    );

    List<ContractListItemResponse> toListItems(
            List<ContractDocument> contractDocuments,
            @Context String currentUser
    );

    default ContractsSummariesResponse toSummary(
            List<ContractDocument> contractDocuments,
            String currentUser
    ) {
        if (contractDocuments == null || contractDocuments.isEmpty()) {
            return new ContractsSummariesResponse(
                    0,
                    0,
                    0,
                    0,
                    0,
                    BigDecimal.ZERO
            );
        }

        long totalContracts = contractDocuments.size();

        long assignedToMe = contractDocuments.stream()
                .filter(contractDocument -> isAssignedToCurrentUser(contractDocument, currentUser))
                .count();

        long uploaded = contractDocuments.stream()
                .filter(contractDocument -> hasStatus(contractDocument, ContractStatus.UPLOADED))
                .count();

        long underAnalysis = contractDocuments.stream()
                .filter(this::isUnderAnalysis)
                .count();

        long unassigned = contractDocuments.stream()
                .filter(contractDocument -> ContractAssignmentStatus.UNASSIGNED.equals(contractDocument.getAssignmentStatus()))
                .count();

        BigDecimal totalValuation = contractDocuments.stream()
                .map(ContractDocument::getContractValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ContractsSummariesResponse(
                totalContracts,
                assignedToMe,
                uploaded,
                underAnalysis,
                unassigned,
                totalValuation
        );
    }

    default boolean isAssignedToCurrentUser(
            ContractDocument contractDocument,
            String currentUser
    ) {
        if (contractDocument == null || currentUser == null) {
            return false;
        }

        return ContractAssignmentStatus.ASSIGNED.equals(contractDocument.getAssignmentStatus())
                && currentUser.equalsIgnoreCase(contractDocument.getAssignedTo());
    }

    default String resolveUiAssignmentStatus(
            ContractDocument contractDocument,
            String currentUser
    ) {
        if (contractDocument == null || contractDocument.getAssignmentStatus() == null) {
            return "UNASSIGNED";
        }

        if (ContractAssignmentStatus.UNASSIGNED.equals(contractDocument.getAssignmentStatus())) {
            return "UNASSIGNED";
        }

        if (isAssignedToCurrentUser(contractDocument, currentUser)) {
            return "ASSIGNED_TO_ME";
        }

        return "ASSIGNED_TO_OTHER";
    }

    default String resolveAssignedToName(ContractDocument contractDocument) {
        if (contractDocument == null || contractDocument.getAssignedTo() == null) {
            return null;
        }

        return contractDocument.getAssignedTo();
    }

    default String resolveStage(ContractDocument contractDocument) {
        if (contractDocument == null || contractDocument.getStatus() == null) {
            return "INTAKE";
        }

        return switch (contractDocument.getStatus()) {
            case UPLOADED -> "INTAKE";
            case ANALYSIS_IN_PROGRESS -> "AI_ANALYSIS";
            case SUBMITTED_FOR_REVIEW -> "REVIEW_PENDING";
            case APPROVED -> "APPROVED_FOR_BID";
            case REJECTED -> "CONTRACT_REJECTED";
            default -> contractDocument.getStatus().name();
        };
    }

    default String resolveCurrency(ContractDocument contractDocument) {
        if (contractDocument == null || contractDocument.getCurrency() == null || contractDocument.getCurrency().isBlank()) {
            return "EUR";
        }

        return contractDocument.getCurrency();
    }

    default Integer countLots(ContractDocument contractDocument) {
        if (contractDocument == null || contractDocument.getLots() == null) {
            return 0;
        }

        return contractDocument.getLots().size();
    }

    default Integer countQualifiedLots(ContractDocument contractDocument) {
        if (contractDocument == null || contractDocument.getLots() == null) {
            return 0;
        }

        return Math.toIntExact(
                contractDocument.getLots()
                        .stream()
                        .filter(this::isQualifiedLot)
                        .count()
        );
    }

    default boolean isQualifiedLot(ContractLot contractLot) {
        if (contractLot == null) {
            return false;
        }

        return LotQualificationStatus.QUALIFIED.equals(contractLot.getQualificationStatus());
    }

    default String resolvePdfUrl(ContractDocument contractDocument) {
        if (contractDocument == null || contractDocument.getId() == null) {
            return null;
        }

        return "/api/contracts/" + contractDocument.getId() + "/pdf";
    }

    default boolean hasStatus(
            ContractDocument contractDocument,
            ContractStatus status
    ) {
        if (contractDocument == null || status == null) {
            return false;
        }

        return status.equals(contractDocument.getStatus());
    }

    default boolean isUnderAnalysis(ContractDocument contractDocument) {
        if (contractDocument == null || contractDocument.getStatus() == null) {
            return false;
        }
        return ContractStatus.ANALYSIS_IN_PROGRESS.equals(contractDocument.getStatus())
                || ContractStatus.SUBMITTED_FOR_REVIEW.equals(contractDocument.getStatus());
    }

    @Named("enumToString")
    default String enumToString(Enum<?> value) {
        if (value == null) {
            return null;
        }

        return value.name();
    }
}
