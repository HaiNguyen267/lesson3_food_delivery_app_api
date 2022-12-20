package com.example.lesson3_food_delivery_app_api.service;

import com.example.lesson3_food_delivery_app_api.dto.request.DeliveryPartnerRegistrationRequest;
import com.example.lesson3_food_delivery_app_api.dto.response.ErrorResponse;
import com.example.lesson3_food_delivery_app_api.dto.response.SuccessResponse;
import com.example.lesson3_food_delivery_app_api.entity.DeliveryPartner;
import com.example.lesson3_food_delivery_app_api.entity.Order;
import com.example.lesson3_food_delivery_app_api.entity.OrderStatus;
import com.example.lesson3_food_delivery_app_api.exception.OrderDeliveringException;
import com.example.lesson3_food_delivery_app_api.repository.DeliveryPartnerRepository;
import com.example.lesson3_food_delivery_app_api.security.Role;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;

    private final PasswordEncoder passwordEncoder;

    private final OrderService orderService;

    public ResponseEntity<?> register(DeliveryPartnerRegistrationRequest registrationRequest) {
        String name = registrationRequest.getName();
        String email = registrationRequest.getEmail();
        String password = registrationRequest.getPassword();

        if (deliveryPartnerRepository.existsByEmailIgnoreCase(email)) {
            ErrorResponse response = new ErrorResponse("Email already registered");
            return ResponseEntity.badRequest().body(response);
        }

        DeliveryPartner deliveryPartner = DeliveryPartner.builder()
                .name(name)
                .build();

        deliveryPartner.setEmail(email);
        deliveryPartner.setPassword(passwordEncoder.encode(password));
        deliveryPartner.setRole(Role.DELIVERY_PARTNER);

        deliveryPartnerRepository.save(deliveryPartner);
        SuccessResponse response = new SuccessResponse("Delivery partner registered successfully");
        return ResponseEntity.ok(response);
    }

    public List<Order> viewReadyOrders() {
        // return orders which are ready to be delivered
        List<Order> readyOrders = orderService.getReadyOrders();
        return readyOrders;
    }

    @Transactional
    public ResponseEntity<?> deliverOrder(String currentDeliveryPartnerEmail, Long orderId) {
        DeliveryPartner deliveryPartner = getDeliveryPartnerByEmail(currentDeliveryPartnerEmail);
        Order order = orderService.getOrderById(orderId);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderDeliveringException("Order is already delivered");
        }

        if (order.getStatus() == OrderStatus.DELIVERING) {
            throw new OrderDeliveringException("Order is already being delivering");
        }

        order.setStatus(OrderStatus.DELIVERING);
        orderService.saveOrder(order);

        deliveryPartner.getDeliveringOrders().add(order);
        deliveryPartnerRepository.save(deliveryPartner);

        return ResponseEntity.ok(new SuccessResponse("Order delivered successfully"));
    }

    @Transactional
    public ResponseEntity<?> finishDelivery(String currentDeliveryPartnerEmail, Long orderId) {
        DeliveryPartner deliveryPartner = getDeliveryPartnerByEmail(currentDeliveryPartnerEmail);
        Order order = orderService.getOrderById(orderId);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderDeliveringException("Order is already delivered");
        }

        if (order.getStatus() == OrderStatus.READY) {
            throw new OrderDeliveringException("Order is not being delivered");
        }

        order.setStatus(OrderStatus.DELIVERED);
        orderService.saveOrder(order);

        deliveryPartner.getDeliveringOrders().remove(order);
        deliveryPartner.getDeliveredOrders().add(order);
        deliveryPartnerRepository.save(deliveryPartner);

        return ResponseEntity.ok(new SuccessResponse("Order delivered successfully"));
    }

    private DeliveryPartner getDeliveryPartnerByEmail(String currentDeliveryPartnerEmail) {
        return deliveryPartnerRepository.findByEmailIgnoreCase(currentDeliveryPartnerEmail)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));
    }
}
