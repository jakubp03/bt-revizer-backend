package com.opr3.opr3.dto.category;

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
}
