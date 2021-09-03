package com.wudaokou.backend.question.recommend;

import com.wudaokou.backend.history.Course;
import com.wudaokou.backend.history.HistoryRepository;
import com.wudaokou.backend.login.Customer;
import com.wudaokou.backend.question.Question;
import com.wudaokou.backend.question.SubjectKeywords;
import com.wudaokou.backend.question.UserQuestion;
import com.wudaokou.backend.question.UserQuestionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

public class Recommend {
    WebClient webClient = WebClient.create("http://open.edukg.cn/opedukg/api/typeOpen/open/questionListByUriName");

    String openEduId;

    public Recommend(String openEduId) {
        this.openEduId = openEduId;
    }

    public List<Question> recommend(Customer customer,
                                    Course course,
                                    UserQuestionRepository userQuestionRepository,
                                    HistoryRepository historyRepository){
        List<Question> questions = new LinkedList<>();
        List<String> labels = new LinkedList<>();
        // 1个错题，1个易错知识点，2个高频访问知识点，1个随机知识点
        final int totalCount = 5;
        final int wrongQuestionCount = 1;
        final int wrongEntityCount = 1;
        final int frequentEntityCount = 2;
        final int randomEntityCount = totalCount - wrongQuestionCount - wrongEntityCount - frequentEntityCount;
        final String[] randomNames = SubjectKeywords.getMap().get(course);

        List<UserQuestion> userQuestions = userQuestionRepository
                .findByUserQuestionId_CustomerAndUserQuestionId_Question_Course(customer, course);
        userQuestions.sort(Comparator.comparingDouble(UserQuestion::recommendationValue));
        if( ! userQuestions.isEmpty() ) {
            questions.add(userQuestions.get(0).getUserQuestionId().getQuestion());  // 1个错题
            labels.add(questions.get(0).getLabel());  // 1个易错知识点
        }

        // 高频知识点
        List<String> topFrequentNamesOfEntity = historyRepository
                .findTopFrequentNameOfEntity(customer, course, PageRequest.of(0, 4));
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
        List<QuestionResponse> responses = fetchQuestions(labels, course).collectList().block();

        assert responses != null;
        for(int i = 0; i < responses.size(); i++){
            List<Question> qs = responses.get(i).getData();
            if(qs == null)
                throw new WebClientResponseException("OpenEdu Not Logged In", 500, "Server Error", null, null, null);
            if(qs.isEmpty()){
                if(i != responses.size() - 1) continue;
                // if the last one is empty
                // size of responses won't be less than 2 unless labels is less than 2
                for(int j = responses.size() - 2; j >= 0 && questions.size() < totalCount; j--){
                    List<Question> qsBackward = responses.get(j).getData();
                    for(Question qBackward : qsBackward){
                        if(questions.size() == totalCount) break;
                        if(containsQuestion(questions, qBackward)) continue;
                        questions.add(qBackward);
                    }
                }
                break;
            }
            Question q;
            do {
                q = qs.get(rand.nextInt(qs.size()));
            }while(containsQuestion(questions, q));
            questions.add(q);
        }

        Collections.shuffle(questions);
        return questions;
    }

    private boolean containsQuestion(List<Question> questions, Question question){
        for(Question q : questions)
            if(Objects.equals(q.getId(), question.getId())) return true;
        return false;
    }

    public Mono<QuestionResponse> getQuestion(String name, Course course){
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("id", openEduId)
                        .queryParam("uriName", name)
                        .build())
                .retrieve()
                .bodyToMono(QuestionResponse.class)
                .map(qr -> {
                    qr.setData(
                            qr.getData().stream().peek(
                                    q -> {
                                        q.setLabel(name);
                                        q.setCourse(course);
                                    }
                            ).collect(Collectors.toList())
                    );
                    return qr;
                });
    }

    public Flux<QuestionResponse> fetchQuestions(List<String> names, Course course){
        return Flux.fromIterable(names)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(name -> getQuestion(name, course))
                .sequential();
    }
}
