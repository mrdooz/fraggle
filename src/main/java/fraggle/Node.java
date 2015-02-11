package fraggle;

import com.rits.cloning.Cloner;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Map;
import java.util.Objects;

public class Node
{
    static Cloner CLONER = new Cloner();

    Vector2i pos, size;
    Vector2i moveStart;

    int id;
    boolean isSink;
    boolean isDefaultRenderTarget;
    boolean isSelected;
    boolean invalidDropPos;
    boolean isMoving;
    String type;
    static int nextNodeId = 1;
    Map<String, Object> properties;

    public Node(String type, Vector2i pos) throws Exception {
        this.isSink = Objects.equals(type, NodeData.SINK_TYPE);
        this.isDefaultRenderTarget = false;
        this.isSelected = false;
        this.type = type;
        this.pos = pos;
        this.size = new Vector2i(3, 2);
        this.id = nextNodeId++;

        Map<String, Object> properties = NodeData.NODE_PROPERTIES.getOrDefault(type, null);
        if (properties == null) {
            throw new Exception("Unknown type: " + type);
        }
        this.properties = CLONER.deepClone(properties);
    }

    Color getNodeColor() {

        Color col;
        if (invalidDropPos) {
            col = new Color(0.6, 0.2, 0.2, 1);
        } else if (isSelected) {
            col = new Color(0.6, 0.6, 0.2, 1);
        } else if (isSink) {
            col = new Color(0.2, 0.2, 0.2, 1);
        } else {
            col = new Color(0.2, 0.6, 0.2, 1);
        }

        return isMoving ? new Color(col.getRed(), col.getGreen(), col.getBlue(), 0.5) : col;
    }

    public void Draw(GraphicsContext gc) {
        gc.setStroke(new Color(0.1, 0.1, 0.1, 1));
        gc.setFill(getNodeColor());

        gc.fillRect(pos.x * Settings.GRID_SIZE, pos.y * Settings.GRID_SIZE, size.x * Settings.GRID_SIZE, size.y * Settings.GRID_SIZE);
        gc.strokeRect(pos.x * Settings.GRID_SIZE, pos.y * Settings.GRID_SIZE, size.x * Settings.GRID_SIZE, size.y * Settings.GRID_SIZE);
    }
}
