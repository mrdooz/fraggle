package fraggle;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Map;

public class Node
{
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

    public Node(String type, Vector2i pos) {
        this.isSink = false;
        this.isDefaultRenderTarget = false;
        this.isSelected = false;
        this.type = type;
        this.pos = pos;
        this.size = new Vector2i(3, 2);
        this.id = nextNodeId++;
    }

    Color getNodeColor() {
        if (isMoving) {
            if (invalidDropPos) {
                return new Color(0.6, 0.2, 0.2, 0.5);
            } else {
                return new Color(0.2, 0.6, 0.2, 0.5);
            }
        } else {
            if (isSelected) {
                return new Color(0.6, 0.6, 0.2, 1);
            } else {
                return new Color(0.2, 0.6, 0.2, 1);
            }
        }
    }

    public void Draw(GraphicsContext gc) {
        gc.setStroke(new Color(0.1, 0.1, 0.1, 1));
        gc.setFill(getNodeColor());

        gc.fillRect(pos.x * Settings.GRID_SIZE, pos.y * Settings.GRID_SIZE, size.x * Settings.GRID_SIZE, size.y * Settings.GRID_SIZE);
        gc.strokeRect(pos.x * Settings.GRID_SIZE, pos.y * Settings.GRID_SIZE, size.x * Settings.GRID_SIZE, size.y * Settings.GRID_SIZE);
    }
}
