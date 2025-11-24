package com.davyie.expense_tracker.controllers;

import com.davyie.expense_tracker.DTO.ExpenseModelDTO;
import com.davyie.expense_tracker.models.ExpenseModel;
import com.davyie.expense_tracker.services.ExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/query")
public class QueryController {


    private Logger LOG = LoggerFactory.getLogger(QueryController.class);
    private ExpenseService expenseService;

    public QueryController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> helloWorld() {
        LOG.info("Hitting hello world");
        return new ResponseEntity<>("Hello World!", HttpStatus.OK);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<ExpenseModel>> getAll() {
        return new ResponseEntity<>(expenseService.getAllExpenses(), HttpStatus.OK);
    }

    @GetMapping("/get")
    public ResponseEntity<ExpenseModel> getById(@RequestParam Integer id) {
        return new ResponseEntity<>(expenseService.getExpenseById(id), HttpStatus.OK);
    }
}
