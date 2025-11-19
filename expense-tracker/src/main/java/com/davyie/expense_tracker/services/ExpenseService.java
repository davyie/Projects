package com.davyie.expense_tracker.services;

import com.davyie.expense_tracker.models.ExpenseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.davyie.expense_tracker.storages.ExpenseStorage;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExpenseService {

    // Fields
    private List<ExpenseModel> expenseList;

    private ExpenseStorage expenseStorage;

    private Logger LOG = LoggerFactory.getLogger(ExpenseService.class);

    public ExpenseService(ExpenseStorage expenseStorage) {
        this.expenseStorage = expenseStorage;
        this.expenseList = new ArrayList<>();
    }

    public boolean addExpense(ExpenseModel expense) {
        return expenseList.add(expense);
    }

    public boolean updateExpense(ExpenseModel expense) {
        deleteExpenseByName(expense.getName());
        return expenseList.add(expense);
    }

    public List<ExpenseModel> getAllExpenses() {
        return expenseList;
    }

    public ExpenseModel getExpenseById(Integer index) {
        return expenseList.get(index);
    }

    public boolean deleteExpenseByName(String expenseName) {
        LOG.info("Delete expense with name: {}", expenseName);
        expenseList = new ArrayList<>(
                expenseList.stream()
                        .filter(e -> !e.getName().equals(expenseName))
                        .toList());
        return true;
    }
}
