public class ServicePost {
    private String id;
    private String name;

    public ServicePost(final String id, final String name) {
        if (id == null || id.isBlank())
            throw new DomainException("Post ID cannot be empty");
        if (name == null || name.isBlank())
            throw new DomainException("Post name cannot be empty");

        this.id = id;
        this.name = name;
    }

    public final String getId() { return id; }
    public final String getName() { return name; }
}
