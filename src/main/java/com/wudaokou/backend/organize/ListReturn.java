package com.wudaokou.backend.organize;

import lombok.Data;

import java.util.List;

@Data
public class ListReturn<T> {
    List<T> data;
    String code;
    String message;
}
