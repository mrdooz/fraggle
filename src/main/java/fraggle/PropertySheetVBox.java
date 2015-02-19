package fraggle;

import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.List;

public class PropertySheetVBox extends VBox {

    interface getSinksDelegate {
        List<String> apply();
    }

    // this thing is kinda horrible and ass backwards, so it should probably be changed..
    public PropertySheetVBox(PropertySheet propertySheet, getSinksDelegate getSinks) {

        propertySheet.setModeSwitcherVisible(false);
        propertySheet.setSearchBoxVisible(false);
        VBox.setVgrow(propertySheet, Priority.ALWAYS);
        getChildren().add(propertySheet);

        propertySheet.setPropertyEditorFactory(param -> {
            CustomPropertyItem item = (CustomPropertyItem)param;
            if (item.clazz == SourceInput.class) {
                return Editors.createChoiceEditor(param, getSinks.apply());
            } else if (item.clazz == String[].class) {
                return Editors.createChoiceEditor(param, null);
            } else if (item.clazz == Boolean.class) {
                return Editors.createCheckEditor(param);
            } else if (item.clazz == Integer.class) {
                return Editors.createNumericEditor(param);
            } else {
                return Editors.createTextEditor(param);
            }
        });
    }
}
