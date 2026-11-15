package carservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(name = "clients")
@Getter
@ToString
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    protected Client() {
    }


    public Client(final String name, final String phone) {

        if (name == null || name.isBlank()) {
            throw new DomainException("Client name cannot be empty");
        }

        if (phone == null || phone.isBlank()) {
            throw new DomainException("Client phone cannot be empty");
        }

        this.name = name;
        this.phone = phone;
    }
}
