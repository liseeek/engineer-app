package com.example.medhub.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "workers")
@DiscriminatorValue("WORKER")
@Getter
@Setter
public class Worker extends User {
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;
}
