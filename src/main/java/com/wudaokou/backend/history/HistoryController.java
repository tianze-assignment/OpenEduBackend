package com.wudaokou.backend.history;

import com.wudaokou.backend.login.Customer;
import com.wudaokou.backend.login.SecurityRelated;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class HistoryController {
    private final HistoryRepository historyRepository;
    private final SecurityRelated securityRelated;

    private final int MAX_SEARCH_HISTORY_SIZE = 10;

    public HistoryController(HistoryRepository historyRepository, SecurityRelated securityRelated) {
        this.historyRepository = historyRepository;
        this.securityRelated = securityRelated;
    }

    @PostMapping("/api/history/{type}")
    List<History> postSearchHistory(@Valid @RequestBody History history,
                                               @PathVariable HistoryType type){
        history.setType(type);
        Customer customer = securityRelated.getCustomer();
        history.setCustomer(customer);
        historyRepository.save(history);
        List<History> list = historyRepository.findAllByCustomerAndType(customer, type, Sort.by("createdAt").descending());
        if(type == HistoryType.search){
            while(list.size() > MAX_SEARCH_HISTORY_SIZE)
                historyRepository.deleteById( list.remove(MAX_SEARCH_HISTORY_SIZE).getId() );
        }
        return list;
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
