package com.pollution.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.pollution.project.service.SiteCodeResolver;

@SpringBootTest
class PollutionProjectApplicationTests {

	@Test
	void contextLoads() {
	}

	@MockBean
    private SiteCodeResolver siteCodeResolver;

}
