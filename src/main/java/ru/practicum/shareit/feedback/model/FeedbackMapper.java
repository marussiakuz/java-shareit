package ru.practicum.shareit.feedback.model;

import ru.practicum.shareit.feedback.model.dto.FeedbackDto;

public class FeedbackMapper {

    public static FeedbackDto toFeedbackDto(Feedback feedback) {
        return FeedbackDto.builder()
                .id(feedback.getId())
                .itemId(feedback.getItemId())
                .userId(feedback.getUserId())
                .review(feedback.getReview())
                .useful(feedback.getUseful())
                .build();
    }

    public static Feedback toFeedback(FeedbackDto feedbackDto) {
        return Feedback.builder()
                .id(feedbackDto.getId())
                .itemId(feedbackDto.getItemId())
                .userId(feedbackDto.getUserId())
                .review(feedbackDto.getReview())
                .useful(feedbackDto.getUseful())
                .build();
    }
}
