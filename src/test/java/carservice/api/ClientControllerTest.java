package carservice.api;

import carservice.api.dto.ClientResponse;
import carservice.domain.Client;
import carservice.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClientService service;

    @Mock
    private ApiMapper mapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ClientController(service, mapper))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void rejectsInvalidClientRequestWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"phone\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists());
    }

    @Test
    void createsClientAndReturns201() throws Exception {
        Client client = new Client("Olena", "+380971112233");
        ClientResponse response = new ClientResponse(1L, "Olena", "+380971112233");
        when(service.create(any(), any())).thenReturn(client);
        when(mapper.toResponse(client)).thenReturn(response);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Olena\",\"phone\":\"+380971112233\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/clients/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Olena"));
    }
}
