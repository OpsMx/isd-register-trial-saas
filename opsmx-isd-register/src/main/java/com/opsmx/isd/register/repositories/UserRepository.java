package com.opsmx.isd.register.repositories;

import com.opsmx.isd.register.entities.User;
import com.opsmx.isd.register.enums.CDType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByBusinessEmail(String businessEmail);
    List<User> findAllByCreatedAtBetween(Date createdAtStart, Date createdAtEnd);
    Optional<List<User>> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "SELECT cdType FROM User")
    Optional<List<CDType>> findCdTypes();

    @Modifying
    @Transactional
    @Query(value = "UPDATE User SET cdType = :cdType")
    void updateCdType(@Param("cdType") CDType cdType);

    @Modifying
    @Transactional
    @Query(value = "alter table users alter column cd_type set not null", nativeQuery = true)
    void addNotNullConstraint();
}
