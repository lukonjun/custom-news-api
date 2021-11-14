package org.comppress.customnewsapi.repository;

import org.comppress.customnewsapi.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article,Long> {

    boolean existsById(Long id);

    Optional<Article> findByGuid(String guid);

    @Query(value = "SELECT * FROM article at JOIN rss_feed rf on rf.id = at.rss_feed_id " +
            "JOIN publisher p on p.id = rf.publisher_id " +
            "JOIN category c on c.id = rf.category_id WHERE " +
            "(:category is null or :category = '' or c.name LIKE %:category%) AND " +
            "(:publisherName is null or :publisherName = '' or p.name LIKE %:publisherName%) AND " +
            "(:title is null or :title = '' or at.title LIKE %:title%) AND " +
            "at.published_at BETWEEN IFNULL(:fromDate, '1900-01-01 00:00:00') AND " +
            "IFNULL(:toDate,now())",
            countQuery = "SELECT count(*) FROM article at JOIN rss_feed rf on rf.id = at.rss_feed_id " +
                    "JOIN publisher p on p.id = rf.publisher_id " +
                    "JOIN category c on c.id = rf.category_id WHERE " +
                    "(:category is null or :category = '' or c.name LIKE %:category%) AND " +
                    "(:publisherName is null or :publisherName = '' or p.name LIKE %:publisherName%) AND " +
                    "(:title is null or :title = '' or at.title LIKE %:title%) AND " +
                    "at.published_at BETWEEN IFNULL(:fromDate, '1900-01-01 00:00:00') AND " +
                    "IFNULL(:toDate,now())"
            , nativeQuery = true)
    Page<Article> retrieveByCategoryOrPublisherName(@Param("category") String category,
                                                    @Param("publisherName") String publisherName,
                                                    @Param("title") String title,
                                                    @Param("fromDate") LocalDateTime fromDate,
                                                    @Param("toDate") LocalDateTime toDate,
                                                    Pageable pageable);

    @Query(value = "SELECT * FROM article at JOIN rss_feed rf on rf.id = at.rss_feed_id " +
            "JOIN publisher p on p.id = rf.publisher_id " +
            "JOIN category c on c.id = rf.category_id " +
            "JOIN rating r on at.id = r.article_id WHERE " +
            "(:category is null or :category = '' or c.name LIKE %:category%) AND " +
            "(:publisherName is null or :publisherName = '' or p.name LIKE %:publisherName%) AND " +
            "(:title is null or :title = '' or at.title LIKE %:title%) AND " +
            "at.published_at BETWEEN IFNULL(:fromDate, '1900-01-01 00:00:00') AND " +
            "IFNULL(:toDate,now())"
            , nativeQuery = true)
    List<Article> retrieveRatedArticlesByCategoryOrPublisherName(@Param("category") String category,
                                                    @Param("publisherName") String publisherName,
                                                    @Param("title") String title,
                                                    @Param("fromDate") LocalDateTime fromDate,
                                                    @Param("toDate") LocalDateTime toDate);


    @Query(value = "SELECT DISTINCTROW (at.id) FROM article at JOIN rating r on at.id = r.article_id"
            , nativeQuery = true)
    Page<Article> retrieveAllArticlesRated(Pageable pageable);

    @Query(value = "SELECT * FROM article WHERE id IN (SELECT article_id FROM rating)"
            , nativeQuery = true)
    Page<Article> retrieveAllRatedArticles(Pageable pageable);
}
