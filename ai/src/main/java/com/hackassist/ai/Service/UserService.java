package com.hackassist.ai.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hackassist.ai.models.User;
import com.hackassist.ai.repository.UserRepository;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user) {
        if(userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("User already exists with email: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if(user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }
        return user;
    }

    public User getUserById(String uid) {
        Optional<User> user = userRepository.findById(uid);
        if(!user.isPresent()) {
            throw new RuntimeException("User not found with id: " + uid);
        }
        return user.get();
    }

    public User updateUser(User user) {
        if(!userRepository.existsById(user.getUid())) {
            throw new RuntimeException("User not found with id: " + user.getUid());
        }
        return userRepository.save(user);
    }

    public void deleteUser(String uid) {
        if(!userRepository.existsById(uid)) {
            throw new RuntimeException("User not found with id: " + uid);
        }
        userRepository.deleteById(uid);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean userExists(String email) {
        return userRepository.findByEmail(email) != null;
    }
}
