package com.opsmx.isd.register.entities;

import com.opsmx.isd.register.enums.CDType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@ToString
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"businessEmail", "cdType"}))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    @NotBlank(message = "First name is mandatory")
    private String firstName;

    @Column(nullable = false)
    @NotBlank(message = "Last name is mandatory")
    private String lastName;

    @Column(nullable = false)
    @NotBlank(message = "Company name is mandatory")
    private String companyName;

    @Column(nullable = false)
    @NotBlank(message = "Phone is mandatory")
    private String contactNumber;

    @Column(nullable = false)
    @Email(message = "Email is not valid", regexp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
    @NotBlank(message = "Email is mandatory")
    private String businessEmail;

    @Column
    @Enumerated(EnumType.STRING)
    private CDType cdType = CDType.isdSpinnaker;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public User(String firstName, String lastName, String companyName, String contactNumber, String businessEmail) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.companyName = companyName;
        this.contactNumber = contactNumber;
        this.businessEmail = businessEmail;
    }
}

