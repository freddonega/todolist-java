package dev.freddonega.todolist.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.favre.lib.crypto.bcrypt.BCrypt;


@RestController
@RequestMapping("/users")
public class UserController {


    @Autowired
    private IUserRespository userRespository;
    
    @PostMapping("/")
    public ResponseEntity create(@RequestBody UserModel user) {
        
        var userExists = this.userRespository.findByUsername(user.getUsername());

        if(userExists != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists");
        }

        var hashedPassword = BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray());

        user.setPassword(hashedPassword);
        
        var userCreated = this.userRespository.save(user);  
        return ResponseEntity.status(HttpStatus.OK).body(userCreated);
    }
    
}
