package com.evatech.bidplatform.approval.repository.workflow;

import com.evatech.bidplatform.approval.entity.workflow.WorkflowGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowGroupMemberRepository
        extends JpaRepository<WorkflowGroupMember, Long> {

    List<WorkflowGroupMember> findByGroupKeyAndActiveTrue(
            String groupKey
    );

    List<WorkflowGroupMember> findByUserIdAndActiveTrue(
            String userId
    );

    Optional<WorkflowGroupMember> findByGroupKeyAndUserIdAndActiveTrue(
            String groupKey,
            String userId
    );

    boolean existsByGroupKeyAndUserIdAndActiveTrue(
            String groupKey,
            String userId
    );

    @Query("""
            select m.email
            from WorkflowGroupMember m
            where m.groupKey = :groupKey
            and m.active = true
            """)
    List<String> findEmailAddressesByGroupKey(
            @Param("groupKey") String groupKey
    );

    @Query("""
            select m.userId
            from WorkflowGroupMember m
            where m.groupKey = :groupKey
            and m.active = true
            """)
    List<String> findUserIdsByGroupKey(
            @Param("groupKey") String groupKey
    );

    @Query("""
            select count(m)
            from WorkflowGroupMember m
            where m.groupKey = :groupKey
            and m.active = true
            """)
    long countActiveMembers(
            @Param("groupKey") String groupKey
    );
}