public class DataPoint {
    private int id;
    private int temperature;
    private int sale;

    public DataPoint(int id, int temperature, int sale) {
        this.id = id;
        this.temperature = temperature;
        this.sale = sale;
    }

    // Getters & Setters
    public int getId() { return id; }
    public int getTemperature() { return temperature; }
    public int getSale() { return sale; }
}
