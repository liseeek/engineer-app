package com.example.medhub.entity;

import com.example.medhub.enums.DoctorVerificationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@DiscriminatorValue("DOCTOR")
@Getter
@Setter
public class Doctor extends User {

    @Column(name = "pwz", length = 7)
    private String pwz;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, columnDefinition = "varchar(255) default 'PENDING'")
    private DoctorVerificationStatus verificationStatus = DoctorVerificationStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentsEntity> appointments;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinTable(name = "doctor_locations", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "location_id"))
    private List<LocationEntity> locations = new ArrayList<>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "doctor_specializations", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "specialization_id"))
    private List<SpecializationEntity> specializations = new ArrayList<>();

    @Override
    public boolean isEnabled() {
        return verificationStatus == DoctorVerificationStatus.VERIFIED;
    }
}
