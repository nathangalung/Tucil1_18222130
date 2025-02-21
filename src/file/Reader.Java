package file;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import puzzle.Block;

public class Reader {
    private String filename;
    private String puzzleType;
    private int rows, cols, numBlocks;
    private char[][] customConfig;

    public Reader(String filename) {
        this.filename = filename;
    }

    public int[] readDimensions() throws FileNotFoundException {
        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        String[] dimensions = scanner.nextLine().split(" ");
        rows = Integer.parseInt(dimensions[0]);
        cols = Integer.parseInt(dimensions[1]);
        numBlocks = Integer.parseInt(dimensions[2]);
        puzzleType = scanner.nextLine();
        scanner.close();
        return new int[]{rows, cols, numBlocks};
    }

    public String readPuzzleType() throws FileNotFoundException {
        if (puzzleType == null) {
            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            scanner.nextLine(); // Skip dimensions
            puzzleType = scanner.nextLine();
            scanner.close();
        }
        return puzzleType;
    }

    public char[][] readCustomConfig() throws FileNotFoundException {
        if (!puzzleType.equals("CUSTOM")) {
            return null;
        }

        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        scanner.nextLine(); // Skip dimensions
        scanner.nextLine(); // Skip puzzle type
        
        char[][] config = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            String line = scanner.nextLine();
            config[i] = line.toCharArray();
        }
        
        scanner.close();
        this.customConfig = config;
        return config;
    }

    public ArrayList<Block> readBlocks() throws FileNotFoundException {
        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        scanner.nextLine(); // Skip dimensions
        scanner.nextLine(); // Skip puzzle type
        
        // Skip custom configuration if needed
        if (puzzleType.equals("CUSTOM")) {
            for (int i = 0; i < rows; i++) {
                scanner.nextLine();
            }
        }
        
        ArrayList<Block> blocks = new ArrayList<>();
        StringBuilder currentBlock = new StringBuilder();
        String line;
        Character currentId = null;
        
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (!line.isEmpty()) {
                // Find first non-space character as ID
                char blockId = ' ';
                for (char c : line.toCharArray()) {
                    if (c != ' ') {
                        blockId = c;
                        break;
                    }
                }
                
                if (currentId == null) {
                    currentId = blockId;
                    currentBlock.append(line).append("\n");
                } else if (blockId == currentId) {
                    currentBlock.append(line).append("\n");
                } else {
                    // New block starts
                    blocks.add(new Block(currentBlock.toString(), puzzleType));
                    currentBlock = new StringBuilder();
                    currentBlock.append(line).append("\n");
                    currentId = blockId;
                }
            }
        }
        
        // Add the last block
        if (currentBlock.length() > 0) {
            blocks.add(new Block(currentBlock.toString(), puzzleType));
        }
        
        scanner.close();
        return blocks;
    }
}