package com.learningspring.hogwartsartifactonline.hogwartsuser.converter;

import com.learningspring.hogwartsartifactonline.hogwartsuser.HogwartsUser;
import com.learningspring.hogwartsartifactonline.hogwartsuser.dto.UserDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserDtoConverter implements Converter<HogwartsUser, UserDto> {

    @Override
    public UserDto convert(HogwartsUser source) {
        return new UserDto(
                source.getId(),
                source.getUsername(),
                source.isEnabled(),
                source.getRoles()
        );
    }
}
