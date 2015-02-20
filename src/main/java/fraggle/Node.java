package fraggle;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Node
{
    Vector2i pos, size;
    Vector2i moveStart;

    @XStreamAsAttribute
    int id;
    @XStreamAsAttribute
    int renderSegmentId;
    @XStreamAsAttribute
    boolean isSink;
    @XStreamAsAttribute
    RenderSegmentType type;

    @XStreamOmitField
    boolean isSelected;
    @XStreamOmitField
    boolean invalidDropPos;
    @XStreamOmitField
    boolean isMoving;
    @XStreamOmitField
    RenderSegment renderSegment;

    public Node(RenderSegmentType type, Vector2i pos, RenderSegment renderSegment) throws Exception {
        this.isSink = type == RenderSegmentType.SINK;
        this.isSelected = false;
        this.type = type;
        this.pos = pos;
        this.size = new Vector2i(3, 2);
        this.id = Main.instance.nextNodeId();
        this.renderSegment = renderSegment;
        this.renderSegmentId = renderSegment.id;
    }

    Object getProperty(String key) {
        return renderSegment.getProperties().getOrDefault(key, null);
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
