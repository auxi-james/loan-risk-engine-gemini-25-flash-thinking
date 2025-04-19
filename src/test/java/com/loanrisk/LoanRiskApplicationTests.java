package com.loanrisk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;

@SpringBootTest
class LoanRiskApplicationTests {

	@Autowired
	private DataSource dataSource;

	@Test
	void contextLoads() {

		// Verify that the data source is not null
		assert(dataSource != null);
	}

}
