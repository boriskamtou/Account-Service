package account.controllers;

import account.entities.EmployeeAccount;
import account.services.EmployeeAccountService;
import account.utils.AccountCreateSuccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EmployeeAccountController {

    private final EmployeeAccountService employeeAccountService;
    @Autowired
    public EmployeeAccountController(EmployeeAccountService employeeAccountService) {
        this.employeeAccountService = employeeAccountService;
    }

    @PostMapping("/api/acct/payments")
    public ResponseEntity<?> createEmployeeAccounts(@RequestBody List<EmployeeAccount> employeeAccountList) {
        employeeAccountService.createEmployeeAccounts(employeeAccountList);
        return ResponseEntity.ok(new AccountCreateSuccess("Added successfully!"));
    }

    @PutMapping("/api/acct/payments")
    public ResponseEntity<?> updateEmployeeAccounts(@RequestBody EmployeeAccount employeeAccount) {
        employeeAccountService.updateEmployeeAccount(employeeAccount);
        return ResponseEntity.ok(new AccountCreateSuccess("Updated successfully!"));
    }

    @GetMapping("/api/empl/payment")
    public ResponseEntity<?> getEmployeeAccountsDetails(@RequestParam(name = "period", required = false) String period) {
        return ResponseEntity.ok(employeeAccountService.getEmployeeAccountDetails(period));
    }
}
