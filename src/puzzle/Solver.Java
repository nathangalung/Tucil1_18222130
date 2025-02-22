package puzzle;

import java.util.List;
import java.util.ArrayList;

public class Solver {
    private Board board;
    private List<Block> blocks;
    private long iterations;
    private boolean solved;
    private char[][] customConfig;
    private String puzzleType;

    public Solver(int rows, int cols, List<Block> blocks, char[][] customConfig, String puzzleType) {
        this.puzzleType = puzzleType;
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
        return puzzleType.equals("CUSTOM") ? solveCustom(0) : solveDefault(0);
    }

    private boolean solveDefault(int blockIndex) {
        iterations++;

        if (blockIndex >= blocks.size()) {
            solved = true;
            return true;
        }

        Block currentBlock = blocks.get(blockIndex).copy();
        int maxRow = board.getRows() - getBlockHeight(currentBlock);
        int maxCol = board.getCols() - getBlockWidth(currentBlock);

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
                currentBlock.flipHorizontal();
                
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
    
        if (isCustomConfigFilled()) {
            solved = true;
            return true;
        }
    
        if (blockIndex >= blocks.size()) {
            return false;
        }
    
        Block currentBlock = blocks.get(blockIndex).copy();
        int maxRow = board.getRows() - getBlockHeight(currentBlock);
        int maxCol = board.getCols() - getBlockWidth(currentBlock);
    
        for (int row = 0; row <= maxRow; row++) {
            for (int col = 0; col <= maxCol; col++) {
                Block tryBlock = currentBlock.copy();
                
                for (int rotation = 0; rotation < 4; rotation++) {
                    for (int flip = 0; flip < 2; flip++) {
                        if (board.canPlace(tryBlock, row, col)) {
                            board.place(tryBlock, row, col);
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
    
        return solveCustom(blockIndex + 1);
    }

    private List<Block> sortBlocksBySize(List<Block> blocks) {
        List<Block> sorted = new ArrayList<>(blocks);
        sorted.sort((a, b) -> Integer.compare(b.getCoordinates().size(), a.getCoordinates().size()));
        return sorted;
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