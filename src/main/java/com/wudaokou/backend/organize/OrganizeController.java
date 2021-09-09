package com.wudaokou.backend.organize;

import com.wudaokou.backend.BackendApplication;
import com.wudaokou.backend.history.Course;
import org.hibernate.hql.internal.classic.GroupByParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OrganizeController {

    @GetMapping("/api/organize")
    Object organize(@RequestParam String id,
                  @RequestParam Course course,
                  @RequestParam String label){
        Orgnize organize = new Orgnize(id);

//        List<ListReturn<ResponseSearch>> rs = organize.searchConcurrent(course.name().toLowerCase(), labels)
//                .collectList().block();
//
//        assert rs != null;
//        List<ResponseSearch> results = rs.stream()
//                .filter(v -> v.getData() != null)
//                .filter(v -> !v.getData().isEmpty())
//                .map(v -> v.getData().get(0))
//                .collect(Collectors.toList());
//
//        List<String> finalLabels = results.stream().map(ResponseSearch::getLabel).collect(Collectors.toList());
//
        System.out.println("start");

        ListReturn<ResponseLinkRelation> rl = organize.link(course.name().toLowerCase(), label).block();
        assert rl != null;
        System.out.println(rl);
        List<ResponseLinkRelation> dataLink = rl.getData();
        assert dataLink != null;
        List<String> names = dataLink.stream()
                .filter(v -> v.getPredicate().contains("相关"))
                .map(ResponseLinkRelation::getValue)
                .collect(Collectors.toList());

        System.out.println("middle");
//        List<ListReturn<ResponseLinkRelation>> rl = organize.linkConcurrent(course.name().toLowerCase(), finalLabels)
//                .collectList().block();
//
////        System.out.println(rl);
//
//        assert rl != null;
//        List<String> names = rl.stream()
//                .filter(v -> v.getData() != null)
//                .map(v -> v.getData().stream()
//                        .filter(w -> w.getPredicate().contains("相关"))
//                        .map(ResponseLinkRelation::getValue)
//                        .collect(Collectors.toList()))
//                .flatMap(Collection::stream)
//                .collect(Collectors.toList());
//
//        finalLabels.forEach(l -> {
//            if(!names.contains(l))
//                names.add(l);
//        });
//
//        Collections.shuffle(names);
//
//
//        return names;
        ObjectReturn<ResponseInfoData> ri = organize.info(course.name().toLowerCase(), label).block();
        assert ri != null;
        ResponseInfoData data = ri.getData();
        assert data != null;
        List<String> namesInfo = data.getContent().stream()
                .filter(v -> v.getPredicate_label().contains("相关"))
                .map(v -> v.getObject_label()==null ? v.getSubject_label() : v.getObject_label())
                .collect(Collectors.toList());
        System.out.println("end");
        namesInfo.forEach(l -> {
            if(!names.contains(l))
                names.add(l);
        });

        return names;
    }


}
