package com.wudaokou.backend.history;

import com.wudaokou.backend.login.Customer;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

@Getter @Setter
@Entity
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    private Customer customer;

    @Column(columnDefinition = "TIMESTAMP", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @NotNull
    private String searchKey;

    public Map<?, ?> toMap(){
        return Map.of(
                "searchKey", searchKey,
                "createAt", createdAt
        );
    }
}
