package fraggle;

import org.controlsfx.control.PropertySheet;

import java.util.Map;

public class CustomPropertyItem implements PropertySheet.Item {
    private String key;
    private String name;

    Class<?> clazz;
    Map<String, Object> obj;

    public CustomPropertyItem(String key, Map<String, Object> obj, Class<?> clazz) {
        this.key = key;
        this.name = key;
        this.clazz = clazz;
        this.obj = obj;
    }

    @Override
    public Class<?> getType() {
        return clazz;
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Object getValue() {
        return obj.get(key);
    }

    @Override
    public void setValue(Object value) {
        obj.put(key, value);
    }
}
