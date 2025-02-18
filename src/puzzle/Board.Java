package puzzle;

public class Board {
    private int rows;
    private int cols;
    private char[][] grid;

    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = '.';
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public char[][] getGrid() {
        return grid;
    }

    public boolean canPlace(Block block, int row, int col) {
        for (int[] coord : block.getCoordinates()) {
            int newRow = row + coord[0];
            int newCol = col + coord[1];
            if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols || grid[newRow][newCol] != '.') {
                return false;
            }
        }
        return true;
    }

    public void place(Block block, int row, int col) {
        for (int[] coord : block.getCoordinates()) {
            grid[row + coord[0]][col + coord[1]] = block.getId();
        }
    }

    public void remove(Block block, int row, int col) {
        for (int[] coord : block.getCoordinates()) {
            grid[row + coord[0]][col + coord[1]] = '.';
        }
    }
}
