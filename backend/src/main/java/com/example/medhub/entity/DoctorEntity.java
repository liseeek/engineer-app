package com.example.medhub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "doctors")
public class DoctorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private long doctorId;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "pwz", unique = true, length = 7)
    private String pwz;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentsEntity> appointments;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinTable(name = "doctor_locations", joinColumns = @JoinColumn(name = "doctor_id"), inverseJoinColumns = @JoinColumn(name = "location_id"))
    private List<LocationEntity> locations;

    @ManyToOne
    @JoinColumn(name = "specialization_id", nullable = false)
    private SpecializationEntity specialization;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DoctorEntity that = (DoctorEntity) o;
        return doctorId == that.doctorId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctorId);
    }
}
