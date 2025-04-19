package com.loanrisk.controller;

import com.loanrisk.entity.ScoringRule;
import com.loanrisk.service.ScoringRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rules")
public class RuleController {

    private final ScoringRuleService scoringRuleService;

    @Autowired
    public RuleController(ScoringRuleService scoringRuleService) {
        this.scoringRuleService = scoringRuleService;
    }

    @GetMapping
    public ResponseEntity<List<ScoringRule>> getActiveRules() {
        List<ScoringRule> activeRules = scoringRuleService.getActiveScoringRules();
        return ResponseEntity.ok(activeRules);
    }
}