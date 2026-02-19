package my_sawit.authentication_manajemen_akun.service;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.RegisterRequest;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;

    public User register(RegisterRequest request){

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole())
                .build();
        return userRepo.save(user);

    }

    public List<User> fetchAllUsers(){
        return userRepo.findAll();
    }

}
