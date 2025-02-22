package puzzle;

public class Board {
    private int rows;
    private int cols;
    private char[][] grid;
    private char[][] customConfig;
    private boolean isCustomMode;
    private String puzzleType;

    public Board(int rows, int cols, char[][] customConfig, String puzzleType) {
        this.rows = rows;
        this.cols = cols;
        this.customConfig = customConfig;
        this.isCustomMode = (customConfig != null);
        this.puzzleType = puzzleType;
        this.grid = new char[rows][cols];
        
        // Initialize grid with empty spaces
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
        // For DEFAULT mode or when customConfig is null
        if (!isCustomMode || customConfig == null) {
            for (int[] coord : block.getCoordinates()) {
                int newRow = row + coord[0];
                int newCol = col + coord[1];
                
                // Check bounds and overlap
                if (newRow < 0 || newRow >= rows || 
                    newCol < 0 || newCol >= cols || 
                    grid[newRow][newCol] != '.') {
                    return false;
                }
            }
            return true;
        } 
        // For CUSTOM mode with customConfig
        else if (puzzleType.equals("CUSTOM")) {
            boolean touchesX = false;
            
            for (int[] coord : block.getCoordinates()) {
                int newRow = row + coord[0];
                int newCol = col + coord[1];
                
                if (newRow < 0 || newRow >= rows || 
                    newCol < 0 || newCol >= cols || 
                    grid[newRow][newCol] != '.') {
                    return false;
                }
                
                if (customConfig[newRow][newCol] == 'X') {
                    touchesX = true;
                }
            }
            return touchesX;
        }
        // Default case - cannot place block
        return false;
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

    public String getPuzzleType() {
        return puzzleType;
    }
}