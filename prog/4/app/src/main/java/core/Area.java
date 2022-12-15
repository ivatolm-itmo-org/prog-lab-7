package core;

import java.util.ArrayList;

public class Area {
    private ArrayList<Position> points;

    public Area(ArrayList<Position> points) {
        this.points = points;
    }

    public Area(Position point) {
        this.points = new ArrayList<>();
        this.points.add(point);
    }

    public ArrayList<Position> getPoints() {
        return points;
    }

    public Position getCenterPosition() {
        double avgX = 0, avgY = 0, avgZ = 0;

        int n = this.points.size();
        for (int i = 0; i < n; i++) {
            Position point = this.points.get(i);
            avgX += point.getX() / n;
            avgY += point.getY() / n;
            avgZ += point.getZ() / n;
        }

        return new Position(avgX, avgY, avgZ);
    }

    public String toString() {
        String result = "";

        for (int i = 0; i < this.points.size(); i++) {
            result += this.points.get(i);
            if (i < this.points.size() - 1) {
                result += "\n";
            }
        }

        return result;
    }
}
