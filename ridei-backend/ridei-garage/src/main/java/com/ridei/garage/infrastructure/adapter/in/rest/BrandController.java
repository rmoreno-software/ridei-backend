package com.ridei.garage.infrastructure.adapter.in.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.garage.domain.port.in.ListBrandsUseCase;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/garage/brands")
@AllArgsConstructor 
public class BrandController {

    private final ListBrandsUseCase listBrandsUseCase;

    @GetMapping 
    public ResponseEntity<List<BrandResponseDTO>> listAll() {
        List<BrandResponseDTO> brands = listBrandsUseCase.listAll().stream()
            .map(BrandResponseDTO::fromDomain)
            .toList();
        return ResponseEntity.ok(brands);
    }
    
}
