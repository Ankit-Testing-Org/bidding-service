package com.evatech.bidplatform.dashboard.repository;

import com.evatech.bidplatform.dashboard.entity.ProposalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProposalHistoryRepository
        extends JpaRepository<ProposalHistory, Long> {
}