package com.microservice.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String orderNumber;
    private List<OrderLineItemsDto> orderLineItemsListDto;
}
