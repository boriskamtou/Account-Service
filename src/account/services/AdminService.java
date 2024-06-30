package account.services;

import account.dtos.UserDTO;
import account.entities.Users;
import account.utils.StatusResponse;
import account.utils.UserDeleteSuccess;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface AdminService {
    List<Users> getUsersDetails();
    UserDeleteSuccess deleteUser(String userEmail);

    Users updateUserRole(UserDTO userDTO);

    StatusResponse lockOrUnlockUser(String email, String operation);
}
