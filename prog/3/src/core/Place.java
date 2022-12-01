package core;
public enum Place {
    HOME (new Position(0, 0, 0), "Home"),
    CARGO_COMPANY_HEADQUATERS (new Position(1, 1, 0), "Cargo company headquaters"),
    TABLE(new Position(1, 0, 0), "Kitchen table"),
    CHAIR_1(new Position(1, 0, 1), "Kitchen table"),
    CHAIR_2(new Position(1, 0, 1.5), "Kitchen table"),
    ELLIE_BED(new Position(1, 0, 1.5), "Ellie bed"),
    GEDJ_BED(new Position(1, 0, 1.5), "Gedj bed");

    private Position position;
    private String name;

    Place(Position position, String name) {
        this.position = position;
        this.name = name;
    }

    public Position getPosition() {
        return this.position;
    }

    public String getName() {
        return this.name;
    }
}
