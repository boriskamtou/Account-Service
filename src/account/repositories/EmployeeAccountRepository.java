package account.repositories;

import account.entities.EmployeeAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeAccountRepository extends JpaRepository<EmployeeAccount, Long> {
    boolean existsByEmployeeAndPeriod(String employee, String period);

    EmployeeAccount findByPeriodAndEmployee(String period, String employee);
    List<EmployeeAccount> findAllByEmployeeOrderByPeriodDesc(String employeeEmail);
    List<EmployeeAccount> findByPeriodOrderByPeriodDesc(String period);
}
