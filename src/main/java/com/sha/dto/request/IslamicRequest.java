package com.sha.dto.request;

import com.sha.enums.IslamicOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IslamicRequest {
    private IslamicOperation operation;
    private String prayer;
    private String surah;
    private String city;
    private String country;
}