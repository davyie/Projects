package com.davyie.expense_tracker.services;

import com.davyie.expense_tracker.DTO.ExpenseModelDTO;
import com.davyie.expense_tracker.models.Counter;
import com.davyie.expense_tracker.models.ExpenseModel;
import com.davyie.expense_tracker.storages.CounterRepository;
import com.davyie.expense_tracker.storages.ExpenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ExpenseService {

    // Fields
    private List<ExpenseModel> expenseList;

    private ExpenseRepository expenseRepository;

    private CounterRepository counterRepository;

    private MongoOperations mongoOperations;

    private Logger LOG = LoggerFactory.getLogger(ExpenseService.class);

    public ExpenseService(ExpenseRepository expenseRepository,
                          CounterRepository counterRepository,
                          MongoOperations mongoOperations) {
        this.expenseRepository = expenseRepository;
        this.counterRepository = counterRepository;
        this.mongoOperations = mongoOperations;
        this.expenseList = new ArrayList<>();
    }

    private Integer getId() {
        Counter counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is("global_id")),
                new Update().inc("expenseId", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                Counter.class);

        return counter != null ? counter.getExpenseId() : 1;
    }

    public boolean addExpense(ExpenseModelDTO expenseDTO) {
        Integer i = getId();
        ExpenseModel expense = new ExpenseModel(i, expenseDTO.getName(), expenseDTO.getAmount(), expenseDTO.getDescription());
        return expenseList.add(expense);
    }

    public boolean updateExpense(ExpenseModelDTO dto, Integer id) {
        deleteExpenseById(id);
        ExpenseModel expense = new ExpenseModel();
        expense.setId(id);
        expense.setName(dto.getName());
        expense.setAmount(dto.getAmount());
        expense.setDescription(dto.getDescription());
        return expenseList.add(expense);
    }

    public List<ExpenseModel> getAllExpenses() {
        return expenseList;
    }

    public ExpenseModel getExpenseById(Integer id) {
        ExpenseModel expenseModel = expenseList.stream().filter(e -> Objects.equals(e.getId(), id)).findFirst().orElse(null);
        return expenseModel;
    }

    public boolean deleteExpenseByName(String expenseName) {
        expenseList = new ArrayList<>(
                expenseList.stream()
                        .filter(e -> !e.getName().equals(expenseName))
                        .toList());
        return true;
    }
    // Delegates in C#
    public boolean deleteExpenseById(Integer id) {
        return expenseList.remove(id);
    }

    public boolean save() {
        // method to save
        expenseRepository.deleteAll();
        expenseRepository.saveAll(expenseList);
        return true;
    }

    public List<ExpenseModel> load() {
        this.expenseList = new ArrayList<>(expenseRepository.findAll());
        return this.expenseList;
    }
}
