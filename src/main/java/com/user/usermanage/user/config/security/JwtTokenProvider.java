package com.user.usermanage.user.config.security;


import com.user.usermanage.user.Exception.Constants;
import com.user.usermanage.user.Exception.CustomException;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;


@Component
public class JwtTokenProvider {

    private final Logger LOGGER =  LoggerFactory.getLogger(JwtTokenProvider.class);


    private final UserDetailsService userDetailsService;



    @Autowired
    JwtTokenProvider(UserDetailsService userDetailsService){
        this.userDetailsService = userDetailsService;
    }



    private String secretKey = "secretKeysdfdsfasgawegvaegvaergvaegv";
   // private final long accessTokenTime = 1000L * 60 * 60; // 1시간 토큰 유효

    private final long accessTokenTime = 30L * 24L * 60 * 60 * 1000; // 1시간 토큰 유효
    private final long refreshTokenTIme = 30L * 24L * 60 * 60 * 1000; // 1달 토큰 유효

    @PostConstruct
    protected void init() {
        LOGGER.info("[init] JwtTokenProvider 내 secretKey 초기화 시작");
        System.out.println(secretKey);
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
        System.out.println(secretKey);
        LOGGER.info("[init] JwtTokenProvider 내 secretKey 초기화 완료");
    }


    public String createAccessToken(String identifier, String roles) {            // 토큰 생성
        LOGGER.info("[createToken] 토큰 생성 시작");
        Claims claims = Jwts.claims().setSubject(identifier);
        claims.put("roles", roles);

        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenTime))
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화 알고리즘, secret 값 세팅
                .compact();

        LOGGER.info("[createToken] 토큰 생성 완료");
        return token;
    }

    public String createRereshToken() {            // 토큰 생성
        LOGGER.info("[createToken] 토큰 생성 시작");

        Date now = new Date();
        String token = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenTIme))
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화 알고리즘, secret 값 세팅
                .compact();

        LOGGER.info("[createToken] 토큰 생성 완료");
        return token;
    }


    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        LOGGER.info("[getAuthentication] 토큰 인증 정보 조회 시작");

        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserIdentifier(token));


        LOGGER.info("[getAuthentication] 토큰 인증 정보 조회 완료, UserDetails UserName : {}"
        );
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }


    public String getUserIdentifier(String token) {                  // 회원 정보 추출
        LOGGER.info("[getUsername] 토큰 기반 회원 구별 정보 추출");
        String info = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody()
                .getSubject();
        LOGGER.info("[getUsername] 토큰 기반 회원 구별 정보 추출 완료, info : {}");


        return info;
    }




    public String resolveToken(HttpServletRequest request) {              // 헤더에서 토큰 가져오기
        LOGGER.info("[resolveToken] HTTP 헤더에서 Token 값 추출");

        return request.getHeader("X-AUTH-TOKEN");
    }


    public boolean validateToken(String token) throws CustomException {                         // 토큰 유효성 확인
        LOGGER.info("[validateToken] 토큰 유효 체크 시작");
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            LOGGER.info("[validateToken] 토큰 유효 체크 완료");
            return true;
        }  catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            LOGGER.info("잘못된 JWT 서명입니다.");
            throw new CustomException(Constants.ExceptionClass.AUTH, HttpStatus.BAD_REQUEST,"잘못된 JWT 서명입니다." );
        } catch (ExpiredJwtException e) {
            LOGGER.info("만료된 JWT 토큰입니다.");
            throw new CustomException(Constants.ExceptionClass.AUTH, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED,"만료된 JWT 토큰입니다." );
        } catch (UnsupportedJwtException e) {
            LOGGER.info("지원되지 않는 JWT 토큰입니다.");
            throw new CustomException(Constants.ExceptionClass.AUTH, HttpStatus.EXPECTATION_FAILED,"지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            LOGGER.info("JWT 토큰이 잘못되었습니다.");
            throw new CustomException(Constants.ExceptionClass.AUTH, HttpStatus.NO_CONTENT,"JWT 토큰이 잘못되었습니다.");
        }
    }
}