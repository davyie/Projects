package com.davyie.expense_tracker.DTO;

import lombok.Data;

@Data
public class ExpenseModelDTO {
    private String name;
    private Float amount;
    private String description;
}
