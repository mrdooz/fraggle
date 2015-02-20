package fraggle;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.controlsfx.control.PropertySheet;

import java.io.*;
import java.util.*;

// Everything that needs to be serialized goes in State
class State {
    Map<Integer, Node> nodes = new HashMap<>();
    Map<Integer, RenderSegment> renderSegments = new HashMap<>();
    Set<Integer> sinkNodes = new HashSet<>();
    NodeGrid nodeGrid;
    int nextNodeId = 1;
    int nextRenderSegmentId = 1;
}

public class Main extends Application {

    State state = new State();

    static Main instance;

    Group root;
    Scene scene;
    Vector2i curTile = new Vector2i(-1, -1);
    boolean drawGrid = true;
    Set<Integer> selectedNodes = new HashSet<>();
    RenderSegmentType curRenderSegmentType = RenderSegmentType.UNKNOWN;

    Vector2i dragStart;
    boolean validDrop;
    PropertySheet propertySheet = new PropertySheet();
    ListView<String> nodesListView;

    int nextNodeId() {
        return state.nextNodeId++;
    }

    int nextRenderSegmentId() {
        return state.nextRenderSegmentId++;
    }

    Tab createRenderTab(String name) {
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
            Node node = state.nodes.getOrDefault(nodeIdx, null);
            if (node != null) {
                res.add(node);
            }
        }
        return res;
    }

    void displayProperties(Node node) {
        propertySheet.getItems().setAll(NodeData.listFromPropertyMap(node.renderSegment.properties));
    }

    void makeMain(ScrollPane pane)
    {
        int w = 1024;
        int h = 768;
        Canvas canvas = new Canvas(w, h);
        state.nodeGrid = new NodeGrid(w / Settings.GRID_SIZE, h / Settings.GRID_SIZE);
        pane.setContent(canvas);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        double zoom = 1;
        gc.scale(zoom, zoom);

        scene.setOnKeyReleased(keyEvent -> {
            switch (keyEvent.getCode()) {

                case ESCAPE:
                    clearSelectedNodes();
                    curRenderSegmentType = RenderSegmentType.UNKNOWN;
                    nodesListView.getSelectionModel().clearSelection();
                    break;

                case DELETE:
                case BACK_SPACE:
                    for (Node node : getSelectedNodes()) {
                        state.nodes.remove(node.id);
                        state.sinkNodes.remove(node.id);
                        state.nodeGrid.eraseNode(node);
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
                    Node hitTest = state.nodeGrid.nodeAtPos(dragCur);
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
                            for (Node b : state.nodes.values()) {
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
                                state.nodeGrid.eraseNode(node.moveStart.x, node.moveStart.y, node.size.x, node.size.y);
                                state.nodeGrid.addNode(node);
                            }

                            node.invalidDropPos = false;
                            node.isMoving = false;
                            deselectNode(node);
                        }

                        selectedNodes.clear();
                    } else {
                        Node hit = state.nodeGrid.nodeAtPos(v);
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

                            if (curRenderSegmentType != RenderSegmentType.UNKNOWN) {
                                // No node was hit, so create a new one
                                try {
                                    RenderSegment segment = RenderSegment.Create(curRenderSegmentType);
                                    state.renderSegments.put(segment.id, segment);
                                    Node node = new Node(curRenderSegmentType, v, segment);
                                    if (state.nodeGrid.isEmpty(node)) {
                                        state.nodeGrid.addNode(node);
                                        state.nodes.put(node.id, node);
                                        if (curRenderSegmentType == RenderSegmentType.SINK) {
                                            state.sinkNodes.add(node.id);
                                        }
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
                    for (Node n : state.nodes.values()) {
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

    void processNodeStack(Stack<Integer> nodeStack) {

        System.out.printf("** STACK **\n");

        while (!nodeStack.isEmpty()) {
            int nodeId = nodeStack.pop();
            Node node = state.nodes.get(nodeId);
            System.out.printf("Node: %d\n", node.id);
        }
    }

    void verifyBlocks() {

        int sizeX = state.nodeGrid.size.x;
        int sizeY = state.nodeGrid.size.y;

        Set<Integer> processedNodes = new HashSet<>();

        // stacks are valid if they consist of non-sinks, and end with a sink
        for (int y = 0 ; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {

                Node node = state.nodeGrid.nodeAtPos(x, y);

                if (node == null) {
                    continue;
                }

                if (processedNodes.contains(node.id)) {
                    continue;
                }
                processedNodes.add(node.id);

                if (node.isSink) {
                    // top node is a sink; this is invalid, so just skip it
                    System.out.printf("Found dangling sink: %s\n", node.getProperty("name"));
                    continue;
                }

                // found a top node; start processing downwards
                int curId = node.id;
                Stack<Integer> nodeStack = new Stack<>();
                nodeStack.push(curId);
                processedNodes.add(node.id);

                for (int curY = y; curY < sizeY; ++curY) {

                    Node curNode = state.nodeGrid.nodeAtPos(x, curY);
                    if (curNode == null) {
                        // reached end of connected nodes
                        break;
                    } else if (curNode.id == curId) {
                        // current grid is part of the current node
                        continue;
                    }
                    // add new node to the stack
                    nodeStack.push(curNode.id);
                    processedNodes.add(curNode.id);
                    curId = curNode.id;
                }

                if (!nodeStack.isEmpty()) {
                    // process the node stack
                    processNodeStack(nodeStack);
                }
            }
        }
    }

    void PostSerializedFixup() {

        // init the node grid
        state.nodeGrid.data = new int[state.nodeGrid.size.y][state.nodeGrid.size.x];
    }

    void LoadState() {
        try {
            InputStream input = new FileInputStream("/Users/dooz/projects/fraggle/data1.xml");
            XStream xstream = new XStream(new DomDriver());
            xstream.processAnnotations(State.class);
            state = (State)xstream.fromXML(input);

            // Once the state has been restored, we need to do a bunch of fixup
            PostSerializedFixup();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void SaveState() {
        try {
            OutputStream output = new FileOutputStream("/Users/dooz/projects/fraggle/data1.xml");
            XStream xstream = new XStream(new DomDriver());
            xstream.processAnnotations(State.class);
            xstream.toXML(state, output);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void ExportState() {
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        instance = this;
        root = new Group();
        scene = new Scene(root, 1024, 768);

        // Create the border pane that will contain everything
        BorderPane borderPane = new BorderPane();

        // Create the main split pain
        SplitPane mainSplit = new SplitPane();
        mainSplit.setOrientation(Orientation.HORIZONTAL);
        mainSplit.prefWidthProperty().bind(scene.widthProperty());
        mainSplit.prefHeightProperty().bind(scene.heightProperty());
        borderPane.setCenter(mainSplit);

        {
            // Create the menu and the buttons
            VBox vbox = new VBox();

            // Create the menu
            MenuBar menuBar = new MenuBar();
            vbox.getChildren().add(menuBar);

            Menu fileMenu = new Menu("File");
            MenuItem newMenuItem = new MenuItem("New");
            MenuItem loadMenuItem = new MenuItem("Load");
            loadMenuItem.setOnAction(actionEvent -> LoadState() );
            MenuItem saveMenuItem = new MenuItem("Save");
            loadMenuItem.setOnAction(actionEvent -> SaveState() );
            MenuItem exportMenuItem = new MenuItem("Export");
            exportMenuItem.setOnAction(actionEvent -> ExportState() );
            MenuItem exitMenuItem = new MenuItem("Exit");
            exitMenuItem.setOnAction(actionEvent -> Platform.exit() );

            fileMenu.getItems().addAll(newMenuItem,
                    loadMenuItem,
                    saveMenuItem,
                    exportMenuItem,
                    new SeparatorMenuItem(),
                    exitMenuItem
            );
            menuBar.getMenus().add(fileMenu);

            // Create the buttons
            HBox hbox = new HBox();
            Button btnVerify = new Button("verify");
            btnVerify.setOnAction(event -> {
                verifyBlocks();
            });
            hbox.getChildren().addAll(btnVerify);
            vbox.getChildren().add(hbox);
            borderPane.setTop(vbox);
        }

        {
            // Create main view
            TabPane tilesetPane = new TabPane();
            tilesetPane.getTabs().addAll(createRenderTab("main"));
            tilesetPane.setPrefWidth(200);
            tilesetPane.setPrefHeight(400);
            mainSplit.getItems().add(tilesetPane);
        }

        {
            // Create split pane for the property/node type panels
            SplitPane propertySplitPane = new SplitPane();
            propertySplitPane.setOrientation(Orientation.VERTICAL);

            List<String> nodeTypes = new ArrayList<>();
            for (RenderSegmentType type : RenderSegmentType.values()) {
                if (type != RenderSegmentType.UNKNOWN)
                    nodeTypes.add(type.name());
            }
            nodesListView = new ListView<>(FXCollections.observableArrayList(nodeTypes));
            nodesListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                curRenderSegmentType = newValue == null ? RenderSegmentType.UNKNOWN : RenderSegmentType.valueOf(newValue);
            });
            propertySplitPane.getItems().addAll(new PropertySheetVBox(propertySheet, () -> {
                List<String> sinks = new ArrayList<String>();
                for (int id : state.sinkNodes) {
                    Node node = state.nodes.get(id);
                    sinks.add((String)((NodeProperty)node.getProperty("name")).value);
                }
                return sinks;
            }), nodesListView);
            mainSplit.getItems().add(propertySplitPane);
        }


        root.getChildren().add(borderPane);

        primaryStage.setTitle("Fraggle");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
