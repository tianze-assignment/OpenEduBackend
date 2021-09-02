package com.wudaokou.backend.history;

import com.wudaokou.backend.login.Customer;
import com.wudaokou.backend.login.SecurityRelated;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    Object postSearchHistory(@Valid @RequestBody History history,
                                               @PathVariable HistoryType type){
        history.setType(type);
        Customer customer = securityRelated.getCustomer();
        history.setCustomer(customer);
        History ret = historyRepository.save(history);
        if(type != HistoryType.search)
            return ret;
        List<History> list = historyRepository.findAllByCustomerAndType(customer, type, Sort.by("createdAt").descending());
        while(list.size() > MAX_SEARCH_HISTORY_SIZE)
            historyRepository.deleteById( list.remove(MAX_SEARCH_HISTORY_SIZE).getId() );
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

    @DeleteMapping("/api/history/{s}")
    ResponseEntity<?> delete(@PathVariable String s){
        try {
            int id = Integer.parseInt(s);
            historyRepository.deleteById(id);
        }catch(NumberFormatException nfe){
            try{
                Customer customer = securityRelated.getCustomer();
                HistoryType type = HistoryType.valueOf(s);
                historyRepository.deleteByCustomerAndType(customer, type);
            }catch(IllegalArgumentException iae){
                return ResponseEntity.badRequest().body("Illegal type: " + s);
            }
        }
        return ResponseEntity.ok(new History());
    }

}
