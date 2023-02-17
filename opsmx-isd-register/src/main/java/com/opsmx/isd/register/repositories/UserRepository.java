package com.opsmx.isd.register.repositories;

import com.opsmx.isd.register.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByBusinessEmail(String businessEmail);
    List<User> findAllByCreatedAtBetween(Date createdAtStart, Date createdAtEnd);
    Optional<List<User>> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
