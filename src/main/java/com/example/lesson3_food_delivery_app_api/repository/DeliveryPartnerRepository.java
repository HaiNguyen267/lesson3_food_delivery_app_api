package com.example.lesson3_food_delivery_app_api.repository;

import com.example.lesson3_food_delivery_app_api.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<DeliveryPartner> findByEmailIgnoreCase(String currentDeliveryPartnerEmail);
}
