package fraggle;

import org.controlsfx.control.PropertySheet;

import java.util.Map;

public class CustomPropertyItem implements PropertySheet.Item {

    private String name;
    Class<?> clazz;
    Map<String, NodeProperty> property;

    public CustomPropertyItem(String name, Map<String, NodeProperty> property, Class<?> clazz) {
        this.name = name;
        this.clazz = clazz;
        this.property = property;
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
        return property.get(name).value;
    }

    @Override
    public void setValue(Object value) {
        property.get(name).value = value;
    }
}
