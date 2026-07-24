package com.sha.service.skills;

import com.sha.dto.TradeCalculationRequest;
import com.sha.dto.TradeCalculationResponse;
import com.sha.dto.MaterialRequest;
import com.sha.dto.MaterialResponse;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TradeCalculatorSkill implements Skill<TradeCalculationRequest, TradeCalculationResponse> {

    @Override
    public SkillType getType() {
        return SkillType.TRADE_CALCULATOR;
    }

    @Override
    public TradeCalculationResponse execute(TradeCalculationRequest calculationRequest) {

        int sellingPrice = calculationRequest.getSellingPrice();
        int taxAmount = sellingPrice * calculationRequest.getTax() / 100;
        int netReceived = (sellingPrice - taxAmount) * calculationRequest.getQuantity();

        if (calculationRequest.getMaterials() == null || calculationRequest.getMaterials().isEmpty()) {
            TradeCalculationResponse response = new TradeCalculationResponse();
            response.setNetReceived(netReceived);
            return response;
        } else {

            List<MaterialResponse> materialResponses = new ArrayList<>();
            int totalCostPerCraft = 0;

            for (MaterialRequest material : calculationRequest.getMaterials()) {

                int totalMaterialCost = material.getPrice() * material.getRequiredQuantity();
                MaterialResponse response = new MaterialResponse(
                        material.getMaterialName(),
                        material.getRequiredQuantity(),
                        material.getPrice(),
                        totalMaterialCost
                );
                materialResponses.add(response);
                totalCostPerCraft += totalMaterialCost;

            }

            int totalCostPerAllCrafts = totalCostPerCraft * calculationRequest.getQuantity();
            int profitPerCraft = netReceived - totalCostPerCraft;
            int totalProfit = profitPerCraft * calculationRequest.getQuantity();
            double profitPercentage = (profitPerCraft * 100.0) / totalCostPerCraft;

            return new TradeCalculationResponse(materialResponses,
                    netReceived,
                    totalCostPerCraft,
                    totalCostPerAllCrafts,
                    profitPerCraft,
                    totalProfit,
                    profitPercentage);
        }
    }

}
