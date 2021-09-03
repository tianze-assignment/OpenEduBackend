package com.wudaokou.backend.question;

import com.wudaokou.backend.history.Course;
import com.wudaokou.backend.history.HistoryRepository;
import com.wudaokou.backend.login.Customer;
import com.wudaokou.backend.login.SecurityRelated;
import com.wudaokou.backend.question.recommend.Recommend;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class QuestionController {
    private final HistoryRepository historyRepository;
    private final QuestionRepository questionRepository;
    private final UserQuestionRepository userQuestionRepository;
    private final SecurityRelated securityRelated;


    public QuestionController(HistoryRepository historyRepository, QuestionRepository questionRepository, UserQuestionRepository userQuestionRepository, SecurityRelated securityRelated) {
        this.historyRepository = historyRepository;
        this.questionRepository = questionRepository;
        this.userQuestionRepository = userQuestionRepository;
        this.securityRelated = securityRelated;
    }

    @PutMapping("/api/question/count")
    String updateCount(@RequestParam int id,
                        @RequestParam boolean wrong,
                        @RequestParam String qAnswer,
                        @RequestParam String qBody,
                        @RequestParam String label,
                        @RequestParam Course course){
        Customer customer = securityRelated.getCustomer();
        Question question = questionRepository.findById(id).map(
                (q) -> {
                    if(q.getQBody().isEmpty() || q.getQAnswer().isEmpty()){
                        q.setQAnswer(qAnswer);
                        q.setQBody(qBody);
                        return questionRepository.save(q);
                    }
                    return q;
                }
        ).orElseGet(
                () -> questionRepository.save(new Question(id, qAnswer, qBody, label, course))
        );
        int wrongCount = wrong ? 1 : 0;
        Optional<UserQuestion> uq = userQuestionRepository.findByUserQuestionId(new UserQuestionId(customer, question));
        if(uq.isEmpty()) {
            userQuestionRepository.save(new UserQuestion(new UserQuestionId(customer, question), false, 1, wrongCount));
        }else {
            UserQuestion userQuestion = uq.get();
            userQuestion.setTotalCount(userQuestion.getTotalCount() + 1);
            if(wrong)
                userQuestion.setWrongCount(userQuestion.getWrongCount() + 1);
            userQuestionRepository.save(userQuestion);
        }
        return "ok";
    }

    @PutMapping("api/question/star")
    String star(@RequestParam boolean starOrUnstar,
              @RequestParam int id,
              @RequestParam String label,
              @RequestParam Course course){
        Customer customer = securityRelated.getCustomer();
        Question question = questionRepository.findById(id).orElseGet(
                () -> questionRepository.save(new Question(id, "", "", label, course))
        );
        Optional<UserQuestion> uq = userQuestionRepository.findByUserQuestionId(new UserQuestionId(customer, question));
        if(uq.isEmpty()) {
            userQuestionRepository.save(new UserQuestion(new UserQuestionId(customer, question), starOrUnstar, 0, 0));
        }else {
            UserQuestion userQuestion = uq.get();
            userQuestion.setHasStar(starOrUnstar);
            userQuestionRepository.save(userQuestion);
        }
        return "ok";
    }

    @GetMapping("/api/question/recommend")
    Object recommend(@RequestParam Course course,
                             @RequestParam String openEduId){
//        Logger logger = LoggerFactory.getLogger(QuestionController.class);
        return new Recommend(openEduId).recommend(
                securityRelated.getCustomer(),
                course,
                userQuestionRepository,
                historyRepository
        );
    }

}
