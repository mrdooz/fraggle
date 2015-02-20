package fraggle;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Vector2i {
    @XStreamAsAttribute
    int x;
    @XStreamAsAttribute
    int y;

    Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    void add(Vector2i v) { this.x += v.x; this.y += v.y; }
    void sub(Vector2i v) { this.x -= v.x; this.y -= v.y; }

    static Vector2i sub(Vector2i a, Vector2i b) { return new Vector2i(a.x - b.x, a.y - b.y); }
    static Vector2i add(Vector2i a, Vector2i b) { return new Vector2i(a.x + b.x, a.y + b.y); }

    @Override
    public String toString() {
        return String.format("x: %d, y: %d", x, y);
    }

    static Vector2i ZERO = new Vector2i(0,0);
    static Vector2i ONE = new Vector2i(1,1);
}
