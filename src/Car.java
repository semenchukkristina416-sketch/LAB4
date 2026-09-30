public class Car {
    private  String vin;
    private  String brand;
    private  String model;
    private int year;
    private Client owner;

    public Car(final String vin, final String brand, final String model, final int year, final Client owner) {
        if (vin == null || vin.isBlank())
            throw new DomainException("VIN cannot be empty");
        if (brand == null || brand.isBlank())
            throw new DomainException("Brand cannot be empty");
        if (model == null || model.isBlank())
            throw new DomainException("Model cannot be empty");
        if (year < 1886)
            throw new DomainException("Invalid manufacture year");
        if (owner == null)
            throw new DomainException("Owner cannot be null");

        this.vin = vin;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.owner = owner;
    }

    public final String getVin() {
        return vin;
    }

    public final String getBrand() {
        return brand;
    }

    public final String getModel() {
        return model;
    }

    public final int getYear() {
        return year;
    }

    public final Client getOwner() {
        return owner;
    }

}
