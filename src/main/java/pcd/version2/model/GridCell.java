package pcd.version2.model;

public record GridCell(int x, int y) {

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }
}

