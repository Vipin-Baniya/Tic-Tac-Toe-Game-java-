package com.tictactoe;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core game logic for Tic-Tac-Toe.
 * Supports both TWO_PLAYER mode and VS_COMPUTER mode (Minimax AI).
 */
@Service
public class GameService {

    /** In-memory store of active games keyed by gameId. */
    private final Map<String, Game> games = new ConcurrentHashMap<>();

    /** Win combinations: indices on the board that form a line. */
    private static final int[][] WIN_LINES = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // rows
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // cols
        {0, 4, 8}, {2, 4, 6}             // diagonals
    };

    /**
     * Creates a new game with the given mode.
     *
     * @param mode TWO_PLAYER or VS_COMPUTER
     * @return the newly created Game
     */
    public Game createGame(Game.Mode mode) {
        Game game = new Game();
        game.setGameId(UUID.randomUUID().toString());
        game.setMode(mode);
        games.put(game.getGameId(), game);
        return game;
    }

    /**
     * Retrieves a game by its ID.
     *
     * @param gameId the game identifier
     * @return the Game, or null if not found
     */
    public Game getGame(String gameId) {
        return games.get(gameId);
    }

    /**
     * Processes a player's move at the given cell index.
     * If VS_COMPUTER mode and the game is still in progress after the human move,
     * the computer automatically makes its move.
     *
     * @param gameId    the game identifier
     * @param cellIndex 0-8 board index chosen by the human player
     * @return the updated Game, or null if the gameId is invalid
     * @throws IllegalArgumentException if the move is invalid
     */
    public Game makeMove(String gameId, int cellIndex) {
        Game game = games.get(gameId);
        if (game == null) {
            return null;
        }
        if (game.getStatus() != Game.Status.IN_PROGRESS) {
            throw new IllegalArgumentException("Game is already over.");
        }
        if (cellIndex < 0 || cellIndex > 8) {
            throw new IllegalArgumentException("Cell index must be between 0 and 8.");
        }
        if (game.getBoard()[cellIndex] != ' ') {
            throw new IllegalArgumentException("Cell " + cellIndex + " is already occupied.");
        }

        // Apply human move
        applyMove(game, cellIndex, game.getCurrentPlayer());
        updateStatus(game);

        // If VS_COMPUTER and game still in progress, let computer move
        if (game.getMode() == Game.Mode.VS_COMPUTER && game.getStatus() == Game.Status.IN_PROGRESS) {
            int computerMove = bestMove(game.getBoard(), 'O');
            applyMove(game, computerMove, 'O');
            updateStatus(game);
        }

        return game;
    }

    // ---- Private helpers ----

    private void applyMove(Game game, int index, char player) {
        game.getBoard()[index] = player;
        game.setCurrentPlayer(player == 'X' ? 'O' : 'X');
    }

    private void updateStatus(Game game) {
        char winner = checkWinner(game.getBoard());
        if (winner == 'X') {
            game.setStatus(Game.Status.X_WINS);
        } else if (winner == 'O') {
            game.setStatus(Game.Status.O_WINS);
        } else if (isBoardFull(game.getBoard())) {
            game.setStatus(Game.Status.DRAW);
        }
        // else remains IN_PROGRESS
    }

    /**
     * Returns 'X', 'O', or ' ' (no winner yet).
     */
    char checkWinner(char[] board) {
        for (int[] line : WIN_LINES) {
            char a = board[line[0]], b = board[line[1]], c = board[line[2]];
            if (a != ' ' && a == b && b == c) {
                return a;
            }
        }
        return ' ';
    }

    boolean isBoardFull(char[] board) {
        for (char c : board) {
            if (c == ' ') return false;
        }
        return true;
    }

    /**
     * Minimax algorithm to find the best move for the computer ('O').
     *
     * @param board current board state
     * @param player the player to move ('O' for computer, 'X' for human)
     * @return board index of the best move
     */
    int bestMove(char[] board, char player) {
        int bestScore = Integer.MIN_VALUE;
        int bestIndex = -1;
        char[] copy = board.clone();

        for (int i = 0; i < 9; i++) {
            if (copy[i] == ' ') {
                copy[i] = player;
                int score = minimax(copy, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                copy[i] = ' ';
                if (score > bestScore) {
                    bestScore = score;
                    bestIndex = i;
                }
            }
        }
        return bestIndex;
    }

    /**
     * Minimax with alpha-beta pruning.
     * Computer is 'O' (maximizer), human is 'X' (minimizer).
     */
    private int minimax(char[] board, boolean isMaximizing, int alpha, int beta) {
        char winner = checkWinner(board);
        if (winner == 'O') return 10;
        if (winner == 'X') return -10;
        if (isBoardFull(board)) return 0;

        if (isMaximizing) {
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < 9; i++) {
                if (board[i] == ' ') {
                    board[i] = 'O';
                    best = Math.max(best, minimax(board, false, alpha, beta));
                    board[i] = ' ';
                    alpha = Math.max(alpha, best);
                    if (beta <= alpha) break;
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                if (board[i] == ' ') {
                    board[i] = 'X';
                    best = Math.min(best, minimax(board, true, alpha, beta));
                    board[i] = ' ';
                    beta = Math.min(beta, best);
                    if (beta <= alpha) break;
                }
            }
            return best;
        }
    }
}
