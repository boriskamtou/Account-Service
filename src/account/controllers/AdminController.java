package account.controllers;

import account.dtos.UserDTO;
import account.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PutMapping("/user/role")
    @Secured({"ROLE_ADMINISTRATOR"})
    public ResponseEntity<?> updateRole(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(adminService.updateUserRole(userDTO));
    }

    @DeleteMapping("/user/{email}")
    public ResponseEntity<?> deleteRole(@PathVariable String email) {
        var response = adminService.deleteUser(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/user/", name = "Get users infos")
    public ResponseEntity<?> getUsersInfos() {
        return ResponseEntity.ok(adminService.getUsersDetails());
    }

    // lock or unlock the user
    @PutMapping("/user/access")
    public ResponseEntity<?> toggleUserAccount(@RequestBody Map<String, String> objectMap) {
        String user = objectMap.get("user");
        String operation = objectMap.get("operation");
        return ResponseEntity.ok(adminService.lockOrUnlockUser(user, operation.toUpperCase()));
    }

}
