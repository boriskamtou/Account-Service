package account.repositories;

import account.entities.Users;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<Users, Long> {
    Users findUsersByEmail(String email);

    void deleteUsersByEmail(String email);

    boolean existsByEmail(String email);

    @Query("UPDATE Users u SET u.failedAttempt = ?1 WHERE u.email = ?2")
    @Modifying
    @Transactional
    void updateFailedAttempts(int failAttempts, String email);
}
