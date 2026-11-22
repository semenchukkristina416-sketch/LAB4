package carservice.e2e;

import carservice.api.dto.CarCreateRequest;
import carservice.api.dto.ClientCreateRequest;
import carservice.api.dto.ClientResponse;
import carservice.api.dto.CarResponse;
import carservice.api.dto.JobRequest;
import carservice.api.dto.JobResponse;
import carservice.api.dto.MechanicCreateRequest;
import carservice.api.dto.MechanicResponse;
import carservice.api.dto.OrderCreateRequest;
import carservice.api.dto.OrderResponse;
import carservice.api.dto.StartOrderRequest;
import carservice.domain.enumeration.MechanicSpecialization;
import carservice.domain.enumeration.OrderPriority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(carservice.integration.IntegrationTestConfiguration.class)
@ActiveProfiles("test")
class CarServiceE2E {

    private RestTestClient client;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpClient() {
        this.client = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void completesFullOrderLifecycleThroughHttp() {
        ClientResponse createdClient = client.post().uri("/api/clients")
                .body(new ClientCreateRequest("E2E Client", "+380671234567"))
                .exchange().expectStatus().isCreated()
                .expectBody(ClientResponse.class).returnResult().getResponseBody();
        assertNotNull(createdClient);

        CarResponse createdCar = client.post().uri("/api/cars")
                .body(new CarCreateRequest("E2E" + System.nanoTime(), "Toyota", "Camry", createdClient.id()))
                .exchange().expectStatus().isCreated()
                .expectBody(CarResponse.class).returnResult().getResponseBody();
        Long carId = createdCar == null ? null : createdCar.id();
        assertNotNull(carId);

        MechanicResponse mechanic = client.post().uri("/api/mechanics")
                .body(new MechanicCreateRequest("E2E Mechanic", MechanicSpecialization.GENERAL))
                .exchange().expectStatus().isCreated()
                .expectBody(MechanicResponse.class).returnResult().getResponseBody();
        assertNotNull(mechanic);

        OrderResponse order = client.post().uri("/api/orders")
                .body(new OrderCreateRequest(carId, OrderPriority.NORMAL, LocalDateTime.now().plusDays(1), 60))
                .exchange().expectStatus().isCreated()
                .expectBody(OrderResponse.class).returnResult().getResponseBody();
        assertNotNull(order);

        JobResponse job = client.post().uri("/api/orders/{id}/jobs", order.id())
                .body(new JobRequest("Diagnostics", "Full diagnostics", new BigDecimal("100"),
                        new BigDecimal("200"), MechanicSpecialization.GENERAL))
                .exchange().expectStatus().isCreated()
                .expectBody(JobResponse.class).returnResult().getResponseBody();
        client.post().uri("/api/orders/{id}/approve", order.id())
                .exchange().expectStatus().isOk();
        client.post().uri("/api/orders/{id}/start", order.id())
                .body(new StartOrderRequest(mechanic.id()))
                .exchange().expectStatus().isOk();
        client.post().uri("/api/orders/{orderId}/jobs/{jobId}/start", order.id(), job.id())
                .exchange().expectStatus().isOk();
        client.post().uri("/api/orders/{orderId}/jobs/{jobId}/complete", order.id(), job.id())
                .exchange().expectStatus().isOk();
        OrderResponse completed = client.post().uri("/api/orders/{id}/complete", order.id())
                .exchange().expectStatus().isOk()
                .expectBody(OrderResponse.class).returnResult().getResponseBody();

        assertNotNull(completed);
        assertEquals("COMPLETED", completed.status().name());
        assertNotNull(completed.finalCommercialPrice());
    }

    @Test
    void returnsProblemDetailForInvalidRequest() {
        client.post().uri("/api/clients")
                .body(new ClientCreateRequest("", ""))
                .exchange().expectStatus().isBadRequest()
                .expectBody().jsonPath("$.title").isEqualTo("Validation failed")
                .jsonPath("$.fieldErrors.name").exists();
    }

    @Test
    void rejectsAssigningBusyMechanicWithProblemDetail() {
        ClientResponse owner = client.post().uri("/api/clients")
                .body(new ClientCreateRequest("Busy test client", "+380" + System.nanoTime()))
                .exchange().expectStatus().isCreated()
                .expectBody(ClientResponse.class).returnResult().getResponseBody();
        CarResponse car = client.post().uri("/api/cars")
                .body(new CarCreateRequest("BUSY" + System.nanoTime(), "Honda", "Civic", owner.id()))
                .exchange().expectStatus().isCreated()
                .expectBody(CarResponse.class).returnResult().getResponseBody();
        MechanicResponse mechanic = client.post().uri("/api/mechanics")
                .body(new MechanicCreateRequest("Busy mechanic", MechanicSpecialization.GENERAL))
                .exchange().expectStatus().isCreated()
                .expectBody(MechanicResponse.class).returnResult().getResponseBody();

        OrderResponse first = createApprovedOrder(car.id(), "First order");
        OrderResponse second = createApprovedOrder(car.id(), "Second order");

        client.post().uri("/api/orders/{id}/start", first.id())
                .body(new StartOrderRequest(mechanic.id()))
                .exchange().expectStatus().isOk();

        client.post().uri("/api/orders/{id}/start", second.id())
                .body(new StartOrderRequest(mechanic.id()))
                .exchange().expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.title").isEqualTo("Business rule violation")
                .jsonPath("$.detail").exists();
    }

    private OrderResponse createApprovedOrder(final Long carId, final String suffix) {
        OrderResponse order = client.post().uri("/api/orders")
                .body(new OrderCreateRequest(carId, OrderPriority.NORMAL,
                        LocalDateTime.now().plusDays(1), 60))
                .exchange().expectStatus().isCreated()
                .expectBody(OrderResponse.class).returnResult().getResponseBody();
        client.post().uri("/api/orders/{id}/jobs", order.id())
                .body(new JobRequest(suffix, "E2E job", new BigDecimal("50"),
                        new BigDecimal("50"), MechanicSpecialization.GENERAL))
                .exchange().expectStatus().isCreated();
        return client.post().uri("/api/orders/{id}/approve", order.id())
                .exchange().expectStatus().isOk()
                .expectBody(OrderResponse.class).returnResult().getResponseBody();
    }
}
