package com.davyie.expense_tracker.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseModel {
    private Integer id;
    private String name;
    private Float amount;
    private String description;
}
