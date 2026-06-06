package com.gps.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.ProductOrder;
import com.gps.attendance.entity.Stock;
import com.gps.attendance.repository.ProductOrderRepository;
import com.gps.attendance.repository.StockRepository;

@RestController
@CrossOrigin("*")
public class ProductOrderController {

    @Autowired
    private ProductOrderRepository orderRepository;

    @Autowired
    private StockRepository stockRepository;

    @PostMapping("/order/place")
    public ProductOrder placeOrder(@RequestBody ProductOrder order) {

        Stock stock =
                stockRepository.findById(order.getStockId())
                        .orElse(null);

        if (stock == null) {
            throw new RuntimeException("Stock not found");
        }

        if (stock.getQuantity() < order.getOrderQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        stock.setQuantity(
                stock.getQuantity() - order.getOrderQuantity()
        );

        stockRepository.save(stock);

        order.setStatus("Placed");

        return orderRepository.save(order);
    }

    @GetMapping("/order/history/{employeeId}")
    public List<ProductOrder> getOrderHistory(
            @PathVariable Long employeeId) {

        return orderRepository.findByEmployeeId(employeeId);
    }
}