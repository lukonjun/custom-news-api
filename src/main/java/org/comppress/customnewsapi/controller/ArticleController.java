package org.comppress.customnewsapi.controller;

import org.comppress.customnewsapi.dto.ArticleDto;
import org.comppress.customnewsapi.dto.GenericPage;
import org.comppress.customnewsapi.service.article.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/articles")
public class ArticleController {

    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ResponseEntity<GenericPage<ArticleDto>> getArticles(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "publisherNewsPaper", required = false) String publisherNewsPaper,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate
    ) {
        return articleService.getArticles(page, size, title, category, publisherNewsPaper, lang, fromDate, toDate);
    }

    @GetMapping("/unrated")
    public ResponseEntity<GenericPage<ArticleDto>> getArticlesNotRated(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "categoryId") Long categoryId,
            @RequestParam(value = "listPublisherIds", required = false) List<Long> listPublisherIds,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate
    ) {
        return articleService.getArticlesNotRated(page, size, categoryId, listPublisherIds, lang, fromDate, toDate);
    }

    @GetMapping("/rated")
    public ResponseEntity<GenericPage> getRatedArticles(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "categoryId") Long categoryId,
            @RequestParam(value = "listPublisherIds", required = false) List<Long> listPublisherIds,
            @RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            @RequestParam(value = "noPaywall", required = false, defaultValue = "false") Boolean noPaywall,
            @RequestParam(value = "topFeed", required = false, defaultValue = "false") Boolean topFeed
    ) {
        return articleService.getRatedArticles(page, size, categoryId, listPublisherIds, lang, fromDate, toDate, topFeed, noPaywall);
    }

    @GetMapping("/rated/user")
    public ResponseEntity<GenericPage> getRatedArticlesFromUser(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate
    ) {
        return articleService.getRatedArticlesFromUser(page, size, fromDate, toDate);
    }
}