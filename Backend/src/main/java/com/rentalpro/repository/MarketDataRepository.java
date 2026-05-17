package com.rentalpro.repository;

import com.rentalpro.model.entity.MarketData;
import com.rentalpro.model.enums.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketDataRepository extends JpaRepository<MarketData, UUID> {

    List<MarketData> findBySubCityAndPropertyType(String subCity, PropertyType propertyType);

    Optional<MarketData> findBySubCityAndPropertyTypeAndPeriod(String subCity, PropertyType propertyType, LocalDate period);

    List<MarketData> findBySubCityOrderByPeriodDesc(String subCity);
}