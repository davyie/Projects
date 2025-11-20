package com.davyie.expense_tracker.services;

import com.davyie.expense_tracker.DTO.ExpenseModelDTO;
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

    private Integer expenseID;

    private ExpenseStorage expenseStorage;

    private Logger LOG = LoggerFactory.getLogger(ExpenseService.class);

    public ExpenseService(ExpenseStorage expenseStorage) {
        this.expenseStorage = expenseStorage;
        this.expenseList = new ArrayList<>();
        this.expenseID = 0;
    }

    public boolean addExpense(ExpenseModelDTO expenseDTO) {
        ExpenseModel expense = new ExpenseModel(expenseID, expenseDTO.getName(), expenseDTO.getAmount(), expenseDTO.getDescription());
        expenseID++;
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

    public boolean deleteExpenseById(Integer id) {
        expenseList = new ArrayList<>(
                expenseList.stream()
                        .filter(e -> e.getId() != id)
                        .toList()
        );
        return true;
    }
}
