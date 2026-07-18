package com.sha.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialRequest {

    private String materialName;
    private int requiredQuantity;
    private int price;

}
