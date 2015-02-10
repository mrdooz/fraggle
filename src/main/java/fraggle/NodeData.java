package fraggle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.PropertySheet;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// TODO: So, this is probably something I want to read from config or generate

public class NodeData {

    // TODO: I probably want some kind of grouping here..
    public static String[] NODE_TYPES = { "particle", "intro_text", "intro_lines", "sink" };
    public static Map<String, ObservableList<PropertySheet.Item>> NODE_PROPERTIES = new HashMap<>();

    static {
        // add the node properties
        {
            Map<String, Object> sinkDataMap = new LinkedHashMap<>();
            sinkDataMap.put("name", "sink1");
            sinkDataMap.put("floating point", false);
            sinkDataMap.put("default swap chain", true);
            NODE_PROPERTIES.put("sink", listFromItems(sinkDataMap));
        }

        {
            Map<String, Object> sinkDataMap = new LinkedHashMap<>();
            sinkDataMap.put("name", "particle1");
            NODE_PROPERTIES.put("particle", listFromItems(sinkDataMap));
        }

        {
            Map<String, Object> sinkDataMap = new LinkedHashMap<>();
            sinkDataMap.put("name", "intro_text");
            NODE_PROPERTIES.put("intro_text", listFromItems(sinkDataMap));
        }

        {
            Map<String, Object> sinkDataMap = new LinkedHashMap<>();
            sinkDataMap.put("name", "intro_lines");
            NODE_PROPERTIES.put("intro_lines", listFromItems(sinkDataMap));
        }
    }


    public enum Day {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY,
        THURSDAY, FRIDAY, SATURDAY
    }

    static ObservableList<PropertySheet.Item> listFromItems(Map<String, Object> items) {
        ObservableList<PropertySheet.Item> list = FXCollections.observableArrayList();
        for (String key : items.keySet())
            list.add(new CustomPropertyItem(key, items, items.get(key).getClass()));
        return list;
    }


    private Map<String, Object> sinkDataMap = new LinkedHashMap<>();
    {
        sinkDataMap.put("name", "rt1");
        sinkDataMap.put("float point", false);
    }
}
