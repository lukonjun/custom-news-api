package org.comppress.customnewsapi.service.article;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.ParsingFeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.comppress.customnewsapi.dto.ArticleDto;
import org.comppress.customnewsapi.dto.CustomArticleDto;
import org.comppress.customnewsapi.dto.CustomRatedArticleDto;
import org.comppress.customnewsapi.dto.GenericPage;
import org.comppress.customnewsapi.entity.*;
import org.comppress.customnewsapi.exceptions.GeneralException;
import org.comppress.customnewsapi.repository.ArticleRepository;
import org.comppress.customnewsapi.repository.PublisherRepository;
import org.comppress.customnewsapi.repository.RssFeedRepository;
import org.comppress.customnewsapi.repository.UserRepository;
import org.comppress.customnewsapi.service.BaseSpecification;
import org.comppress.customnewsapi.utils.CustomStringUtils;
import org.comppress.customnewsapi.utils.DateUtils;
import org.comppress.customnewsapi.utils.PageHolderUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleServiceImpl implements ArticleService, BaseSpecification {

    @Value("${image.width}")
    private Integer imageWidth;

    @Value("${image.height}")
    private Integer imageHeight;

    private final RssFeedRepository rssFeedRepository;
    private final ArticleRepository articleRepository;
    private final PublisherRepository publisherRepository;
    private final UserRepository userRepository;

    @Autowired
    public ArticleServiceImpl(RssFeedRepository rssFeedRepository, ArticleRepository articleRepository, PublisherRepository publisherRepository, UserRepository userRepository) {
        this.rssFeedRepository = rssFeedRepository;
        this.articleRepository = articleRepository;
        this.publisherRepository = publisherRepository;
        this.userRepository = userRepository;
    }

    public List<Article> fetchArticlesFromTopNewsFeed(TopNewsFeed topNewsFeed) {
        List<Article> articles = new ArrayList<>();

        SyndFeed feed = new SyndFeedImpl();
        try {
            URL feedSource = new URL(topNewsFeed.getUrl());
            SyndFeedInput input = new SyndFeedInput();
            feed = input.build(new XmlReader(feedSource));
            log.info("Fetching News from " + topNewsFeed.getUrl());
        } catch (ParsingFeedException e) {
            log.error("Feed can not be parsed, please recheck the url " + topNewsFeed.getUrl());
            return null;
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException most likely a dead link, check the url " + topNewsFeed.getUrl());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        for (SyndEntry syndEntry : feed.getEntries()) {
            Article article = customMappingSyndEntryImplToArticle(syndEntry, null);
            articles.add(article);
        }

        return articles;
    }

    public void fetchArticlesFromRssFeeds() {

        for (RssFeed rssFeed : rssFeedRepository.findAll()) {
            SyndFeed feed;
            try {
                URL feedSource = new URL(rssFeed.getUrl());
                SyndFeedInput input = new SyndFeedInput();
                feed = input.build(new XmlReader(feedSource));
                log.info("Fetching News from " + rssFeed.getUrl());
            } catch (ParsingFeedException e) {
                log.error("Feed can not be parsed, please recheck the url " + rssFeed.getUrl());
                continue;
            } catch (FileNotFoundException e) {
                log.error("FileNotFoundException most likely a dead link, check the url " + rssFeed.getUrl());
                continue;
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            log.info("Feed of size " + feed.getEntries().size());
            saveArticle(rssFeed, feed);
        }
    }

    private void saveArticle(RssFeed rssFeed, SyndFeed feed) {
        for (SyndEntry syndEntry : feed.getEntries()) {
            Article article = customMappingSyndEntryImplToArticle(syndEntry, rssFeed);
            if (articleRepository.findByGuid(article.getGuid()).isPresent()) continue;
            try {
                articleRepository.save(article);
            } catch (DataIntegrityViolationException e) {
                log.error("Duplicate Record found while saving data {}", e.getLocalizedMessage());
            } catch (Exception e) {
                log.error("Error while saving data {}", e.getLocalizedMessage());
            }
        }
    }

    @Override
    @Async("ThreadPoolExecutor")
    public void update(Article article) throws URISyntaxException, IOException {
        try {
            String response = urlReader(article.getUrl());
            if (response.contains("\"isAccessibleForFree\":false") || response.contains("\"isAccessibleForFree\": false")) {
                article.setAccessible(false);
            } else {
                article.setAccessible(true);
            }
            article.setAccessibleUpdated(true);
            articleRepository.save(article);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String formatText(String text) {
        //TODO ENHANCE, FILTER ALSO FOR HTML TAGS LIKE <p>
        //String title = Text.normalizeString(text);
        text = text.replace("\n", "");
        text = text.replace("\t", "");
        text = text.replace("\r", "");
        return text;
    }

    public Article customMappingSyndEntryImplToArticle(SyndEntry syndEntry, RssFeed rssFeed) {
        Article article = new Article();
        if (syndEntry.getAuthor() != null) {
            article.setAuthor(syndEntry.getAuthor());
        }
        if (syndEntry.getTitle() != null) {
            article.setTitle(formatText(syndEntry.getTitle()));
        }
        if (syndEntry.getLink() != null) {
            article.setUrl(syndEntry.getLink());
        }
        if (syndEntry.getEnclosures() != null && !syndEntry.getEnclosures().isEmpty()) {
            article.setUrlToImage(syndEntry.getEnclosures().get(0).getUrl());
        } else {
            String imgUrl = null;
            if (syndEntry.getDescription() != null) {
                imgUrl = CustomStringUtils.getImgLinkFromTagSrc(syndEntry.getDescription().getValue(), "src=\"");
            }
            if (imgUrl == null && syndEntry.getContents() != null && syndEntry.getContents().size() > 0) {
                imgUrl = CustomStringUtils.getImgLinkFromTagSrc(syndEntry.getContents().get(0).getValue(), "src=\"");
            }
            if (imgUrl == null && syndEntry.getContents() != null && syndEntry.getContents().size() > 0) {
                imgUrl = CustomStringUtils.getImgLinkFromTagSrc(syndEntry.getContents().get(0).getValue(), "url=\"");
            }

            Dimension dimension = new Dimension(0, 0);

            if (imgUrl != null) {
                dimension = getImageDimension(imgUrl);
            }

            boolean isBadResolution = false;
            // If width or length of the image is less than 200px then we save the publisher image
            if ((dimension.getHeight() < imageHeight || dimension.getWidth() < imageWidth) && imgUrl != null) {
                isBadResolution = true;
                log.debug("Picture with image url {} has a bad resolution", imgUrl);
            }
            if(rssFeed != null){
                if (imgUrl == null || imgUrl.isEmpty() || isBadResolution) {
                    Optional<Publisher> publisher = publisherRepository.findById(rssFeed.getPublisherId());
                    article.setUrlToImage(publisher.get().getUrlToImage());
                } else {
                    article.setUrlToImage(imgUrl);
                }
            }
        }
        if (syndEntry.getUri() != null) {
            article.setGuid(syndEntry.getUri());
        }
        if (syndEntry.getPublishedDate() != null) {
            article.setPublishedAt(syndEntry.getPublishedDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        if (syndEntry.getContents() != null && !syndEntry.getContents().isEmpty()) {
            article.setContent(formatText(syndEntry.getContents().get(0).getValue()));
        }
        if (syndEntry.getDescription() != null) {
            article.setDescription(formatText(syndEntry.getDescription().getValue()));
        }

        if (rssFeed != null) {
            article.setRssFeedId(rssFeed.getId());
        } else {
            article.setRssFeedId(-1L);
        }
        return article;
    }

    private String urlReader(String url) throws URISyntaxException, IOException, InterruptedException {
        log.info("Send GET request to " + url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Accept-Encoding", "identity")
                .GET()
                .build();

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        var response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public ResponseEntity<GenericPage<CustomArticleDto>> getArticles(int page, int size, String title, String category, String publisherNewsPaper, String lang, String fromDate, String toDate) {

        Page<ArticleRepository.CustomArticle> articlesPage = articleRepository
                .retrieveByCategoryOrPublisherNameToCustomArticle(category,
                        publisherNewsPaper, title, lang,
                        DateUtils.stringToLocalDateTime(fromDate), DateUtils.stringToLocalDateTime(toDate),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));


        GenericPage<CustomArticleDto> genericPage = new GenericPage<>();
        genericPage.setData(articlesPage.stream().map(this::toCustomDto).collect(Collectors.toList()));
        BeanUtils.copyProperties(articlesPage, genericPage);

        return ResponseEntity.status(HttpStatus.OK).body(genericPage);

    }

    private CustomArticleDto toCustomDto(ArticleRepository.CustomArticle s) {
        CustomArticleDto dto = new CustomArticleDto();
        BeanUtils.copyProperties(s, dto);
        return dto;
    }

    @Override
    public ResponseEntity<GenericPage> getRatedArticles(int page, int size, Long categoryId,
                                                        List<Long> listPublisherIds, String lang,
                                                        String fromDate, String toDate, Boolean topFeed, Boolean noPaywall) {
        if (listPublisherIds == null) {
            listPublisherIds = publisherRepository.findAll().stream().map(Publisher::getId).collect(Collectors.toList());
        }
        List<ArticleRepository.CustomRatedArticle> customRatedArticleList = articleRepository.retrieveAllRatedArticlesInDescOrder(
                categoryId, listPublisherIds, lang,
                DateUtils.stringToLocalDateTime(fromDate), DateUtils.stringToLocalDateTime(toDate), topFeed, noPaywall);
        /* // TODO WHY CAN WE NOT USE THE COUNT QUERY HERE; SQL ERROR
        Page<ArticleRepository.CustomRatedArticle> customRatedArticlePage = articleRepository.retrieveAllRatedArticlesInDescOrder(
                title, category, publisherNewsPaper, lang,
                DateUtils.stringToLocalDateTime(fromDate), DateUtils.stringToLocalDateTime(toDate),PageRequest.of(page,size));
        getCustomRatedArticleDtoGenericPage(customRatedArticlePage)
        */
        List<CustomRatedArticleDto> customRatedArticleDtoList = new ArrayList<>();
        customRatedArticleList.forEach(customRatedArticle -> {
            CustomRatedArticleDto customRatedArticleDto = new CustomRatedArticleDto();
            BeanUtils.copyProperties(customRatedArticle, customRatedArticleDto);
            customRatedArticleDtoList.add(customRatedArticleDto);
        });
        return PageHolderUtils.getResponseEntityGenericPage(page, size, customRatedArticleDtoList);
    }

    @Override
    public ResponseEntity<GenericPage<CustomArticleDto>> getArticlesNotRated(int page, int size, Long categoryId, List<Long> listPublisherIds, String lang, String fromDate, String toDate, Boolean topFeed) {
        if (listPublisherIds == null) {
            listPublisherIds = publisherRepository.findAll().stream().map(Publisher::getId).collect(Collectors.toList());
        }
        Page<ArticleRepository.CustomArticle> articlesPage = articleRepository.retrieveUnratedArticlesByCategoryIdAndPublisherIdsAndLanguage(categoryId, listPublisherIds, lang, DateUtils.stringToLocalDateTime(fromDate), DateUtils.stringToLocalDateTime(toDate), topFeed,PageRequest.of(page, size));

        GenericPage<CustomArticleDto> genericPage = new GenericPage<>();
        genericPage.setData(articlesPage.stream().map(this::toCustomDto).collect(Collectors.toList()));
        BeanUtils.copyProperties(articlesPage, genericPage);

        return ResponseEntity.status(HttpStatus.OK).body(genericPage);

    }

    @Override
    public ResponseEntity<GenericPage> getRatedArticlesFromUser(int page, int size, String fromDate, String toDate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = userRepository.findByUsernameAndDeletedFalse(authentication.getName());

        List<Article> articleList = articleRepository.getRatedArticleFromUser(
                userEntity.getId(),
                DateUtils.stringToLocalDateTime(fromDate),
                DateUtils.stringToLocalDateTime(toDate));

        List<ArticleDto> articleDtos = articleList.stream().map(Article::toDto).collect(Collectors.toList());
        return PageHolderUtils.getResponseEntityGenericPage(page, size, articleDtos);
    }

    @Override
    public ResponseEntity<GenericPage> getPersonalRatedArticlesFromUser(int page, int size, String fromDate, String toDate) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = userRepository.findByUsernameAndDeletedFalse(authentication.getName());

        List<ArticleRepository.CustomRatedArticle> customRatedArticles =
                articleRepository.retrieveAllPersonalRatedArticlesInDescOrder(
                        userEntity.getId(),
                        DateUtils.stringToLocalDateTime(fromDate),
                        DateUtils.stringToLocalDateTime(toDate)
                );

        List<CustomRatedArticleDto> customRatedArticleDtoList = new ArrayList<>();
        customRatedArticles.forEach(customRatedArticle -> {
            CustomRatedArticleDto customRatedArticleDto = new CustomRatedArticleDto();
            BeanUtils.copyProperties(customRatedArticle, customRatedArticleDto);
            if (customRatedArticle.getCount_comment() == null) {
                customRatedArticleDto.setCount_comment(0);
            }
            customRatedArticleDtoList.add(customRatedArticleDto);
        });

        return PageHolderUtils.getResponseEntityGenericPage(page, size, customRatedArticleDtoList);
    }

    private Dimension getImageDimension(String imageUrl) {

        BufferedImage image;
        URL url = null;
        try {
            url = new URL(imageUrl);
            image = ImageIO.read(url);


//            TODO size checking

//            DataBuffer dataBuffer = image.getData().getDataBuffer();
//            long sizeBytes = ((long) dataBuffer.getSize()) * 4L;
//            long sizeMB = sizeBytes / (1024L * 1024L);

            return new Dimension(image.getHeight(), image.getWidth());

        } catch (IOException e) {
            throw new GeneralException("Error while get the resolution of the image", String.valueOf(url));
        }

    }
}
