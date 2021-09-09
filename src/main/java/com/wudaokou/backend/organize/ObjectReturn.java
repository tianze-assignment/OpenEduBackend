package com.wudaokou.backend.organize;

import lombok.Data;

@Data
public class ObjectReturn<T> {
    T data;
    String code;
    String message;
}
