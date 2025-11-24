package com.davyie.expense_tracker.storages;

import com.davyie.expense_tracker.models.Counter;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CounterRepository extends MongoRepository<Counter, String> {
}
