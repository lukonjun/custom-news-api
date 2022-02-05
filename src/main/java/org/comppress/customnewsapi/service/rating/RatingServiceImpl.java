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
    public ResponseEntity<ResponseDto> submitRating(SubmitRatingDto submitRatingDto, String guid) {

        UserEntity userEntity = getUserEntityIfGuidNotSet(guid);

        checkIfCriteriaIdAndArticleIdExist(submitRatingDto);
        boolean isNewRating = checkIfNewRating(submitRatingDto, guid);
        List<Rating> ratings = new ArrayList<>();
        
        Article article = articleRepository.findById(submitRatingDto.getArticleId()).get();
        if(isNewRating){
            // New Rating
            getAddNewRatings(submitRatingDto, article);
            articleRepository.save(article);
            getPreparedUpdatedOrNewRatings(submitRatingDto, guid, false, userEntity, ratings);
            ratingRepository.saveAll(ratings);
            return ResponseEntity.status(HttpStatus.CREATED).body(UpdateRatingResponseDto.builder()
                    .message("Created rating for article")
                    .submitRatingDto(submitRatingDto)
                    .build());
        } else {
            // Update Rating
            // Subtract Values from old article Object
            getSubtractRatingsFromArticle(submitRatingDto, article,guid,userEntity.getId());
            getAddNewRatings(submitRatingDto, article);
            articleRepository.save(article);
            getPreparedUpdatedOrNewRatings(submitRatingDto, guid, true, userEntity, ratings);
            ratingRepository.saveAll(ratings);

            return ResponseEntity.status(HttpStatus.CREATED).body(CreateRatingResponseDto.builder()
                    .message("Updated rating for article")
                    .submitRatingDto(submitRatingDto)
                    .build());
        }
    }

    private void getSubtractRatingsFromArticle(SubmitRatingDto submitRatingDto, Article article, String guid, Long userId) {
        // How to differntiate from Users who are logged in?
        // Or search for find by userId
        for(CriteriaRatingDto criteriaRatingDto:submitRatingDto.getRatings()){
            Rating rating;
            if(guid != null){
                rating = ratingRepository.findByGuidAndArticleIdAndCriteriaId(guid,
                        submitRatingDto.getArticleId(),
                        criteriaRatingDto.getCriteriaId());
            } else {
                rating = ratingRepository.findByUserIdAndArticleIdAndCriteriaId(userId,
                        submitRatingDto.getArticleId(),
                        criteriaRatingDto.getCriteriaId());
            }

            if(criteriaRatingDto.getCriteriaId() == 1 && rating.getRating() != criteriaRatingDto.getRating()){
                article.setRating1sum(article.getRating1sum() - rating.getRating() + criteriaRatingDto.getRating());
                article.setAverageRating1(article.getRating1sum()/(double) article.getCountRating1());
            }
            if(criteriaRatingDto.getCriteriaId() == 2 && rating.getRating() != criteriaRatingDto.getRating()){
                article.setRating1sum(article.getRating2sum() - rating.getRating() + criteriaRatingDto.getRating());
                article.setAverageRating1(article.getRating2sum()/(double) article.getCountRating2());
            }
            if(criteriaRatingDto.getCriteriaId() == 3 && rating.getRating() != criteriaRatingDto.getRating()){
                article.setRating1sum(article.getRating3sum() - rating.getRating() + criteriaRatingDto.getRating());
                article.setAverageRating1(article.getRating3sum()/(double) article.getCountRating3());
            }
        }
        // Recalculation


    }

    private void getPreparedUpdatedOrNewRatings(SubmitRatingDto submitRatingDto, String guid, boolean isUpdateRating, UserEntity userEntity, List<Rating> ratings) {
        if(guid ==  null) guid = "-";
        Long userId;
        if(userEntity == null){
            userId = 0L;
        }else{
            userId = userEntity.getId();
        }

        for(CriteriaRatingDto criteriaRating:submitRatingDto.getRatings()) {
            Rating rating = ratingRepository.findByGuidAndArticleIdAndCriteriaId(guid,
                    submitRatingDto.getArticleId(),
                    criteriaRating.getCriteriaId());
            if (rating != null) {
                rating.setRating(criteriaRating.getRating());
                ratings.add(rating);
            } else {
                Rating newRating = Rating.builder()
                        .rating(criteriaRating.getRating())
                        .articleId(submitRatingDto.getArticleId())
                        .criteriaId(criteriaRating.getCriteriaId())
                        .userId(userId)
                        .guid(guid)
                        .build();
                ratings.add(newRating);
            }
        }
    }

    private void checkIfCriteriaIdAndArticleIdExist(SubmitRatingDto submitRatingDto) {
        for (CriteriaRatingDto criteriaRating : submitRatingDto.getRatings()) {
            validateArticleAndCriteria(submitRatingDto, criteriaRating);
        }
    }

    private void getAddNewRatings(SubmitRatingDto submitRatingDto, Article article) {
        for(CriteriaRatingDto rating:submitRatingDto.getRatings()){
            if(rating.getCriteriaId() == 1){
                article.setCountRating1(article.getCountRating1() + 1);
                article.setRating1sum(article.getRating1sum() + rating.getRating());
                article.setAverageRating1(article.getRating1sum()/(double) article.getCountRating1());
            }
            if(rating.getCriteriaId() == 2){
                article.setCountRating2(article.getCountRating2() + 1);
                article.setRating2sum(article.getRating2sum() + rating.getRating());
                article.setAverageRating2(article.getRating2sum()/(double) article.getCountRating2());
            }
            if(rating.getCriteriaId() == 3){
                article.setCountRating3(article.getCountRating3() + 1);
                article.setRating3sum(article.getRating3sum() + rating.getRating());
                article.setAverageRating3(article.getRating3sum()/(double) article.getCountRating3());
            }
        }
        article.setCountRatings(article.getCountRatings() + 1);
        Integer validCriteria = 0;
        if(article.getAverageRating1() != 0) validCriteria++;
        if(article.getAverageRating2() != 0) validCriteria++;
        if(article.getAverageRating3() != 0) validCriteria++;
        Double totalSumRating = (article.getAverageRating1() + article.getAverageRating2() + article.getAverageRating3())/(double)validCriteria;
        article.setTotalAverageRating(totalSumRating);
    }

    private boolean checkIfNewRating(SubmitRatingDto submitRatingDto, String guid) {
        boolean isNewRating = true;
        if(guid ==  null) guid = "-";
        for (CriteriaRatingDto criteriaRating : submitRatingDto.getRatings()) {
            Rating rating = ratingRepository.findByGuidAndArticleIdAndCriteriaId(guid,
                    submitRatingDto.getArticleId(),
                    criteriaRating.getCriteriaId());
            if(rating != null){
                isNewRating = false;
                break;
            }
        }
        return isNewRating;
    }

    private UserEntity getUserEntityIfGuidNotSet(String guid) {
        UserEntity userEntity = null;
        if (guid == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            userEntity = userRepository.findByUsername(authentication.getName());
            if(userEntity == null) throw new AuthenticationException("You are not authorized, please login","");
        }
        return userEntity;
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
