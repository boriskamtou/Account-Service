package account.repositories;

import account.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<Users, Long> {
    Users findByEmail(String email);
}
