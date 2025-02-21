package main;  // Add this line at the top

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;  // Add this import
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate; // Add this import
import javafx.stage.Stage;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import file.*;
import puzzle.*;


public class Main extends Application {
    private static final double SPHERE_RADIUS = 20.0;
    private Group puzzleGroup;
    private Label statusLabel;
    private Label timeLabel;
    private Label iterationsLabel;
    private TextField fileNameField;
    private ProgressIndicator loadingIndicator;
    private Board currentBoard;
    private HBox buttonControls;  // Add this field
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private long solveTime;
    private long iterationCount;
    private Set<String> addedConnections = new HashSet<>();

    // Colors matching Writer.COLORS
    private static final Color[] PIECE_COLORS = {
        Color.RED,           // A
        Color.GREEN,        // B
        Color.YELLOW,       // C
        Color.BLUE,         // D
        Color.PURPLE,       // E
        Color.CYAN,         // F
        Color.rgb(255,0,0), // G (Bright Red)
        Color.rgb(0,255,0), // H (Bright Green)
        Color.rgb(255,255,0), // I (Bright Yellow)
        Color.rgb(0,0,255),   // J (Bright Blue)
        Color.rgb(255,0,255), // K (Bright Magenta)
        Color.rgb(0,255,255), // L (Bright Cyan)
        // ... add more colors as needed
    };

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        
        // Create input controls
        HBox inputControls = new HBox(10);
        fileNameField = new TextField();
        fileNameField.setPromptText("Enter test case filename");
        Button loadButton = new Button("Load & Solve");
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        inputControls.getChildren().addAll(fileNameField, loadButton, loadingIndicator);
        inputControls.setPadding(new Insets(10));

        // Create button controls with initial invisible state
        buttonControls = new HBox(10); // Assign to class field
        Button saveButton = new Button("Save Solution");
        Button resetViewButton = new Button("Reset View");
        buttonControls.getChildren().addAll(saveButton, resetViewButton);
        buttonControls.setPadding(new Insets(10));
        buttonControls.setVisible(false); // Hide initially

        // Add controls to top section
        VBox topControls = new VBox(10);
        topControls.getChildren().addAll(inputControls, buttonControls);
        topControls.setPadding(new Insets(10));
        root.setTop(topControls);

        // Create status container
        VBox statusContainer = new VBox(5);
        statusLabel = new Label("Enter a filename to start");
        timeLabel = new Label();
        iterationsLabel = new Label();
        statusContainer.getChildren().addAll(statusLabel, timeLabel, iterationsLabel);
        statusContainer.setPadding(new Insets(10));

        // Create 3D scene container
        SubScene puzzleScene = createPuzzleScene();
        
        // Create center content with proper layout
        VBox centerContent = new VBox(10);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(puzzleScene, statusContainer); // Add children only once
        centerContent.setPadding(new Insets(10));
        root.setCenter(centerContent);

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
                    
                    // Run solver in background thread
                    Thread solverThread = new Thread(() -> solvePuzzle(filename));
                    solverThread.start();
                }
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
                loadingIndicator.setVisible(false);
            }
        });

        saveButton.setOnAction(e -> saveSolution());
        resetViewButton.setOnAction(e -> resetView());

        // Layout setup
        root.setTop(inputControls);
        root.setCenter(centerContent);

        // Scene setup
        Scene scene = new Scene(root, 800, 700);
        primaryStage.setTitle("IQ Puzzler Pro Solver");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Handle window close
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

    private SubScene createPuzzleScene() {
        puzzleGroup = new Group();
        
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

        root3D.getChildren().addAll(puzzleGroup, ambient, light1, light2);
        puzzleGroup.getTransforms().addAll(rotateX, rotateY);

        // Create subscene
        SubScene subScene = new SubScene(root3D, 600, 500, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(30, 30, 30));

        // Setup camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-800);
        camera.setTranslateY(0); // Changed from -200 to center view
        camera.setNearClip(0.1);
        camera.setFarClip(2000.0);
        camera.setFieldOfView(35);
        subScene.setCamera(camera);

        // Initial rotation to show balls
        rotateX.setAngle(180); // Flip to show balls
        rotateY.setAngle(0);

        return subScene;
    }

    private void solvePuzzle(String filename) {
        try {
            // Process filename
            final String inputPath = filename.contains("test/input/") ? 
                filename : "test/input/" + filename;
            final String fullPath = inputPath.endsWith(".txt") ? 
                inputPath : inputPath + ".txt";
    
            // Check if file exists
            File file = new File(fullPath);
            if (!file.exists()) {
                Platform.runLater(() -> {
                    showError("File not found: " + fullPath); // Now using final variable
                    loadingIndicator.setVisible(false);
                });
                return;
            }
    
            // Read and solve puzzle
            Reader reader = new Reader(fullPath);
            int[] dimensions = reader.readDimensions();
            String puzzleType = reader.readPuzzleType();
            char[][] customConfig = null;
            if (puzzleType.equals("CUSTOM")) {
                customConfig = reader.readCustomConfig();
            }
            ArrayList<Block> blocks = reader.readBlocks();
    
            long startTime = System.currentTimeMillis();
            Solver solver = new Solver(dimensions[0], dimensions[1], blocks, customConfig, puzzleType);
            boolean hasSolution = solver.solve();
            long endTime = System.currentTimeMillis();
            final long duration = endTime - startTime;
    
            if (hasSolution) {
                currentBoard = solver.getBoard();
                final Board finalBoard = solver.getBoard();
                final long finalIterations = solver.getIterations();
                
                // Update GUI
                Platform.runLater(() -> {
                    displayPuzzle();
                    updateStatus(duration, finalIterations);
                    loadingIndicator.setVisible(false);
                });
                
                // Print in CLI
                Writer.printColoredSolution(finalBoard);
                System.out.printf("\nWaktu pencarian: %d ms\n", duration);
                System.out.printf("Banyak kasus yang ditinjau: %d\n", finalIterations);
            } else {
                Platform.runLater(() -> {
                    showError("No solution found!");
                    loadingIndicator.setVisible(false);
                });
            }
        } catch (Exception e) {
            final String errorMessage = e.getMessage();
            Platform.runLater(() -> {
                showError("Error: " + errorMessage);
                loadingIndicator.setVisible(false);
            });
        }
    }

    private void displayPuzzle() {
        puzzleGroup.getChildren().clear();
        addedConnections.clear();
        
        if (currentBoard == null) return;
        
        // Show button controls directly using the class field
        Platform.runLater(() -> {
            buttonControls.setVisible(true);
        });
        
        char[][] grid = currentBoard.getGrid();
        double spacing = SPHERE_RADIUS * 2.5;

        // Create board with adjusted position
        double boardWidth = grid[0].length * spacing * 1.2;
        double boardHeight = grid.length * spacing * 1.2;
        double boardDepth = spacing * 0.5;
        double curveRadius = SPHERE_RADIUS * 1.2;
        
        // Create main board with centered position
        Box mainBoard = new Box(boardWidth, boardHeight, boardDepth);
        PhongMaterial boardMaterial = new PhongMaterial(Color.rgb(40, 40, 40));
        mainBoard.setMaterial(boardMaterial);
        
        // Create curved edge with proper alignment
        Cylinder curve = new Cylinder(curveRadius, boardWidth);
        curve.setMaterial(boardMaterial);
        curve.setRotate(90);
        curve.setTranslateZ(-boardDepth/2 - curveRadius/2);
        curve.setTranslateY(boardHeight/2); // Changed to position at bottom
        
        // Group and position board components
        Group boardGroup = new Group(mainBoard, curve);
        boardGroup.setTranslateX(-boardWidth/2); // Center horizontally
        boardGroup.setTranslateY(-boardHeight/2); // Center vertically
        boardGroup.setTranslateZ(-spacing); // Position behind spheres
        
        puzzleGroup.getChildren().add(boardGroup);
        
        // Map to store spheres of same piece
        Map<Character, List<Sphere>> pieceSpheres = new HashMap<>();
        
        // Add spheres for pieces with centered coordinates
        for (int row = grid.length - 1; row >= 0; row--) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] != '.') {
                    Sphere sphere = new Sphere(SPHERE_RADIUS * 0.9);
                    PhongMaterial material = new PhongMaterial();
                    material.setDiffuseColor(PIECE_COLORS[grid[row][col] - 'A']);
                    material.setSpecularColor(Color.WHITE);
                    material.setSpecularPower(32);
                    sphere.setMaterial(material);
                    
                    double x = (col - grid[0].length/2.0) * spacing;
                    double y = ((grid.length - 1 - row) - grid.length/2.0) * spacing;
                    sphere.setTranslateX(x);
                    sphere.setTranslateY(y);
                    sphere.setTranslateZ(SPHERE_RADIUS); // Position just above board
                    
                    puzzleGroup.getChildren().add(sphere);
                    
                    // Store sphere for connecting lines
                    pieceSpheres.computeIfAbsent(grid[row][col], k -> new ArrayList<>()).add(sphere);
                }
            }
        }
        
        // In displayPuzzle() method, update the connector creation section
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] != '.') {
                    Sphere currentSphere = null;
                    
                    // Find the current sphere
                    for (List<Sphere> sphereList : pieceSpheres.values()) {
                        for (Sphere s : sphereList) {
                            double x = (col - grid[0].length/2.0) * spacing;
                            double y = ((grid.length - 1 - row) - grid.length/2.0) * spacing;
                            if (Math.abs(s.getTranslateX() - x) < 0.1 && 
                                Math.abs(s.getTranslateY() - y) < 0.1) {
                                currentSphere = s;
                                break;
                            }
                        }
                        if (currentSphere != null) break;
                    }
                    
                    if (currentSphere != null) {
                        // Check connections in right and down directions only
                        int[][] directions = {
                            {0, 1},  // Right
                            {1, 0}   // Down
                        };
                        
                        for (int[] dir : directions) {
                            int adjRow = row + dir[0];
                            int adjCol = col + dir[1];
                            
                            // Check if adjacent position has a sphere (any piece)
                            if (adjRow < grid.length && adjCol < grid[0].length && 
                                grid[adjRow][adjCol] != '.') {
                                
                                // Find adjacent sphere
                                Sphere adjSphere = null;
                                double adjX = (adjCol - grid[0].length/2.0) * spacing;
                                double adjY = ((grid.length - 1 - adjRow) - grid.length/2.0) * spacing;
                                
                                // Find matching sphere in correct position
                                for (List<Sphere> sphereList : pieceSpheres.values()) {
                                    for (Sphere s : sphereList) {
                                        if (Math.abs(s.getTranslateX() - adjX) < 0.1 && 
                                            Math.abs(s.getTranslateY() - adjY) < 0.1) {
                                            adjSphere = s;
                                            break;
                                        }
                                    }
                                    if (adjSphere != null) break;
                                }
                                
                                if (adjSphere != null) {
                                    String connectionKey = Math.min(row, adjRow) + "," + 
                                                        Math.min(col, adjCol) + "-" + 
                                                        Math.max(row, adjRow) + "," + 
                                                        Math.max(col, adjCol);
                                    
                                    if (!addedConnections.contains(connectionKey)) {
                                        // Create connector
                                        Cylinder connector = new Cylinder(SPHERE_RADIUS * 0.3, spacing);
                                        PhongMaterial material = new PhongMaterial();
                                        
                                        // Use same color if same piece, gray if different
                                        if (grid[row][col] == grid[adjRow][adjCol]) {
                                            material.setDiffuseColor(PIECE_COLORS[grid[row][col] - 'A']);
                                            connector.setMaterial(material);

                                            if (dir[0] == 0) { // Horizontal connection
                                                connector.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
                                                connector.setTranslateX(currentSphere.getTranslateX() + spacing/2);
                                                connector.setTranslateY(currentSphere.getTranslateY());
                                            } else { // Vertical connection
                                                connector.getTransforms().add(new Rotate(0, Rotate.X_AXIS));
                                                connector.setTranslateX(currentSphere.getTranslateX());
                                                connector.setTranslateY(currentSphere.getTranslateY() - spacing/2);
                                            }
                                            
                                            connector.setTranslateZ(currentSphere.getTranslateZ());
                                            puzzleGroup.getChildren().add(connector);
                                            addedConnections.add(connectionKey);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        resetView();
    }

    private void saveSolution() {
        if (currentBoard == null) {
            showError("No solution to save!");
            return;
        }
    
        try {
            // Save text solution
            String txtPath = "output/txt/" + fileNameField.getText().replace(".txt", "") + "_solution.txt";
            new File(txtPath).getParentFile().mkdirs();
            Writer writer = new Writer(txtPath);
            writer.writeSolution(currentBoard, solveTime, iterationCount);
    
            // Save image
            String imgPath = "output/img/" + fileNameField.getText().replace(".txt", "") + "_solution.png";
            new File(imgPath).getParentFile().mkdirs();
            
            WritableImage image = puzzleGroup.getScene().snapshot(null);
            File file = new File(imgPath);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
    
            showInfo("Solution saved successfully!");
        } catch (Exception e) {
            showError("Error saving solution: " + e.getMessage());
        }
    }
    
    private void resetView() {
        rotateX.setAngle(180);
        rotateY.setAngle(0);
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateStatus(long duration, long iterations) {
        this.solveTime = duration;      // Store duration
        this.iterationCount = iterations; // Store iterations
        statusLabel.setText("Solution found!");
        timeLabel.setText(String.format("Time: %d ms", duration));
        iterationsLabel.setText(String.format("Iterations: %d", iterations));
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