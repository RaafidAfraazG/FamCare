package com.famcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FamCare - Family Mental Health Tracker
 * 
 * Main entry point for the Spring Boot application.
 * This class starts the entire application.
 */
@SpringBootApplication
public class FamcareApplication {

    public static void main(String[] args) {
        SpringApplication.run(FamcareApplication.class, args);
        System.out.println("===========================================");
        System.out.println("🎉 FamCare Application Started Successfully!");
        System.out.println("===========================================");
        System.out.println("📱 Open your browser and go to: http://localhost:8080");
        System.out.println("🔐 Login with your credentials");
        System.out.println("===========================================");
    }
}