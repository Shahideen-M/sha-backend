package com.sha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialResponse {

    private String materialName;
    private int requiredQuantity;
    private int price;
    private int totalMaterialCost;

}
