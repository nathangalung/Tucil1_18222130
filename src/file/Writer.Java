package file;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import puzzle.Board;

public class Writer {
    private String outputPath;
    
    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String[] COLORS = {
        "\u001B[31m", // RED
        "\u001B[32m", // GREEN
        "\u001B[33m", // YELLOW
        "\u001B[34m", // BLUE
        "\u001B[35m", // PURPLE
        "\u001B[36m", // CYAN
        "\u001B[91m", // BRIGHT RED
        "\u001B[92m"  // BRIGHT GREEN
    };
    
    public Writer(String inputPath) {
        this.outputPath = inputPath.replace("input", "output");
    }
    
    public void writeSolution(Board board, long timeMs, long iterations) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            char[][] grid = board.getGrid();
            // Write plain solution to file
            for (char[] row : grid) {
                writer.println(new String(row));
            }
            writer.printf("\nWaktu pencarian: %d ms\n", timeMs);
            writer.printf("Banyak kasus yang ditinjau: %d\n", iterations);
        }
    }
    
    public static void printColoredSolution(Board board) {
        char[][] grid = board.getGrid();
        java.util.Map<Character, Integer> colorMap = new java.util.HashMap<>();
        int colorIndex = 0;
        
        for (char[] row : grid) {
            for (char c : row) {
                if (c != '.' && !colorMap.containsKey(c)) {
                    colorMap.put(c, colorIndex++ % COLORS.length);
                }
            }
        }
        
        for (char[] row : grid) {
            for (char c : row) {
                if (c == '.') {
                    System.out.print('.');
                } else {
                    System.out.print(COLORS[colorMap.get(c)] + c + RESET);
                }
            }
            System.out.println();
        }
    }
}