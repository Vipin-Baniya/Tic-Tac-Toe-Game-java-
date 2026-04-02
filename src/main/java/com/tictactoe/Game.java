package com.tictactoe;

/**
 * Represents the state of a Tic-Tac-Toe game.
 */
public class Game {

    public enum Mode {
        TWO_PLAYER, VS_COMPUTER
    }

    public enum Status {
        IN_PROGRESS, X_WINS, O_WINS, DRAW
    }

    /** 9-cell board: index 0-8 (row-major). ' ' = empty, 'X' = X, 'O' = O. */
    private char[] board;

    /** The player whose turn it currently is ('X' or 'O'). */
    private char currentPlayer;

    private Mode mode;
    private Status status;
    private String gameId;

    public Game() {
        this.board = new char[9];
        for (int i = 0; i < 9; i++) {
            board[i] = ' ';
        }
        this.currentPlayer = 'X';
        this.status = Status.IN_PROGRESS;
    }

    // ---- Getters / Setters ----

    public char[] getBoard() {
        return board;
    }

    public void setBoard(char[] board) {
        this.board = board;
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(char currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}
