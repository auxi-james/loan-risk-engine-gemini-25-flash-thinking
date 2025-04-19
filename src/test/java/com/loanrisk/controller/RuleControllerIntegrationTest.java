package com.loanrisk.controller;

import com.loanrisk.entity.ScoringRule;
import com.loanrisk.repository.ScoringRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RuleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScoringRuleRepository scoringRuleRepository;

    @BeforeEach
    void setUp() {
        scoringRuleRepository.deleteAll();

        ScoringRule rule1 = new ScoringRule();
        rule1.setName("Rule 1");
        rule1.setRuleValue("condition1");
        rule1.setRiskPoints(10);
        rule1.setPriority(1);
        rule1.setEnabled(true);

        ScoringRule rule2 = new ScoringRule();
        rule2.setName("Rule 2");
        rule2.setRuleValue("condition2");
        rule2.setRiskPoints(20);
        rule2.setPriority(2);
        rule2.setEnabled(false);

        ScoringRule rule3 = new ScoringRule();
        rule3.setName("Rule 3");
        rule3.setRuleValue("condition3");
        rule3.setRiskPoints(30);
        rule3.setPriority(0);
        rule3.setEnabled(true);

        scoringRuleRepository.save(rule1);
        scoringRuleRepository.save(rule2);
        scoringRuleRepository.save(rule3);
    }

    @Test
    void getActiveRules_shouldReturnOnlyEnabledRulesOrderedByPriority() throws Exception {
        mockMvc.perform(get("/rules")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Rule 3")))
                .andExpect(jsonPath("$[0].priority", is(0)))
                .andExpect(jsonPath("$[1].name", is("Rule 1")))
                .andExpect(jsonPath("$[1].priority", is(1)));
    }
}