package com.wudaokou.backend.history;

import com.wudaokou.backend.login.Customer;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Integer> {
    List<History> findAllByCustomer(Customer customer, Sort sort);
//    List<History> findAllByCustomer(Customer customer, Pageable pageable);
    List<History> findAllByCustomerAndType(Customer customer, HistoryType type, Sort sort);
//    List<History> findAllByCustomerAndType(Customer customer, HistoryType type, Pageable pageable);
    @Transactional
    void deleteByCustomerAndType(Customer customer, HistoryType type);
}