package fraggle;

public class PosSize
{
    // Note, both pos and size are in grid units, not pixels
    Vector2i pos;
    Vector2i size;

    public PosSize(Vector2i pos, Vector2i size) {
        this.pos = pos;
        this.size = size;
    }
}
