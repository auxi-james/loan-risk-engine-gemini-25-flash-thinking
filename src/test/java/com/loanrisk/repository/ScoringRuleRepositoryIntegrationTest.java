package com.loanrisk.repository;

import com.loanrisk.entity.ScoringRule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ScoringRuleRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScoringRuleRepository scoringRuleRepository;

    @Test
    public void whenSaveRule_thenRuleIsPersisted() {
        ScoringRule rule = new ScoringRule();
        rule.setName("High Income");
        rule.setField("income");
        rule.setOperator(">");
        rule.setRuleValue("50000");
        rule.setRiskPoints(-10);
        rule.setPriority(1);
        rule.setEnabled(true);

        ScoringRule savedRule = scoringRuleRepository.save(rule);
        assertThat(savedRule).isNotNull();
        assertThat(savedRule.getId()).isNotNull();
        assertThat(savedRule.getName()).isEqualTo("High Income");
    }

    @Test
    public void whenFindEnabledOrderByPriority_thenCorrectRulesAreReturned() {
        ScoringRule rule1 = new ScoringRule();
        rule1.setName("Low Credit Score");
        rule1.setField("creditScore");
        rule1.setOperator("<");
        rule1.setRuleValue("600");
        rule1.setRiskPoints(20);
        rule1.setPriority(2);
        rule1.setEnabled(true);
        entityManager.persist(rule1);

        ScoringRule rule2 = new ScoringRule();
        rule2.setName("High Debt Ratio");
        rule2.setField("debtRatio");
        rule2.setOperator(">");
        rule2.setRuleValue("0.4");
        rule2.setRiskPoints(15);
        rule2.setPriority(3);
        rule2.setEnabled(true);
        entityManager.persist(rule2);

        ScoringRule rule3 = new ScoringRule();
        rule3.setName("Unemployed");
        rule3.setField("employmentStatus");
        rule3.setOperator("==");
        rule3.setRuleValue("Unemployed");
        rule3.setRiskPoints(30);
        rule3.setPriority(1);
        rule3.setEnabled(true);
        entityManager.persist(rule3);

        ScoringRule rule4 = new ScoringRule();
        rule4.setName("Disabled Rule");
        rule4.setField("someField");
        rule4.setOperator("==");
        rule4.setRuleValue("someValue");
        rule4.setRiskPoints(5);
        rule4.setPriority(4);
        rule4.setEnabled(false);
        entityManager.persist(rule4);
        entityManager.flush();

        List<ScoringRule> enabledRules = scoringRuleRepository.findByEnabledOrderByPriorityAsc(true);

        assertThat(enabledRules).hasSize(3);
        assertThat(enabledRules.get(0).getName()).isEqualTo("Unemployed");
        assertThat(enabledRules.get(1).getName()).isEqualTo("Low Credit Score");
        assertThat(enabledRules.get(2).getName()).isEqualTo("High Debt Ratio");
    }

    @Test
    public void whenUpdateRule_thenRuleIsUpdated() {
        ScoringRule rule = new ScoringRule();
        rule.setName("Initial Name");
        rule.setField("initialField");
        rule.setOperator("==");
        rule.setRuleValue("initialValue");
        rule.setRiskPoints(10);
        rule.setPriority(1);
        rule.setEnabled(true);
        entityManager.persist(rule);
        entityManager.flush();

        ScoringRule foundRule = entityManager.find(ScoringRule.class, rule.getId());
        foundRule.setName("Updated Name");
        foundRule.setRiskPoints(25);
        scoringRuleRepository.save(foundRule);
        entityManager.flush();

        ScoringRule updatedRule = entityManager.find(ScoringRule.class, rule.getId());
        assertThat(updatedRule.getName()).isEqualTo("Updated Name");
        assertThat(updatedRule.getRiskPoints()).isEqualTo(25);
    }

    @Test
    public void whenDeleteRule_thenRuleIsDeleted() {
        ScoringRule rule = new ScoringRule();
        rule.setName("Rule to Delete");
        rule.setField("field");
        rule.setOperator("==");
        rule.setRuleValue("value");
        rule.setRiskPoints(10);
        rule.setPriority(1);
        rule.setEnabled(true);
        entityManager.persist(rule);
        entityManager.flush();

        scoringRuleRepository.deleteById(rule.getId());
        entityManager.flush();

        Optional<ScoringRule> deletedRule = scoringRuleRepository.findById(rule.getId());
        assertThat(deletedRule).isEmpty();
    }
}