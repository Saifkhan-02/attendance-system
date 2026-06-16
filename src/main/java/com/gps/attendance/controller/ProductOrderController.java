package com.gps.attendance.controller;

<<<<<<< HEAD
import java.time.LocalDate;
=======
import java.util.HashMap;
>>>>>>> cca82ee376ae58397759ea1240b9707e15a73317
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.GlobalStock;
import com.gps.attendance.entity.ProductOrder;
import com.gps.attendance.repository.GlobalStockRepository;
import com.gps.attendance.repository.ProductOrderRepository;

import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.util.ArrayList;

import com.lowagie.text.Image;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;

import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;

import com.lowagie.text.Rectangle;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;

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

<<<<<<< HEAD
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

    @GetMapping("/history/today/{employeeId}")
public List<ProductOrder> getTodayOrders(@PathVariable Long employeeId) {
    String today = LocalDate.now().toString();

    return orderRepository
            .findByEmployeeIdAndOrderDateOrderByIdDesc(employeeId, today);
}

@GetMapping("/history/monthly/{employeeId}")
public List<ProductOrder> getMonthlyOrders(
        @PathVariable Long employeeId,
        @RequestParam String month
) {
    return orderRepository
            .findByEmployeeIdAndOrderDateStartingWithOrderByIdDesc(employeeId, month);
}

=======
    @GetMapping("/history/{employeeId}")
    public List<ProductOrder> getOrderHistory(@PathVariable Long employeeId) {
        return orderRepository.findByEmployeeId(employeeId);
    }

    @GetMapping("/all")
    public List<ProductOrder> getAllOrders() {

        return orderRepository.findAll();

    }

    @GetMapping("/summary")
public Map<String, Object> getSummary(

        @RequestParam String month,
        @RequestParam String year

) {

       List<ProductOrder> allOrders
        = orderRepository.findAll();

List<ProductOrder> orders
        = new ArrayList<>();

for (ProductOrder order : allOrders) {

    if (order.getOrderDate() != null) {

        String[] parts =
                order.getOrderDate().split("-");

        if (parts.length == 3) {

           
            String orderYear = parts[0];
            String orderMonth = parts[1];

            if (orderMonth.equals(month)
                    && orderYear.equals(year)) {

                orders.add(order);

            }
        }
    }
}

        double totalAmount = 0.0;
        double paidAmount = 0.0;
        double dueAmount = 0.0;

        for (ProductOrder order : orders) {

            totalAmount
                    += order.getOrderAmount() == null
                    ? 0.0
                    : order.getOrderAmount();

            paidAmount
                    += order.getPaidAmount() == null
                    ? 0.0
                    : order.getPaidAmount();

            dueAmount
                    += order.getDueAmount() == null
                    ? 0.0
                    : order.getDueAmount();
        }

        Map<String, Object> result
                = new HashMap<>();

        result.put("totalOrders",
                orders.size());

        result.put("totalAmount",
                totalAmount);

        result.put("paidAmount",
                paidAmount);

        result.put("dueAmount",
                dueAmount);

        return result;
    }

    @GetMapping("/search")
    public List<ProductOrder> searchOrders(
            @RequestParam(defaultValue = "") String employeeName,
            @RequestParam(defaultValue = "") String doctorName
    ) {

        return orderRepository
                .findByEmployeeNameContainingIgnoreCaseAndDoctorNameContainingIgnoreCase(
                        employeeName,
                        doctorName
                );
    }

    @GetMapping("/bill/{id}")
    public ResponseEntity<byte[]> downloadBill(
            @PathVariable Long id) throws Exception {

        ProductOrder order
                = orderRepository.findById(id)
                        .orElseThrow();

        ByteArrayOutputStream out
                = new ByteArrayOutputStream();

        Document document
                = new Document();

        PdfWriter writer
                = PdfWriter.getInstance(document, out);

        document.open();

        PdfContentByte canvas
                = writer.getDirectContent();

        Rectangle border
                = new Rectangle(
                        20,
                        20,
                        PageSize.A4.getWidth() - 20,
                        PageSize.A4.getHeight() - 20
                );

        border.setBorder(Rectangle.BOX);
        border.setBorderWidth(2);
        border.setBorderColor(new Color(0, 102, 51));

        canvas.rectangle(border);

        Image logo = Image.getInstance(
                "src/main/resources/static/images/Inflix-logo-web.png"
        );

        logo.scaleToFit(120, 60);
        logo.setAlignment(Image.ALIGN_CENTER);

        document.add(logo);

        Font titleFont = new Font(
                Font.HELVETICA,
                18,
                Font.BOLD,
                Color.GREEN
        );

        Paragraph title = new Paragraph(
                "PHARMAWEB PRODUCT ORDER INVOICE",
                titleFont
        );

        title.setAlignment(Paragraph.ALIGN_CENTER);

        document.add(title);

        document.add(new Paragraph(" "));
        PdfPTable invoiceBox = new PdfPTable(2);
        invoiceBox.setWidthPercentage(40);
        invoiceBox.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);

        invoiceBox.addCell("Invoice No");
        invoiceBox.addCell("INV-" + order.getId());

        invoiceBox.addCell("Generated Date");
        invoiceBox.addCell(String.valueOf(java.time.LocalDate.now()));

        document.add(invoiceBox);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("CUSTOMER DETAILS"));
        document.add(new Paragraph("================================"));
        document.add(new Paragraph(" "));

        PdfPTable customerBox = new PdfPTable(2);
        customerBox.setWidthPercentage(80);
        customerBox.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);

        customerBox.addCell("Order ID");
        customerBox.addCell(String.valueOf(order.getId()));

        customerBox.addCell("Employee");
        customerBox.addCell(order.getEmployeeName());

        customerBox.addCell("Doctor");
        customerBox.addCell(order.getDoctorName());

        customerBox.addCell("Order Date");
        customerBox.addCell(String.valueOf(order.getOrderDate()));

        document.add(customerBox);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("PRODUCT DETAILS"));
        document.add(new Paragraph("================================"));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);

        table.setWidthPercentage(100);

        PdfPCell h1 = new PdfPCell(new Paragraph("Product"));
        h1.setBackgroundColor(Color.GREEN);

        PdfPCell h2 = new PdfPCell(new Paragraph("Quantity"));
        h2.setBackgroundColor(Color.GREEN);

        PdfPCell h3 = new PdfPCell(new Paragraph("Price"));
        h3.setBackgroundColor(Color.GREEN);

        PdfPCell h4 = new PdfPCell(new Paragraph("Amount"));
        h4.setBackgroundColor(Color.GREEN);

        table.addCell(h1);
        table.addCell(h2);
        table.addCell(h3);
        table.addCell(h4);

        table.addCell(order.getProductName());

        table.addCell(
                String.valueOf(order.getOrderQuantity())
        );

        table.addCell(
                "₹" + (order.getSellingPrice() == null
                ? 0
                : order.getSellingPrice())
        );

        table.addCell(
                "₹" + (order.getOrderAmount() == null
                ? 0
                : order.getOrderAmount())
        );

        document.add(table);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("PAYMENT SUMMARY"));
        document.add(new Paragraph("================================"));

        document.add(
                new Paragraph(
                        "Total Amount : ₹"
                        + (order.getOrderAmount() == null
                        ? 0
                        : order.getOrderAmount())
                )
        );

        document.add(
                new Paragraph(
                        "Paid Amount : ₹"
                        + (order.getPaidAmount() == null
                        ? 0
                        : order.getPaidAmount())
                )
        );

        document.add(
                new Paragraph(
                        "Due Amount : ₹"
                        + (order.getDueAmount() == null
                        ? 0
                        : order.getDueAmount())
                )
        );

        String status
                = (order.getDueAmount() != null
                && order.getDueAmount() > 0)
                ? "PENDING"
                : "PAID";
        document.add(new Paragraph("Status : " + status));

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("================================"));

        Paragraph sign = new Paragraph(
                "________________________\nAuthorized Signature"
        );
        sign.setAlignment(Paragraph.ALIGN_RIGHT);

        document.add(sign);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Generated By PharmaWEB System"));
        document.add(new Paragraph("Inflix Pharma Pvt Ltd"));
        document.add(new Paragraph("Email : support@inflixpharma.com"));
        document.add(new Paragraph("Phone : +91 9876543210"));
        document.close();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Bill_" + order.getId() + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }

>>>>>>> cca82ee376ae58397759ea1240b9707e15a73317
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
