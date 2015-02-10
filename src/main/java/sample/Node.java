package sample;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Node
{
    static int nextNodeId = 1;
    public Node(Vector2i pos) {
        isSink = false;
        isDefaultRenderTarget = false;
        isSelected = false;
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

    Vector2i pos, size;
    Vector2i moveStart;

    int id;
    boolean isSink;
    boolean isDefaultRenderTarget;
    boolean isSelected;
    boolean invalidDropPos;
    boolean isMoving;
}
