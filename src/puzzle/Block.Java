package puzzle;

import java.util.ArrayList;
import java.util.List;

public class Block {
    private char id;
    private List<int[]> coordinates;

    public Block(String pattern) {
        this.coordinates = new ArrayList<>();
        String[] lines = pattern.split("\n");
        this.id = pattern.charAt(0);

        int height = lines.length;
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, line.length());
        }

        // Store coordinates of the block shape (connected cells of the same character)
        for (int i = 0; i < height; i++) {
            String line = lines[i];
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == id) {
                    coordinates.add(new int[]{i, j});
                }
            }
        }
        normalizeCoordinates();
    }

    public void rotate90() {
        // Rotate coordinates 90 degrees clockwise
        for (int[] coord : coordinates) {
            int temp = coord[0];
            coord[0] = coord[1];
            coord[1] = -temp;
        }
        normalizeCoordinates();
    }

    public void flipHorizontal() {
        // Flip horizontally (reflect across the vertical axis)
        for (int[] coord : coordinates) {
            coord[1] = -coord[1];
        }
        normalizeCoordinates();
    }

    private void normalizeCoordinates() {
        // Normalize coordinates to the smallest top-left corner (0,0)
        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;

        for (int[] coord : coordinates) {
            minRow = Math.min(minRow, coord[0]);
            minCol = Math.min(minCol, coord[1]);
        }

        // Adjust all coordinates so that the block's shape is always relative to the smallest corner (0,0)
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

    public Block copy() {
        Block newBlock = new Block(String.valueOf(id));
        newBlock.coordinates = new ArrayList<>();
        for (int[] coord : coordinates) {
            newBlock.coordinates.add(new int[]{coord[0], coord[1]});
        }
        return newBlock;
    }

    // Convert block coordinates to a grid string for easy visualization
    public String toGridString(int maxRows, int maxCols) {
        char[][] grid = new char[maxRows][maxCols];
        for (int i = 0; i < maxRows; i++) {
            for (int j = 0; j < maxCols; j++) {
                grid[i][j] = '.';
            }
        }

        for (int[] coord : coordinates) {
            grid[coord[0]][coord[1]] = id;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxRows; i++) {
            sb.append(new String(grid[i])).append("\n");
        }
        return sb.toString();
    }
}
