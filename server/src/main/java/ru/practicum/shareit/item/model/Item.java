package ru.practicum.shareit.item.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @NotNull(message = "Name may not be null")
    @NotBlank(message = "Name may not be blank")
    @Column(name = "item_name", nullable = false)
    private String name;

    @NotNull(message = "Description may not be null")
    @NotBlank(message = "Description may not be blank")
    @Column(name = "item_description", nullable = false)
    private String description;

    @NotNull(message = "Available may not be null")
    @Column(name = "is_available", nullable = false)
    private Boolean available;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @JsonProperty("id")
    public Long getId() {
        return id;
    }
}
