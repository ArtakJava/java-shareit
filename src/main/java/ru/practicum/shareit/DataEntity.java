package ru.practicum.shareit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Accessors(chain = true)
@NoArgsConstructor
@Getter
@Setter
public abstract class DataEntity {
    private long id;
    private String name;
}