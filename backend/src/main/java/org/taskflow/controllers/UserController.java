package org.taskflow.controllers;

import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.taskflow.dtos.AuthDTO;
import org.taskflow.dtos.UserDTO;
import org.taskflow.models.User;
import org.taskflow.service.UserService;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @CrossOrigin
    @PostMapping(value = "/create")
    public ResponseEntity<String> addUser(@Valid @RequestBody UserDTO user){
        return userService.createUser(user);
    }

    @CrossOrigin
    @PostMapping(value = "/authenticate")
    public ResponseEntity<String> authenticateUser(@Valid @RequestBody AuthDTO authDTO) {
        return userService.authenticateUser(authDTO);
    }
    
    @CrossOrigin
    @PutMapping(value = "/updateUsername")
    public ResponseEntity<String> updateUsername(@RequestParam int userId, @RequestParam String username){
        return userService.updateUsername(userId, username);
    }

    @CrossOrigin
    @PutMapping(value = "/updatePassword")
    public ResponseEntity<String> updatePassword(@RequestParam int userId, @RequestParam String password){
        return userService.updatePassword(userId, password);
    }

    @CrossOrigin
    @PutMapping(value = "/updateEmail")
    public ResponseEntity<String> updateEmail(@RequestParam int userId, @RequestParam String email){
        return userService.updateEmail(userId, email);
    }

    @CrossOrigin
    @GetMapping(value = "/{userId}")
    public String getUserbyUserId(@PathVariable int userId){
        return userService.getUserById(userId).getUsername();
    }

    @CrossOrigin
    @GetMapping(value = "/username/{username}")
    public User getUserbyUsername(@PathVariable String username){
        return userService.getUserByUsername(username);
    }

    @CrossOrigin
    @GetMapping(value = "/email/{email}")
    public User getUserbyEmail(@PathVariable String email){
        return userService.getUserByEmail(email);
    }
}
