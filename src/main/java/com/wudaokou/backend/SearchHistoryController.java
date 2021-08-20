package com.wudaokou.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class SearchHistoryController {
    private final SearchHistoryRepository searchHistoryRepository;

    public SearchHistoryController(SearchHistoryRepository searchHistoryRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
    }

    @GetMapping("/test")
    ResponseEntity<?> test(){
        SearchHistory s = new SearchHistory();
        s.setLocalDateTime(LocalDateTime.now());
        searchHistoryRepository.save(s);
        return ResponseEntity.ok("ok");
    }
}
