package com.wudaokou.backend.organize;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResponseInfoContent {
    String predicate_label;
    String object_label;
    String subject_label;
}
