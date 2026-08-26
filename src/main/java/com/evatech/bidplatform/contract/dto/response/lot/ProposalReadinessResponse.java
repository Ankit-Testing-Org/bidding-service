package com.evatech.bidplatform.contract.dto.response.lot;

import java.util.List;

public record ProposalReadinessResponse(

        Long contractId,

        int totalLots,

        int qualifiedLots,

        int unqualifiedLots,

        int undecidedLots,

        boolean allLotsDecided,

        boolean atLeastOneQualifiedLot,

        boolean proposalGenerationAllowed,

        String message,

        List<Long> qualifiedLotIds,

        List<Long> undecidedLotIds
) {
}