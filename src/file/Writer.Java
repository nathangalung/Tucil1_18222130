package file;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import puzzle.Board;

public class Writer {
    private String outputPath;
    
    // ANSI color codes for 26 different colors (A-Z)
    public static final String RESET = "\u001B[0m";
    public static final String[] COLORS = {
        "\u001B[31m",    // RED (A)
        "\u001B[32m",    // GREEN (B)
        "\u001B[33m",    // YELLOW (C)
        "\u001B[34m",    // BLUE (D)
        "\u001B[35m",    // PURPLE (E)
        "\u001B[36m",    // CYAN (F)
        "\u001B[91m",    // BRIGHT RED (G)
        "\u001B[92m",    // BRIGHT GREEN (H)
        "\u001B[93m",    // BRIGHT YELLOW (I)
        "\u001B[94m",    // BRIGHT BLUE (J)
        "\u001B[95m",    // BRIGHT MAGENTA (K)
        "\u001B[96m",    // BRIGHT CYAN (L)
        "\u001B[41m",    // RED BACKGROUND (M)
        "\u001B[42m",    // GREEN BACKGROUND (N)
        "\u001B[43m",    // YELLOW BACKGROUND (O)
        "\u001B[44m",    // BLUE BACKGROUND (P)
        "\u001B[45m",    // PURPLE BACKGROUND (Q)
        "\u001B[46m",    // CYAN BACKGROUND (R)
        "\u001B[101m",   // BRIGHT RED BACKGROUND (S)
        "\u001B[102m",   // BRIGHT GREEN BACKGROUND (T)
        "\u001B[103m",   // BRIGHT YELLOW BACKGROUND (U)
        "\u001B[104m",   // BRIGHT BLUE BACKGROUND (V)
        "\u001B[105m",   // BRIGHT MAGENTA BACKGROUND (W)
        "\u001B[106m",   // BRIGHT CYAN BACKGROUND (X)
        "\u001B[97m",    // BRIGHT WHITE (Y)
        "\u001B[37m"     // WHITE (Z)
    };
    
    public Writer(String inputPath) {
        this.outputPath = inputPath.replace("input", "output/txt");
    }
    
    public void writeSolution(Board board, long timeMs, long iterations) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            char[][] grid = board.getGrid();
            
            // Write to file
            for (char[] row : grid) {
                writer.println(new String(row));
            }
            writer.printf("\nWaktu pencarian: %d ms\n", timeMs);
            writer.printf("Banyak kasus yang ditinjau: %d\n", iterations);
            
            // Print to console with colors
            System.out.println("\nSolution:");
            printColoredSolution(board);
            System.out.printf("\nWaktu pencarian: %d ms\n", timeMs);
            System.out.printf("Banyak kasus yang ditinjau: %d\n", iterations);
        }
    }
    
    public static void printColoredSolution(Board board) {
        char[][] grid = board.getGrid();
        System.out.println(); // Add blank line before solution
        for (char[] row : grid) {
            for (char c : row) {
                if (c == '.') {
                    System.out.print('.');
                } else {
                    int colorIndex = c - 'A';
                    if (colorIndex >= 0 && colorIndex < COLORS.length) {
                        System.out.print(COLORS[colorIndex] + c + RESET);
                    } else {
                        System.out.print(c);
                    }
                }
            }
            System.out.println();
        }
    }
}