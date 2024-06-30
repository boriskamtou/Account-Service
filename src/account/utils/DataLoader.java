package account.utils;

import account.entities.Group;
import account.repositories.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader {

    static final String ROLE_ADMINISTRATOR = "ADMINISTRATOR";
    static final String ROLE_ACCOUNTANT = "ACCOUNTANT";
    static final String ROLE_USER = "USER";
    static final String ROLE_AUDITOR = "AUDITOR";

    private final GroupRepository groupRepository;

    @Autowired
    public DataLoader(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
        createRoles();
    }

    private void createRoles() {
        List<String> roles = Arrays.asList(ROLE_ADMINISTRATOR, ROLE_ACCOUNTANT, ROLE_USER, ROLE_AUDITOR);
        try {
            for (String role : roles) {
                if (!groupRepository.existsGroupByGroupName(role)) {
                    groupRepository.save(new Group(role));
                }
            }
        } catch (Exception e) {
//                e.printStackTrace();
        }
    }
}