package com.rentalpro.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContractConfirmationRequest {

    @NotBlank(message = "Digital signature is required")
    private String signature;
}