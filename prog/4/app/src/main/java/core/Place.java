package core;

public enum Place {
    HOME (new Position(0, 0, 0), "Home"),
    CARGO_COMPANY_HEADQUATERS (new Position(1, 1, 0), "Cargo company headquaters"),
    TABLE(new Position(1, 0, 0), "Kitchen table"),
    CHAIR_1(new Position(1, 0, 1), "Kitchen table"),
    CHAIR_2(new Position(1, 0, 1.5), "Kitchen table"),
    ELLIE_BED(new Position(1, 0, 1.5), "Ellie bed"),
    GEDJ_BED(new Position(1, 0, 1.5), "Gedj bed"),
    CHERCH_BED(new Position(1, 0, 1.5), "Cherch bed"),
    CHILDREN_ROOM(new Room(
            new Position(1, 0, 1.5),
            new Position(1, 0, 1.5),
            new Position(1, 0, 1.5),
            new Position(1, 0, 1.5)
        ),
        "Children room"),
    ;

    private Area area;
    private String name;

    Place(Area area, String name) {
        this.area = area;
        this.name = name;
    }

    Place(Position position, String name) {
        this.area = new Area(position);
        this.name = name;
    }

    public Area getArea() {
        return this.area;
    }

    public Position getPosition() {
        return this.area.getCenterPosition();
    }

    public String getName() {
        return this.name;
    }
}
