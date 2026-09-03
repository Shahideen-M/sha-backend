package com.sha.service.skills;

import com.sha.brain.prompt.SkillPrompt;
import com.sha.dto.request.TradeCalculationRequest;
import com.sha.dto.response.TradeCalculationResponse;
import com.sha.dto.request.MaterialRequest;
import com.sha.dto.response.MaterialResponse;
import com.sha.brain.enums.SkillType;
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
    public TradeCalculationResponse executeTyped(TradeCalculationRequest calculationRequest) {

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

    @Override
    public Class<TradeCalculationRequest> getRequestClass() {
        return TradeCalculationRequest.class;
    }

    @Override
    public TradeCalculationResponse execute(Object request) {
        return executeTyped((TradeCalculationRequest) request);
    }

    @Override
    public SkillPrompt<?> describe() {
        List.of(
                "profit",
                "profit calculation",
                "trade",
                "trading",
                "selling",
                "sell",
                "craft",
                "crafting",
                "calculator",
                "calculate"
        );
        return null;
    }

}
