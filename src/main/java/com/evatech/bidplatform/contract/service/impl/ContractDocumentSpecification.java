package com.evatech.bidplatform.contract.service.impl;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.dashboard.dto.contract.request.ContractSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public final class ContractDocumentSpecification {

    private ContractDocumentSpecification() {
    }

    public static Specification<ContractDocument> search(
            ContractSearchRequest request,
            String currentUser
    ) {

        return (root, query, cb) -> {

            Predicate predicate = cb.conjunction();

            if (request.searchText() != null &&
                    !request.searchText().isBlank()) {

                predicate = cb.and(
                        predicate,
                        cb.like(
                                cb.lower(root.get("originalFileName")),
                                "%" + request.searchText().toLowerCase() + "%"
                        )
                );
            }

            return predicate;
        };
    }
}