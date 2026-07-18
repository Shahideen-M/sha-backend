package com.sha.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CraftCalculationResponse {

    private List<MaterialResponse> materials;
    private int netReceived;
    private int totalCostPerCraft;
    private int totalCostPerAllCrafts;
    private int profitPerCraft;
    private int totalProfit;
    private double profitPercentage;

}
