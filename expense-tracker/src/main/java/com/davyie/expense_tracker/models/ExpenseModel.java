package com.davyie.expense_tracker.models;

import com.davyie.expense_tracker.services.ExpenseService;
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

    public ExpenseModel(String name, Float amount, String description) {
        this.name = name;
        this.amount = amount;
        this.description = description;
    }
}
