package com.wudaokou.backend.history;

import com.wudaokou.backend.login.Customer;
import com.wudaokou.backend.login.SecurityRelated;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/api/history/search")
    List<?> get(@RequestParam int page, @RequestParam int size){
        List<SearchHistory> historyList = searchHistoryRepository.findAllByCustomer(
                securityRelated.getCustomer(),
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return historyList.stream().map(SearchHistory::toMap).collect(Collectors.toList());
    }
}
