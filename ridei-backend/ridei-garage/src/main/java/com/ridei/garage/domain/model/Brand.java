package com.ridei.garage.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter 
public class Brand {

    private final BrandId id;
    private final String name;

    public static Brand reconstitute(
        BrandId id,
        String name
    ) {
        return new Brand(id ,name);
    }
}
