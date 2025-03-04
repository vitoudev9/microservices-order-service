package com.microservice.order_service.service;

import com.microservice.order_service.dto.InventoryResponse;
import com.microservice.order_service.dto.OrderLineItemsDto;
import com.microservice.order_service.dto.OrderRequest;
import com.microservice.order_service.dto.OrderResponse;
import com.microservice.order_service.model.OrderLineItems;
import com.microservice.order_service.model.Orders;
import com.microservice.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${app.inventoryservice.url}")
    private String inventoryServiceUrl;

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

        List<String> skuCodeList = order.getOrderLineItems().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        // Check stock in Inventory Microservices
        InventoryResponse[] inventoryServiceResponse =  webClientBuilder.build()
                .get()
                .uri(inventoryServiceUrl,
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodeList).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        if (inventoryServiceResponse == null || inventoryServiceResponse.length == 0) {
            throw new IllegalArgumentException("Empty result from Inventory Service API");
        }

        if (inventoryServiceResponse.length != skuCodeList.size()) {
            throw new IllegalArgumentException("One or more SKU Codes is not in inventory");
        }

        boolean allProductsInStock = Arrays.stream(inventoryServiceResponse).allMatch(InventoryResponse::isInStock);

        if (!allProductsInStock) {
            throw new IllegalArgumentException("The item is not in stock");
        }

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