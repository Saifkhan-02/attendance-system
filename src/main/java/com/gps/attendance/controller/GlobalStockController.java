package com.gps.attendance.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.gps.attendance.entity.GlobalStock;
import com.gps.attendance.repository.GlobalStockRepository;

@RestController
@RequestMapping("/stock/global")
@CrossOrigin("*")
public class GlobalStockController {

    private final GlobalStockRepository globalStockRepository;

    public GlobalStockController(GlobalStockRepository globalStockRepository) {
        this.globalStockRepository = globalStockRepository;
    }

    @GetMapping("/products")
    public List<GlobalStock> getActiveProducts() {
        return globalStockRepository.findByStatus("Active");
    }

    @GetMapping("/admin/all")
    public List<GlobalStock> getAllProductsForAdmin() {
        return globalStockRepository.findAll();
    }

    @PostMapping("/admin/add")
    public GlobalStock addProduct(@RequestBody GlobalStock stock) {
        if (stock.getStatus() == null || stock.getStatus().isBlank()) {
            stock.setStatus("Active");
        }

        if (stock.getAvailableUnits() == null) {
            stock.setAvailableUnits(0);
        }

        return globalStockRepository.save(stock);
    }

    @PutMapping("/admin/update/{id}")
    public GlobalStock updateProduct(
            @PathVariable Long id,
            @RequestBody GlobalStock updatedStock
    ) {
        GlobalStock stock = globalStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        stock.setProductName(updatedStock.getProductName());
        stock.setSellingPrice(updatedStock.getSellingPrice());
        stock.setStatus(updatedStock.getStatus());

        return globalStockRepository.save(stock);
    }

    @DeleteMapping("/admin/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        globalStockRepository.deleteById(id);
        return "Product deleted successfully";
    }

    @PutMapping("/admin/add-stock/{id}")
    public GlobalStock addStock(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        GlobalStock stock = globalStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Integer addQuantity =
                Integer.parseInt(request.get("quantity").toString());

        BigDecimal sellingPrice =
                new BigDecimal(request.get("sellingPrice").toString());

        Integer currentUnits =
                stock.getAvailableUnits() == null
                        ? 0
                        : stock.getAvailableUnits();

        stock.setAvailableUnits(currentUnits + addQuantity);
        stock.setSellingPrice(sellingPrice);

        return globalStockRepository.save(stock);
    }
}