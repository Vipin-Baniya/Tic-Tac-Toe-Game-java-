# Tic-Tac-Toe Game – Java Full-Stack

A full-stack Tic-Tac-Toe game built with **Spring Boot** (Java) on the backend and plain **HTML/CSS/JavaScript** on the frontend.

## Features

| Feature | Details |
|---------|---------|
| 🤖 vs Computer | Unbeatable AI powered by the **Minimax** algorithm with alpha-beta pruning |
| 👥 2-Player | Hot-seat local multiplayer (Player X vs Player O) |
| 📊 Scoreboard | Persistent win/draw/loss tallies for the current session |
| 🎨 Modern UI | Responsive dark-theme with animated winning-cell highlights |

## Tech Stack

- **Backend**: Java 17, Spring Boot 3, RESTful API
- **Frontend**: Vanilla HTML5, CSS3, JavaScript (no frameworks needed)
- **AI**: Minimax algorithm with alpha-beta pruning (unbeatable computer)

## Project Structure

```
src/
├── main/
│   ├── java/com/tictactoe/
│   │   ├── TicTacToeApplication.java   # Spring Boot entry point
│   │   ├── Game.java                   # Game state model
│   │   ├── GameService.java            # Game logic + Minimax AI
│   │   └── GameController.java         # REST API controller
│   └── resources/
│       ├── application.properties
│       └── static/
│           ├── index.html              # Single-page frontend
│           ├── style.css
│           └── script.js
└── test/
    └── java/com/tictactoe/
        └── GameServiceTest.java        # Unit tests (16 tests)
```

## REST API

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/game/new` | Start a new game (`{"mode":"TWO_PLAYER"\|"VS_COMPUTER"}`) |
| `GET`  | `/api/game/{id}` | Get current game state |
| `POST` | `/api/game/{id}/move` | Make a move (`{"cellIndex":0-8}`) |

## How to Run

### Prerequisites
- Java 17+
- Maven 3.6+

### Steps

```bash
# Clone the repo
git clone https://github.com/Vipin-Baniya/Tic-Tac-Toe-Game-java-.git
cd Tic-Tac-Toe-Game-java-

# Build and run
mvn spring-boot:run
```

Open your browser at **http://localhost:8080**

### Run Tests

```bash
mvn test
```

## How to Play

1. Choose **2 Player** (hot-seat) or **vs Computer** mode.
2. Click any empty cell to place your mark.
3. In Computer mode you are always **X** and go first; the AI plays **O**.
4. Use **Restart** to play again in the same mode, or **Home** to switch modes.