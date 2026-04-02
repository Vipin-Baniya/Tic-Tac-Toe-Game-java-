package com.tictactoe;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller exposing the Tic-Tac-Toe game API.
 *
 * POST  /api/game/new          – start a new game (body: {"mode":"TWO_PLAYER"|"VS_COMPUTER"})
 * GET   /api/game/{id}         – get current game state
 * POST  /api/game/{id}/move    – make a move     (body: {"cellIndex": 0-8})
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /** Start a new game. */
    @PostMapping("/new")
    public ResponseEntity<?> newGame(@RequestBody Map<String, String> body) {
        String modeStr = body.getOrDefault("mode", "TWO_PLAYER").toUpperCase();
        Game.Mode mode;
        try {
            mode = Game.Mode.valueOf(modeStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid mode: " + modeStr));
        }
        Game game = gameService.createGame(mode);
        return ResponseEntity.ok(toResponse(game));
    }

    /** Get the current state of a game. */
    @GetMapping("/{id}")
    public ResponseEntity<?> getGame(@PathVariable String id) {
        Game game = gameService.getGame(id);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(game));
    }

    /** Make a move. */
    @PostMapping("/{id}/move")
    public ResponseEntity<?> move(@PathVariable String id, @RequestBody Map<String, Integer> body) {
        Integer cellIndex = body.get("cellIndex");
        if (cellIndex == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing cellIndex"));
        }
        try {
            Game game = gameService.makeMove(id, cellIndex);
            if (game == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(toResponse(game));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Converts a Game to a JSON-friendly map. */
    private Map<String, Object> toResponse(Game game) {
        // Convert char[] board to String[] for clean JSON output
        String[] boardStr = new String[9];
        for (int i = 0; i < 9; i++) {
            boardStr[i] = String.valueOf(game.getBoard()[i]).trim();
        }
        return Map.of(
            "gameId", game.getGameId(),
            "board", boardStr,
            "currentPlayer", String.valueOf(game.getCurrentPlayer()),
            "mode", game.getMode().name(),
            "status", game.getStatus().name()
        );
    }
}
