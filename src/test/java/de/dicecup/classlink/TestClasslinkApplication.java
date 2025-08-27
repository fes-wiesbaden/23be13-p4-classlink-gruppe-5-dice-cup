package de.dicecup.classlink;

import org.springframework.boot.SpringApplication;

public class TestClasslinkApplication {

    public static void main(String[] args) {
        SpringApplication.from(ClasslinkApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
