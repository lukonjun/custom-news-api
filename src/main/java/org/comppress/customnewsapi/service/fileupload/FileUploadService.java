package org.comppress.customnewsapi.service.fileupload;

import org.comppress.customnewsapi.dto.CategoryDto;
import org.comppress.customnewsapi.dto.CriteriaDto;
import org.comppress.customnewsapi.dto.PublisherDto;
import org.comppress.customnewsapi.dto.TopNewsFeedDto;
import org.comppress.customnewsapi.entity.RssFeed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileUploadService {

    ResponseEntity<List<RssFeed>> saveRssFeeds(MultipartFile file) throws Exception;
    ResponseEntity<List<CriteriaDto>> saveCriteria(MultipartFile file);

    ResponseEntity<List<CategoryDto>> saveCategorySVGs(MultipartFile file);

    ResponseEntity<List<PublisherDto>> savePublisherSVGs(MultipartFile file);

    ResponseEntity<List<TopNewsFeedDto>> saveTopNews(MultipartFile file);

    ResponseEntity<List<CategoryDto>> saveCategories(MultipartFile file);
}
