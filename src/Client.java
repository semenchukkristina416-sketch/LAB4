public class Client {
        private String id;
        private String name;
        private String phone;

        public Client(final String id, final String name, final String phone) {

        if (id == null || id.isBlank()) {
        throw new DomainException("Client ID cannot be empty");
    }

        if (name == null || name.isBlank()) {
        throw new DomainException("Client name cannot be empty");
    }

        if (phone == null || phone.isBlank()) {
        throw new DomainException("Client phone cannot be empty");
    }

        this.id = id;
        this.name = name;
        this.phone = phone;
}

public String getId() {
    return id;
}

public String getName() {
    return name;
}

public String getPhone() {
    return phone;
}
}
