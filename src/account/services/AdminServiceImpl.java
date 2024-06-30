package account.services;

import account.adapters.UserAdapter;
import account.dtos.UserDTO;
import account.entities.Users;
import account.exceptions.*;
import account.repositories.AuthRepository;
import account.repositories.GroupRepository;
import account.utils.Constants;
import account.utils.StatusResponse;
import account.utils.UserDeleteSuccess;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    private final AuthRepository authRepository;
    private final GroupRepository groupRepository;
    private final SecurityEventService securityEventService;

    static final String ROLE_ADMINISTRATOR = "ADMINISTRATOR";
    static final String ROLE_ACCOUNTANT = "ACCOUNTANT";
    static final String ROLE_USER = "USER";
    static final String ROLE_AUDITOR = "AUDITOR";

    @Autowired
    public AdminServiceImpl(AuthRepository authRepository, GroupRepository groupRepository, SecurityEventService securityEventService) {
        this.authRepository = authRepository;
        this.groupRepository = groupRepository;
        this.securityEventService = securityEventService;
    }

    @Override
    public List<Users> getUsersDetails() {
        UserAdapter userAdapter = (UserAdapter) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Users currentUser = authRepository.findUsersByEmail(userAdapter.getUsername().toLowerCase());
//        if (currentUser != null && !currentUser.getRoles().contains("ROLE_ADMINISTRATOR")) {
//            throw new ForbiddenException();
//        }
        return authRepository.findAll();
    }

    @Override
    @Transactional
    public UserDeleteSuccess deleteUser(String userEmail) {
        UserAdapter userAdapter = (UserAdapter) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Users currentUser = authRepository.findUsersByEmail(userAdapter.getUsername().toLowerCase());
        Users userToDelete = authRepository.findUsersByEmail(userEmail.toLowerCase());

        if (userToDelete == null || currentUser == null) {
            throw new UserNotFoundException();
        }

        if (!currentUser.getRoles().contains("ROLE_ADMINISTRATOR")) {
            throw new ForbiddenException();
        }

        if (userToDelete.getRoles().contains("ROLE_ADMINISTRATOR")) {
            throw new DeleteAdminException(userEmail);
        }

        authRepository.deleteUsersByEmail(userEmail.toLowerCase());
        UserDeleteSuccess userDeleteSuccess = new UserDeleteSuccess();
        userDeleteSuccess.setUser(userEmail);
        securityEventService.saveEvent(Constants.EVENT_DELETE_USER, currentUser.getEmail(), userToDelete.getEmail(), "/api/admin/user");
        userDeleteSuccess.setStatus("Deleted successfully!");
        return userDeleteSuccess;
    }

    @Override
    @Transactional
    public Users updateUserRole(UserDTO userDTO) {
        boolean isUserExist = authRepository.existsByEmail(userDTO.getUser().toLowerCase());
        boolean isRoleExist = groupRepository.existsGroupByGroupName(userDTO.getRole());

        UserAdapter userAdapter = (UserAdapter) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Users user = authRepository.findUsersByEmail(userDTO.getUser().toLowerCase());
        Users currentUser = authRepository.findUsersByEmail(userAdapter.getUsername().toLowerCase());

        if (!currentUser.getRoles().contains("ROLE_" + ROLE_ADMINISTRATOR)) {
            System.out.println("ACCESS DENIED");
            throw new ForbiddenException();
        }
        if (!isUserExist) {
            throw new UserNotFoundException();
        }
        if (!isRoleExist) {
            throw new RoleNotFoundException("Role not found!");
        }

        if (userDTO.getOperation().equals("REMOVE")) {
            if (userDTO.getRole().equals(ROLE_ADMINISTRATOR)) {
                throw new BadRequestException("Can't remove ADMINISTRATOR role!");
            }

            if (!user.getRoles().contains("ROLE_" + userDTO.getRole())) {
                System.out.println("Use roles");
                user.getRoles().forEach(System.out::println);
                System.out.println("Posted role");
                System.out.println(userDTO.getRole());
                throw new BadRequestException("The user does not have a role!");
            }
            if (user.getRoles().size() == 1) {
                throw new BadRequestException("The user must have at least one role!");
            }
            for (var group : user.getGroups()) {
                if (group.getGroupName().equals(userDTO.getRole().toUpperCase())) {
                    user.getGroups().remove(group);
                    securityEventService.saveEvent(Constants.EVENT_REMOVE_ROLE, userAdapter.getUsername().toLowerCase(), String.format("Remove role %s from %s", group.getGroupName(), user.getEmail()), "/api/admin/user/role");
                    break;
                }
            }
        } else if (userDTO.getOperation().equals("GRANT")) {
            if (
                    (user.getRoles().contains("ROLE_" + ROLE_ADMINISTRATOR) &&
                            (userDTO.getRole().equals(ROLE_ACCOUNTANT) || userDTO.getRole().equals(ROLE_USER) || userDTO.getRole().equals(ROLE_AUDITOR))) || ((user.getRoles().contains("ROLE_" + ROLE_ACCOUNTANT) || user.getRoles().contains("ROLE_" + ROLE_USER) || user.getRoles().contains("ROLE_" + ROLE_AUDITOR)) && userDTO.getRole().equals(ROLE_ADMINISTRATOR))) {
                System.out.println("CALL ENTER HERE!!!");
                throw new BadRequestException("The user cannot combine administrative and business roles!");
            }
            if (groupRepository.findGroupByGroupName(userDTO.getRole()).isPresent()) {
                user.getGroups().add(groupRepository.findGroupByGroupName(userDTO.getRole()).get());
                securityEventService.saveEvent(Constants.EVENT_GRANT_ROLE, userAdapter.getUsername().toLowerCase(), String.format("Grant role %s to %s", groupRepository.findGroupByGroupName(userDTO.getRole()).get().getGroupName(), user.getEmail()), "/api/admin/user/role");
            }
        } else {
            throw new BadRequestException("Bad Operation!!!");
        }
        return authRepository.save(user);
    }

    @Override
    public StatusResponse lockOrUnlockUser(String email, String operation) {
        boolean isUserExist = authRepository.existsByEmail(email.toLowerCase());
        UserAdapter userAdapter = (UserAdapter) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!isUserExist) {
            throw new UserNotFoundException();
        }
        Users currentUser = authRepository.findUsersByEmail(userAdapter.getUsername().toLowerCase());

        if (currentUser == null) {
            throw new UnauthorizedException();
        }

        if (operation.equals("LOCK")) {
            lockUser(email);
            securityEventService.saveEvent(Constants.EVENT_LOCK_USER, currentUser.getEmail(), String.format("Lock user %s", email.toLowerCase()), "/api/admin/user/access");
            return new StatusResponse(String.format("User %s locked!", email.toLowerCase()));
        } else if (operation.equals("UNLOCK")) {
            unLockUser(email);
            securityEventService.saveEvent(Constants.EVENT_UNLOCK_USER, currentUser.getEmail(), String.format("Unlock user %s", email.toLowerCase()), "/api/admin/user/access");
            return new StatusResponse(String.format("User %s unlocked!", email.toLowerCase()));
        } else {
            throw new BadRequestException();
        }

    }

    public void lockUser(String email) {
        Users user = authRepository.findUsersByEmail(email.toLowerCase());

        if (user != null) {
            if (user.getRoles().contains("ROLE_ADMINISTRATOR")) {
                throw new BadRequestException("Can't lock the ADMINISTRATOR!");
            }
            if (!user.isActive()) {
                throw new LockedException("User account is locked");
            }
            user.setActive(false);
            authRepository.save(user);
        }
    }

    public void unLockUser(String email) {
        Users user = authRepository.findUsersByEmail(email.toLowerCase());
        if (user != null) {
            if (!user.isActive()) {
                user.setActive(true);
                user.setFailedAttempt(0);
                authRepository.save(user);
            }
        }
    }

}
