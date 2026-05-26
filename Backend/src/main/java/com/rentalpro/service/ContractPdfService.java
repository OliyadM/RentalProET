package com.rentalpro.service;

import com.rentalpro.model.entity.RentalContract;

public interface ContractPdfService {
    
    /**
     * Generates a complete Ethiopian rental contract PDF
     * @param contract The rental contract entity
     * @return PDF as byte array
     */
    byte[] generateContractPdf(RentalContract contract);
}
