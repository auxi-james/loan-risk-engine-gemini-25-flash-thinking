package com.loanrisk.repository;

import com.loanrisk.entity.ScoringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoringRuleRepository extends JpaRepository<ScoringRule, Long> {

    List<ScoringRule> findByEnabledOrderByPriorityAsc(boolean enabled);
}