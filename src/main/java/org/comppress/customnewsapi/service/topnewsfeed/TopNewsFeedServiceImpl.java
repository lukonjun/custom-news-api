package org.comppress.customnewsapi.service.topnewsfeed;

import lombok.extern.slf4j.Slf4j;
import org.comppress.customnewsapi.entity.Article;
import org.comppress.customnewsapi.entity.TopNewsFeed;
import org.comppress.customnewsapi.repository.ArticleRepository;
import org.comppress.customnewsapi.repository.TopNewsFeedRepository;
import org.comppress.customnewsapi.service.article.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TopNewsFeedServiceImpl implements TopNewsFeedService {

    private final ArticleService articleService;
    private final TopNewsFeedRepository topNewsFeedRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public TopNewsFeedServiceImpl(ArticleService articleService, TopNewsFeedRepository topNewsFeedRepository, ArticleRepository articleRepository) {
        this.articleService = articleService;
        this.topNewsFeedRepository = topNewsFeedRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    public void checkTopNewsFeeds() {
        // Get All Top News
        List<TopNewsFeed> listTopNewsFeeds = topNewsFeedRepository.findAll();

        // Fetch TopNews
        for(TopNewsFeed topNewsFeed:listTopNewsFeeds){
            List<Article> listTopNewsArticles = new ArrayList<>();
            try{
                listTopNewsArticles = articleService.fetchArticlesFromTopNewsFeed(topNewsFeed);
                for(Article article:listTopNewsArticles){
                    Optional<Article> articleOptional = articleRepository.findByUrlAndIsTopNewsFalse(article.getUrl());
                    if(articleOptional.isPresent()){
                        // If yes give them a Flag
                        articleOptional.get().setTopNews(true);
                        articleRepository.save(articleOptional.get());
                        log.info("Set Flag for Top News for article with id {}",articleOptional.get().getId());
                    }
                }
                log.info("Fetching {} News from {}",listTopNewsArticles.size(),topNewsFeed.getUrl());
            }catch(Exception e){
                log.error("AN exception occurred {}", e.getMessage());
            }
        }
    }
}