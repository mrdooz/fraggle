package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    Group root;
    Scene scene;
    int gridSize = 20;
    Vector2i curTile = new Vector2i(-1, -1);
    boolean drawGrid = true;
    List<Node> nodes = new ArrayList<>();
    int nextNodeId = 1;

    class Node
    {
        public Node(Vector2i pos) {
            isSink = false;
            isDefaultRenderTarget = false;
            this.pos = pos;
            this.size = new Vector2i(3 * gridSize, 2 * gridSize);
            this.id = nextNodeId++;
        }

        public void Draw(GraphicsContext gc) {
            gc.setStroke(new Color(0.1, 0.1, 0.1, 1));
            gc.setFill(new Color(0.2, 0.6, 0.2, 1));
            gc.fillRect(pos.x, pos.y, size.x, size.y);
            gc.strokeRect(pos.x, pos.y, size.x, size.y);
        }

        Vector2i pos, size;

        int id;
        boolean isSink;
        boolean isDefaultRenderTarget;
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

        public boolean isEmpty(int x, int y, int w, int h) {
            // TODO: bounds check
            for (int i = 0; i < y; ++i) {
                for (int j = 0; j < x; ++j) {
                    if (data[i][j] != -1) {
                        return false;
                    }
                }
            }

            return true;
        }

        public void addNode(int x, int y, int w, int h, int id) {
            for (int i = 0; i < y; ++i) {
                for (int j = 0; j < x; ++j) {
                    assert(data[i][j] == -1);
                    data[i][j] = id;
                }
            }
        }

        public void eraseNode(int x, int y, int w, int h) {
            for (int i = 0; i < y; ++i) {
                for (int j = 0; j < x; ++j) {
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

    void makeMain(ScrollPane pane)
    {
        int w = 1024;
        int h = 768;
        Canvas canvas = new Canvas(w, h);
        nodeGrid = new NodeGrid(w/gridSize, h/gridSize);
        pane.setContent(canvas);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        double zoom = 1;
        gc.scale(zoom, zoom);

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED,
                mouseEvent -> {
                    curTile = gridPos(mouseEvent.getX(), mouseEvent.getY());
                });

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                mouseEvent -> {
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                mouseEvent -> {
//                    mousePos = snappedPos(mouseEvent.getX(), mouseEvent.getY());
//                    editor.applyBrush(mousePos);
                });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                mouseEvent -> {
                    Vector2i v = gridPos(mouseEvent.getX(), mouseEvent.getY());

                    if (nodeGrid.isEmpty(v.x, v.y, ....))
                    nodes.add(new Node(snappedPos(mouseEvent.getX(), mouseEvent.getY())));
                    MouseButton btn = mouseEvent.getButton();
                    switch (btn) {
//                        case PRIMARY: editor.applyBrush(mousePos); break;
//                        case SECONDARY: editor.deleteTile(mousePos); break;
                    }
                });

        Path path = new Path();
        path.setStrokeWidth(3);

        path.getElements().addAll(new MoveTo(0, 0), new LineTo(0, 0), new LineTo(100, 100));
        root.getChildren().add(path);

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

                    gc.setStroke(new Color(0.1, 0.1, 0.1, 1));
                    for (Node n : nodes) {
                        gc.setFill(new Color(0.2, 0.6, 0.2, 1));
                        gc.fillRect(n.pos.x, n.pos.y, n.size.x, n.size.y);
                        gc.strokeRect(n.pos.x, n.pos.y, n.size.x, n.size.y);
                    }
                }

            }
        }.start();

    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        root = new Group();
        scene = new Scene(root, 1024, 768);

//        Canvas canvas = new Canvas(1024, 768);
//        root.getChildren().add(canvas);
//        final GraphicsContext gc = canvas.getGraphicsContext2D();
////        double zoom = 2;
////        gc.scale(zoom, zoom);
//
//
//        canvas.addEventHandler(MouseEvent.MOUSE_MOVED,
//                mouseEvent -> {
//                    System.out.println("moved");
////                    mousePos = snappedPos(mouseEvent.getX(), mouseEvent.getY());
//                });
//
//        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
//                mouseEvent -> {
//                    System.out.println("pressed");
//                });

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
//        mapPane.getTabs().add(createMapTab(new Map("map1", new Vector2i(100, 100), new Vector2i(32, 32))));
//        mapPane.getTabs().add(createMapTab(new Map("map1", new Vector2i(100, 100), new Vector2i(16, 16))));

//        mapPane.getSelectionModel().selectedItemProperty().addListener(
//                (observableValue, oldTab, newTab) -> {
//                    find the map associated with the new tab
//                    selectedMap = tabToMap.get(newTab);
//                }
//        );

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

        primaryStage.setTitle("JTiled");
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
