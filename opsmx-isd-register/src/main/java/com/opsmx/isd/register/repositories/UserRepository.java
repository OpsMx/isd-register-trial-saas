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
    Optional<List<User>> findByBusinessEmailAndCdType(String businessEmail, CDType cdType);
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
    void addNotNullConstraintOnCDType();

    @Modifying
    @Transactional
    @Query(value = "alter table users alter column business_email set not null", nativeQuery = true)
    void addNotNullConstraintOnBusinessEmail();

    @Modifying
    @Transactional
    @Query(value = "alter table users alter column company_name set not null", nativeQuery = true)
    void addNotNullConstraintOnCompanyName();

    @Modifying
    @Transactional
    @Query(value = "alter table users alter column contact_number set not null", nativeQuery = true)
    void addNotNullConstraintOnContactNumber();

    @Modifying
    @Transactional
    @Query(value = "alter table users alter column first_name set not null", nativeQuery = true)
    void addNotNullConstraintOnFirstName();

    @Modifying
    @Transactional
    @Query(value = "alter table users alter column last_name set not null", nativeQuery = true)
    void addNotNullConstraintOnLastName();

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_b9nwfqo68so84l81wo4wmnf9b", nativeQuery = true)
    void dropBusinessEmailUniqueConstraint();

}
