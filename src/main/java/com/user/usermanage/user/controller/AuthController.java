package com.user.usermanage.user.controller;


import com.user.usermanage.user.Exception.CustomException;
import com.user.usermanage.user.config.security.CustomUser;
import com.user.usermanage.user.config.security.JwtTokenProvider;
import com.user.usermanage.user.dto.*;
import com.user.usermanage.user.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/user/auth")
public class AuthController {
    private final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider){
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping(value = "/sign-up")
    public ResponseDto signUp(@Validated  @RequestBody SignUpRequestDto signUpRequest) throws CustomException {

        ResponseDto signUpResponseDto = authService.signUp(signUpRequest);
        
        LOGGER.info("회원가입 완료");
        
        return signUpResponseDto;
    }

    @PostMapping(value = "/sign-in")
    public SignInResponseDto signIn(@Validated @RequestBody SignInRequestDto signInRequestDto) throws  CustomException {

        SignInResponseDto signInResponseDto = authService.signIn(signInRequestDto);

        LOGGER.info("로그인 완료");

        return signInResponseDto;
    }

    @DeleteMapping("/logout")
    public ResponseDto logout(@AuthenticationPrincipal CustomUser customUser) throws  CustomException {

       ResponseDto logoutResponseDto = authService.logout(customUser.getUserId());

        LOGGER.info("로그아웃 완료");

        return logoutResponseDto;
    }

    @GetMapping(value = "/reissue-token")
    public ReissueTokenResponseDto reissueToken(HttpServletRequest request) throws  CustomException {


        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);


        ReissueTokenResponseDto reissueTokenResponseDto = authService.reissueToken(refreshToken);

        LOGGER.info("토큰 재발급 완료");

        return reissueTokenResponseDto;
    }



}
