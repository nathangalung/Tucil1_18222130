package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.*;
import file.*;
import puzzle.*;

public class Main extends Application {
    static {
        // Environment properties for WSL
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.forceGPU", "false");
        System.setProperty("glass.platform", "gtk");
        System.setProperty("javafx.platform", "gtk");
        System.setProperty("embedded", "false");
        System.setProperty("java.awt.headless", "false");
        System.setProperty("DISPLAY", ":0");
    }
    // Constants
    private static final double SPHERE_RADIUS = 20.0;
    
    // UI Components
    private Group puzzleGroup;
    private TextField fileNameField;
    private ProgressIndicator loadingIndicator;
    private Board currentBoard;
    private HBox buttonControls;
    private Label statusLabel = new Label("Enter a filename to start");
    private Label timeLabel = new Label();
    private Label iterationsLabel = new Label();
    private Label solutionFoundLabel = new Label("Solution found!");
    private VBox fileNameContainer;
    private VBox solutionContainer;
    private Group solutionFoundGroup;

    // 3D Controls
    private double mousePosX, mousePosY, mouseOldX, mouseOldY;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private double scaleValue = 1.0;
    private final Scale scaleTransform = new Scale(1, 1, 1);

    // State tracking
    private long solveTime, iterationCount, finalIterations;
    private Set<String> addedConnections = new HashSet<>();

    // Colors for puzzle pieces
    private static final Color[] PIECE_COLORS = {
        Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.PURPLE, Color.CYAN,
        Color.rgb(255, 50, 50), Color.rgb(50, 255, 50), Color.rgb(255, 255, 50),
        Color.rgb(50, 50, 255), Color.rgb(255, 50, 255), Color.rgb(50, 255, 255),
        Color.rgb(180, 0, 0), Color.rgb(0, 180, 0), Color.rgb(180, 180, 0),
        Color.rgb(0, 0, 180), Color.rgb(180, 0, 180), Color.rgb(0, 180, 180),
        Color.rgb(255, 100, 100), Color.rgb(100, 255, 100), Color.rgb(255, 255, 100),
        Color.rgb(100, 100, 255), Color.rgb(255, 100, 255), Color.rgb(100, 255, 255),
        Color.WHITE, Color.LIGHTGRAY
    };

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        
        // Create input controls at top
        HBox inputControls = new HBox(10);
        fileNameField = new TextField();
        fileNameField.setPromptText("Enter test case filename");
        Button loadButton = new Button("Load & Solve");
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        inputControls.getChildren().addAll(fileNameField, loadButton, loadingIndicator);
        inputControls.setPadding(new Insets(10));
        inputControls.setAlignment(Pos.CENTER);

        // Create button controls
        buttonControls = new HBox(10);
        Button saveButton = new Button("Save Solution");
        saveButton.setOnAction(e -> showSaveOptions());
        Button resetViewButton = new Button("Reset View");
        resetViewButton.setOnAction(e -> resetView());
        buttonControls.getChildren().addAll(saveButton, resetViewButton);
        buttonControls.setPadding(new Insets(10));
        buttonControls.setVisible(false);
        buttonControls.setAlignment(Pos.CENTER);

        // Create 3D scene container
        SubScene puzzleScene = createPuzzleScene();

        // Layout setup
        VBox topContent = new VBox(10);
        topContent.getChildren().addAll(inputControls, buttonControls);
        topContent.setAlignment(Pos.CENTER);
        root.setTop(topContent);
        root.setCenter(puzzleScene);

        // Add mouse control for rotation
        puzzleScene.setOnMousePressed(me -> {
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });

        puzzleScene.setOnMouseDragged(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            rotateX.setAngle(rotateX.getAngle() - (mousePosY - mouseOldY) / 2);
            rotateY.setAngle(rotateY.getAngle() + (mousePosX - mouseOldX) / 2);
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
        });

        // Setup load button action
        loadButton.setOnAction(e -> {
            try {
                String filename = fileNameField.getText();
                if (!filename.isEmpty()) {
                    loadingIndicator.setVisible(true);
                    statusLabel.setText("Solving puzzle...");
                    Thread solverThread = new Thread(() -> solvePuzzle(filename));
                    solverThread.start();
                }
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
                loadingIndicator.setVisible(false);
            }
        });

        // Scene setup
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setTitle("IQ Puzzler Pro Solver");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Handle window close
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

    // Update showSaveOptions() method:
    private void showSaveOptions() {
        if (currentBoard == null) {
            showError("No solution to save!");
            return;
        }

        // Create dialog
        Stage saveDialog = new Stage();
        saveDialog.setTitle("Save Solution");
        saveDialog.initModality(Modality.APPLICATION_MODAL);

        VBox dialogVbox = new VBox(10);
        dialogVbox.setPadding(new Insets(10));
        dialogVbox.setAlignment(Pos.CENTER);

        Button txtButton = new Button("Save as TXT");
        Button imgButton = new Button("Save as PNG");

        txtButton.setOnAction(e -> {
            saveAsTxt();
            saveDialog.close();
        });

        imgButton.setOnAction(e -> {
            saveAsImage();
            saveDialog.close();
        });

        dialogVbox.getChildren().addAll(txtButton, imgButton);

        Scene dialogScene = new Scene(dialogVbox, 200, 100);
        saveDialog.setScene(dialogScene);
        saveDialog.show();
    }

    private void saveAsTxt() {
        try {
            String txtPath = "test/output/txt/" + fileNameField.getText().replace(".txt", "") + ".txt";
            new File(txtPath).getParentFile().mkdirs();
            Writer writer = new Writer(txtPath);
            String message = statusLabel.getText(); // Get the current status message
            writer.writeSolution(currentBoard, solveTime, iterationCount, message);
            System.out.println("\nAttempted solution saved as text file: " + txtPath);
            showInfo("Text solution saved successfully!");
        } catch (Exception e) {
            System.out.println("\nError saving text solution: " + e.getMessage());
            showError("Error saving text solution: " + e.getMessage());
        }
    }

    private void saveAsImage() {
        try {
            resetView();
            String imgPath = "test/output/png/" + fileNameField.getText().replace(".txt", "") + ".png";
            new File(imgPath).getParentFile().mkdirs();
            
            SubScene subScene = (SubScene) puzzleGroup.getScene().getRoot().lookup("#puzzleSubScene");
            WritableImage image = subScene.snapshot(null, null);
            File file = new File(imgPath);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            
            System.out.println("\nSolution saved as image: " + imgPath);
            showInfo("Image saved successfully!");
        } catch (Exception e) {
            System.out.println("\nError saving image: " + e.getMessage());
            showError("Error saving image: " + e.getMessage());
        }
    }

    private SubScene createPuzzleScene() {
        puzzleGroup = new Group();
        puzzleGroup.getTransforms().addAll(rotateX, rotateY, scaleTransform);
        
        // Create 3D scene
        Group root3D = new Group();
        AmbientLight ambient = new AmbientLight(Color.WHITE);
        
        PointLight light1 = new PointLight(Color.WHITE);
        light1.setTranslateZ(-1000);
        light1.setTranslateX(500);
        light1.setTranslateY(-500);
    
        PointLight light2 = new PointLight(Color.WHITE);
        light2.setTranslateZ(-1000);
        light2.setTranslateX(-500);
        light2.setTranslateY(500);
    
        // Initialize containers
        fileNameContainer = new VBox(5);
        statusLabel.setTextFill(Color.WHITE);
        fileNameContainer.getChildren().add(statusLabel);
        fileNameContainer.setPadding(new Insets(20));
        fileNameContainer.setVisible(true);

        solutionContainer = new VBox(5);
        timeLabel.setTextFill(Color.WHITE);
        iterationsLabel.setTextFill(Color.WHITE);
        solutionContainer.getChildren().addAll(timeLabel, iterationsLabel);
        solutionContainer.setPadding(new Insets(20));
        solutionContainer.setVisible(false);

        solutionFoundLabel.setTextFill(Color.WHITE);
        solutionFoundGroup = new Group(solutionFoundLabel);
        solutionFoundGroup.setVisible(false);

        // Create separate Groups for positioning
        Group fileNameGroup = new Group(fileNameContainer);
        Group solutionStatsGroup = new Group(solutionContainer);

        // Position each text group independently
        fileNameGroup.setTranslateX(-85);
        fileNameGroup.setTranslateY(-25);

        solutionFoundGroup.setTranslateX(-45);
        solutionFoundGroup.setTranslateY(-200);

        solutionStatsGroup.setTranslateX(50);
        solutionStatsGroup.setTranslateY(125);

        // Add everything to root3D
        root3D.getChildren().addAll(
            puzzleGroup, 
            ambient, 
            light1, 
            light2, 
            fileNameGroup,
            solutionFoundGroup,
            solutionStatsGroup
        );
        
        // Create larger subscene with black background
        SubScene subScene = new SubScene(root3D, 1000, 700, true, SceneAntialiasing.BALANCED);
        subScene.setId("puzzleSubScene");
        subScene.setFill(Color.rgb(30, 30, 30));
        
        // Setup camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-800);
        camera.setTranslateY(0);
        camera.setTranslateX(0);
        camera.setNearClip(0.1);
        camera.setFarClip(2000.0);
        camera.setFieldOfView(30);
    
        subScene.setCamera(camera);
    
        // Add zoom control
        subScene.setOnScroll(event -> {
            double delta = event.getDeltaY() * 0.002;
            scaleValue = Math.max(0.5, Math.min(2.0, scaleValue + delta));
            scaleTransform.setX(scaleValue);
            scaleTransform.setY(scaleValue);
            scaleTransform.setZ(scaleValue);
            event.consume();
        });
    
        return subScene;
    }

    private void solvePuzzle(String filename) {
        try {
            // Clear previous puzzle display first
            Platform.runLater(() -> {
                puzzleGroup.getChildren().clear();
                buttonControls.setVisible(false);
                fileNameContainer.setVisible(true);
                solutionFoundGroup.setVisible(false);
                solutionContainer.setVisible(false);
                loadingIndicator.setVisible(true);
                statusLabel.setText("Solving puzzle...");
            });
    
            // Process filename
            final String inputPath = filename.contains("test/input/") ? 
                filename : "test/input/" + filename;
            final String fullPath = inputPath.endsWith(".txt") ? 
                inputPath : inputPath + ".txt";
    
            // Check if file exists
            File file = new File(fullPath);
            if (!file.exists()) {
                Platform.runLater(() -> {
                    statusLabel.setText("File not found: " + fullPath);
                    fileNameContainer.setVisible(true);
                    solutionFoundGroup.setVisible(false);
                    solutionContainer.setVisible(false);
                    loadingIndicator.setVisible(false);
                });
                return;
            }
    
            // When starting to solve
            Platform.runLater(() -> {
                currentBoard = null;
                statusLabel.setText("Solving puzzle...");
                fileNameContainer.setVisible(true);
                solutionFoundGroup.setVisible(false);
                solutionContainer.setVisible(false);
            });
    
            try {
                Reader reader = new Reader(fullPath);
                int[] dimensions = reader.readDimensions();
                String puzzleType = reader.readPuzzleType();
                char[][] customConfig = null;
                if (puzzleType.equals("CUSTOM")) {
                    customConfig = reader.readCustomConfig();
                }
                ArrayList<Block> blocks = reader.readBlocks();
    
                if (blocks.isEmpty() || dimensions[2] != blocks.size()) {
                    throw new IllegalArgumentException("Invalid file structure: Incomplete or malformed puzzle definition");
                }
    
                // Solve puzzle
                long startTime = System.currentTimeMillis();
                Solver solver = new Solver(dimensions[0], dimensions[1], blocks, customConfig, puzzleType);
                boolean hasSolution = solver.solve();
                long endTime = System.currentTimeMillis();
                final long duration = endTime - startTime;
                finalIterations = solver.getIterations();

                if (hasSolution) {
                    currentBoard = solver.getBoard();
                    System.out.println("\nSolution found!");
                    System.out.println("\nPuzzle solution:");
                    Writer.printColoredSolution(currentBoard);
                    System.out.printf("\nSearch duration: %d ms\n", duration);
                    System.out.printf("Number of iterations: %d\n", finalIterations);
                    
                    Platform.runLater(() -> {
                        statusLabel.setText("Solution found!");
                        solutionFoundLabel.setText("Solution found!");
                        fileNameContainer.setVisible(false);
                        solutionFoundGroup.setVisible(true);
                        solutionContainer.setVisible(true);
                        displayPuzzle();
                        updateStatus(duration, finalIterations);
                        loadingIndicator.setVisible(false);
                        buttonControls.setVisible(true);
                    });
                } else {
                    String message;
                    switch (solver.getSolutionStatus()) {
                        case 1: // PUZZLE_BIGGER
                            message = "No solution found: Puzzle pieces > Board space!";
                            break;
                        case 2: // BOARD_BIGGER
                            message = "No solution found: Puzzle pieces < Board space!";
                            break;
                        default:
                            message = "No solution found: Cannot fit pieces perfectly!";
                    }
        
                    currentBoard = solver.getBoard();
                    final String finalMessage = message;
                    
                    Platform.runLater(() -> {
                        statusLabel.setText(finalMessage);
                        switch (solver.getSolutionStatus()) {
                            case 1: // PUZZLE_BIGGER
                                solutionFoundLabel.setText("No solution found: Puzzle > Board!");
                                break;
                            case 2: // BOARD_BIGGER
                                solutionFoundLabel.setText("No solution found: Puzzle < Board!");
                                break;
                            default:
                                solutionFoundLabel.setText("No solution found: Cannot fit perfectly!");
                        }
                        fileNameContainer.setVisible(false);
                        solutionFoundGroup.setVisible(true);
                        solutionContainer.setVisible(true);
                        displayPuzzle();
                        updateStatus(duration, finalIterations);
                        loadingIndicator.setVisible(false);
                        buttonControls.setVisible(true);
                    });
                    
                    System.out.println("\n" + message);
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Invalid file structure");
                    fileNameContainer.setVisible(true);
                    solutionFoundGroup.setVisible(false);
                    solutionContainer.setVisible(false);
                    loadingIndicator.setVisible(false);
                });
                System.out.println("\nInvalid file structure");
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                statusLabel.setText("Error: " + e.getMessage());
                fileNameContainer.setVisible(true);
                solutionFoundGroup.setVisible(false);
                solutionContainer.setVisible(false);
                loadingIndicator.setVisible(false);
            });
            System.out.println("\nError: " + e.getMessage());
        }
    }

    private void displayPuzzle() {
        puzzleGroup.getChildren().clear();
        addedConnections.clear();
        
        if (currentBoard == null) return;
        
        char[][] grid = currentBoard.getGrid();
        double spacing = SPHERE_RADIUS * 2.5;

        // Create board components with updated offsets
        double xOffset = 25.0; 
        double yOffset = 15.0;

        // Create and add board group
        Group boardGroup = createBoardGroup(grid, spacing, xOffset, yOffset);
        puzzleGroup.getChildren().add(boardGroup);

        // Create and add spheres with connections
        Map<Character, List<Sphere>> pieceSpheres = createPieceSpheres(grid, spacing, xOffset, yOffset);
        createConnections(grid, spacing, pieceSpheres, xOffset, yOffset);

        // Update view
        resetView();
        buttonControls.setVisible(true);
    }

    private Group createBoardGroup(char[][] grid, double spacing, double xOffset, double yOffset) {
        double boardWidth = grid[0].length * spacing * 1.2;
        double boardHeight = grid.length * spacing * 1.2;
        double boardDepth = spacing * 0.5;
        double curveRadius = SPHERE_RADIUS * 1.2;

        // Create main board
        Box mainBoard = new Box(boardWidth, boardHeight, boardDepth);
        PhongMaterial boardMaterial = new PhongMaterial(Color.rgb(40, 40, 40));
        mainBoard.setMaterial(boardMaterial);
        mainBoard.setTranslateZ(-boardDepth/2);

        // Create indentations
        Group indentations = createIndentations(grid, spacing, xOffset, yOffset);

        // Create curved edge
        Cylinder curve = createCurvedEdge(curveRadius, boardWidth, boardHeight);

        Group boardGroup = new Group(mainBoard, indentations, curve);
        boardGroup.setTranslateZ(-spacing * 1.5);

        return boardGroup;
    }

    private Group createIndentations(char[][] grid, double spacing, double xOffset, double yOffset) {
        Group indentations = new Group();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] != '.') {
                    double x = (col - grid[0].length/2.0) * spacing + xOffset;
                    double y = ((grid.length - 1 - row) - grid.length/2.0) * spacing + yOffset;
                    
                    Sphere indent = new Sphere(SPHERE_RADIUS * 0.9);
                    PhongMaterial indentMaterial = new PhongMaterial(Color.rgb(30, 30, 30));
                    indent.setMaterial(indentMaterial);
                    indent.setTranslateX(x);
                    indent.setTranslateY(y);
                    indent.setTranslateZ(SPHERE_RADIUS/2);
                    indentations.getChildren().add(indent);
                }
            }
        }
        return indentations;
    }

    private Cylinder createCurvedEdge(double radius, double width, double height) {
        Cylinder curve = new Cylinder(radius, width);
        curve.setRotate(90);
        curve.setTranslateY(height/2);
        curve.setTranslateZ(-radius);
        PhongMaterial curveMaterial = new PhongMaterial(Color.rgb(50, 50, 50));
        curve.setMaterial(curveMaterial);
        return curve;
    }

    private Map<Character, List<Sphere>> createPieceSpheres(char[][] grid, double spacing, double xOffset, double yOffset) {
        Map<Character, List<Sphere>> pieceSpheres = new HashMap<>();
        
        for (int row = grid.length - 1; row >= 0; row--) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] != '.') {
                    Sphere sphere = new Sphere(SPHERE_RADIUS * 0.9);
                    PhongMaterial material = new PhongMaterial();
                    material.setDiffuseColor(PIECE_COLORS[grid[row][col] - 'A']);
                    material.setSpecularColor(Color.WHITE);
                    material.setSpecularPower(32);
                    sphere.setMaterial(material);
                    
                    double x = (col - grid[0].length/2.0) * spacing + xOffset;
                    double y = ((grid.length - 1 - row) - grid.length/2.0) * spacing + yOffset;
                    sphere.setTranslateX(x);
                    sphere.setTranslateY(y);
                    sphere.setTranslateZ(0);
                    
                    puzzleGroup.getChildren().add(sphere);
                    pieceSpheres.computeIfAbsent(grid[row][col], k -> new ArrayList<>()).add(sphere);
                }
            }
        }
        return pieceSpheres;
    }

    private void createConnections(char[][] grid, double spacing, Map<Character, List<Sphere>> pieceSpheres, 
                                double xOffset, double yOffset) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] != '.') {
                    // Check right and down connections
                    checkAndCreateConnection(grid, row, col, row, col + 1, spacing, pieceSpheres, xOffset, yOffset);
                    checkAndCreateConnection(grid, row, col, row + 1, col, spacing, pieceSpheres, xOffset, yOffset);
                }
            }
        }
    }

    private void checkAndCreateConnection(char[][] grid, int row1, int col1, int row2, int col2, 
                                        double spacing, Map<Character, List<Sphere>> pieceSpheres,
                                        double xOffset, double yOffset) {
        if (row2 >= 0 && row2 < grid.length && col2 >= 0 && col2 < grid[0].length &&
            grid[row1][col1] == grid[row2][col2]) {
            
            String connectionKey = String.format("%d,%d-%d,%d", row1, col1, row2, col2);
            if (!addedConnections.contains(connectionKey)) {
                createConnection(row1, col1, row2, col2, grid[row1][col1], spacing, xOffset, yOffset);
                addedConnections.add(connectionKey);
            }
        }
    }

    private void createConnection(int row1, int col1, int row2, int col2, char pieceId, 
                                double spacing, double xOffset, double yOffset) {
        double x1 = (col1 - currentBoard.getGrid()[0].length/2.0) * spacing + xOffset;
        double y1 = ((currentBoard.getGrid().length - 1 - row1) - currentBoard.getGrid().length/2.0) * spacing + yOffset;
        double x2 = (col2 - currentBoard.getGrid()[0].length/2.0) * spacing + xOffset;
        double y2 = ((currentBoard.getGrid().length - 1 - row2) - currentBoard.getGrid().length/2.0) * spacing + yOffset;

        Cylinder connection = new Cylinder(SPHERE_RADIUS * 0.4, 
            Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2)));
        
        // Position and rotate the connection
        double angle = Math.toDegrees(Math.atan2(y2-y1, x2-x1));
        connection.getTransforms().addAll(
            new Rotate(90, Rotate.Z_AXIS),
            new Rotate(-angle, Rotate.Z_AXIS)
        );
        
        connection.setTranslateX((x1 + x2)/2);
        connection.setTranslateY((y1 + y2)/2);
        
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(PIECE_COLORS[pieceId - 'A']);
        connection.setMaterial(material);
        
        puzzleGroup.getChildren().add(connection);
    }
    
    private void resetView() {
        rotateX.setAngle(180);
        rotateY.setAngle(0);
        scaleValue = 1.0;
        scaleTransform.setX(scaleValue);
        scaleTransform.setY(scaleValue);
        scaleTransform.setZ(scaleValue);
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateStatus(long duration, long iterations) {
        this.solveTime = duration;
        this.iterationCount = iterations;
        Platform.runLater(() -> {
            timeLabel.setText(String.format("Search duration: %d ms", duration));
            iterationsLabel.setText(String.format("Number of iterations: %d", iterations));
            
            // Ensure labels are visible
            statusLabel.setVisible(true);
            timeLabel.setVisible(true);
            iterationsLabel.setVisible(true);
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}