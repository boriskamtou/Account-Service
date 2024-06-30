package account.services;

import account.entities.EmployeeAccount;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmployeeAccountService {
    void createEmployeeAccounts(List<EmployeeAccount> employeeAccountList);
    void updateEmployeeAccount(EmployeeAccount employeeAccount);
    Object getEmployeeAccountDetails(String period);
}
