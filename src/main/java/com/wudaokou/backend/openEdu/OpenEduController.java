package com.wudaokou.backend.openEdu;

import com.wudaokou.backend.history.Course;
import com.wudaokou.backend.history.History;
import com.wudaokou.backend.history.HistoryRepository;
import com.wudaokou.backend.history.HistoryType;
import com.wudaokou.backend.login.Customer;
import com.wudaokou.backend.login.SecurityRelated;
import com.wudaokou.backend.openEdu.response.Instance;
import com.wudaokou.backend.openEdu.response.ReturnList;
import org.hibernate.mapping.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestMapping("/open")
@RestController
public class OpenEduController {

    SecurityRelated securityRelated;
    Client client;
    HistoryRepository historyRepository;

    public OpenEduController(SecurityRelated securityRelated, Client client, HistoryRepository historyRepository) {
        this.securityRelated = securityRelated;
        this.client = client;
        this.historyRepository = historyRepository;
    }

    @GetMapping("/instanceList")
    Object instanceList(@RequestHeader("Authorization") String token,
                        @RequestParam Course course,
                        @RequestParam String searchKey,
                        @RequestParam String id){
        Optional<Customer> customer = securityRelated.getCustomer(token);
        ReturnList<Instance> rsp = client.instanceList(course.toString().toLowerCase(), searchKey, id).block();
        if(rsp == null)
            return ResponseEntity.internalServerError();
        if(customer.isPresent()){
            HashSet<String> set = historyRepository.findAllByCustomerAndType(customer.get(), HistoryType.star).stream()
                    .map(History::getUri).collect(Collectors.toCollection(HashSet::new));
            rsp.setData(rsp.getData().stream()
                            .peek(v -> v.setHasStar(set.contains(v.getUri()))).collect(Collectors.toList()));
        }
        return rsp;
    }


}
