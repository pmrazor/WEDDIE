package com.ssafy.exhi.domain.ai.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
public class PythonService {
    //impl 안만듬
    public String aiPythonScript() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", "launcher.py");
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(new File("../pythonScript/launcher.py"));

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String imagePath = reader.readLine();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python script execution failed");
            }
            return imagePath;
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute Python script", e);
        }
    }
}
