package core;

class Place extends Area {
    private String name;

    public Place(Point[] points) {
        super(points);
    }

    public Place(Point[] points, String name) {
        super(points);

        this.name = name;
    }

    String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }
}
