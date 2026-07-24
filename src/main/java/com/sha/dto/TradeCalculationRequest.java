package com.sha.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeCalculationRequest {

    private List<MaterialRequest> materials;
    private String name;
    private int sellingPrice;
    private int tax;
    private int quantity;
}
