package org.taskflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.taskflow.DTO.AuthDTO;
import org.taskflow.config.JwtService;
import org.taskflow.model.User;
import org.taskflow.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserDataService {

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();


    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<String> createUser(User user) {
        try{
            if(userRepository.existsByUserId(user.getUserId())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User already exist");
            }
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User successfully added");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public ResponseEntity<String> authenticateUser(AuthDTO authDTO) {
        Optional<User> userOpt = userRepository.findByUsername(authDTO.getUsername()).stream().findFirst();
        if (userOpt.isEmpty() || !bCryptPasswordEncoder.matches(authDTO.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        String token = jwtService.generateToken(new AuthDTO( authDTO.getPassword(),userOpt.get().getUserId(), authDTO.getUsername()));
        return ResponseEntity.ok(token);
    }

    public UserDetails loadUserById(int userId) {
        User user = userRepository.findByUserId(userId).getFirst();
        if(user == null){
            throw new RuntimeException("User not found");
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities("USER")
                .build();
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

    public List<User> getUsersById(int userId){
        return userRepository.findByUserId(userId);
    }
    public User getUserById(int userId){
        return userRepository.findByUserId(userId).getFirst();
    }

    public User getUserByUsername(String username){
        return userRepository.findByUsername(username).getFirst();
    }

    public List<User> getUserByUsernameContaining(String username){
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email).getFirst();
    }

    public List<User> getUserByEmailContaining(String email){
        return userRepository.findByEmailContainingIgnoreCase(email);
    }

    public List<User> getTaskByCreatedAtBefore(LocalDateTime createdAt, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("createdAt")) : sort;
        return userRepository.findByCreatedAtBefore(createdAt, finalSort);
    }

    public List<User> getTaskByCreatedAtAfter(LocalDateTime createdAt, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("createdAt")) : sort;
        return userRepository.findByCreatedAtAfter(createdAt, finalSort);
    }

    public List<User> getTaskByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("createdAt")) : sort;
        return userRepository.findByCreatedAtBetween(from, to, finalSort);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    public boolean isValidUser(int userId){
        return userRepository.existsByUserId(userId);
    }


}