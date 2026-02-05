package com.example.medhub.entity;

import com.example.medhub.converter.PeselAttributeConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "patients")
@DiscriminatorValue("PATIENT")
@Getter
@Setter
public class Patient extends User {

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppointmentsEntity> appointments;

    @Column(name = "pesel_encrypted", length = 512)
    @Convert(converter = PeselAttributeConverter.class)
    private String pesel;

    @Column(name = "pesel_hash", length = 64)
    private String peselHash;
}
