package com.learningspring.hogwartsartifactonline.security;

import com.learningspring.hogwartsartifactonline.client.rediscache.RedisCacheClient;
import com.learningspring.hogwartsartifactonline.hogwartsuser.HogwartsUser;
import com.learningspring.hogwartsartifactonline.hogwartsuser.MyUserPrincipal;
import com.learningspring.hogwartsartifactonline.hogwartsuser.converter.UserToUserDtoConverter;
import com.learningspring.hogwartsartifactonline.hogwartsuser.dto.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final JwtProvider jwtProvider;

    private final UserToUserDtoConverter userToUserDtoConverter;

    private final RedisCacheClient redisCacheClient;

    public AuthService(JwtProvider jwtProvider,
            UserToUserDtoConverter userToUserDtoConverter,
            RedisCacheClient redisCacheClient) {
        this.jwtProvider = jwtProvider;
        this.userToUserDtoConverter = userToUserDtoConverter;
        this.redisCacheClient = redisCacheClient;
    }

    public Map<String, Object> createLoginInfo(Authentication authentication) {
        // Create user info
        MyUserPrincipal principal = (MyUserPrincipal) authentication.getPrincipal();
        HogwartsUser hogwartsUser = principal.getHogwartsUser();
        UserDto userDto = this.userToUserDtoConverter.convert(hogwartsUser);

        // Create a JWT
        String token = this.jwtProvider.createToken(authentication);

        // Save the token in Redis. Key is "whitelist:" + userId, value is token.
        this.redisCacheClient.set("whitelist:" + hogwartsUser.getId(), token, 2, TimeUnit.HOURS);

        Map<String, Object> loginResultMap = new HashMap<>();
        loginResultMap.put("userInfo", userDto);
        loginResultMap.put("token", token);

        return loginResultMap;
    }
}
