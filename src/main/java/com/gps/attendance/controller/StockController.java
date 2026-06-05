package com.gps.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.Stock;
import com.gps.attendance.repository.StockRepository;

@RestController
@CrossOrigin("*")
public class StockController {

    @Autowired
    private StockRepository repository;

    @PostMapping("/stock/add")
    public Stock addStock(@RequestBody Stock stock) {

        return repository.save(stock);
    }

    @GetMapping("/stock/history/{employeeId}")
    public List<Stock> getStockHistory(@PathVariable Long employeeId) {

        return repository.findByEmployeeId(employeeId);
    }
}