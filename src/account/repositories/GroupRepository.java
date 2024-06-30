package account.repositories;

import account.entities.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findGroupByGroupName(String groupName);
    boolean existsGroupByGroupName(String groupName);
}
