package com.example.medhub.entity;

import com.example.medhub.dto.request.SpecializationCreateRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "specializations")
public class SpecializationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "specialization_id")
    private Long specializationId;

    @Column(name = "specialization_name")
    private String specializationName;

    @OneToMany(mappedBy = "specialization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoctorEntity> doctors;

    public static SpecializationEntity from(SpecializationCreateRequestDto specializationCreateRequestDto) {
        SpecializationEntity specializationEntity = new SpecializationEntity();
        specializationEntity.setSpecializationName(specializationCreateRequestDto.getSpecializationName());
        return specializationEntity;
    }
}
