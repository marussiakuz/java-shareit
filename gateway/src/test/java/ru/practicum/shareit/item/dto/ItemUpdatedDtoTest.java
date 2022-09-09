package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class ItemUpdatedDtoTest {
    @Autowired
    JacksonTester<ItemUpdatedDto> json;

    @Test
    void testItemDto() throws IOException {
        ItemUpdatedDto itemUpdatedDto = ItemUpdatedDto.builder()
                .name("book")
                .description("on java")
                .available(true)
                .requestId(5L)
                .build();

        JsonContent<ItemUpdatedDto> result = json.write(itemUpdatedDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("book");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("on java");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);
    }
}