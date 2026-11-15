package carservice.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ServicePost {
    private String id;
    private String name;
    private boolean available;

    public ServicePost(final String id, final String name) {
        if (id == null || id.isBlank()) {
            throw new DomainException("Service post ID cannot be empty");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("Service post name cannot be empty");
        }

        this.id = id;
        this.name = name;
        this.available = true;
    }

    public void occupy() {
        if (!available) {
            throw new DomainException("Service post is already occupied");
        }
        this.available = false;
    }

    public void release() {
        this.available = true;
    }
}
