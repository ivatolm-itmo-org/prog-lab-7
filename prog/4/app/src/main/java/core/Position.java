package core;

public class Position {
    private double x, y, z;

    public Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void shift(double shiftX, double shiftY, double shiftZ) {
        this.x += shiftX;
        this.y += shiftY;
        this.z += shiftZ;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public String toString() {
        return Double.toString(this.x) + ", " +
               Double.toString(this.y) + ", " +
               Double.toString(this.z);
    }
}
