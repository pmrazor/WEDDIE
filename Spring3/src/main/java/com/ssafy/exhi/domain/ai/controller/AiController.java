package com.ssafy.exhi.domain.ai.controller;

import com.ssafy.exhi.domain.ai.service.PythonService;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequiredArgsConstructor
public class AiController {
    private final PythonService pythonService;
    // 임의로 만듬
    @GetMapping("/ai/invite")
    public ResponseEntity<Resource> generateAiInviteImage(){
        try {
            // 파이썬 스크립트가 로컬 서버 상의 이미지 경로를 반환함
            String imagePath = pythonService.aiPythonScript();
            // 리소스로 타입 변경
            Resource resource = new UrlResource(new File(imagePath).toURI());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
