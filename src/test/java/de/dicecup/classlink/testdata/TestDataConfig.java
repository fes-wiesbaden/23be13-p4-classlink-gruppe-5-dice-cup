package de.dicecup.classlink.testdata;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestDataConfig {
    @Bean
    public UserTestData userTestData() {
        return new UserTestData();
    }
}
