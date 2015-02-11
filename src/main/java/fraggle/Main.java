package fraggle;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.controlsfx.control.PropertySheet;

import java.util.*;

public class Main extends Application {

    Group root;
    Scene scene;
    Vector2i curTile = new Vector2i(-1, -1);
    boolean drawGrid = true;
    Map<Integer, Node> nodes = new HashMap<>();
    Set<Integer> selectedNodes = new HashSet<>();
    String curNodeType;

    NodeGrid nodeGrid;
    Vector2i dragStart;
    boolean validDrop;
    PropertySheet propertySheet = new PropertySheet();
    ListView<String> nodesListView;

    public class PropertySheetVBox extends VBox {

        public PropertySheetVBox() {
            propertySheet.setModeSwitcherVisible(false);
            propertySheet.setSearchBoxVisible(false);
            VBox.setVgrow(propertySheet, Priority.ALWAYS);
            getChildren().add(propertySheet);
        }
    }

    Tab createMapTab(String name) {
        Tab tab = new Tab(name);
        ScrollPane pane = new ScrollPane();
        makeMain(pane);
        tab.setContent(pane);
        return tab;
    }

    Vector2i gridPos(double x, double y) {
        return new Vector2i((int)x / Settings.GRID_SIZE, (int)y / Settings.GRID_SIZE);
    }

    void clearSelectedNodes() {
        for (Node node : getSelectedNodes()) {
            deselectNode(node);
        }
        selectedNodes.clear();
        propertySheet.getItems().clear();
    }

    void selectNode(Node node) {
        selectedNodes.add(node.id);
        node.isSelected = true;
    }

    void deselectNode(Node node) {
        selectedNodes.remove(node.id);
        node.isSelected = false;
    }

    void toggleSelectedNode(Node node) {
        if (node.isSelected)
            deselectNode(node);
        else
            selectNode(node);
    }

    List<Node> getSelectedNodes() {
        List<Node> res = new ArrayList<>();
        for (int nodeIdx : selectedNodes) {
            Node node = nodes.getOrDefault(nodeIdx, null);
            if (node != null) {
                res.add(node);
            }
        }
        return res;
    }

    void displayProperties(Node node) {
        propertySheet.getItems().setAll(NodeData.listFromItems(node.properties));
    }

    void makeMain(ScrollPane pane)
    {
        int w = 1024;
        int h = 768;
        Canvas canvas = new Canvas(w, h);
        nodeGrid = new NodeGrid(this, w / Settings.GRID_SIZE, h / Settings.GRID_SIZE);
        pane.setContent(canvas);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        double zoom = 1;
        gc.scale(zoom, zoom);

        scene.setOnKeyReleased(keyEvent -> {
            switch (keyEvent.getCode()) {

                case ESCAPE:
                    clearSelectedNodes();
                    curNodeType = null;
                    nodesListView.getSelectionModel().clearSelection();
                    break;

                case DELETE:
                case BACK_SPACE:
                    for (Node node : getSelectedNodes()) {
                        nodes.remove(node.id);
                        nodeGrid.eraseNode(node);
                    }
                    selectedNodes.clear();
                    break;
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED,
                mouseEvent -> {
                    curTile = gridPos(mouseEvent.getX(), mouseEvent.getY());
                });

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                mouseEvent -> {
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                mouseEvent -> {
                    Vector2i dragCur = gridPos(mouseEvent.getX(), mouseEvent.getY());

                    validDrop = true;

                    // if we're above a node, and no nodes are selected, then add the current node to the
                    // selected list
                    Node hitTest = nodeGrid.nodeAtPos(dragCur);
                    if (hitTest != null && selectedNodes.isEmpty()) {
                        selectNode(hitTest);
                    }

                    boolean firstMove = false;
                    if (dragStart == null) {
                        dragStart = dragCur;
                        firstMove = true;
                    }

                    Vector2i diff = Vector2i.sub(dragCur, dragStart);

                    // check that the move is valid before performing it
                    if (!firstMove) {
                        for (Node a : getSelectedNodes()) {
                            for (Node b : nodes.values()) {
                                if (a == b || b.isSelected)
                                    continue;

                                a.invalidDropPos = false;
                                a.isMoving = true;

                                if (Utils.nodeOverlap(new PosSize(a.pos, a.size), new PosSize(b.pos, b.size))) {
                                    a.invalidDropPos = true;
                                    validDrop = false;
                                }
                            }
                        }

                        for (Node node : getSelectedNodes()) {
                            node.pos = Vector2i.add(node.moveStart, diff);
                        }

                    } else {
                        for (Node node : getSelectedNodes()) {
                            node.moveStart = node.pos;
                        }
                    }
                });

        canvas.addEventHandler(MouseEvent.DRAG_DETECTED,
                mouseEvent -> {
                    dragStart = gridPos(mouseEvent.getX(), mouseEvent.getY());
                });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                mouseEvent -> {
                    Vector2i v = gridPos(mouseEvent.getX(), mouseEvent.getY());

                    // check if this is the end of a drag
                    if (dragStart != null) {
                        dragStart = null;
                        for (Node node : getSelectedNodes()) {
                            // if this wasn't a valid drop, then move the node back to its starting position
                            if (!validDrop) {
                                node.pos = node.moveStart;
                            } else {
                                nodeGrid.eraseNode(node.moveStart.x, node.moveStart.y, node.size.x, node.size.y);
                                nodeGrid.addNode(node);
                            }

                            node.invalidDropPos = false;
                            node.isMoving = false;
                            deselectNode(node);
                        }

                        selectedNodes.clear();
                    } else {
                        Node hit = nodeGrid.nodeAtPos(v);
                        if (hit != null) {
                            // If ctrl is pressed, then add/remove the node from the selected set; otherwise just clear
                            // the selected set, and insert the node
                            if (!mouseEvent.isControlDown()) {
                                clearSelectedNodes();
                            }
                            toggleSelectedNode(hit);

                            // If a single item is hit, change the property settings
                            if (selectedNodes.size() == 1) {
                                displayProperties(hit);
                            }

                        } else {

                            clearSelectedNodes();

                            if (curNodeType != null) {
                                // No node was hit, so create a new one
                                try {
                                    Node node = new Node(curNodeType, v);
                                    if (nodeGrid.isEmpty(node)) {
                                        nodeGrid.addNode(node);
                                        nodes.put(node.id, node);
                                    }
                                } catch (Exception e) {
                                    System.out.print(e.getMessage());
                                }
                            }
                        }
                    }
                });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                final GraphicsContext gc = canvas.getGraphicsContext2D();
                int w = (int)canvas.getWidth();
                int h = (int)canvas.getHeight();
                gc.clearRect(0, 0, w, h);
                pane.setPrefSize(w, h);

                if (drawGrid) {
                    double s = 0.8;
                    gc.setStroke(new Color(s, s, s, 1));

                    for (int y = 0; y < h / Settings.GRID_SIZE + 1; ++y) {
                        for (int x = 0; x < w / Settings.GRID_SIZE + 1; ++x) {
                            gc.strokeRect(x* Settings.GRID_SIZE, y* Settings.GRID_SIZE, Settings.GRID_SIZE, Settings.GRID_SIZE);
                        }
                    }

                    // draw the selected nodes in a later pass
                    List<Node> deferred = new ArrayList<>();
                    for (Node n : nodes.values()) {
                        if (n.isMoving || n.isSelected)
                            deferred.add(n);
                        else
                            n.Draw(gc);
                    }

                    for (Node n : deferred) {
                        n.Draw(gc);
                    }

                    if (curTile.x != -1) {
                        gc.setStroke(new Color(0.1, 0.1, 0.9, 1));
                        gc.strokeRect(curTile.x* Settings.GRID_SIZE, curTile.y* Settings.GRID_SIZE, Settings.GRID_SIZE, Settings.GRID_SIZE);
                    }

                }

            }
        }.start();

    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        root = new Group();
        scene = new Scene(root, 1024, 768);

        BorderPane border = new BorderPane();

        SplitPane split = new SplitPane();
        split.prefWidthProperty().bind(scene.widthProperty());
        split.prefHeightProperty().bind(scene.heightProperty());
        split.setOrientation(Orientation.HORIZONTAL);

        SplitPane propertySplitPane = new SplitPane();
        propertySplitPane.setOrientation(Orientation.VERTICAL);

        nodesListView = new ListView<>(FXCollections.observableArrayList(NodeData.NODE_TYPES));
        nodesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            curNodeType = newValue;
        });
        propertySplitPane.getItems().addAll(new PropertySheetVBox(), nodesListView);

        TabPane tilesetPane = new TabPane();
        tilesetPane.getTabs().addAll(createMapTab("tjong"));
        tilesetPane.setPrefWidth(200);
        tilesetPane.setPrefHeight(400);

        SplitPane rightSplit = new SplitPane();
        rightSplit.setOrientation(Orientation.HORIZONTAL);
        rightSplit.getItems().addAll(tilesetPane, propertySplitPane);

        StackPane right = new StackPane();
        right.getChildren().add(rightSplit);
        split.getItems().addAll(right);
        border.setCenter(split);

        right.setPrefWidth(200);

        root.getChildren().add(border);

        primaryStage.setTitle("Fraggle");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
