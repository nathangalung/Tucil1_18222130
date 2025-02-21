package puzzle;

import java.util.ArrayList;
import java.util.List;

public class Block {
    private char id;
    private List<int[]> coordinates;
    private String puzzleType;

    public Block(String pattern, String puzzleType) {
        this.coordinates = new ArrayList<>();
        this.puzzleType = puzzleType;
        String[] lines = pattern.trim().split("\n");
        
        // Find first non-space character as ID
        for (int i = 0; i < lines[0].length(); i++) {
            char c = lines[0].charAt(i);
            if (c != ' ') {
                this.id = c;
                break;
            }
        }
        
        // Store coordinates for each character in the pattern, ignoring spaces
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == id) {
                    coordinates.add(new int[]{i, j});
                }
            }
        }
        normalizeCoordinates();
    }

    // Update copy() method to include puzzleType
    public Block copy() {
        Block newBlock = new Block(String.valueOf(id), puzzleType);
        newBlock.coordinates = new ArrayList<>();
        for (int[] coord : coordinates) {
            newBlock.coordinates.add(new int[]{coord[0], coord[1]});
        }
        return newBlock;
    }

    // Rest of the methods remain the same
    public void rotate90() {
        for (int[] coord : coordinates) {
            int temp = coord[0];
            coord[0] = coord[1];
            coord[1] = -temp;
        }
        normalizeCoordinates();
    }

    public void rotate3D() {
        for (int[] coord : coordinates) {
            // For 3D rotation in 2D space, we'll just do a special rotation
            int temp = coord[0];
            coord[0] = -coord[1];
            coord[1] = temp;
        }
        normalizeCoordinates();
    }

    public void flipHorizontal() {
        for (int[] coord : coordinates) {
            coord[1] = -coord[1];
        }
        normalizeCoordinates();
    }

    public void flipVertical() {
        for (int[] coord : coordinates) {
            coord[0] = -coord[0];
        }
        normalizeCoordinates();
    }

    private void normalizeCoordinates() {
        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;

        for (int[] coord : coordinates) {
            minRow = Math.min(minRow, coord[0]);
            minCol = Math.min(minCol, coord[1]);
        }

        for (int[] coord : coordinates) {
            coord[0] -= minRow;
            coord[1] -= minCol;
        }
    }

    public char getId() {
        return id;
    }

    public List<int[]> getCoordinates() {
        return coordinates;
    }
}