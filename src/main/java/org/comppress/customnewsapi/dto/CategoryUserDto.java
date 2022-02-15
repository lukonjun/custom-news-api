package org.comppress.customnewsapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CategoryUserDto {
    private String name;
    private String lang;
    private Long id;
    @JsonProperty(value = "url_to_image")
    private String urlToImage;
    private boolean selected;
}