package com.wudaokou.backend;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

@Getter
@Setter
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Pattern(regexp = "^[0-9a-zA-Z_]+$", groups = {CheckInfo.class, Default.class})
    @NotNull(groups = {CheckInfo.class, Default.class})
    @Column(nullable = false, unique = true)
    private String username;

    @NotNull
    @NotEmpty
    @NotBlank
    @Column(nullable = false)
    private String password;

    private String token;
}

interface CheckInfo{}
