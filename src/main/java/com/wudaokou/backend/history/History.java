package com.wudaokou.backend.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wudaokou.backend.login.Customer;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class History {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    private Customer customer;

    @Column(columnDefinition = "TIMESTAMP", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HistoryType type;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Course course;

    @NotNull
    @Column(nullable = false)
    private String name;

    private String uri;

}