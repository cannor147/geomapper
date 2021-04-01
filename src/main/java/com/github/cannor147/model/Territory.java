package com.github.cannor147.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.github.cannor147.model.named.Synonymized;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Territory implements Synonymized {
    private String name;
    private String[] synonyms;
    private Coordinates[] coordinates;
}
