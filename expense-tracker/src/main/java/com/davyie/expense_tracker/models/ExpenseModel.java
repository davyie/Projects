package com.davyie.expense_tracker.models;

import lombok.Data;

@Data
public class ExpenseModel {
    private Integer id;
    private String name;
    private Float amount;
    private String description;
}
