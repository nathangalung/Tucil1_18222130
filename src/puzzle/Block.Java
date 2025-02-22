package puzzle;

import java.util.ArrayList;
import java.util.List;

public class Block {
    private char id;
    private List<int[]> coordinates;

    public Block(String pattern) {
        this.coordinates = new ArrayList<>();
        String[] lines = pattern.trim().split("\n");
        
        // Find first non-space character as ID
        for (int i = 0; i < lines[0].length(); i++) {
            char c = lines[0].charAt(i);
            if (c != ' ') {
                this.id = c;
                break;
            }
        }
        
        // Store coordinates for each character in the pattern
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

    public Block copy() {
        Block newBlock = new Block(String.valueOf(id));
        newBlock.coordinates = new ArrayList<>();
        for (int[] coord : coordinates) {
            newBlock.coordinates.add(new int[]{coord[0], coord[1]});
        }
        return newBlock;
    }

    public void rotate90() {
        for (int[] coord : coordinates) {
            int temp = coord[0];
            coord[0] = coord[1];
            coord[1] = -temp;
        }
        normalizeCoordinates();
    }

    public void flipHorizontal() {
        for (int[] coord : coordinates) {
            coord[1] = -coord[1];
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