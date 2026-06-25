package com.gps.attendance.controller;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.entity.DistributorStock;
import com.gps.attendance.entity.PaymentHistory;
import com.gps.attendance.entity.ProductOrder;
import com.gps.attendance.repository.DistributorStockRepository;
import com.gps.attendance.repository.PaymentHistoryRepository;
import com.gps.attendance.repository.ProductOrderRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@RestController
@RequestMapping("/order")
@CrossOrigin("*")
public class ProductOrderController {

    private final ProductOrderRepository orderRepository;
    private final DistributorStockRepository distributorStockRepository;
   private final PaymentHistoryRepository paymentHistoryRepository;

    public ProductOrderController(ProductOrderRepository orderRepository,
            DistributorStockRepository distributorStockRepository,
            PaymentHistoryRepository paymentHistoryRepository) {

        this.orderRepository = orderRepository;
        this.distributorStockRepository = distributorStockRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
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

                String[] parts
                        = order.getOrderDate().split("-");

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

    @PostMapping("/place-multiple")
    public List<ProductOrder> placeMultipleOrders(@RequestBody List<ProductOrder> orders) {

        for (ProductOrder order : orders) {

            DistributorStock stock = distributorStockRepository
                    .findByDistributorIdAndProductId(
                            order.getDistributorId(),
                            order.getProductId()
                    )
                    .orElseThrow(() -> new RuntimeException("Distributor stock not found for " + order.getProductName()));

            Integer availableUnits = stock.getAvailableUnits() == null ? 0 : stock.getAvailableUnits();

            if (order.getOrderQuantity() > availableUnits) {
                throw new RuntimeException("Insufficient stock for " + order.getProductName());
            }
        }

        for (ProductOrder order : orders) {

            DistributorStock stock = distributorStockRepository
                    .findByDistributorIdAndProductId(
                            order.getDistributorId(),
                            order.getProductId()
                    )
                    .orElseThrow(() -> new RuntimeException("Distributor stock not found"));

            stock.setAvailableUnits(stock.getAvailableUnits() - order.getOrderQuantity());
            distributorStockRepository.save(stock);

            order.setStatus("Placed");
        }

        return orderRepository.saveAll(orders);
    }

    @GetMapping("/admin/all")
    public List<ProductOrder> getAllOrdersForAdmin() {
        return orderRepository.findAll();
    }

    @GetMapping("/admin/sales-payment/summary")
    public Map<String, Object> getSalesSummary() {

        Map<String, Object> result = new HashMap<>();

        result.put("totalSale",
                orderRepository.getTotalSale());

        result.put("totalCollection",
                orderRepository.getTotalCollection());

        result.put("totalDue",
                orderRepository.getTotalDue());

        result.put("totalOrders",
                orderRepository.count());

        return result;
    }

    @GetMapping("/admin/sales-payment/employee/{employeeId}")
    public Map<String, Object> getEmployeeSales(
            @PathVariable Long employeeId) {

        List<ProductOrder> orders
                = orderRepository.findByEmployeeId(employeeId);

        double sale = 0;
        double collection = 0;
        double due = 0;

        for (ProductOrder order : orders) {

            sale += order.getOrderAmount() == null
                    ? 0
                    : order.getOrderAmount();

            collection += order.getPaidAmount() == null
                    ? 0
                    : order.getPaidAmount();

            due += order.getDueAmount() == null
                    ? 0
                    : order.getDueAmount();
        }

        Map<String, Object> result = new HashMap<>();

        result.put("sale", sale);
        result.put("collection", collection);
        result.put("due", due);
        result.put("orders", orders.size());

        return result;
    }

    @GetMapping("/employee/{id}/order-count")
    public Long getOrderCount(@PathVariable Long id) {
        return orderRepository.countOrdersByEmployeeId(id);
    }

   @PutMapping("/collect-payment/{orderId}")
public ProductOrder collectPayment(
        @PathVariable Long orderId,
        @RequestBody Map<String, Object> request) {

    ProductOrder order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

    Double receivedAmount =
            Double.valueOf(request.get("receivedAmount").toString());

    String paymentMode =
            request.get("paymentMode") == null
                    ? order.getPaymentMode()
                    : request.get("paymentMode").toString();

    if (receivedAmount <= 0) {
        throw new RuntimeException("Received amount must be greater than 0");
    }

    Double currentDue = order.getDueAmount() == null ? 0.0 : order.getDueAmount();

    if (receivedAmount > currentDue) {
        throw new RuntimeException("Received amount cannot be greater than due amount");
    }

    double oldPaid =
            order.getPaidAmount() == null ? 0 : order.getPaidAmount();

    order.setPaidAmount(oldPaid + receivedAmount);
    order.setDueAmount(currentDue - receivedAmount);
    order.setPaymentMode(paymentMode);

    if (order.getDueAmount() == 0) {
        order.setStatus("PAID");
    } else {
        order.setStatus("PARTIAL");
    }

    PaymentHistory history = new PaymentHistory();

history.setOrderId(order.getId());

history.setEmployeeId(order.getEmployeeId());
history.setEmployeeName(order.getEmployeeName());

history.setDoctorId(order.getDoctorId());
history.setDoctorName(order.getDoctorName());

history.setReceivedAmount(receivedAmount);

history.setPaymentMode(paymentMode);

history.setRemarks(
    request.get("remarks") == null
        ? ""
        : request.get("remarks").toString()
);

history.setPaymentDate(LocalDateTime.now());

paymentHistoryRepository.save(history);

    return orderRepository.save(order);
}

@GetMapping("/admin/payment-history")
public List<PaymentHistory> getPaymentHistory() {

    return paymentHistoryRepository
            .findAllByOrderByPaymentDateDesc();
}
}