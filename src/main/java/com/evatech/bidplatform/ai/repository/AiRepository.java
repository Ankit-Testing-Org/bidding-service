package com.evatech.bidplatform.ai.repository;

import com.evatech.bidplatform.ai.entity.AiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiRepository extends JpaRepository<AiEntity, Long>  {
}
