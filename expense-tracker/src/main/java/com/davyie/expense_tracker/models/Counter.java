package com.davyie.expense_tracker.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("counter_id")
@Data
public class Counter {
    private String global_id;
    private Integer expenseId;
}
