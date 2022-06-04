package org.comppress.customnewsapi.service.article;

import org.comppress.customnewsapi.dto.CustomArticleDto;
import org.comppress.customnewsapi.dto.GenericPage;
import org.comppress.customnewsapi.entity.Article;
import org.comppress.customnewsapi.entity.TopNewsFeed;
import org.comppress.customnewsapi.utils.GenerateGenericPageUtils;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface ArticleService extends GenerateGenericPageUtils {

    ResponseEntity<GenericPage<CustomArticleDto>> getArticles(int page, int size,
                                                              String title, String category,
                                                              String publisherNewsPaper, String lang,
                                                              String fromDate, String toDate);

    void fetchArticlesFromRssFeeds();

    List<Article> fetchArticlesFromTopNewsFeed(TopNewsFeed topNewsFeed);

    void update(Article article) throws URISyntaxException, IOException;

    ResponseEntity<GenericPage>  getRatedArticles(int page, int size, Long categoryId,
                                                 List<Long> listPublisherIds, String lang,
                                                 String fromDate, String toDate, Boolean topFeed, Boolean noPaywall, String guid);

    ResponseEntity<GenericPage<CustomArticleDto>> getArticlesNotRated(int page, int size, Long categoryId,
                                                                List<Long> listPublisherIds, String lang,
                                                                String fromDate, String toDate, Boolean topFeed);

    ResponseEntity<GenericPage> getRatedArticlesFromUser(int page, int size, String fromDate, String toDate);

    ResponseEntity<GenericPage> getPersonalRatedArticlesFromUser(int page, int size, String fromDate, String toDate);
}
