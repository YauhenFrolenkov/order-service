package com.innowise.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.order.entity.Item;
import com.innowise.order.repository.ItemRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.innowise.order.repository.OrderItemRepository;
import com.innowise.order.repository.OrderRepository;
import com.innowise.order.security.JwtProvider;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderControllerIT {

    static {
        System.setProperty("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "npipe:////./pipe/docker_engine");
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("user-service.url", () -> "http://localhost:8080");
    }

    static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private Long testItemId;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        configureFor("localhost", 8080);
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        when(jwtProvider.validateToken(anyString())).thenReturn(true);
        when(jwtProvider.getUserIdFromToken(anyString())).thenReturn(10L);
        when(jwtProvider.getRoleFromToken(anyString())).thenReturn("USER");

        if (orderItemRepository != null) {
            orderItemRepository.deleteAll();
        }
        if (orderRepository != null) {
            orderRepository.deleteAll();
        }

        itemRepository.deleteAll();

        Item item = Item.builder()
                .name("Item 1")
                .price(new BigDecimal("100.00"))
                .build();
        Item savedItem = itemRepository.save(item);
        testItemId = savedItem.getId();
    }

    @Test
    void createOrder_shouldReturnOrderWithUserInfo() throws Exception {
        stubFor(get(urlEqualTo("/users/10"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 10,
                                  "name": "Test",
                                  "surname": "User",
                                  "email": "test@example.com"
                                }
                                """)));

        String requestJson = String.format("""
                {
                  "items": [
                    {
                      "itemId": %d,
                      "quantity": 2
                    }
                  ]
                }
                """, testItemId);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", "Bearer test-jwt-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.userName").value("Test User"))
                .andExpect(jsonPath("$.totalPrice").value(200.00));
    }

    @Test
    void createOrder_shouldReturnNotFoundForInvalidItem() throws Exception {
        String requestJson = """
            {
              "items": [
                {
                  "itemId": 999,
                  "quantity": 2
                }
              ]
            }
            """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", "Bearer test-jwt-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void createOrder_shouldReturnValidationErrorForEmptyItems() throws Exception {
        String requestJson = """
            {
              "items": []
            }
            """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", "Bearer test-jwt-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.messages.items").exists());
    }

    @Test
    void createOrder_shouldReturnUnauthorizedWithoutJwt() throws Exception {
        String requestJson = String.format("""
            {
              "items": [
                {
                  "itemId": %d,
                  "quantity": 1
                }
              ]
            }
            """, testItemId);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void createOrder_shouldReturnUserNotFound() throws Exception {
        stubFor(get(urlEqualTo("/users/10"))
                .willReturn(aResponse().withStatus(404)));

        String requestJson = String.format("""
        {
          "items": [
            {
              "itemId": %d,
              "quantity": 1
            }
          ]
        }
        """, testItemId);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", "Bearer test-jwt-token"))
                .andExpect(status().isCreated())  // ← СТАТУС 201 (fallback сработал)
                .andExpect(jsonPath("$.userEmail").value("unknown@example.com"))
                .andExpect(jsonPath("$.userName").value("Unknown User"));
    }
}
