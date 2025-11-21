package com.davyie.expense_tracker.controllers;

import com.davyie.expense_tracker.DTO.ExpenseModelDTO;
import com.davyie.expense_tracker.models.ExpenseModel;
import com.davyie.expense_tracker.services.ExpenseService;
import jakarta.websocket.server.PathParam;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/command")
public class CommandController {

    private ExpenseService expenseService;

    private Logger LOG = LoggerFactory.getLogger(CommandController.class);

    public CommandController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addExpenseModel(@RequestBody ExpenseModelDTO expenseDTO) {
        expenseService.addExpense(expenseDTO);
        return new ResponseEntity("Successful add", HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteExpense(@RequestBody String expenseName ) {
        expenseService.deleteExpenseByName(expenseName);
        return new ResponseEntity<>("Successful delete", HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateExpense(@RequestBody ExpenseModelDTO dto, @RequestParam Integer id) {
        LOG.info(dto.toString());
        expenseService.updateExpense(dto, id);
        return new ResponseEntity<>("Successful Update", HttpStatus.OK);
    }
}
