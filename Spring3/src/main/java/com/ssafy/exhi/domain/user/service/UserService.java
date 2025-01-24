package com.ssafy.exhi.domain.user.service;


import com.ssafy.exhi.domain.user.model.dto.UserRequest;
import com.ssafy.exhi.domain.user.model.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    UserResponse.TokenDTO login(UserRequest.LoginDTO memberDto);

    UserResponse.SimpleResultDTO register(UserRequest.SignUpDTO memberDto);

    UserResponse.DetailResultDTO getMyInfo(Integer userId);

    UserResponse.DetailResultDTO update(Integer userId, UserRequest.@Valid UpdateDTO userRequest);

    boolean checkUser(String userId);

    void delete(Integer userId);

}
