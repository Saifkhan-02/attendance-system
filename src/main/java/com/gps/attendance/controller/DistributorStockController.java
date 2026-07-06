package com.gps.attendance.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.AdminAddProduct;
import com.gps.attendance.entity.Distributor;
import com.gps.attendance.entity.DistributorStock;
import com.gps.attendance.repository.AdminAddProductRepository;
import com.gps.attendance.repository.DistributorRepository;
import com.gps.attendance.repository.DistributorStockRepository;


@RestController
@CrossOrigin("*")
public class DistributorStockController {

    private final DistributorStockRepository distributorStockRepository;
    private final DistributorRepository distributorRepository;
   private final AdminAddProductRepository adminAddProductRepository;

    public DistributorStockController(
            DistributorStockRepository distributorStockRepository,
            DistributorRepository distributorRepository,
            AdminAddProductRepository adminAddProductRepository
    ) {
        this.distributorStockRepository = distributorStockRepository;
        this.distributorRepository = distributorRepository;
        this.adminAddProductRepository = adminAddProductRepository;
    }

    @GetMapping("/distributor-stock/{distributorId}")
    public List<DistributorStock> getDistributorStock(
            @PathVariable Long distributorId
    ) {
        return distributorStockRepository.findByDistributorId(distributorId);
    }

    @PutMapping("/admin/distributor-stock/add")
    public DistributorStock addDistributorStock(
            @RequestBody Map<String, Object> request
    ) {
        Long distributorId = Long.valueOf(request.get("distributorId").toString());
        Long productId = Long.valueOf(request.get("productId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());

            Distributor distributor = distributorRepository.findById(distributorId)
                .orElseThrow(() -> new RuntimeException("Distributor not found"));

       AdminAddProduct product = adminAddProductRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<DistributorStock> existingStock =
                distributorStockRepository.findByDistributorIdAndProductId(
                        distributorId,
                        productId
                );

        DistributorStock stock;

        if (existingStock.isPresent()) {
            stock = existingStock.get();

                stock.setDistributorName(distributor.getDistributorName());
                stock.setHeadquarters(distributor.getHeadquarters());
        

            stock.setAvailableUnits(
                    (stock.getAvailableUnits() == null ? 0 : stock.getAvailableUnits())
                            + quantity
            );
           
        } else {
            stock = new DistributorStock();
            stock.setDistributorId(distributor.getId());
            stock.setDistributorName(distributor.getDistributorName());
            stock.setProductId(product.getId());
            stock.setProductName(product.getProductName());
            stock.setAvailableUnits(quantity);
            stock.setHeadquarters(distributor.getHeadquarters());
            stock.setStatus("Active");
        }

        return distributorStockRepository.save(stock);
    }

    @PutMapping("/admin/distributor-stock/update/{id}")
public ResponseEntity<DistributorStock> updateStock(
        @PathVariable Long id,
        @RequestBody Map<String, Object> request) {

    DistributorStock stock = distributorStockRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Stock not found"));

    Integer quantity = Integer.valueOf(request.get("availableUnits").toString());

    if (quantity < 0) {
        throw new RuntimeException("Quantity cannot be negative");
    }

    stock.setAvailableUnits(quantity);

    return ResponseEntity.ok(distributorStockRepository.save(stock));
}

    @DeleteMapping("/admin/distributor-stock/{id}")
public ResponseEntity<String> deleteStock(@PathVariable Long id) {

    distributorStockRepository.deleteById(id);

    return ResponseEntity.ok("Stock deleted successfully");
}

    
}