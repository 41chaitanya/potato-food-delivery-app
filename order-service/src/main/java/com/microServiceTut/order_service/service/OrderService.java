package com.microServiceTut.order_service.service;

import com.microServiceTut.order_service.client.PaymentClient;
import com.microServiceTut.order_service.dto.CreateOrderRequest;
import com.microServiceTut.order_service.dto.OrderResponse;
import com.microServiceTut.order_service.dto.PaymentRequest;
import com.microServiceTut.order_service.dto.PaymentResponse;
import com.microServiceTut.order_service.model.Order;
import com.microServiceTut.order_service.model.OrderStatus;
import com.microServiceTut.order_service.model.PaymentStatus;
import com.microServiceTut.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentClient paymentClient;

    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {

        // 1. Create order
        Order order = new Order();
        order.setCustomerName(createOrderRequest.getCustomerName());
        order.setRestaurantName(createOrderRequest.getRestaurantName());
        order.setTotalAmount(createOrderRequest.getTotalAmount());
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setCreatedAt(LocalDateTime.now());

        // 2. Save order FIRST (ID generated here)
        Order savedOrder = orderRepository.save(order);

        // 3. Call payment service
        try {
            PaymentResponse paymentResponse =
                    paymentClient.makePayment(
                            new PaymentRequest(
                                    savedOrder.getId(),
                                    savedOrder.getTotalAmount()
                            )
                    );

            if (PaymentStatus.SUCCESS.equals(paymentResponse.getStatus())) {
                savedOrder.setStatus(OrderStatus.PAID);
            } else {
                savedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
            }

        } catch (Exception e) {
            e.printStackTrace();
            savedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        // 4. Save UPDATED status
        orderRepository.save(savedOrder);

        // 5. Return response
        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getStatus(),
                savedOrder.getTotalAmount()
        );
    }
}
