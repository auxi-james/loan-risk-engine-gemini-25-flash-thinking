INSERT INTO scoring_rule (name, field, operator, rule_value, risk_points, priority, enabled) VALUES
('Credit too low', 'creditScore', '<', '600', 30, 1, true),
('Credit average', 'creditScore', '<', '700', 15, 2, true),
('Loan-to-income high', 'loanRatio', '>', '0.5', 25, 3, true),
('Debt is high', 'existingDebtRatio', '>', '0.4', 20, 4, true),
('Too young', 'age', '<', '21', 20, 5, true),
('Vacation loan', 'loanPurpose', '==', 'vacation', 10, 6, true);