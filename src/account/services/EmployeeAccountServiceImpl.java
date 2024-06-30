package account.services;

import account.adapters.UserAdapter;
import account.entities.EmployeeAccount;
import account.entities.Users;
import account.exceptions.DuplicatePeriodException;
import account.exceptions.EmployeeNotFoundException;
import account.exceptions.InvalidFieldException;
import account.exceptions.UnauthorizedException;
import account.repositories.AuthRepository;
import account.repositories.EmployeeAccountRepository;
import account.utils.EmployeeSalary;
import account.utils.Utils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class EmployeeAccountServiceImpl implements EmployeeAccountService {
    private final AuthRepository authRepository;
    private final EmployeeAccountRepository employeeAccountRepository;
    private final SecurityEventService securityEventService;

    static final String PATTERN_MATCHER = "^(0[1-9]|1[0-2])-(\\d{4})$";

    static final Logger LOGGER = Logger.getLogger("EmployeeAccountServiceImpl");

    @Autowired
    public EmployeeAccountServiceImpl(AuthRepository authRepository, EmployeeAccountRepository employeeAccountRepository, SecurityEventService securityEventService) {
        this.authRepository = authRepository;
        this.employeeAccountRepository = employeeAccountRepository;
        this.securityEventService = securityEventService;
    }

    @Transactional
    @Override
    public void createEmployeeAccounts(List<EmployeeAccount> employeeAccountList) {
        for (var employee : employeeAccountList) {
            if (employee.getSalary() < 0 || !employee.getPeriod().matches(PATTERN_MATCHER)) {
                throw new InvalidFieldException("Invalid field!");
            }
            if (!authRepository.existsByEmail(employee.getEmployee().toLowerCase())) {
                throw new EmployeeNotFoundException();
            }
            if (employeeAccountRepository.existsByEmployeeAndPeriod(employee.getEmployee().toLowerCase(), employee.getPeriod())) {
                throw new DuplicatePeriodException();
            }
        }
        employeeAccountRepository.saveAll(employeeAccountList);
    }

    @Transactional
    @Override
    public void updateEmployeeAccount(EmployeeAccount employeeAccount) {
        final List<EmployeeAccount> employeeAccountList = employeeAccountRepository.findAll();

        if (!authRepository.existsByEmail(employeeAccount.getEmployee().toLowerCase())) {
            throw new EmployeeNotFoundException();
        }

        if (employeeAccount.getSalary() < 0 || !employeeAccount.getPeriod().matches(PATTERN_MATCHER)) {
            throw new InvalidFieldException("Invalid field!");
        }

        EmployeeAccount employeeAccountToUpdate = null;
        for (var empl : employeeAccountList) {
            if (empl.getEmployee().equalsIgnoreCase(employeeAccount.getEmployee())) {
                employeeAccountToUpdate = empl;
                break;
            }
        }

        if (employeeAccountToUpdate != null) {
            employeeAccountToUpdate.setEmployee(employeeAccount.getEmployee().toLowerCase());
            employeeAccountToUpdate.setPeriod(employeeAccountToUpdate.getPeriod());
            employeeAccountToUpdate.setPeriod(employeeAccountToUpdate.getPeriod());
            employeeAccountToUpdate.setSalary(employeeAccount.getSalary());

            employeeAccountRepository.save(employeeAccountToUpdate);
        }
    }

    @Override
    public Object getEmployeeAccountDetails(String period) {
        UserAdapter currentUser = (UserAdapter) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final Users user = authRepository.findUsersByEmail(currentUser.getUsername().toLowerCase());

        List<EmployeeSalary> objectResponse = new ArrayList<>();

        if (user == null) {
            LOGGER.info("USER NOT FOUND IN EMPL");
            throw new UnauthorizedException();
        }

        List<EmployeeAccount> employeeAccounts = employeeAccountRepository.findAllByEmployeeOrderByPeriodDesc(user.getEmail().toLowerCase());

        if (period != null && !period.matches(PATTERN_MATCHER)) {
            throw new InvalidFieldException("Invalid field!");
        }

        if (period == null || period.isEmpty()) {
            for (var employee : employeeAccounts) {
                System.out.println(employee.getPeriod());
                EmployeeSalary employeeSalary = new EmployeeSalary();
                employeeSalary.setName(user.getName());
                employeeSalary.setLastname(user.getLastname());
                employeeSalary.setPeriod(Utils.getConcatenatedPeriod(employee.getPeriod()));
                employeeSalary.setSalary(Utils.convertCentsToDollarsAndCents(employee.getSalary()));

                objectResponse.add(employeeSalary);
            }
            return objectResponse;
        } else {
            boolean hasFoundPeriod = false;

            for (var emp : employeeAccounts) {
                if (emp.getPeriod().equals(period)) {
                    hasFoundPeriod = true;
                    break;
                }
            }

            if (hasFoundPeriod) {
                EmployeeAccount employeeAccount = employeeAccountRepository.findByPeriodAndEmployee(period, user.getEmail().toLowerCase());
                EmployeeSalary employeeSalary = new EmployeeSalary();
                employeeSalary.setName(user.getName());
                employeeSalary.setLastname(user.getLastname());
                employeeSalary.setPeriod(Utils.getConcatenatedPeriod(employeeAccount.getPeriod()));
                employeeSalary.setSalary(Utils.convertCentsToDollarsAndCents(employeeAccount.getSalary()));

                return employeeSalary;

            } else {
                throw new InvalidFieldException("Error!");
            }

        }
    }
}
