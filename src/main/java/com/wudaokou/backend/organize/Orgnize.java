package com.wudaokou.backend.organize;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;

public class Orgnize {
    WebClient searchClient = WebClient.create("http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList");
    WebClient linkClient = WebClient.create("http://open.edukg.cn/opedukg/api/typeOpen/open/relatedsubject");
    WebClient infoClient = WebClient.create("http://open.edukg.cn/opedukg/api/typeOpen/open/infoByInstanceName");
    String openEduId;

    public Orgnize(String openEduId) {
        this.openEduId = openEduId;
    }

    public Mono<ListReturn<ResponseSearch>> search(String course, String key){
        return searchClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("id", openEduId)
                        .queryParam("course", course)
                        .queryParam("searchKey", key)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>(){});
    }

    public Flux<ListReturn<ResponseSearch>> searchConcurrent(String course, List<String> keys){
        return Flux.fromIterable(keys)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(key -> search(course, key))
                .sequential();
    }

    public Mono<ListReturn<ResponseLinkRelation>> link(String course, String subjectName){
        return linkClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(new LinkedMultiValueMap<>(){{
                    add("course", course);
                    add("subjectName", subjectName);
                    add("id", openEduId);
                }}))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>(){});
    }

    public Flux<ListReturn<ResponseLinkRelation>> linkConcurrent(String course, List<String> subjectNames){
        return Flux.fromIterable(subjectNames)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(name -> link(course, name))
                .sequential();
    }

    public Mono<ObjectReturn<ResponseInfoData>> info(String course, String name){
        return infoClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("id", openEduId)
                        .queryParam("course", course)
                        .queryParam("name", name)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>(){});
    }
}
