package com.davyie.expense_tracker.storages;

import com.davyie.expense_tracker.models.ExpenseModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExpenseRepository extends MongoRepository<ExpenseModel, Integer> {
}
