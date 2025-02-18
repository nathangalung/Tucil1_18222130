import file.Reader;
import file.Writer;
import java.util.ArrayList;
import java.util.Scanner;
import puzzle.Block;
import puzzle.Solver;

public class Main {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter test case file path: ");
            String filename = scanner.nextLine();
            
            if (!filename.contains("test/input/")) {
                filename = "test/input/" + filename;
            }
            if (!filename.endsWith(".txt")) {
                filename += ".txt";
            }
            
            Reader reader = new Reader(filename);
            int[] dimensions = reader.readDimensions();
            String puzzleType = reader.readPuzzleType();
            ArrayList<Block> blocks = reader.readBlocks();
            
            System.out.println("\nSolving puzzle...");
            long startTime = System.currentTimeMillis();
            
            Solver solver = new Solver(dimensions[0], dimensions[1], blocks);
            boolean hasSolution = solver.solve();
            
            long endTime = System.currentTimeMillis();
            
            if (hasSolution) {
                Writer.printColoredSolution(solver.getBoard());
                System.out.printf("\nWaktu pencarian: %d ms\n", (endTime - startTime));
                System.out.printf("Banyak kasus yang ditinjau: %d\n", solver.getIterations());
                
                System.out.print("\nApakah anda ingin menyimpan solusi? (ya/tidak): ");
                if (scanner.nextLine().toLowerCase().startsWith("y")) {
                    Writer writer = new Writer(filename);
                    writer.writeSolution(solver.getBoard(), (endTime - startTime), solver.getIterations());
                    System.out.println("Solusi berhasil disimpan!");
                }
            }
            
            scanner.close();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}