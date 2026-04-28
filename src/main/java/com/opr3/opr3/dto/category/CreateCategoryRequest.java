package com.opr3.opr3.dto.category;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    private String name;
    private String description;
    private String color;
    private List<Long> quizIds;
}
