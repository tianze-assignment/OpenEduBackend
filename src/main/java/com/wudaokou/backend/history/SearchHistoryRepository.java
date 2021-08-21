package com.wudaokou.backend.history;

import com.wudaokou.backend.login.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Integer> {
    List<SearchHistory> findAllByCustomer(Customer customer, Pageable pageable);
}
