package carservice.integration;

import carservice.repository.ClientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiMockMvcIT extends IntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(
                (org.springframework.web.context.WebApplicationContext) applicationContext).build();
    }

    @AfterEach
    void cleanUp() {
        clientRepository.deleteAll();
    }

    @Test
    void fullWebRequestCreatesClientAndPersistsIt() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Web Client\",\"phone\":\"+380501112233\"}"))
                .andExpect(status().isCreated());

        org.junit.jupiter.api.Assertions.assertEquals(1, clientRepository.count());
    }
}
