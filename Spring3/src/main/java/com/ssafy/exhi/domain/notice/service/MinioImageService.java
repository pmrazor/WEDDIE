package com.ssafy.exhi.domain.notice.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioImageService {

    String uploadImage(MultipartFile file);

    String getImageUrl(String filename);

}
