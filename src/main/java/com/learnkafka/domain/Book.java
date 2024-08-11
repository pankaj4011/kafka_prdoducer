package com.learnkafka.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record Book(
        //  @NonNull
        Integer bookId,
        @NotBlank
        String bookName,
        @NotBlank
        String bookAuthor
) {
}
