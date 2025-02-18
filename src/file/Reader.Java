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
        scanner.nextLine();
        scanner.nextLine();
        
        ArrayList<Block> blocks = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String blockStr = scanner.nextLine();
            if (!blockStr.isEmpty()) {
                blocks.add(new Block(blockStr));
            }
        }
        scanner.close();
        return blocks;
    }
}