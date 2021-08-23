package com.wudaokou.backend.history;

import com.wudaokou.backend.login.SecurityRelated;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class HistoryController {
    private final HistoryRepository historyRepository;
    private final SecurityRelated securityRelated;

    public HistoryController(HistoryRepository historyRepository, SecurityRelated securityRelated) {
        this.historyRepository = historyRepository;
        this.securityRelated = securityRelated;
    }

    @PostMapping("/api/history/search")
    public ResponseEntity<?> postSearchHistory(@Valid @RequestBody History history){
        history.setType(HistoryType.SEARCH);
        history.setCustomer(securityRelated.getCustomer());
        historyRepository.save(history);
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/api/history/search")
    List<?> get(@RequestParam int page, @RequestParam int size, @RequestParam Course course){
        return historyRepository.findAllByCustomerAndCourse(
                securityRelated.getCustomer(),
                course,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
    }
}
