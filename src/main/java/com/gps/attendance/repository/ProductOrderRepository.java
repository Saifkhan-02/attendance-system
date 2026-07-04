package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gps.attendance.entity.ProductOrder;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {

        // BASIC ORDER METHODS

    List<ProductOrder> findByEmployeeId(Long employeeId);

    long countByEmployeeIdAndOrderDateStartingWith(Long employeeId, String month);

    List<ProductOrder> findByDoctorId(Long doctorId);

    // Date wise
    List<ProductOrder> findByOrderDate(String orderDate);

    // Month wise
    List<ProductOrder> findByOrderDateStartingWith(String month);

    List<ProductOrder> findByEmployeeIdOrderByIdDesc(Long employeeId);

    List<ProductOrder> findAllByOrderByIdDesc();

    @Query("SELECT COALESCE(SUM(p.orderAmount), 0) FROM ProductOrder p WHERE p.employeeId = :employeeId AND p.orderDate LIKE CONCAT(:month, '%')")
    Double getMonthlySalesByEmployee(
        @Param("employeeId") Long employeeId,
            @Param("month") String month);

    List<ProductOrder> findByEmployeeIdAndOrderDateOrderByIdDesc(
            Long employeeId,
            String orderDate
    );

    List<ProductOrder> findByEmployeeIdAndOrderDateStartingWithOrderByIdDesc(
            Long employeeId,
            String month
    );

    List<ProductOrder>
            findByEmployeeNameContainingIgnoreCaseAndDoctorNameContainingIgnoreCase(
                    String employeeName,
                    String doctorName
            );

    @Query("""
SELECT COALESCE(SUM(p.orderAmount), 0)
FROM ProductOrder p
WHERE p.employeeId = :employeeId
""")
    Double getTotalSalesByEmployee(@Param("employeeId") Long employeeId);

    @Query("""
SELECT COALESCE(SUM(p.paidAmount), 0)
FROM ProductOrder p
WHERE p.employeeId = :employeeId
""")
    Double getTotalCollectionByEmployee(@Param("employeeId") Long employeeId);

    @Query("""
SELECT COALESCE(SUM(p.dueAmount), 0)
FROM ProductOrder p
WHERE p.employeeId = :employeeId
""")
    Double getTotalDueByEmployee(@Param("employeeId") Long employeeId);

    @Query("""
SELECT p.distributorName,
       COALESCE(SUM(p.orderAmount), 0),
       COALESCE(SUM(p.paidAmount), 0),
       COALESCE(SUM(p.dueAmount), 0)
FROM ProductOrder p
WHERE p.employeeId = :employeeId
GROUP BY p.distributorName
""")
    List<Object[]> getDistributorWiseSales(@Param("employeeId") Long employeeId);

    @Query("""
SELECT p
FROM ProductOrder p
WHERE p.employeeId = :employeeId
AND (:distributorName = '' OR p.distributorName = :distributorName)
ORDER BY p.id DESC
""")
    List<ProductOrder> getOrdersByEmployeeAndDistributor(
            @Param("employeeId") Long employeeId,
            @Param("distributorName") String distributorName
    );

    @Query("""
SELECT COALESCE(SUM(p.orderAmount),0)
FROM ProductOrder p
""")
    Double getTotalSale();

    @Query("""
SELECT COALESCE(SUM(p.paidAmount),0)
FROM ProductOrder p
""")
    Double getTotalCollection();

    @Query("""
SELECT COALESCE(SUM(p.dueAmount),0)
FROM ProductOrder p
""")
    Double getTotalDue();

    @Query("SELECT COUNT(p) FROM ProductOrder p WHERE p.employeeId = :employeeId")
    long countOrdersByEmployeeId(@Param("employeeId") Long employeeId);

   List<ProductOrder> findByInvoiceNo(String invoiceNo);
   List<ProductOrder> findByInvoiceNoOrderByIdAsc(String invoiceNo);

    @Query("""
SELECT p
FROM ProductOrder p
WHERE
(:employeeId IS NULL OR p.employeeId = :employeeId)
ORDER BY p.id DESC
""")
    List<ProductOrder> findByEmployeeFilter(
            @Param("employeeId") Long employeeId
    );

    @Query("""
SELECT p
FROM ProductOrder p
WHERE
(:employeeId IS NULL OR p.employeeId = :employeeId)
ORDER BY p.id DESC
""")
List<ProductOrder> searchOrders(
        @Param("employeeId") Long employeeId
);

@Query("""
SELECT p
FROM ProductOrder p
WHERE
(:employeeId IS NULL OR p.employeeId = :employeeId)
AND
(:date IS NULL OR :date = '' OR p.orderDate = :date)
ORDER BY p.id DESC
""")
Page<ProductOrder> searchOrdersWithPagination(
        @Param("employeeId") Long employeeId,
        @Param("date") String date,
        Pageable pageable
);

@Query("""
SELECT p
FROM ProductOrder p
WHERE p.employeeId = :employeeId
AND p.orderDate LIKE CONCAT(:year,'-',:month,'%')
ORDER BY p.orderDate ASC, p.id ASC
""")
List<ProductOrder> findEmployeeMonthlyOrders(
        @Param("employeeId") Long employeeId,
        @Param("year") String year,
        @Param("month") String month
);

@Query("""
SELECT COUNT(DISTINCT p.invoiceNo)
FROM ProductOrder p
WHERE p.employeeId = :employeeId
AND p.orderDate LIKE CONCAT(:year,'-',:month,'%')
""")
Long countInvoices(
        @Param("employeeId") Long employeeId,
        @Param("year") String year,
        @Param("month") String month
);

@Query("""
SELECT p
FROM ProductOrder p
WHERE p.employeeId = :employeeId
AND p.orderDate = :date
ORDER BY p.id ASC
""")
List<ProductOrder> findEmployeeDailyOrders(
        @Param("employeeId") Long employeeId,
        @Param("date") String date
);

@Query("""
SELECT COUNT(DISTINCT p.invoiceNo)
FROM ProductOrder p
WHERE p.employeeId = :employeeId
AND p.orderDate = :date
""")
Long countDailyInvoices(
        @Param("employeeId") Long employeeId,
        @Param("date") String date
);

}
