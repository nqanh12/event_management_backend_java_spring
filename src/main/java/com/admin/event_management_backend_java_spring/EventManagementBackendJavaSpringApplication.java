package com.admin.event_management_backend_java_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class EventManagementBackendJavaSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventManagementBackendJavaSpringApplication.class, args);
	}

}
