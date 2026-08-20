package com.evatech.bidplatform.dashboard.repository;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.dashboard.entity.Proposal;
import com.evatech.bidplatform.dashboard.entity.ProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    long countByStatus(ProposalStatus status);

    long countByStatusIn(Collection<ProposalStatus> statuses);

    @Query("""
            SELECT p
            FROM Proposal p
            WHERE p.submissionDate >= :currentDate
            ORDER BY p.submissionDate ASC
            """)
    List<Proposal> findUpcomingDeadlines(LocalDate currentDate, Pageable pageable);

    @Query("""
            SELECT p
            FROM Proposal p
            ORDER BY p.createdAt DESC
            """)
    List<Proposal> findRecentProposals(Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(p.proposalValue), 0)
            FROM Proposal p
            WHERE p.status IN (
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.DRAFT,
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.SUBMITTED,
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.IN_REVIEW
            )
            """)
    BigDecimal getActiveProposalValue();

    @Query("""
            SELECT COALESCE(SUM(p.proposalValue), 0)
            FROM Proposal p
            WHERE p.status =
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.WON
            """)
    BigDecimal getWonProposalValue();

    @Query("""
            SELECT COALESCE(SUM(p.proposalValue), 0)
            FROM Proposal p
            WHERE p.status =
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.LOST
            """)
    BigDecimal getLostProposalValue();

    @Query("""
            SELECT COALESCE(SUM(p.proposalValue), 0)
            FROM Proposal p
            WHERE p.status =
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.NOT_BIDDED
            """)
    BigDecimal getNotBiddedProposalValue();

    @Query("""
            SELECT COUNT(p)
            FROM Proposal p
            WHERE p.status IN (
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.DRAFT,
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.SUBMITTED,
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.IN_REVIEW
            )
            """)
    long countActiveProposals();

    @Query("""
            SELECT COUNT(p)
            FROM Proposal p
            WHERE p.status =
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.WON
            """)
    long countWonProposals();

    @Query("""
            SELECT COUNT(p)
            FROM Proposal p
            WHERE p.status =
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.LOST
            """)
    long countLostProposals();

    @Query("""
            SELECT COUNT(p)
            FROM Proposal p
            WHERE p.status =
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.NOT_BIDDED
            """)
    long countNotBiddedProposals();

    @Query("""
            SELECT COUNT(p)
            FROM Proposal p
            WHERE p.status IN (
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.WON,
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.LOST,
                com.evatech.bidplatform.dashboard.entity.ProposalStatus.NOT_BIDDED
            )
            """)
    long countClosedProposals();

    @Query("""
    select coalesce(sum(p.proposalValue), 0)
    from Proposal p
    where p.status in (
        com.evatech.bidplatform.dashboard.entity.ProposalStatus.ACTIVE,
        com.evatech.bidplatform.dashboard.entity.ProposalStatus.IN_REVIEW
    )
""")
    BigDecimal sumActiveProposalValue();

    @Query("""
    select coalesce(sum(p.proposalValue), 0)
    from Proposal p
    where p.status =
        com.evatech.bidplatform.dashboard.entity.ProposalStatus.SUBMITTED
""")
    BigDecimal sumSubmittedProposalValue();

    @Query("""
    select coalesce(sum(p.proposalValue), 0)
    from Proposal p
    where p.status =
        com.evatech.bidplatform.dashboard.entity.ProposalStatus.WON
""")
    BigDecimal sumWonProposalValue();

    @Query("""
    SELECT COALESCE(SUM(p.proposalValue), 0)
    FROM Proposal p
    """)
    BigDecimal getTotalProposalValue();

    @Query("""
    select p
    from Proposal p
    left join p.contractDocument cd
    where
        (:status is null or p.status = :status)
    and
        (
            :searchText is null
            or trim(:searchText) = ''
            or lower(p.title) like lower(concat('%', :searchText, '%'))
            or lower(p.proposalNumber) like lower(concat('%', :searchText, '%'))
            or lower(cd.name) like lower(concat('%', :searchText, '%'))
            or lower(p.createdBy) like lower(concat('%', :searchText, '%'))
        )
""")
    Page<Proposal> searchProposals(
            String searchText,
            ProposalStatus status,
            Pageable pageable
    );

    Proposal getByIdAndContractDocument(Long proposalId, ContractDocument contractDocument);

    Proposal getByContractDocument(ContractDocument contractDocument);

}