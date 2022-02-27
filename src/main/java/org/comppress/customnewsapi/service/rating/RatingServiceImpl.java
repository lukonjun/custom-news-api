package org.comppress.customnewsapi.service.rating;

import org.comppress.customnewsapi.dto.CriteriaRatingDto;
import org.comppress.customnewsapi.dto.RatingDto;
import org.comppress.customnewsapi.dto.SubmitRatingDto;
import org.comppress.customnewsapi.dto.response.CreateRatingResponseDto;
import org.comppress.customnewsapi.dto.response.ResponseDto;
import org.comppress.customnewsapi.dto.response.UpdateRatingResponseDto;
import org.comppress.customnewsapi.entity.Article;
import org.comppress.customnewsapi.entity.Criteria;
import org.comppress.customnewsapi.entity.Rating;
import org.comppress.customnewsapi.entity.UserEntity;
import org.comppress.customnewsapi.exceptions.AuthenticationException;
import org.comppress.customnewsapi.exceptions.CriteriaDoesNotExistException;
import org.comppress.customnewsapi.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final CriteriaRepository criteriaRepository;
    private final ArticleRepository articleRepository;

    public RatingServiceImpl(RatingRepository ratingRepository, UserRepository userRepository, CriteriaRepository criteriaRepository, ArticleRepository articleRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.criteriaRepository = criteriaRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    public List<RatingDto> getRatings() {
        List<Rating> ratingList = ratingRepository.findAll();
        List<RatingDto> ratingDtoList = new ArrayList<>();
        ratingList.stream().map(rating -> ratingDtoList.add(rating.toDto()));
        return ratingDtoList;
    }

    @Override
    public ResponseEntity<ResponseDto> submitRating(SubmitRatingDto submitRatingDto, String guid) throws Exception {
        UserEntity userEntity = null;
        if (guid == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            userEntity = userRepository.findByUsernameAndDeletedFalse(authentication.getName());
            if(userEntity == null) throw new AuthenticationException("You are not authorized, please login","");
        }

        List<Rating> ratings = new ArrayList<>();
        boolean isUpdateRating = false;
        if (userEntity == null && guid != null) {
            // NOT LOGGED IN USER WITH GUID
            for (CriteriaRatingDto criteriaRating : submitRatingDto.getRatings()) {
                validateArticleAndCriteria(submitRatingDto, criteriaRating);
                Rating rating = ratingRepository.findByGuidAndArticleIdAndCriteriaId(guid,
                        submitRatingDto.getArticleId(),
                        criteriaRating.getCriteriaId());
                isUpdateRating = prepareRating(isUpdateRating,submitRatingDto, guid, ratings, criteriaRating, rating, 0L);
            }
        } else {
            // LOGGED IN USER WITH JWT
            for (CriteriaRatingDto criteriaRating : submitRatingDto.getRatings()) {
                validateArticleAndCriteria(submitRatingDto, criteriaRating);
                Rating rating = ratingRepository.findByUserIdAndArticleIdAndCriteriaId(userEntity.getId(),
                        submitRatingDto.getArticleId(),
                        criteriaRating.getCriteriaId());
                isUpdateRating = prepareRating(isUpdateRating,submitRatingDto, "-", ratings, criteriaRating, rating, userEntity.getId());
            }
        }
        ratingRepository.saveAll(ratings);
        if(!isUpdateRating){
            Optional<Article> article = articleRepository.findById(submitRatingDto.getArticleId());
            if (article.isPresent()) {
                Integer countRatings = article.get().getCountRatings();
                if (countRatings == null) countRatings = 0;
                countRatings = countRatings + 1;
                article.get().setCountRatings(countRatings);
                articleRepository.save(article.get());
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(UpdateRatingResponseDto.builder()
                    .message("Created rating for article")
                    .submitRatingDto(submitRatingDto)
                    .build());
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(CreateRatingResponseDto.builder()
                    .message("Updated rating for article")
                    .submitRatingDto(submitRatingDto)
                    .build());
        }
    }

    private boolean prepareRating(boolean isUpdateRating, SubmitRatingDto submitRatingDto, String guid, List<Rating> ratings, CriteriaRatingDto criteriaRating, Rating rating, long l) {
        if (rating != null) {
            rating.setRating(criteriaRating.getRating());
            ratings.add(rating);
            isUpdateRating = true;
        } else {
            Rating newRating = Rating.builder()
                    .rating(criteriaRating.getRating())
                    .articleId(submitRatingDto.getArticleId())
                    .criteriaId(criteriaRating.getCriteriaId())
                    .userId(l) // Anonymous User
                    .guid(guid)
                    .build();
            ratings.add(newRating);
        }
        return isUpdateRating;
    }

    private void validateArticleAndCriteria(SubmitRatingDto submitRatingDto, CriteriaRatingDto criteriaRating) {
        if (!criteriaRepository.existsById(criteriaRating.getCriteriaId())) {
            throw new CriteriaDoesNotExistException("Criteria id not found", String.valueOf(criteriaRating.getCriteriaId()));
        }
        if (!articleRepository.existsById(submitRatingDto.getArticleId())) {
            throw new CriteriaDoesNotExistException("Article id not found", String.valueOf(submitRatingDto.getArticleId()));
        }
    }

    @Override
    public void createRandomRatings(int numberRandomRatings) throws Exception {
        Random random = new Random();
        String guid = UUID.randomUUID().toString();
        List<Criteria> criteriaList = criteriaRepository.findAll();
        for (Article article : articleRepository.retrieveRandomArticles(numberRandomRatings)) {
            SubmitRatingDto submitRatingDto = new SubmitRatingDto();
            List<CriteriaRatingDto> criteriaRatingDtoList = new ArrayList<>();
            for (Criteria criteria : criteriaList) {
                CriteriaRatingDto criteriaRatingDto = new CriteriaRatingDto();
                criteriaRatingDto.setRating(random.nextInt(5) + 1);
                criteriaRatingDto.setCriteriaId(criteria.getId());
                criteriaRatingDtoList.add(criteriaRatingDto);
            }
            submitRatingDto.setRatings(criteriaRatingDtoList);
            submitRatingDto.setArticleId(article.getId());
            submitRating(submitRatingDto, guid);
        }
    }

}
