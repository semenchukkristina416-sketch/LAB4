package carservice.domain;

import carservice.exception.DomainException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServicePostTest {

    @Test
    void shouldCreateServicePostSuccessfully() {
        ServicePost post = new ServicePost("1", "Box A");

        assertEquals("1", post.getId());
        assertEquals("Box A", post.getName());
        assertTrue(post.isAvailable());
    }

    @Test
    void shouldThrowExceptionWhenIdIsBlank() {
        assertThrows(DomainException.class, () -> new ServicePost("", "Box A"));
        assertThrows(DomainException.class, () -> new ServicePost(null, "Box A"));
        assertThrows(DomainException.class, () -> new ServicePost("   ", "Box A"));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(DomainException.class, () -> new ServicePost("1", ""));
        assertThrows(DomainException.class, () -> new ServicePost("1", null));
        assertThrows(DomainException.class, () -> new ServicePost("1", "   "));
    }

    @Test
    void shouldOccupyAvailablePostSuccessfully() {
        ServicePost post = new ServicePost("1", "Box A");
        post.occupy();

        assertFalse(post.isAvailable());
    }

    @Test
    void shouldThrowExceptionWhenOccupyingAlreadyOccupiedPost() {
        ServicePost post = new ServicePost("1", "Box A");
        post.occupy();

        assertThrows(DomainException.class, post::occupy);
    }

    @Test
    void shouldReleaseOccupiedPostSuccessfully() {
        ServicePost post = new ServicePost("1", "Box A");
        post.occupy();
        post.release();

        assertTrue(post.isAvailable());
    }
}
