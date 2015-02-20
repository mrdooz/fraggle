package fraggle;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class NodeGrid
{
    Vector2i size;

    // Note, we don't store the node data, as this can be trivially recreated by just
    // looping over the nodes and adding them
    @XStreamOmitField
    int[][] data;

    public NodeGrid(int x, int y) {
        this.size = new Vector2i(x, y);
        data = new int[y][x];
        for (int i = 0; i < y; ++i) {
            for (int j = 0; j < x; ++j) {
                data[i][j] = -1;
            }
        }
    }

    public Node nodeAtPos(int x, int y) {
        int id = data[y][x];
        return id == -1 ? null : Main.instance.state.nodes.get(id);
    }

    public Node nodeAtPos(Vector2i pos) {
        return nodeAtPos(pos.x, pos.y);
    }

    public boolean isEmpty(Node node)
    {
        return isEmpty(node.pos.x, node.pos.y, node.size.x, node.size.y);
    }

    public boolean isEmpty(int x, int y, int w, int h) {
        // TODO: bounds check
        for (int i = y; i < y + h; ++i) {
            for (int j = x; j < x + w; ++j) {
                if (data[i][j] != -1) {
                    return false;
                }
            }
        }
        return true;
    }

    public void addNode(Node node) {
        addNode(node.pos.x, node.pos.y, node.size.x, node.size.y, node.id);
    }

    public void eraseNode(Node node) {
        addNode(node.pos.x, node.pos.y, node.size.x, node.size.y, -1);
    }

    public void addNode(int x, int y, int w, int h, int id) {
        for (int i = y; i < y + h; ++i) {
            for (int j = x; j < x + w; ++j) {
                assert(data[i][j] == -1);
                data[i][j] = id;
            }
        }
    }

    public void eraseNode(int x, int y, int w, int h) {
        addNode(x, y, w, h, -1);
    }
}
