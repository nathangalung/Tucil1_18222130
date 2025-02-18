package file;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import puzzle.Block;

public class Reader {
    private String filename;

    public Reader(String filename) {
        this.filename = filename;
    }

    public int[] readDimensions() throws FileNotFoundException {
        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        String[] dimensions = scanner.nextLine().split(" ");
        scanner.close();
        return new int[]{Integer.parseInt(dimensions[0]), 
                        Integer.parseInt(dimensions[1]),
                        Integer.parseInt(dimensions[2])};
    }

    public String readPuzzleType() throws FileNotFoundException {
        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        scanner.nextLine();
        String type = scanner.nextLine();
        scanner.close();
        return type;
    }

    public ArrayList<Block> readBlocks() throws FileNotFoundException {
        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        scanner.nextLine(); // Skip dimensions
        scanner.nextLine(); // Skip puzzle type
        
        ArrayList<Block> blocks = new ArrayList<>();
        StringBuilder currentBlock = new StringBuilder();
        String line;
        char currentId = ' ';
        
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (!line.isEmpty()) {
                if (currentId == ' ') {
                    currentId = line.charAt(0);
                    currentBlock.append(line).append("\n");
                } else if (line.charAt(0) == currentId) {
                    currentBlock.append(line).append("\n");
                } else {
                    // New block starts
                    blocks.add(new Block(currentBlock.toString()));
                    currentBlock = new StringBuilder();
                    currentBlock.append(line).append("\n");
                    currentId = line.charAt(0);
                }
            }
        }
        
        // Add the last block
        if (currentBlock.length() > 0) {
            blocks.add(new Block(currentBlock.toString()));
        }
        
        scanner.close();
        return blocks;
    }
}