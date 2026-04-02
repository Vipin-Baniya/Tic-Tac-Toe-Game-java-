package com.tictactoe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService service;

    @BeforeEach
    void setUp() {
        service = new GameService();
    }

    // -------- createGame --------

    @Test
    void createGame_twoPlayer_setsMode() {
        Game game = service.createGame(Game.Mode.TWO_PLAYER);
        assertEquals(Game.Mode.TWO_PLAYER, game.getMode());
        assertNotNull(game.getGameId());
        assertEquals(Game.Status.IN_PROGRESS, game.getStatus());
        assertEquals('X', game.getCurrentPlayer());
    }

    @Test
    void createGame_vsComputer_setsMode() {
        Game game = service.createGame(Game.Mode.VS_COMPUTER);
        assertEquals(Game.Mode.VS_COMPUTER, game.getMode());
    }

    // -------- makeMove – basic --------

    @Test
    void makeMove_placesXOnBoard() {
        Game game = service.createGame(Game.Mode.TWO_PLAYER);
        String id = game.getGameId();

        Game updated = service.makeMove(id, 0);
        assertEquals('X', updated.getBoard()[0]);
    }

    @Test
    void makeMove_alternatesPlayers() {
        Game game = service.createGame(Game.Mode.TWO_PLAYER);
        String id = game.getGameId();

        service.makeMove(id, 0);   // X
        Game g2 = service.makeMove(id, 1);  // O
        assertEquals('O', g2.getBoard()[1]);
    }

    @Test
    void makeMove_rejectsOccupiedCell() {
        Game game = service.createGame(Game.Mode.TWO_PLAYER);
        String id = game.getGameId();
        service.makeMove(id, 4);

        assertThrows(IllegalArgumentException.class, () -> service.makeMove(id, 4));
    }

    @Test
    void makeMove_rejectsInvalidIndex() {
        Game game = service.createGame(Game.Mode.TWO_PLAYER);
        String id = game.getGameId();
        assertThrows(IllegalArgumentException.class, () -> service.makeMove(id, 9));
    }

    @Test
    void makeMove_returnsNullForUnknownGame() {
        assertNull(service.makeMove("no-such-id", 0));
    }

    // -------- win detection --------

    @Test
    void makeMove_detectsXWinsOnRow() {
        Game game = service.createGame(Game.Mode.TWO_PLAYER);
        String id = game.getGameId();
        // X: 0,1,2  O: 3,4
        service.makeMove(id, 0);  // X
        service.makeMove(id, 3);  // O
        service.makeMove(id, 1);  // X
        service.makeMove(id, 4);  // O
        Game end = service.makeMove(id, 2);  // X wins row 0

        assertEquals(Game.Status.X_WINS, end.getStatus());
    }

    @Test
    void makeMove_detectsOWinsOnColumn() {
        Game game = service.createGame(Game.Mode.TWO_PLAYER);
        String id = game.getGameId();
        // O wins column 0: indices 0, 3, 6
        service.makeMove(id, 1);  // X
        service.makeMove(id, 0);  // O
        service.makeMove(id, 2);  // X
        service.makeMove(id, 3);  // O
        service.makeMove(id, 4);  // X
        Game end = service.makeMove(id, 6);  // O wins col 0

        assertEquals(Game.Status.O_WINS, end.getStatus());
    }

    @Test
    void makeMove_detectsDraw() {
        Game game = service.createGame(Game.Mode.TWO_PLAYER);
        String id = game.getGameId();
        // Board that results in a draw:
        // X O X
        // X O X
        // O X O
        int[] moves = {0, 1, 3, 4, 8, 6, 2, 5, 7};
        for (int m : moves) {
            game = service.makeMove(id, m);
        }
        assertEquals(Game.Status.DRAW, game.getStatus());
    }

    @Test
    void makeMove_rejectsAfterGameOver() {
        Game game = service.createGame(Game.Mode.TWO_PLAYER);
        String id = game.getGameId();
        service.makeMove(id, 0);
        service.makeMove(id, 3);
        service.makeMove(id, 1);
        service.makeMove(id, 4);
        service.makeMove(id, 2); // X wins

        assertThrows(IllegalArgumentException.class, () -> service.makeMove(id, 5));
    }

    // -------- VS_COMPUTER mode --------

    @Test
    void makeMove_vsComputer_computerResponds() {
        Game game = service.createGame(Game.Mode.VS_COMPUTER);
        String id = game.getGameId();

        Game updated = service.makeMove(id, 0); // human plays X at 0

        // Computer should have played O somewhere else
        long oCount = 0;
        for (char c : updated.getBoard()) {
            if (c == 'O') oCount++;
        }
        assertEquals(1, oCount, "Computer should have placed exactly one O");
    }

    @Test
    void makeMove_vsComputer_computerBlocksWinningMove() {
        // If human is about to win, computer should block
        Game game = service.createGame(Game.Mode.VS_COMPUTER);
        String id = game.getGameId();

        // Human: 0 -> Computer responds somewhere
        // Human: 1 -> Computer should block cell 2
        service.makeMove(id, 0);
        Game g = service.makeMove(id, 1);

        // Computer must have played O at cell 2 to block
        assertEquals('O', g.getBoard()[2], "Computer should block X from winning at cell 2");
    }

    // -------- checkWinner helper --------

    @Test
    void checkWinner_returnsXForDiagonal() {
        char[] board = {
            'X', 'O', 'O',
            ' ', 'X', ' ',
            ' ', ' ', 'X'
        };
        assertEquals('X', service.checkWinner(board));
    }

    @Test
    void checkWinner_returnsSpaceWhenNoWinner() {
        char[] board = {
            'X', 'O', 'X',
            'X', 'O', ' ',
            ' ', ' ', ' '
        };
        assertEquals(' ', service.checkWinner(board));
    }

    // -------- bestMove (Minimax) --------

    @Test
    void bestMove_takesWinningMove() {
        // O can win at index 8 (diagonal 2,4,6 already taken — let's use row)
        char[] board = {
            'O', 'O', ' ',  // O should take cell 2
            'X', 'X', ' ',
            ' ', ' ', ' '
        };
        int move = service.bestMove(board, 'O');
        assertEquals(2, move, "Computer should take winning cell 2");
    }
}
