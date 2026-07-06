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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String date
    ) {

        List<ProductOrder> orders
                = orderRepository.findByEmployeeFilter(employeeId);

        double totalAmount = 0.0;
        double paidAmount = 0.0;
        double dueAmount = 0.0;

        int totalOrders = 0;

        for (ProductOrder order : orders) {

            if (order.getOrderDate() == null) {
                continue;
            }

            if (date != null
                    && !date.isBlank()
                    && !order.getOrderDate().equals(date)) {
                continue;
            }

            totalOrders++;

            totalAmount += order.getOrderAmount() == null
                    ? 0
                    : order.getOrderAmount();

            paidAmount += order.getPaidAmount() == null
                    ? 0
                    : order.getPaidAmount();

            dueAmount += order.getDueAmount() == null
                    ? 0
                    : order.getDueAmount();
        }

        Map<String, Object> result = new HashMap<>();

        result.put("totalOrders", totalOrders);
        result.put("totalAmount", totalAmount);
        result.put("paidAmount", paidAmount);
        result.put("dueAmount", dueAmount);

        return result;
    }

    @GetMapping("/search")
public Page<ProductOrder> searchOrders(

        @RequestParam(required = false) Long employeeId,

        @RequestParam(required = false) String date,

        @RequestParam(defaultValue = "0") int page,

        @RequestParam(defaultValue = "20") int size

) {

    Pageable pageable = PageRequest.of(page, size);

    return orderRepository.searchOrdersWithPagination(
            employeeId,
            date,
            pageable
    );

}

// SINGLE INVOICE / BILL PDF DOWNLOAD

    @GetMapping("/bill/{id}")
    public ResponseEntity<byte[]> downloadBill(
            @PathVariable Long id) throws Exception {

        ProductOrder firstOrder
                = orderRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Order not found"));

        List<ProductOrder> orders
                = orderRepository.findByInvoiceNoOrderByIdAsc(
                        firstOrder.getInvoiceNo()
                );
        if (orders.isEmpty()) {
            orders.add(firstOrder);
        }

        double totalAmount = 0;
        double paidAmount = 0;
        double dueAmount = 0;

        for (ProductOrder order : orders) {

            totalAmount
                    += order.getOrderAmount() == null
                    ? 0
                    : order.getOrderAmount();

            paidAmount
                    += order.getPaidAmount() == null
                    ? 0
                    : order.getPaidAmount();

            dueAmount
                    += order.getDueAmount() == null
                    ? 0
                    : order.getDueAmount();
        }

        ByteArrayOutputStream out
                = new ByteArrayOutputStream();

        Document document
                = new Document(PageSize.A4);

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
        invoiceBox.addCell(firstOrder.getInvoiceNo());

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
        customerBox.addCell(firstOrder.getInvoiceNo());

        customerBox.addCell("Employee");
        customerBox.addCell(firstOrder.getEmployeeName());

        customerBox.addCell("Doctor");
        customerBox.addCell(firstOrder.getDoctorName());

        customerBox.addCell("Order Date");
        customerBox.addCell(firstOrder.getOrderDate());

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

        for (ProductOrder order : orders) {

            table.addCell(order.getProductName());

            table.addCell(String.valueOf(
                    order.getOrderQuantity()
            ));

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

        }
        document.add(table);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("PAYMENT SUMMARY"));
        document.add(new Paragraph("================================"));

        document.add(
                new Paragraph(
                        "Total Amount : ₹" + totalAmount
                )
        );

        document.add(
                new Paragraph(
                        "Paid Amount : ₹" + paidAmount
                )
        );

        document.add(
                new Paragraph(
                        "Due Amount : ₹" + dueAmount
                )
        );

        String status
                = dueAmount > 0
                        ? "PARTIAL"
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
        document.add(new Paragraph("Email : inflixpharma@gmail.com"));
        document.add(new Paragraph("Phone : +91 8874438874"));
        document.close();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + firstOrder.getInvoiceNo() + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }

// EMPLOYEE DAILY REPORT PDF DOWNLOAD

    @GetMapping("/employee-report")
    public ResponseEntity<byte[]> employeeReport(
            @RequestParam Long employeeId,
            @RequestParam String date
    ) throws Exception {

        List<ProductOrder> orders
                = orderRepository.findEmployeeDailyOrders(
                        employeeId,
                        date
                );

        Long totalInvoices
                = orderRepository.countDailyInvoices(
                        employeeId,
                        date
                );

        if (orders.isEmpty()) {

            return ResponseEntity.noContent().build();

        }

        ProductOrder firstOrder = orders.get(0);

        double totalAmount = 0;
        double paidAmount = 0;
        double dueAmount = 0;

        int totalQuantity = 0;

        for (ProductOrder order : orders) {

            totalAmount
                    += order.getOrderAmount() == null
                    ? 0
                    : order.getOrderAmount();

            paidAmount
                    += order.getPaidAmount() == null
                    ? 0
                    : order.getPaidAmount();

            dueAmount
                    += order.getDueAmount() == null
                    ? 0
                    : order.getDueAmount();

            totalQuantity
                    += order.getOrderQuantity() == null
                    ? 0
                    : order.getOrderQuantity();

        }

        ByteArrayOutputStream out
                = new ByteArrayOutputStream();

        Document document
                = new Document(PageSize.A4);

        PdfWriter writer
                = PdfWriter.getInstance(document, out);

        document.open();

        // ================= BORDER =================
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

// ================= LOGO =================
        Image logo = Image.getInstance(
                "src/main/resources/static/images/Inflix-logo-web.png"
        );

        logo.scaleToFit(120, 60);
        logo.setAlignment(Image.ALIGN_CENTER);

        document.add(logo);

// ================= TITLE =================
        Font titleFont
                = new Font(
                        Font.HELVETICA,
                        18,
                        Font.BOLD,
                        new Color(0, 102, 51)
                );

        Paragraph title
                = new Paragraph(
                        "EMPLOYEE DAILY ORDER REPORT",
                        titleFont
                );

        title.setAlignment(Paragraph.ALIGN_CENTER);

        document.add(title);

        document.add(new Paragraph(" "));

// ================= EMPLOYEE DETAILS =================
        PdfPTable employeeBox
                = new PdfPTable(2);

        employeeBox.setWidthPercentage(100);

        employeeBox.addCell("Employee ID");
        employeeBox.addCell(
                String.valueOf(firstOrder.getEmployeeId())
        );

        employeeBox.addCell("Employee Name");
        employeeBox.addCell(firstOrder.getEmployeeName());

        employeeBox.addCell("Report Date");
        employeeBox.addCell(date);

        employeeBox.addCell("Generated Date");
        employeeBox.addCell(LocalDate.now().toString());

        document.add(employeeBox);

        document.add(new Paragraph(" "));

// ================= SUMMARY =================
        PdfPTable summary
                = new PdfPTable(2);

        summary.setWidthPercentage(100);

        summary.addCell("Total Orders");
        summary.addCell(String.valueOf(orders.size()));

        summary.addCell("Total Invoices");
        summary.addCell(String.valueOf(totalInvoices));

        summary.addCell("Total Quantity");
        summary.addCell(String.valueOf(totalQuantity));

        summary.addCell("Total Amount");
        summary.addCell(formatAmount(totalAmount));

        summary.addCell("Paid Amount");
        summary.addCell(formatAmount(paidAmount));

        summary.addCell("Due Amount");
        summary.addCell(formatAmount(dueAmount));

        document.add(summary);

        document.add(new Paragraph(" "));

// ================= ORDER DETAILS =================
        Paragraph orderTitle = new Paragraph(
                "ORDER DETAILS",
                new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLUE)
        );

        document.add(orderTitle);
        document.add(new Paragraph(" "));

// ================= GROUP BY DOCTOR =================
        Map<String, List<ProductOrder>> doctorWiseOrders
                = new HashMap<>();

        for (ProductOrder order : orders) {

            String doctor
                    = order.getDoctorName() == null
                    ? "Unknown Doctor"
                    : order.getDoctorName();

            doctorWiseOrders
                    .computeIfAbsent(
                            doctor,
                            k -> new ArrayList<>()
                    )
                    .add(order);

        }

// ================= DOCTOR WISE TABLE =================
        for (Map.Entry<String, List<ProductOrder>> entry : doctorWiseOrders.entrySet()) {

            String doctorName = entry.getKey();

            List<ProductOrder> doctorOrders = entry.getValue();

            Paragraph doctorHeading
                    = new Paragraph(
                            "Doctor : " + doctorName,
                            new Font(
                                    Font.HELVETICA,
                                    13,
                                    Font.BOLD,
                                    new Color(0, 102, 51)
                            )
                    );

            document.add(doctorHeading);

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);

            table.setWidthPercentage(100);

            table.setWidths(new float[]{
                1f,
                5f,
                1.5f,
                2f,
                2f
            });

            String[] headers = {
                "S.No",
                "Product",
                "Qty",
                "Rate",
                "Amount"
            };

            for (String h : headers) {

                PdfPCell cell
                        = new PdfPCell(
                                new Paragraph(
                                        h,
                                        new Font(
                                                Font.HELVETICA,
                                                11,
                                                Font.BOLD
                                        )
                                )
                        );

                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);

                cell.setBackgroundColor(new Color(0, 102, 51));

                table.addCell(cell);

            }

            int sr = 1;

            double doctorTotal = 0;

            int totalQty = 0;

            for (ProductOrder order : doctorOrders) {

                table.addCell(String.valueOf(sr++));

                table.addCell(
                        order.getProductName() == null
                        ? "-"
                        : order.getProductName()
                );

                table.addCell(
                        String.valueOf(
                                order.getOrderQuantity() == null
                                ? 0
                                : order.getOrderQuantity()
                        )
                );

                table.addCell(
                        formatAmount(
                                order.getSellingPrice() == null
                                ? 0.0
                                : order.getSellingPrice().doubleValue()
                        )
                );

                table.addCell(
                        formatAmount(
                                order.getOrderAmount() == null
                                ? 0
                                : order.getOrderAmount()
                        )
                );

                doctorTotal
                        += order.getOrderAmount() == null
                        ? 0
                        : order.getOrderAmount();

                totalQty
                        += order.getOrderQuantity() == null
                        ? 0
                        : order.getOrderQuantity();

            }

            document.add(table);

            document.add(new Paragraph(" "));

            Paragraph subtotal
                    = new Paragraph(
                            "Doctor Total : "
                            + formatAmount(doctorTotal),
                            new Font(
                                    Font.HELVETICA,
                                    11,
                                    Font.BOLD
                            )
                    );

            subtotal.setAlignment(Paragraph.ALIGN_RIGHT);

            document.add(subtotal);

            Paragraph qty
                    = new Paragraph(
                            "Total Quantity : " + totalQty,
                            new Font(
                                    Font.HELVETICA,
                                    11,
                                    Font.BOLD
                            )
                    );

            qty.setAlignment(Paragraph.ALIGN_RIGHT);

            document.add(qty);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

        }

        document.add(new Paragraph(" "));

// ================= PAYMENT SUMMARY =================
        Paragraph paymentTitle = new Paragraph(
                "PAYMENT SUMMARY",
                new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLUE)
        );

        document.add(paymentTitle);

        document.add(new Paragraph(" "));

        PdfPTable paymentTable = new PdfPTable(2);

        paymentTable.setWidthPercentage(60);

        paymentTable.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);

        paymentTable.addCell("Total Amount");
        paymentTable.addCell(formatAmount(totalAmount));

        paymentTable.addCell("Paid Amount");
        paymentTable.addCell(formatAmount(paidAmount));

        paymentTable.addCell("Due Amount");
        paymentTable.addCell(formatAmount(dueAmount));

        String status = dueAmount > 0 ? "PARTIALLY PAID" : "PAID";

        paymentTable.addCell("Payment Status");
        paymentTable.addCell(status);

        document.add(paymentTable);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

// ================= GRAND TOTAL =================
        Paragraph grandTotal = new Paragraph(
                "Grand Total : " + formatAmount(totalAmount),
                new Font(Font.HELVETICA, 15, Font.BOLD, new Color(0, 102, 51))
        );

        grandTotal.setAlignment(Paragraph.ALIGN_RIGHT);

        document.add(grandTotal);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

// ================= SIGNATURE =================
        Paragraph sign = new Paragraph(
                "____________________________\nAuthorized Signature",
                new Font(Font.HELVETICA, 11, Font.BOLD)
        );

        sign.setAlignment(Paragraph.ALIGN_RIGHT);

        document.add(sign);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

// ================= CLOSE PDF =================
        document.close();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Employee_Report_"
                        + firstOrder.getEmployeeName()
                        + "_"
                        + date
                        + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());

    }

    @PostMapping("/place-multiple")
    public List<ProductOrder> placeMultipleOrders(@RequestBody List<ProductOrder> orders) {

        String invoiceNo = "INV-" + System.currentTimeMillis();
        String orderTime = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a"));

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

            order.setInvoiceNo(invoiceNo);
            order.setOrderTime(orderTime);
        }

        return orderRepository.saveAll(orders);
    }

    @GetMapping("/admin/all")
    public List<ProductOrder> getAllOrdersForAdmin() {
        return orderRepository.findAll();
    }

    @GetMapping("/admin/sales-payment/summary")
    public Map<String, Object> getSalesSummary() {

        Map<String, Object> result = new java.util.LinkedHashMap<>();

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

        Double receivedAmount
                = Double.valueOf(request.get("receivedAmount").toString());

        String paymentMode
                = request.get("paymentMode") == null
                ? order.getPaymentMode()
                : request.get("paymentMode").toString();

        if (receivedAmount <= 0) {
            throw new RuntimeException("Received amount must be greater than 0");
        }

        Double currentDue = order.getDueAmount() == null ? 0.0 : order.getDueAmount();

        if (receivedAmount > currentDue) {
            throw new RuntimeException("Received amount cannot be greater than due amount");
        }

        double oldPaid
                = order.getPaidAmount() == null ? 0 : order.getPaidAmount();

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

// PDF AMOUNT FORMATTER

    private String formatAmount(double amount) {

        return String.format("₹%,.2f", amount);

    }

}
