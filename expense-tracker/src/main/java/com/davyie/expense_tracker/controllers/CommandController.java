package com.davyie.expense_tracker.controllers;

import com.davyie.expense_tracker.models.ExpenseModel;
import com.davyie.expense_tracker.services.ExpenseService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/command")
public class CommandController {

    private ExpenseService expenseService;

    public CommandController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addExpenseModel(@RequestBody ExpenseModel expense) {
        expenseService.addExpense(expense);
        return new ResponseEntity("Successful add", HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteExpense(@RequestBody String expenseName ) {
        expenseService.deleteExpenseByName(expenseName);
        return new ResponseEntity<>("Successful delete", HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateExpense(@RequestBody ExpenseModel expenseModel) {
        expenseService.updateExpense(expenseModel);
        return new ResponseEntity<>("Successful Update", HttpStatus.OK);
    }
}
