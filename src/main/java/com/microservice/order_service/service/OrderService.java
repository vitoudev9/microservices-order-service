package com.microservice.order_service.service;

import com.microservice.order_service.dto.OrderLineItemsDto;
import com.microservice.order_service.dto.OrderRequest;
import com.microservice.order_service.dto.OrderResponse;
import com.microservice.order_service.model.OrderLineItems;
import com.microservice.order_service.model.Orders;
import com.microservice.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void placeOrder(OrderRequest orderRequest) {

        Orders order = new Orders();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItems(orderRequest.getOrderLineItemsListDto()
                .stream()
                .map(orderLineItemsDto -> new OrderLineItems(
                        orderLineItemsDto.getSkuCode(),
                        orderLineItemsDto.getQuantity(),
                        orderLineItemsDto.getPrice()))
                .toList());

        orderRepository.save(order);
        log.info("Order placed successfully with id: {}", order.getId());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll().stream()
                .map(order -> new OrderResponse(
                        order.getOrderNumber(),
                        order.getOrderLineItems().stream()
                                .map(orderLineItems -> new OrderLineItemsDto(
                                        orderLineItems.getSkuCode(),
                                        orderLineItems.getPrice(),
                                        orderLineItems.getQuantity()))
                                .toList()))
                .toList();
    }
}