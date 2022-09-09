package ru.practicum.shareit.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

import javax.persistence.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", length = 128, nullable = false)
    private String name;

    @NotNull(message = "Email may not be null")
    @NotBlank(message = "Email may not be blank")
    @Email(message = "The email is incorrect")
    @Column(length = 128, unique = true)
    private String email;

    @JsonProperty("id")
    public Long getId() {
        return id;
    }
}
