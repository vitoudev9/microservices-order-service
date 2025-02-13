package com.microservice.order_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_line_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;

    public OrderLineItems(String skuCode, Integer quantity, BigDecimal price) {
        this.skuCode = skuCode;
        this.quantity = quantity;
        this.price = price;
    }
}
