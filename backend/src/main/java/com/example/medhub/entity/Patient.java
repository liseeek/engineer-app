package com.example.medhub.entity;

import com.example.medhub.converter.PeselAttributeConverter;
import com.example.medhub.listener.PatientEntityListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "patients")
@DiscriminatorValue("PATIENT")
@Getter
@Setter
@EntityListeners(PatientEntityListener.class)
public class Patient extends User {

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentsEntity> appointments;

    @Column(name = "pesel_encrypted", length = 512)
    @Convert(converter = PeselAttributeConverter.class)
    private String pesel;

    @Column(name = "pesel_hash", length = 64)
    private String peselHash;
}
