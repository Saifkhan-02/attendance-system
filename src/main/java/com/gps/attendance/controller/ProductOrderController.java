
package com.gps.attendance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.GlobalStock;
import com.gps.attendance.entity.ProductOrder;
import com.gps.attendance.repository.GlobalStockRepository;
import com.gps.attendance.repository.ProductOrderRepository;

@RestController
@RequestMapping("/order")
@CrossOrigin("*")
public class ProductOrderController {

    private final ProductOrderRepository orderRepository;
    private final GlobalStockRepository globalStockRepository;

    public ProductOrderController(ProductOrderRepository orderRepository,
                           GlobalStockRepository globalStockRepository) {
        this.orderRepository = orderRepository;
        this.globalStockRepository = globalStockRepository;
    }

   @PostMapping("/place")
public ProductOrder placeOrder(@RequestBody ProductOrder order) {

    System.out.println("ORDER PRODUCT ID: " + order.getProductId());
    System.out.println("ORDER QTY: " + order.getOrderQuantity());

    GlobalStock stock = globalStockRepository.findById(order.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

    Integer availableUnits = 0;

    if (stock.getAvailableUnits() != null) {
        availableUnits = stock.getAvailableUnits();
    }

    System.out.println("STOCK PRODUCT: " + stock.getProductName());
    System.out.println("AVAILABLE UNITS: " + availableUnits);

    if (order.getOrderQuantity() > availableUnits) {
        throw new RuntimeException("Insufficient stock");
    }

    stock.setAvailableUnits(availableUnits - order.getOrderQuantity());
    globalStockRepository.save(stock);

    order.setStatus("Placed");

    return orderRepository.save(order);
}

    @GetMapping("/history/{employeeId}")
    public List<ProductOrder> getOrderHistory(@PathVariable Long employeeId) {
        return orderRepository.findByEmployeeId(employeeId);
    }
}
// package com.gps.attendance.controller;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RestController;

// import com.gps.attendance.entity.ProductOrder;
// import com.gps.attendance.entity.GlobalStock;
// import com.gps.attendance.repository.ProductOrderRepository;
// import com.gps.attendance.repository.GlobalStockRepository;

// @RestController
// @CrossOrigin("*")
// public class ProductOrderController {

//     @Autowired
//     private ProductOrderRepository orderRepository;

//     @Autowired
//     private GlobalStockRepository stockRepository;

//     @PostMapping("/order/place")
//     public ProductOrder placeOrder(@RequestBody ProductOrder order) {

//         GlobalStock stock =
//                 stockRepository.findById(order.getStockId())
//                         .orElse(null);

//         if (stock == null) {
//             throw new RuntimeException("Stock not found");
//         }

//         if (stock.getQuantity() < order.getOrderQuantity()) {
//             throw new RuntimeException("Insufficient stock");
//         }

//         stock.setQuantity(
//                 stock.getQuantity() - order.getOrderQuantity()
//         );

//         stockRepository.save(stock);

//         order.setStatus("Placed");

//         return orderRepository.save(order);
//     }

//     @GetMapping("/order/history/{employeeId}")
//     public List<ProductOrder> getOrderHistory(
//             @PathVariable Long employeeId) {

//         return orderRepository.findByEmployeeId(employeeId);
//     }
// }