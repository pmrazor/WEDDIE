package com.ssafy.exhi.domain.notice.repository;

import com.ssafy.exhi.domain.notice.model.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Integer> {

    Page<Notice> findAll(Pageable pageable);

    /**
     * 각 NoticeCategory별로 createdAt 기준 최신 게시글 조회 단, 동일 카테고리 내에 생성일자가 같은 게시글이 여러 건이면 모두 반환됩니다.
     *
     * @param pageable 페이징 정보
     * @return 최신 게시글 Page
     */
    @Query("SELECT n FROM Notice n " +
            "WHERE n.createdAt = (" +
            "    SELECT MAX(n2.createdAt) FROM Notice n2 " +
            "    WHERE n2.noticeCategory = n.noticeCategory" +
            ") " +
            "ORDER BY n.createdAt DESC")
    Page<Notice> findNewestNotices(Pageable pageable);

}
