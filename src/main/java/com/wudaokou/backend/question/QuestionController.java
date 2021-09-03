package com.wudaokou.backend.question;

import com.wudaokou.backend.history.Course;
import com.wudaokou.backend.history.HistoryRepository;
import com.wudaokou.backend.login.Customer;
import com.wudaokou.backend.login.SecurityRelated;
import org.springframework.data.domain.PageRequest;
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
    List<?> recommend(@RequestParam Course course,
                             @RequestParam String openEduId){
        List<Question> questions = new LinkedList<>();
        List<String> labels = new LinkedList<>();
        // 1个错题，1个易错知识点，2个高频访问知识点，1个随机知识点
        final int totalCount = 5;
        final int wrongQuestionCount = 1;
        final int wrongEntityCount = 1;
        final int frequentEntityCount = 2;
        final int randomEntityCount = totalCount - wrongQuestionCount - wrongEntityCount - frequentEntityCount;
        final String[] randomNames = SubjectKeywords.getMap().get(course);

        Customer customer = securityRelated.getCustomer();
        List<UserQuestion> userQuestions = userQuestionRepository.findByUserQuestionId_CustomerAndUserQuestionId_Question_Course(customer, course);
        userQuestions.sort(Comparator.comparingDouble(UserQuestion::recommendationValue));
        if( ! userQuestions.isEmpty() ) {
            questions.add(userQuestions.get(0).getUserQuestionId().getQuestion());  // 1个错题
            labels.add(questions.get(0).getLabel());  // 1个易错知识点
        }

        // 高频知识点
        List<String> topFrequentNamesOfEntity = historyRepository.findTopFrequentNameOfEntity(customer, course, PageRequest.of(0, 4));
        while( !topFrequentNamesOfEntity.isEmpty() &&
                labels.size() < wrongEntityCount + frequentEntityCount + (questions.isEmpty() ? 1 : 0) ){
            labels.add(topFrequentNamesOfEntity.remove(0));
        }

        // 随机知识点
        Random rand = new Random();
        while(labels.size() < totalCount - (questions.isEmpty() ? 0 : 1)){
            String label = randomNames[rand.nextInt(randomNames.length)];
            if(!labels.contains(label))
                labels.add(label);
        }

        // 请求
        String baseUrl = "http://open.edukg.cn/opedukg/api/typeOpen/open/questionListByUriName";


        return questions;
    }

}
