package carservice.domain.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(IntegrationTestConfiguration.class)
@ActiveProfiles("test")
public abstract class IntegrationTest {
}