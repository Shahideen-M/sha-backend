package com.sha.dto.request;

import com.sha.enums.CareerOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CareerRequest {
    private CareerOperation operation;
    private String role;
    private String location;
    private String experienceLevel;
}
