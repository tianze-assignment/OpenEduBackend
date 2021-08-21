package com.wudaokou.backend.history;

import com.wudaokou.backend.login.SecurityRelated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class SearchHistoryController {
    private final SearchHistoryRepository searchHistoryRepository;
    private final SecurityRelated securityRelated;

    public SearchHistoryController(SearchHistoryRepository searchHistoryRepository, SecurityRelated securityRelated) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.securityRelated = securityRelated;
    }

    @PostMapping ("/api/history/search")
    ResponseEntity<?> post(@Valid @RequestBody SearchHistory searchHistory){
        searchHistory.setCustomer(securityRelated.getCustomer());
        searchHistoryRepository.save(searchHistory);
        return ResponseEntity.ok("ok");
    }
}
