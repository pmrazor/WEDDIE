package com.ssafy.exhi.domain.couplemail.repository;

import com.ssafy.exhi.domain.couplemail.model.entity.MailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailTemplateRepository extends JpaRepository<MailTemplate, Integer> {
}
