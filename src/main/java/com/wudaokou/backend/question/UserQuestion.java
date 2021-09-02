package com.wudaokou.backend.question;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class UserQuestion {
    @EmbeddedId
    UserQuestionId userQuestionId;

    private Boolean hasStar;

    private Integer totalCount;

    private Integer wrongCount;

    public UserQuestion(UserQuestionId userQuestionId, Boolean hasStar, Integer totalCount, Integer wrongCount) {
        this.userQuestionId = userQuestionId;
        this.hasStar = hasStar;
        this.totalCount = totalCount;
        this.wrongCount = wrongCount;
    }

    public UserQuestion() {

    }
}
