package com.learnfirebase.ecommerce.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@SpringBootApplication(
    scanBasePackages = "com.learnfirebase.ecommerce"
)
@EnableScheduling
public class EcommerceApplication {
    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(EcommerceApplication.class, args);
    }

    private static void loadDotEnv() {
        File envFile = new File(".env");
        if (!envFile.exists()) {
            envFile = new File("../.env");
        }
        if (!envFile.exists()) {
            envFile = new File("../../.env");
        }
        if (!envFile.exists()) {
            envFile = new File("../../../.env");
        }

        if (envFile.exists()) {
            System.out.println("[ENV LOADER] Found .env file at: " + envFile.getAbsolutePath());
            try {
                List<String> lines = Files.readAllLines(envFile.toPath());
                int loadedCount = 0;
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        if (value.startsWith("\"") && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("'") && value.endsWith("'")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                            loadedCount++;
                        }
                    }
                }
                System.out.println("[ENV LOADER] Successfully loaded " + loadedCount + " variables into System Properties.");
            } catch (IOException e) {
                System.err.println("[ENV LOADER] Error reading .env file: " + e.getMessage());
            }
        } else {
            System.err.println("[ENV LOADER] WARNING: .env file NOT found in any search paths! Using default spring/docker properties.");
        }
    }
}
