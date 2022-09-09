package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdatedDto {

    private String name;
    @NotBlank(message = "Email may not be blank")
    @Email(message = "The email is incorrect")
    private String email;
}
