package org.comppress.customnewsapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CustomRatedArticleDto {

    @JsonProperty(value = "id")
    private Long article_id;
    private String author;
    private String title;
    private String description;
    private String url;
    @JsonProperty(value = "url_to_image")
    private String url_to_image;
    @JsonProperty(value = "published_at")
    private String published_at;
    @JsonProperty(value = "count_ratings")
    private Integer count_ratings;
    @JsonProperty(value = "average_rating_criteria_1")
    private double average_rating_criteria_1;
    @JsonProperty(value = "average_rating_criteria_2")
    private double average_rating_criteria_2;
    @JsonProperty(value = "average_rating_criteria_3")
    private double average_rating_criteria_3;
    @JsonProperty(value = "total_average_rating")
    private double total_average_rating;
    @JsonProperty(value = "is_accessible")
    private Boolean is_accessible;
    @JsonProperty(value = "is_top_news")
    private Boolean is_top_news;
    @JsonProperty(value = "publisher_name")
    private String publisher_name;
    @JsonProperty(value = "publisher_id")
    private Long publisher_id;

    @JsonProperty(value = "count_comment", defaultValue = "0")
    private Integer count_comment;

    @JsonProperty(value = "category_id")
    private Long category_id;

    @JsonProperty(value = "category_name")
    private String category_name;

    @JsonProperty("is_rated")
    private Boolean isRated = false;
}
