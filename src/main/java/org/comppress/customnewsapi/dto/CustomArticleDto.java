package org.comppress.customnewsapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CustomArticleDto {

    @JsonProperty(value = "id")
    private Long id;

    private String author;

    private String title;

    private String description;

    private String url;

    @JsonProperty(value = "url_to_image")
    private String urlToImage;

    @JsonProperty(value = "published_at")
    private String publishedAt;

    @JsonProperty(value = "count_ratings")
    private Integer countRatings;

    @JsonProperty(value = "is_accessible")
    private Boolean isAccessible;

    @JsonProperty(value = "is_top_news")
    private Boolean isTopNews;

    @JsonProperty(value = "publisher_name")
    private String publisherName;

    @JsonProperty(value = "publisher_id")
    private Long publisherId;

    @JsonProperty(value = "count_comment", defaultValue = "0")
    private Integer countComment;

    @JsonProperty(value = "category_id")
    private Long categoryId;

    @JsonProperty(value = "category_name")
    private String categoryName;

    @JsonProperty("is_rated")
    private Boolean isRated = false;
}
