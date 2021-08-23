package com.wudaokou.backend.history;

import com.wudaokou.backend.login.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Integer> {
    List<History> findAllByCustomerAndCourse(Customer customer, Course course, Pageable pageable);
}