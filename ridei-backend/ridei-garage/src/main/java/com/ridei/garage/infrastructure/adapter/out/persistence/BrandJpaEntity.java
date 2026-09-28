package com.ridei.garage.infrastructure.adapter.out.persistence;

import java.util.UUID;

import com.ridei.garage.domain.model.Brand;
import com.ridei.garage.domain.model.BrandId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "brands")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder  
public class BrandJpaEntity {

    @Id 
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    public Brand toDomain() {
        return Brand.reconstitute(new BrandId(id), name);
    }
    
}
