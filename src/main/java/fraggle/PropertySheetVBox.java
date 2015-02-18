package fraggle;

import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PropertySheet;

public class PropertySheetVBox extends VBox {

    // this thing is kinda horrible and ass backwards, so it should probably be changed..
    public PropertySheetVBox(PropertySheet propertySheet) {
        propertySheet.setModeSwitcherVisible(false);
        propertySheet.setSearchBoxVisible(false);
        VBox.setVgrow(propertySheet, Priority.ALWAYS);
        getChildren().add(propertySheet);
    }
}
