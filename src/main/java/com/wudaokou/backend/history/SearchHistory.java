package com.wudaokou.backend.history;

import com.wudaokou.backend.login.Customer;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    private Customer customer;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime localDateTime;
}
