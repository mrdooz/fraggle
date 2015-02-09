package sample;

public class Vector2i {
    int x;
    int y;

    Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Vector2i add(int x, int y) {
        return new Vector2i(this.x + x, this.y + y);
    }

    Vector2i add(Vector2i v) {
        return new Vector2i(this.x + v.x, this.y + v.y);
    }

    @Override
    public String toString() {
        return String.format("x: %d, y: %d", x, y);
    }

    static Vector2i ZERO = new Vector2i(0,0);
    static Vector2i ONE = new Vector2i(1,1);
}
