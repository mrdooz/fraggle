package sample;

import com.sun.webkit.dom.KeyboardEventImpl;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.util.*;

public class Main extends Application {

    Group root;
    Scene scene;
    int gridSize = 20;
    Vector2i curTile = new Vector2i(-1, -1);
    boolean drawGrid = true;
    Map<Integer, Node> nodes = new HashMap<>();
    Set<Integer> selectedNodes = new HashSet<>();
    int nextNodeId = 1;

    class Node
    {
        public Node(Vector2i pos) {
            isSink = false;
            isDefaultRenderTarget = false;
            isSelected = false;
            this.pos = pos;
            this.size = new Vector2i(3, 2);
            this.id = nextNodeId++;
            //this.rect = new Rectangle(pos.x * gridSize, pos.y * gridSize, 3 * gridSize, 2 * gridSize);
            //root.getChildren().add(this.rect);
        }

        public void Draw(GraphicsContext gc) {
            gc.setStroke(new Color(0.1, 0.1, 0.1, 1));

            if (isSelected) {
                gc.setFill(new Color(0.6, 0.6, 0.2, 1));
            } else {
                gc.setFill(new Color(0.2, 0.6, 0.2, 1));
            }
            gc.fillRect(pos.x * gridSize, pos.y * gridSize, size.x * gridSize, size.y * gridSize);
            gc.strokeRect(pos.x * gridSize, pos.y * gridSize, size.x * gridSize, size.y * gridSize);
        }

        // Note, both pos and size are in grid units, not pixels
        Vector2i pos, size;

        int id;
        boolean isSink;
        boolean isDefaultRenderTarget;
        boolean isSelected;
        Rectangle rect;
    }

    class NodeGrid
    {
        public NodeGrid(int x, int y) {
            data = new int[y][x];
            for (int i = 0; i < y; ++i) {
                for (int j = 0; j < x; ++j) {
                    data[i][j] = -1;
                }
            }
        }

        public Node nodeAtPos(Vector2i pos) {
            int x = pos.x;
            int y = pos.y;
            int id = data[y][x];
            return id == -1 ? null : nodes.get(id);
        }

        public boolean isEmpty(Node node)
        {
            return isEmpty(node.pos.x, node.pos.y, node.size.x, node.size.y);
        }

        public boolean isEmpty(int x, int y, int w, int h) {
            // TODO: bounds check
            for (int i = y; i < y + h; ++i) {
                for (int j = x; j < x + w; ++j) {
                    if (data[i][j] != -1) {
                        return false;
                    }
                }
            }

            return true;
        }

        public void addNode(Node node) {
            addNode(node.pos.x, node.pos.y, node.size.x, node.size.y, node.id);
        }

        public void addNode(int x, int y, int w, int h, int id) {
            for (int i = y; i < y + h; ++i) {
                for (int j = x; j < x + w; ++j) {
                    assert(data[i][j] == -1);
                    data[i][j] = id;
                }
            }
        }

        public void eraseNode(Node node) {
            eraseNode(node.pos.x, node.pos.y, node.size.x, node.size.y);
        }

        public void eraseNode(int x, int y, int w, int h) {
            for (int i = y; i < y + h; ++i) {
                for (int j = x; j < x + w; ++j) {
                    data[i][j] = -1;
                }
            }
        }

        int[][] data;
    }

    NodeGrid nodeGrid;

    Tab createMapTab(String name) {
        Tab tab = new Tab(name);
        ScrollPane pane = new ScrollPane();
        makeMain(pane);
        tab.setContent(pane);
        return tab;
    }

    Vector2i snappedPos(double x, double y) {
        return new Vector2i(gridSize * ((int)x / gridSize), gridSize * ((int)y / gridSize));
    }

    Vector2i gridPos(double x, double y) {
        return new Vector2i((int)x / gridSize, (int)y / gridSize);
    }

    void clearSelectedNodes() {
        for (int id : selectedNodes) {
            Node n = nodes.getOrDefault(id, null);
            if (n != null) {
                n.isSelected = false;
            }
        }
        selectedNodes.clear();
    }

    void toggleSelectedNode(Node node) {

        if (node.isSelected) {
            selectedNodes.remove(node.id);
        } else {
            selectedNodes.add(node.id);
        }
        node.isSelected = !node.isSelected;
    }


    void makeMain(ScrollPane pane)
    {
        int w = 1024;
        int h = 768;
        Canvas canvas = new Canvas(w, h);
        nodeGrid = new NodeGrid(w / gridSize, h / gridSize);
        pane.setContent(canvas);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        double zoom = 1;
        gc.scale(zoom, zoom);

        scene.setOnKeyReleased(keyEvent -> {
            switch (keyEvent.getCode()) {

                case ESCAPE:
                    clearSelectedNodes();
                    break;

                case DELETE:
                    for (int id : selectedNodes) {
                        Node n = nodes.getOrDefault(id, null);
                        if (n != null) {
                            nodeGrid.eraseNode(n);
                            nodes.remove(id);
                            n = null;
                        }
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
                    int a = 10;
                });

        canvas.addEventHandler(MouseEvent.DRAG_DETECTED,
                mouseEvent -> {
                    int a = 10;
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                mouseEvent -> {
                    int a = 10;
                });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                mouseEvent -> {
                    Vector2i v = gridPos(mouseEvent.getX(), mouseEvent.getY());

                    Node hit = nodeGrid.nodeAtPos(v);
                    if (hit != null) {
                        // If ctrl is pressed, then add/remove the node from the selected set; otherwise just clear
                        // the selected set, and insert the node
                        if (!mouseEvent.isControlDown()) {
                            clearSelectedNodes();
                        }
                        toggleSelectedNode(hit);
                    } else {

                        clearSelectedNodes();

                        // No node was hit, so create a new one
                        Node node = new Node(v);
                        if (nodeGrid.isEmpty(node)) {
                            nodeGrid.addNode(node);
                            nodes.put(node.id, node);
                        }
                    }
//                    MouseButton btn = mouseEvent.getButton();
//                    switch (btn) {
//                        case PRIMARY: editor.applyBrush(mousePos); break;
//                        case SECONDARY: editor.deleteTile(mousePos); break;
//                    }
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

                    for (int y = 0; y < h / gridSize + 1; ++y) {
                        for (int x = 0; x < w / gridSize + 1; ++x) {
                            gc.strokeRect(x*gridSize, y*gridSize, gridSize, gridSize);
                        }
                    }

                    if (curTile.x != -1) {
                        gc.setStroke(new Color(0.1, 0.1, 0.9, 1));
                        gc.strokeRect(curTile.x*gridSize, curTile.y*gridSize, gridSize, gridSize);
                    }

                    for (Node n : nodes.values()) {
                        n.Draw(gc);
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
        HBox hbox = new HBox();
        Button btnSave = new Button("save");
        Button btnNewMap = new Button("new map");
        btnNewMap.setOnAction(event -> {
            System.out.println("new map");
        });
        hbox.getChildren().addAll(btnSave, btnNewMap);
        border.setTop(hbox);

        SplitPane split = new SplitPane();
        split.prefWidthProperty().bind(scene.widthProperty());
        split.prefHeightProperty().bind(scene.heightProperty());

        split.setOrientation(Orientation.HORIZONTAL);

        SplitPane propertySplitPane = new SplitPane();
        propertySplitPane.setOrientation(Orientation.HORIZONTAL);

        TabPane layerPane = new TabPane();
        Tab minimapTab = new Tab("Mini-map");
        Tab objectsTab = new Tab("Objects");
        layerPane.getTabs().addAll(minimapTab, objectsTab);

        TabPane tilesetPane = new TabPane();
        tilesetPane.getTabs().addAll(createMapTab("tjong"));
        tilesetPane.setPrefWidth(200);
        tilesetPane.setPrefHeight(400);

        SplitPane rightSplit = new SplitPane();
        rightSplit.setOrientation(Orientation.HORIZONTAL);
        rightSplit.getItems().addAll(tilesetPane, layerPane);

        StackPane right = new StackPane();
        right.getChildren().add(rightSplit);
        split.getItems().addAll(right);
        border.setCenter(split);

        right.setPrefWidth(200);

        root.getChildren().add(border);

        primaryStage.setTitle("Fraggle");
        primaryStage.setScene(scene);
        primaryStage.show();

        if (true) {
//            Tileset t = new Tileset("test", "/Users/dooz/tmp/tmw_desert_spacing.png",
//                    new Vector2i(32, 32), new Vector2i(1, 1), new Vector2i(1, 1));
//            Tileset t = new Tileset("test", "/Users/dooz/tmp/dungeon_sheet_0.png", new Vector2i(16, 16));
//            tilesets.add(t);
//            selectedTileset = t;
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
