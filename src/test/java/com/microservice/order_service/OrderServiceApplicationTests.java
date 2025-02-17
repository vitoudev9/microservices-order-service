package com.microservice.order_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.microservice.order_service.dto.OrderLineItemsDto;
import com.microservice.order_service.dto.OrderRequest;
import com.microservice.order_service.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class OrderServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Container
	static MySQLContainer<?> mySQLContainer =new MySQLContainer<>("mysql:8.0");

	private static final Faker faker = new Faker();

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
		dynamicPropertyRegistry.add("spring.datasource.username", mySQLContainer::getUsername);
		dynamicPropertyRegistry.add("spring.datasource.password", mySQLContainer::getPassword);
	}

	@Test
	void testDatabaseConnection() {
		System.out.println("MySQL Container is running at: " + mySQLContainer.getJdbcUrl());
		System.out.println("MySQL username is: " + mySQLContainer.getUsername());
		System.out.println("MySQL password is: " + mySQLContainer.getPassword());
	}

	@Test
	void testCreateOrder() throws Exception {

		OrderRequest orderRequest = new OrderRequest();
		List<OrderLineItemsDto> orderLineItemsDtoList = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			OrderLineItemsDto orderLineItemsDto = new OrderLineItemsDto();
			orderLineItemsDto.setPrice(new BigDecimal(faker.commerce().price()));
			orderLineItemsDto.setQuantity(faker.number().numberBetween(1,10));
			orderLineItemsDto.setSkuCode(faker.commerce().productName());

			orderLineItemsDtoList.add(orderLineItemsDto);
		}

		orderRequest.setOrderLineItemsListDto(orderLineItemsDtoList);

		String strOrderRequest = objectMapper.writeValueAsString(orderRequest);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(strOrderRequest))
				.andExpect(status().isCreated());
		Assertions.assertEquals(1, orderRepository.findAll().size());
	}

}
