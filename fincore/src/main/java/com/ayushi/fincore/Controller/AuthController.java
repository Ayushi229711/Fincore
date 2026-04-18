package com.ayushi.fincore.Controller;

import com.ayushi.fincore.dto.AuthRequest;
import com.ayushi.fincore.dto.RegisterRequest;
import com.ayushi.fincore.Model.User;
import com.ayushi.fincore.Security.JwtUtil;
import com.ayushi.fincore.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;



    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        return userService.register(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );
    }



    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest request) {

        User user = userService.login(request.getEmail(), request.getPassword());

        return jwtUtil.generateToken(user.getEmail());
    }
}
