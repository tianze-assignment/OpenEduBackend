package com.wudaokou.backend;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Map;
import java.util.Optional;

@RestController
public class LoginController {

    private final UserRepository userRepository;
    private final byte[] salt;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.salt = "fifthStreet".getBytes();
    }

    Boolean userExists(User user){
        return userRepository.existsByUsername(user.getUsername());
    }

    @PostMapping("/checkUsername")
    ResponseEntity<?> checkUsername(@Validated(CheckInfo.class) @RequestBody User user){
        return ResponseEntity.ok(Map.of(
                "valid", !userExists(user)
        ));
    }

    String hashPassword(String password){
        // iteration times
        final int ITERATION_COUNT = 65536;
        // number of bits
        final int KEY_LENGTH = 128;
        // number of bytes
        final int HASH_BYTE_ARRAY_LENGTH = KEY_LENGTH / 8;
        // one byte corresponds to two hexadecimal digits
        final int HEX_DIGITS_LENGTH = KEY_LENGTH / 4;

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = null;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = new byte[0];
        try {
            assert factory != null;
            hash = factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        // convert byte array to hex string
        char[] hexDigits = new char[HEX_DIGITS_LENGTH];
        for(int i = 0; i < HASH_BYTE_ARRAY_LENGTH; i++){
            hexDigits[2*i] = Character.forDigit((hash[i]>>4) & 0xF, 16);
            hexDigits[2*i+1] = Character.forDigit(hash[i] & 0xF, 16);
        }
        return new String(hexDigits);
    }

    String randomString(int length) {
        String availables = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int)(availables.length() * Math.random());
            sb.append(availables.charAt(index));
        }
        return sb.toString();
    }

    String generateToken(int length) {
        String token;
        do{
            token = randomString(length);
        }while(userRepository.existsByToken(token));
        return token;
    }

    @PostMapping("/register")
    ResponseEntity<?> register(@Valid @RequestBody User user) {
        if( userExists(user) ){
            return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
        }
        // hash password
        String hashedPassword = hashPassword(user.getPassword());

        // generate token
        String token = generateToken(64);

        // store to db
        user.setPassword(hashedPassword);
        user.setToken(token);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "token", token
        ));
    }

    @PostMapping("/login")
    ResponseEntity<?> login(@Valid @RequestBody User user) {
        Optional<User> dbUser = userRepository.findByUsername(user.getUsername());
        if(dbUser.isEmpty())
            return new ResponseEntity<>("User does not exist", HttpStatus.NOT_FOUND);
        User realUser = dbUser.get();

        // hash password
        String hashedPassword = hashPassword(user.getPassword());

        // validate
        if(!hashedPassword.equals(realUser.getPassword()))
            return new ResponseEntity<>("Wrong password", HttpStatus.NOT_ACCEPTABLE);

        // generate token
        String token = generateToken(64);

        // store to db
        realUser.setToken(token);
        userRepository.save(realUser);

        return ResponseEntity.ok(Map.of(
                "token", token
        ));
    }

    @PutMapping("/changePassword")
    ResponseEntity<?> changePassword(@Valid @RequestBody changePasswordParams user){
        Optional<User> dbUser = userRepository.findByUsername(user.getUsername());
        if(dbUser.isEmpty())
            return new ResponseEntity<>("User does not exist", HttpStatus.NOT_FOUND);
        User realUser = dbUser.get();

        // validate old password
        String hashedOldPassword = hashPassword(user.getOldPassword());
        if(!hashedOldPassword.equals(realUser.getPassword()))
            return new ResponseEntity<>("Wrong password", HttpStatus.NOT_ACCEPTABLE);

        // store new password
        String hashedNewPassword = hashPassword(user.getNewPassword());
        realUser.setPassword(hashedNewPassword);
        userRepository.save(realUser);

        return ResponseEntity.ok("Changed password successfully");
    }

}

@Getter
@Setter
class changePasswordParams{
    @Pattern(regexp = Constants.USERNAME_REGEX)
    @NotNull
    private String username;

    @NotNull
    @NotEmpty
    @NotBlank
    private String oldPassword;

    @NotNull
    @NotEmpty
    @NotBlank
    private String newPassword;
}


