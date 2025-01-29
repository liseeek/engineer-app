package com.example.medhub.entity;

import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.mapper.DoctorMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
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

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentsEntity> appointments;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
            name = "doctor_locations",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private List<LocationEntity> locations;

    @ManyToOne
    @JoinColumn(name = "specialization_id", nullable = false)
    private SpecializationEntity specialization;

    public static DoctorEntity from(DoctorCreateRequestDto newDoctor) {
        return DoctorMapper.DOCTOR_MAPPER.toDoctor(newDoctor);
    }
}
