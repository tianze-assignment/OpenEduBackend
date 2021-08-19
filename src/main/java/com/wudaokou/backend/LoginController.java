package com.wudaokou.backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
public class LoginController {

    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

//    @PostMapping("/login")
//    ResponseEntity<String> login(@RequestBody User user){
//
//    }

//    @PostMapping("/register")

    Boolean userExists(User user){
        return userRepository.existsByUsername(user.getUsername());
    }

    @PostMapping("/checkUsername")
    ResponseEntity<?> checkUsername(@Validated(CheckInfo.class) @RequestBody User user){
        return ResponseEntity.ok(Map.of(
                "valid", !userExists(user)
        ));
    }

    @PostMapping("/register")
    ResponseEntity<?> register(@Valid @RequestBody User user){
        if( userExists(user) ){
            return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
        }
//        if(user.getPassword().isBlank())
        return ResponseEntity.ok(
                Map.of("token", "abc", "password", user.getPassword())
        );
    }

}
