package com.example.medhub.entity;

import com.example.medhub.dto.request.LocationCreateRequestDto;
import com.example.medhub.mapper.LocationMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "locations")
public class LocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "location_name")
    private String locationName;

    @Column
    private String address;

    @Column
    private String city;

    @Column
    private String country;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkerEntity> workers;

    @ManyToMany(mappedBy = "locations")
    private List<DoctorEntity> doctors;

    public static LocationEntity from(LocationCreateRequestDto locationCreateRequestDto) {
        return LocationMapper.LOCATION_MAPPER.toLocationEntity(locationCreateRequestDto);
    }
}
