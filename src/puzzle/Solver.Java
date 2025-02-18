package puzzle;

import java.util.List;

public class Solver {
    private Board board;
    private List<Block> blocks;
    private long iterations;
    private boolean solved;

    public Solver(int rows, int cols, List<Block> blocks) {
        this.board = new Board(rows, cols);
        this.blocks = blocks;
        this.iterations = 0;
        this.solved = false;
    }

    public boolean solve() {
        return solve(0);
    }

    private boolean solve(int blockIndex) {
        iterations++;

        // If all blocks are placed, return true
        if (blockIndex == blocks.size()) {
            solved = true;
            return true;
        }

        Block currentBlock = blocks.get(blockIndex).copy();

        // Try all rotations (0, 90, 180, 270 degrees)
        for (int rotation = 0; rotation < 4; rotation++) {
            // Try flipping horizontally
            for (int flip = 0; flip < 2; flip++) {
                // Try placing the block at all positions on the board
                for (int row = 0; row < board.getRows(); row++) {
                    for (int col = 0; col < board.getCols(); col++) {
                        if (board.canPlace(currentBlock, row, col)) {
                            board.place(currentBlock, row, col);  // Place the block
                            if (solve(blockIndex + 1)) {         // Try to solve with the next block
                                return true;
                            }
                            board.remove(currentBlock, row, col); // Backtrack
                        }
                    }
                }
                currentBlock.flipHorizontal();  // Flip the block for the next try
            }
            currentBlock.rotate90(); // Rotate the block for the next try
        }

        return false;
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
