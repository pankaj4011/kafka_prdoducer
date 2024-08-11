package com.learnkafka.domain;

import jakarta.validation.Valid;
import lombok.NonNull;

public record LibraryEvent(

        Integer libraryEventId,
        LibraryEventType libraryEventType,
        @NonNull
        @Valid
        Book book
) {
}
