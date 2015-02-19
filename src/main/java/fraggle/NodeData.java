package fraggle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;

import java.util.HashMap;
import java.util.Map;

// TODO: So, this is probably something I want to read from config or generate

class NodeProperty {
    String name;
    Class<?> type;
    Object value;

    public NodeProperty(Class<?> type, Object value) {
        this.type = type;
        this.value = value;
    }
}

enum RenderSegmentType {
  UNKNOWN, SINK, SOURCE, PARTICLE,
};

abstract class RenderSegment {
    RenderSegment(RenderSegmentType type) {
        this.type = type;
    }

    RenderSegmentType getType() {
        return type;
    }

    Map<String, NodeProperty> getProperties() {
        return properties;
    }

    Map<String, NodeProperty> properties = new HashMap<>();
    RenderSegmentType type;
}

class RenderSegmentSink extends RenderSegment {
    public RenderSegmentSink() {
        super(RenderSegmentType.SINK);
        properties.put("name", new NodeProperty(String.class, "sink1"));
        properties.put("floating point", new NodeProperty(boolean.class, false));
    }
}

class SourceInput {
    String name;
}

class RenderSegmentSource extends RenderSegment {

    RenderSegmentSource() {
        super(RenderSegmentType.SOURCE);
        properties.put("name", new NodeProperty(String.class, "source1"));
        properties.put("input", new NodeProperty(SourceInput.class, "input"));
    }
}

class RenderSegmentParticle extends RenderSegment {

    RenderSegmentParticle() {
        super(RenderSegmentType.PARTICLE);
        properties.put("name", new NodeProperty(String.class, "particle1"));
    }
}

    class RenderSegmentFactory
    {
        public static RenderSegment Create(RenderSegmentType type) {
            switch (type) {
                case SINK: return new RenderSegmentSink();
                case SOURCE: return new RenderSegmentSource();
                case PARTICLE: return new RenderSegmentParticle();
            }
            return null;
        }
    }

public class NodeData {

    public static ObservableList<PropertySheet.Item> listFromPropertyMap(Map<String, NodeProperty> properties) {
        ObservableList<PropertySheet.Item> list = FXCollections.observableArrayList();
        for (String key : properties.keySet()) {
            list.add(new CustomPropertyItem(key, properties, properties.get(key).type));
        }
        return list;
    }
}
