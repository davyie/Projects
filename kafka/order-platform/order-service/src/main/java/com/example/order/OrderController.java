package com.example.order;

import com.example.events.OrderPlacedEvent;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderPlacedEvent placeOrder(@RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(request);
    }
}
