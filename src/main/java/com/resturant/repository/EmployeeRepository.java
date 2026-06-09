package com.resturant.repository;

import com.resturant.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartment(String department);


    List<Employee> findByPosition(String position);


    boolean existsByEmail(String email);


    Optional<Employee> findByEmployeeCode(String employeeCode);



    Optional<Employee> findByUserId(Long userId);


    List<Employee> findByActiveTrue();
    List<Employee> findByShift(String shift);


    boolean existsByEmployeeCode(String employeeCode);
    @Query("SELECT e FROM Employee e WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Employee> findByNameContaining(@Param("name") String name);

}
