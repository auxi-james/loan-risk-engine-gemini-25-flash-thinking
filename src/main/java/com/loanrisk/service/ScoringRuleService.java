package com.loanrisk.service;

import com.loanrisk.entity.ScoringRule;
import com.loanrisk.repository.ScoringRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoringRuleService {

    private final ScoringRuleRepository scoringRuleRepository;

    @Autowired
    public ScoringRuleService(ScoringRuleRepository scoringRuleRepository) {
        this.scoringRuleRepository = scoringRuleRepository;
    }

    public List<ScoringRule> getActiveScoringRules() {
        return scoringRuleRepository.findByEnabledOrderByPriorityAsc(true);
    }
}