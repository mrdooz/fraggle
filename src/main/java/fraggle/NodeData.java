package fraggle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// TODO: So, this is probably something I want to read from config or generate

public class NodeData {

    public static final String SINK_TYPE = "sink";

    // TODO: I probably want some kind of grouping here..
    public static String[] NODE_TYPES = { "particle", "intro_text", "intro_lines", "sink" };
    public static Map<String, ObservableList<PropertySheet.Item>> NODE_PROPERTIES_LIST = new HashMap<>();
    public static Map<String, Map<String, Object>> NODE_PROPERTIES = new HashMap<>();

    static {
        // Add the default node properties
        {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", "sink1");
            data.put("floating point", false);
            data.put("default swap chain", true);
            NODE_PROPERTIES.put("sink", data);
            NODE_PROPERTIES_LIST.put("sink", listFromItems(data));
        }

        {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", "particle1");
            NODE_PROPERTIES.put("particle", data);
            NODE_PROPERTIES_LIST.put("particle", listFromItems(data));
        }

        {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", "intro_text");
            NODE_PROPERTIES.put("intro_text", data);
            NODE_PROPERTIES_LIST.put("intro_text", listFromItems(data));
        }

        {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("name", "intro_lines");
            NODE_PROPERTIES.put("intro_lines", data);
            NODE_PROPERTIES_LIST.put("intro_lines", listFromItems(data));
        }
    }

    public static ObservableList<PropertySheet.Item> listFromItems(Map<String, Object> items) {
        ObservableList<PropertySheet.Item> list = FXCollections.observableArrayList();
        for (String key : items.keySet())
            list.add(new CustomPropertyItem(key, items, items.get(key).getClass()));
        return list;
    }
}
