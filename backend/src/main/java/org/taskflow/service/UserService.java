package org.taskflow.service;

import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.taskflow.dtos.AuthDTO;
import org.taskflow.dtos.UserDTO;
import org.taskflow.config.JwtService;
import org.taskflow.models.User;
import org.taskflow.repository.UserRepository;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();


    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<String> createUser(UserDTO userDTO) {
        try{
            if(userRepository.existsByEmail(userDTO.getEmail())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User already exist");
            }

            User user = new User(userDTO.getUsername(), userDTO.getEmail(), bCryptPasswordEncoder.encode(userDTO.getPassword()));
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User successfully added");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public ResponseEntity<String> authenticateUser(AuthDTO authDTO) throws JOSEException {
        try {
            Optional<User> userOpt = userRepository.findByUsername(authDTO.getUsername()).stream().findFirst();
            if (userOpt.isEmpty() || !bCryptPasswordEncoder.matches(authDTO.getPassword(), userOpt.get().getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
            String token = jwtService.generateToken(userOpt.get().getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(token);
        }catch (JOSEException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error has occurred during authentication");
        }
    }


    public ResponseEntity<String> deleteUser (int userId) {
        if(!userRepository.existsByUserId(userId)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User does not exist");
        }else{
            try{
                User user = userRepository.findByUserId(userId).getFirst();
                userRepository.delete(user);
                return ResponseEntity.ok().body("User successfully deleted");
            }catch(Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("There was an error processing your request: " + e.getMessage());
            }
        }
    }

    public ResponseEntity<String> updateUsername (int userId, String username) {
        if(!userRepository.existsByUserId(userId)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User does not exist");
        }
        try {
            User user = userRepository.findByUserId(userId).getFirst();
            user.setUsername(username);
            userRepository.save(user);
            return ResponseEntity.ok().body("Username successfully updated");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public ResponseEntity<String> updatePassword (int userId, String password) {
        if(!userRepository.existsByUserId(userId)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User does not exist");
        }
        try{
            User user = userRepository.findByUserId(userId).getFirst();
            user.setPassword(bCryptPasswordEncoder.encode(password));
            userRepository.save(user);

            return ResponseEntity.ok()
                    .body("Password successfully updated");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public ResponseEntity<String> updateEmail (int userId, String email) {
        if(!userRepository.existsByUserId(userId)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User does not exist");
        }
        if (!isValidEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid email format");
        }
        try{
            User user = userRepository.findByUserId(userId).getFirst();
            user.setEmail(email);
            userRepository.save(user);
            return ResponseEntity.ok().body("Email successfully updated");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There was an error processing your request: " + e.getMessage());
        }
    }

    public User getUserById(int userId){
        return userRepository.findByUserId(userId).getFirst();
    }

    public User getUserByUsername(String username){
        return userRepository.findByUsername(username).getFirst();
    }


    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).getFirst();
    }


    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }
    public boolean isValidUser(String email){
        return userRepository.existsByEmail(email);
    }

    public boolean isValidUser(int userId){
        return userRepository.existsByUserId(userId);
    }
}