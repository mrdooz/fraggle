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

    public NodeProperty(String name, Class<?> type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
}

enum RenderSegmentType {
  SINK, SOURCE, PARTICLE,
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

class RenderSegmentSource extends RenderSegment {

    RenderSegmentSource() {
        super(RenderSegmentType.SOURCE);
        properties.put("name", new NodeProperty(String.class, "source1"));
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

    // todo: can i enum these guys?
    public static final String SINK_TYPE = "sink";
    public static final String SOURCE_TYPE = "source";

    // TODO: I probably want some kind of grouping here..
    public static String[] NODE_TYPES = { SOURCE_TYPE, "particle", "intro_text", "intro_lines", SINK_TYPE };
    public static Map<String, Map<String, NodeProperty>> NODE_PROPERTIES = new HashMap<>();


    /*
        workflow
        - write render segment config. one per segment
        - output java class?
     */

    public static void Init() {
        // Add the default node properties
        {
            Map<String, NodeProperty> props = new HashMap<>();
            props.put("name", new NodeProperty(String.class, "sink1"));
            props.put("floating point", new NodeProperty(boolean.class, false));
            NODE_PROPERTIES.put(SINK_TYPE, props);
        }

        {
            Map<String, NodeProperty> props = new HashMap<>();
            props.put("name", new NodeProperty(String.class, "source1"));
            // TODO: make drop down
            props.put("input", new NodeProperty(String.class, "sink1"));
            props.put("floating point", new NodeProperty(boolean.class, false));
            NODE_PROPERTIES.put(SOURCE_TYPE, props);
        }

        {
            Map<String, NodeProperty> props = new HashMap<>();
            props.put("name", new NodeProperty(String.class, "sink1"));
            NODE_PROPERTIES.put("particle", props);
        }

        {
            Map<String, NodeProperty> props = new HashMap<>();
            props.put("name", new NodeProperty(String.class, "sink1"));
            NODE_PROPERTIES.put("intro_text", props);
        }

        {
            Map<String, NodeProperty> props = new HashMap<>();
            props.put("name", new NodeProperty(String.class, "sink1"));
            NODE_PROPERTIES.put("intro_lines", props);
        }
    }

    public static ObservableList<PropertySheet.Item> listFromPropertyMap(Map<String, NodeProperty> properties) {
        ObservableList<PropertySheet.Item> list = FXCollections.observableArrayList();
        for (String key : properties.keySet()) {
            list.add(new CustomPropertyItem(key, properties, properties.get(key).type));
        }
        return list;
    }
}
