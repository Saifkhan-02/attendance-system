package com.gps.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gps.attendance.entity.ProductOrder;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {

    List<ProductOrder> findByEmployeeId(Long employeeId);

    long countByEmployeeIdAndOrderDateStartingWith(Long employeeId, String month);

     List<ProductOrder> findByDoctorId(Long doctorId);

    // Date wise
    List<ProductOrder> findByOrderDate(String orderDate);

    // Month wise
    List<ProductOrder> findByOrderDateStartingWith(String month);

    @Query("SELECT COALESCE(SUM(p.orderAmount), 0) FROM ProductOrder p WHERE p.employeeId = :employeeId AND p.orderDate LIKE CONCAT(:month, '%')")
Double getMonthlySalesByEmployee(@Param("employeeId") Long employeeId,
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
   
}
