package org.comppress.customnewsapi.entity;

import lombok.Data;
import org.comppress.customnewsapi.dto.ArticleDto;
import org.springframework.beans.BeanUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Data
@Entity
public class Article extends AbstractEntity{

    private String author;
    private String title;
    @Column(length = 65536 * 64)
    private String description;
    @Column(columnDefinition = "TEXT")
    private String url;
    @Column(columnDefinition = "TEXT")
    private String urlToImage;
    //@Column(unique = true, columnDefinition = "TEXT", length = 65536 * 3000)
    private String guid;
    private LocalDateTime publishedAt;
    @Column(length = 65536 * 64)
    private String content;
    private Long rssFeedId;
    //@Column(columnDefinition = "integer default 0",nullable = false)
    private boolean isAccessible = true;
    private boolean isAccessibleUpdated = false;
    private boolean isTopNews = false;
    private Integer countRatings = 0;
    private Double averageRating1 = 0.0;
    //@Column(columnDefinition = "integer default 0",nullable = false)
    private Integer countRating1 = 0;
    private Double rating1sum = 0.0;
    private Double averageRating2 = 0.0;
    private Integer countRating2 = 0;
    private Double rating2sum = 0.0;
    private Double averageRating3 = 0.0;
    private Integer countRating3 = 0;
    private Double rating3sum = 0.0;
    private Double totalAverageRating = 0.0;

    public ArticleDto toDto(){
        ArticleDto articleDto = new ArticleDto();
        BeanUtils.copyProperties(this, articleDto);
        return articleDto;
    }

}
