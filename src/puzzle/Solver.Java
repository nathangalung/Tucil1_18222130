package puzzle;

import java.util.List;
import java.util.ArrayList;

public class Solver {
    private Board board;
    private List<Block> blocks;
    private long iterations;
    private boolean solved;
    private char[][] customConfig;
    private String puzzleType; // Add this field

    public Solver(int rows, int cols, List<Block> blocks, char[][] customConfig, String puzzleType) { // Update constructor
        this.puzzleType = puzzleType; // Initialize puzzleType
        this.board = new Board(rows, cols, customConfig, puzzleType);
        this.blocks = sortBlocksBySize(blocks);
        this.iterations = 0;
        this.solved = false;
        this.customConfig = customConfig;
    }

    private boolean isCustomConfigFilled() {
        if (customConfig == null) return true;
        
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (customConfig[i][j] == 'X' && board.getGrid()[i][j] == '.') {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean solve() {
        if (puzzleType.equals("CUSTOM")) {
            return solveCustom(0);
        } else if (puzzleType.equals("PYRAMID")) {
            boolean result = solvePyramid(0, 0);
            return result;
        }
        return solveDefault(0);
    }

    private boolean solveDefault(int blockIndex) {
        iterations++;

        // Move this check to after placing all blocks
        if (blockIndex >= blocks.size()) {
            // For DEFAULT mode, all blocks must be used
            if (customConfig == null) {
                solved = true;
                return true;
            }
            // For CUSTOM mode, check if configuration is filled
            return isCustomConfigFilled();
        }
        Block currentBlock = blocks.get(blockIndex).copy();
        int maxRow = board.getRows() - getBlockHeight(currentBlock);
        int maxCol = board.getCols() - getBlockWidth(currentBlock);

        // Try each position on the board
        for (int row = 0; row <= maxRow; row++) {
            for (int col = 0; col <= maxCol; col++) {
                // Try original orientation
                if (tryPlaceBlock(currentBlock, row, col, blockIndex)) {
                    return true;
                }
                
                // Try flipped orientation
                currentBlock.flipHorizontal();
                if (tryPlaceBlock(currentBlock, row, col, blockIndex)) {
                    return true;
                }
                currentBlock.flipHorizontal(); // Restore original
                
                // Try 90-degree rotation
                currentBlock.rotate90();
                if (tryPlaceBlock(currentBlock, row, col, blockIndex)) {
                    return true;
                }
                
                // Try flipped 90-degree rotation
                currentBlock.flipHorizontal();
                if (tryPlaceBlock(currentBlock, row, col, blockIndex)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private boolean solveCustom(int blockIndex) {
        iterations++;
    
        // Check if custom configuration is filled
        if (isCustomConfigFilled()) {
            solved = true;
            return true;
        }
    
        // Try next block if we've used all blocks but haven't filled the pattern
        if (blockIndex >= blocks.size()) {
            return false;
        }
    
        Block currentBlock = blocks.get(blockIndex).copy();
        int maxRow = board.getRows() - getBlockHeight(currentBlock);
        int maxCol = board.getCols() - getBlockWidth(currentBlock);
    
        // Try placing the block
        for (int row = 0; row <= maxRow; row++) {
            for (int col = 0; col <= maxCol; col++) {
                Block tryBlock = currentBlock.copy();
                
                // Try all rotations
                for (int rotation = 0; rotation < 4; rotation++) {
                    // Try both normal and flipped
                    for (int flip = 0; flip < 2; flip++) {
                        if (board.canPlace(tryBlock, row, col)) {
                            board.place(tryBlock, row, col);
                            
                            // Try next block
                            if (solveCustom(blockIndex + 1)) {
                                return true;
                            }
                            
                            board.remove(tryBlock, row, col);
                        }
                        tryBlock.flipHorizontal();
                    }
                    tryBlock.rotate90();
                }
            }
        }
    
        // Skip this block and try with next one
        return solveCustom(blockIndex + 1);
    }
    
    private boolean solvePyramid(int blockIndex, int layer) {
        iterations++;
    
        // Base case - pyramid complete
        if (layer >= board.getRows()) {
            solved = true;
            printPyramidLayers();
            return true;
        }
    
        // Current layer dimensions
        int layerSize = board.getRows() - layer;
        int startRow = (layer * (layer + 1)) / 2;
    
        // Move to next layer if current is filled
        if (isLayerFilled(layer)) {
            return solvePyramid(0, layer + 1);
        }
    
        // Try each remaining block
        for (int i = blockIndex; i < blocks.size(); i++) {
            Block currentBlock = blocks.get(i).copy();
            
            // Skip blocks too big for current layer
            if (currentBlock.getCoordinates().size() > layerSize) {
                continue;
            }
    
            // Try each position in current layer
            for (int row = startRow; row < startRow + layerSize; row++) {
                for (int col = 0; col < layerSize; col++) {
                    // Try standard orientations
                    for (int rotation = 0; rotation < 4; rotation++) {
                        if (board.canPlace(currentBlock, row, col)) {
                            board.place(currentBlock, row, col);
                            if (solvePyramid(i + 1, layer)) {
                                return true;
                            }
                            board.remove(currentBlock, row, col);
                        }
                        currentBlock.rotate90();
                    }
    
                    // Try flipped
                    currentBlock.flipHorizontal();
                    for (int rotation = 0; rotation < 4; rotation++) {
                        if (board.canPlace(currentBlock, row, col)) {
                            board.place(currentBlock, row, col);
                            if (solvePyramid(i + 1, layer)) {
                                return true;
                            }
                            board.remove(currentBlock, row, col);
                        }
                        currentBlock.rotate90();
                    }
                    currentBlock.flipHorizontal(); // Reset flip
                }
            }
        }
        return false;
    }
    
    private void printPyramidLayers() {
        char[][] grid = board.getGrid();
        int n = board.getRows();
        int currentRow = 0;
    
        // Print each layer of the pyramid
        for (int layer = 0; layer < n; layer++) {
            int layerSize = n - layer;
            
            // Print current layer's rows
            for (int r = 0; r < layerSize; r++) {
                // Print leading spaces for pyramid shape
                for (int s = 0; s < layer; s++) {
                    System.out.print(" ");
                }
                // Print row content
                for (int col = 0; col < layerSize; col++) {
                    System.out.print(grid[currentRow + r][col]);
                }
                System.out.println();
            }
            currentRow += layerSize;
        }
    }
    
    private List<Block> sortBlocksBySize(List<Block> blocks) {
        if (puzzleType.equals("PYRAMID")) {
            List<Block> sorted = new ArrayList<>(blocks);
            sorted.sort((a, b) -> {
                int sizeA = a.getCoordinates().size();
                int sizeB = b.getCoordinates().size();
                
                // Single blocks first for top layer
                if (sizeA == 1) return -1;
                if (sizeB == 1) return 1;
                
                // Then sort by size descending (larger blocks first)
                return Integer.compare(sizeB, sizeA);
            });
            return sorted;
        }
        else {
            // Original sorting for other modes
            List<Block> sorted = new ArrayList<>(blocks);
            sorted.sort((a, b) -> 
                Integer.compare(b.getCoordinates().size(), a.getCoordinates().size()));
            return sorted;
        }
    }

    private boolean tryPlaceBlock(Block block, int row, int col, int blockIndex) {
        if (board.canPlace(block, row, col)) {
            board.place(block, row, col);
            if (solveDefault(blockIndex + 1)) {
                return true;
            }
            board.remove(block, row, col);
        }
        return false;
    }

    private int getBlockHeight(Block block) {
        int maxRow = 0;
        for (int[] coord : block.getCoordinates()) {
            maxRow = Math.max(maxRow, coord[0]);
        }
        return maxRow + 1;
    }

    private int getBlockWidth(Block block) {
        int maxCol = 0;
        for (int[] coord : block.getCoordinates()) {
            maxCol = Math.max(maxCol, coord[1]);
        }
        return maxCol + 1;
    }

    private boolean isLayerFilled(int layer) {
        int layerSize = board.getRows() - layer;
        int startRow = (layer * (layer + 1)) / 2;
        
        // Check if all positions in current layer are filled
        for (int row = startRow; row < startRow + layerSize; row++) {
            for (int col = 0; col < layerSize; col++) {
                if (board.getGrid()[row][col] == '.') {
                    return false;
                }
            }
        }
        return true;
    }

    public long getIterations() {
        return iterations;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isSolved() {
        return solved;
    }
}