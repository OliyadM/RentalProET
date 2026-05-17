package com.rentalpro.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContractRejectionRequest {

    @NotBlank(message = "Rejection reason is required")
    private String reason;
}