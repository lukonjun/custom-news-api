package org.comppress.customnewsapi.service.home;

import org.comppress.customnewsapi.dto.CustomRatedArticleDto;
import org.comppress.customnewsapi.dto.GenericPage;
import org.comppress.customnewsapi.utils.GenerateGenericPageUtils;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface HomeService extends GenerateGenericPageUtils {

    ResponseEntity<GenericPage> getUserPreference(int page, int size,String lang, List<Long> categoryId,
                                                                     List<Long> publisherIds, String fromDate, String toDate, Boolean noPaywall);

    ResponseEntity<GenericPage<CustomRatedArticleDto>> getArticleForCategory(int page, int size,
                                                                             List<Long> categoryIds, List<Long> publisherIds,
                                                                             String lang, String fromDate, String toDate, Boolean noPaywall);
}
