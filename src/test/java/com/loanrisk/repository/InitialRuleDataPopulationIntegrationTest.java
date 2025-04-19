package com.loanrisk.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class InitialRuleDataPopulationIntegrationTest {

    @Autowired
    private ScoringRuleRepository scoringRuleRepository;

    @Test
    public void whenApplicationStarts_thenInitialRulesAreLoaded() {
        long ruleCount = scoringRuleRepository.count();
        assertThat(ruleCount).isEqualTo(6); // Expecting 6 rules from data.sql
    }
}