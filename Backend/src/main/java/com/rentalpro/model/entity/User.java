package com.rentalpro.model.entity;

import com.rentalpro.model.enums.AccountStatus;
import com.rentalpro.model.enums.EntityType;
import com.rentalpro.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    private String subCityZone;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // KYC/Profile fields
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.PENDING_PROFILE;

    private LocalDate dateOfBirth;

    @Column(length = 500)
    private String residentialAddress;

    @Column(unique = true)
    private String nationalIdNumber;

    private String nationalIdDocumentUrl;

    @Column(unique = true)
    private String tinNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EntityType entityType = EntityType.INDIVIDUAL;

    private String businessRegNumber;

    private String businessRegDocumentUrl;

    @Column(length = 1000)
    private String verificationNotes;

    @ManyToOne
    @JoinColumn(name = "verified_by_id")
    private User verifiedBy;

    private LocalDateTime verifiedAt;

    @Column(length = 500)
    private String rejectionReason;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}