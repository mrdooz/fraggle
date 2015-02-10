package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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

    class PosSize
    {
        // Note, both pos and size are in grid units, not pixels
        Vector2i pos;
        Vector2i size;

        public PosSize(Vector2i pos, Vector2i size) {
            this.pos = pos;
            this.size = size;
        }
    }

    class Node
    {
        public Node(Vector2i pos) {
            isSink = false;
            isDefaultRenderTarget = false;
            isSelected = false;
            this.pos = pos;
            this.size = new Vector2i(3, 2);
            this.id = nextNodeId++;
        }

        Color getNodeColor() {
            if (isMoving) {
                if (invalidDropPos) {
                    return new Color(0.6, 0.2, 0.2, 1);
                } else {
                    return new Color(0.2, 0.2, 0.6, 1);
                }
            } else {
                if (isSelected) {
                    return new Color(0.6, 0.6, 0.2, 1);
                } else {
                    return new Color(0.2, 0.6, 0.2, 1);
                }
            }
        }

        public void Draw(GraphicsContext gc) {
            gc.setStroke(new Color(0.1, 0.1, 0.1, 1));
            gc.setFill(getNodeColor());

            gc.fillRect(pos.x * gridSize, pos.y * gridSize, size.x * gridSize, size.y * gridSize);
            gc.strokeRect(pos.x * gridSize, pos.y * gridSize, size.x * gridSize, size.y * gridSize);
        }

        Vector2i pos, size;
        Vector2i moveStart;

        int id;
        boolean isSink;
        boolean isDefaultRenderTarget;
        boolean isSelected;
        boolean invalidDropPos;
        boolean isMoving;
    }

    boolean nodeOverlap(PosSize lhs, PosSize rhs) {
        PosSize mn = lhs.pos.x < rhs.pos.x ? lhs : rhs;
        PosSize mx = lhs.pos.x < rhs.pos.x ? rhs : lhs;
        if (mn.pos.x <= mx.pos.x && mn.pos.x + mn.size.x > mx.pos.x) {

            mn = lhs.pos.y < rhs.pos.y ? lhs : rhs;
            mx = lhs.pos.y < rhs.pos.y ? rhs : lhs;
            return mn.pos.y <= mx.pos.y && mn.pos.y + mn.size.y > mx.pos.y;
        }
        return false;
    }

    public class NodeGrid
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
            addNode(node.pos.x, node.pos.y, node.size.x, node.size.y, -1);
        }

        public void eraseNode(int x, int y, int w, int h) {
            addNode(x, y, w, h, -1);
        }

        int[][] data;
    }

    NodeGrid nodeGrid;
    Vector2i dragStart;
    boolean validDrop;

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
        for (Node node : getSelectedNodes()) {
            deselectNode(node);
        }
        selectedNodes.clear();
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
                    int a = 10;
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

                                if (nodeOverlap(new PosSize(a.pos, a.size), new PosSize(b.pos, b.size))) {
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
                        } else {

                            clearSelectedNodes();

                            // No node was hit, so create a new one
                            Node node = new Node(v);
                            if (nodeGrid.isEmpty(node)) {
                                nodeGrid.addNode(node);
                                nodes.put(node.id, node);
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

                    for (int y = 0; y < h / gridSize + 1; ++y) {
                        for (int x = 0; x < w / gridSize + 1; ++x) {
                            gc.strokeRect(x*gridSize, y*gridSize, gridSize, gridSize);
                        }
                    }

                    // draw the selected nodes in a later pass
                    List<Node> deferred = new ArrayList<Node>();
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
                        gc.strokeRect(curTile.x*gridSize, curTile.y*gridSize, gridSize, gridSize);
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
