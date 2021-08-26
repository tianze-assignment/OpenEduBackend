package com.wudaokou.backend.history;

import com.wudaokou.backend.login.Customer;
import com.wudaokou.backend.login.SecurityRelated;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @PostMapping("/api/history/{type}")
    History postSearchHistory(@Valid @RequestBody History history,
                                               @PathVariable HistoryType type){
        history.setType(type);
        history.setCustomer(securityRelated.getCustomer());
        return historyRepository.save(history);
    }

    @GetMapping(value = {"/api/history", "/api/history/{type}"})
    List<?> get(@PathVariable(required = false) HistoryType type){
        Customer customer = securityRelated.getCustomer();
        Sort sort = Sort.by("createdAt").descending();
        // @RequestParam(required = false) int page, @RequestParam(required = false) int size
        // Pageable pageable = PageRequest.of(page, size, sort);
        if(type == null)
            return historyRepository.findAllByCustomer(customer, sort);
        return historyRepository.findAllByCustomerAndType(customer, type, sort);
    }

    @DeleteMapping("/api/history/{id}")
    void delete(@PathVariable int id){
        historyRepository.deleteById(id);
    }
}
