package com.sha.brain;

import com.sha.brain.dto.ShaBrainResponse;
import com.sha.brain.enums.ShaResponseType;
import org.springframework.stereotype.Service;

@Service
public class ShaBrainValidator {

    public void validate(ShaBrainResponse response) {
        if (response.getType() == ShaResponseType.CHAT) validateChat(response);
    }

    public void validateChat(ShaBrainResponse response) {
        if (response.getMessage() != null) return;
        else throw new RuntimeException("Message is required for CHAT response.");
    }

}
