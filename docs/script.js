/* =============================================================
   Tic-Tac-Toe – fully client-side (no backend required)
   Works on GitHub Pages or any static host.
   ============================================================= */

// ---- Constants ----

const WIN_LINES = [
  [0, 1, 2], [3, 4, 5], [6, 7, 8], // rows
  [0, 3, 6], [1, 4, 7], [2, 5, 8], // cols
  [0, 4, 8], [2, 4, 6]             // diagonals
];

// ---- Game state ----

let board = Array(9).fill('');   // '' | 'X' | 'O'
let currentPlayer = 'X';
let mode = null;                 // 'TWO_PLAYER' | 'VS_COMPUTER'
let status = 'IN_PROGRESS';     // 'IN_PROGRESS' | 'X_WINS' | 'O_WINS' | 'DRAW'
let boardLocked = false;

const scores = { X: 0, O: 0, draws: 0 };

// ---- Screen helpers ----

function showScreen(id) {
  document.querySelectorAll('.screen').forEach(s => s.classList.add('hidden'));
  document.getElementById(id).classList.remove('hidden');
}

function goHome() {
  showScreen('mode-screen');
  mode = null;
  boardLocked = false;
  scores.X = 0; scores.O = 0; scores.draws = 0;
  document.getElementById('score-x').textContent = '0';
  document.getElementById('score-o').textContent = '0';
  document.getElementById('score-draws').textContent = '0';
}

// ---- Game flow ----

function startGame(selectedMode) {
  mode = selectedMode;
  board = Array(9).fill('');
  currentPlayer = 'X';
  status = 'IN_PROGRESS';
  boardLocked = false;
  updateScoreLabels(mode);
  showScreen('game-screen');
  render();
}

function restartGame() {
  if (!mode) return;
  startGame(mode);
}

function handleClick(cellIndex) {
  if (boardLocked || status !== 'IN_PROGRESS') return;
  if (board[cellIndex] !== '') {
    showStatus('⚠️ Cell already taken!', 'error');
    return;
  }

  applyMove(cellIndex, currentPlayer);
  updateStatus();

  // Computer's turn in VS_COMPUTER mode
  if (mode === 'VS_COMPUTER' && status === 'IN_PROGRESS') {
    boardLocked = true;   // prevent clicks while AI "thinks"
    setTimeout(() => {
      const aiMove = bestMove(board, 'O');
      if (aiMove !== -1) {
        applyMove(aiMove, 'O');
        updateStatus();
      }
      boardLocked = false;
      render();
    }, 250);   // small delay so the AI move feels natural
    render();  // render human move immediately
    return;
  }

  render();
}

// ---- Core game logic ----

function applyMove(index, player) {
  board[index] = player;
  currentPlayer = player === 'X' ? 'O' : 'X';
}

function updateStatus() {
  const winner = checkWinner(board);
  if (winner === 'X') {
    status = 'X_WINS';
  } else if (winner === 'O') {
    status = 'O_WINS';
  } else if (board.every(c => c !== '')) {
    status = 'DRAW';
  } else {
    status = 'IN_PROGRESS';
  }
}

function checkWinner(b) {
  for (const [a, b2, c] of WIN_LINES) {
    if (b[a] && b[a] === b[b2] && b[b2] === b[c]) return b[a];
  }
  return null;
}

// ---- Minimax AI with alpha-beta pruning ----
// Computer = 'O' (maximizer), Human = 'X' (minimizer)

function bestMove(b, player) {
  let bestScore = -Infinity;
  let bestIndex = -1;
  const copy = [...b];

  for (let i = 0; i < 9; i++) {
    if (copy[i] === '') {
      copy[i] = player;
      const score = minimax(copy, false, -Infinity, Infinity);
      copy[i] = '';
      if (score > bestScore) {
        bestScore = score;
        bestIndex = i;
      }
    }
  }
  return bestIndex;
}

function minimax(b, isMaximizing, alpha, beta) {
  const winner = checkWinner(b);
  if (winner === 'O') return 10;
  if (winner === 'X') return -10;
  if (b.every(c => c !== '')) return 0;

  if (isMaximizing) {
    let best = -Infinity;
    for (let i = 0; i < 9; i++) {
      if (b[i] === '') {
        b[i] = 'O';
        best = Math.max(best, minimax(b, false, alpha, beta));
        b[i] = '';
        alpha = Math.max(alpha, best);
        if (beta <= alpha) break;
      }
    }
    return best;
  } else {
    let best = Infinity;
    for (let i = 0; i < 9; i++) {
      if (b[i] === '') {
        b[i] = 'X';
        best = Math.min(best, minimax(b, true, alpha, beta));
        b[i] = '';
        beta = Math.min(beta, best);
        if (beta <= alpha) break;
      }
    }
    return best;
  }
}

// ---- Rendering ----

function render() {
  const cells = document.querySelectorAll('.cell');
  cells.forEach((cell, i) => {
    const val = board[i];
    cell.textContent = val;
    cell.className = 'cell';
    if (val === 'X') cell.classList.add('taken', 'x');
    else if (val === 'O') cell.classList.add('taken', 'o');
  });

  if (status === 'X_WINS' || status === 'O_WINS') {
    highlightWinner(status === 'X_WINS' ? 'X' : 'O');
  }

  boardLocked = status !== 'IN_PROGRESS';

  if (status === 'X_WINS') {
    const label = mode === 'VS_COMPUTER' ? 'You win! 🎉' : 'Player X wins! 🎉';
    showStatus(label, 'win');
    scores.X++;
    document.getElementById('score-x').textContent = scores.X;
  } else if (status === 'O_WINS') {
    const label = mode === 'VS_COMPUTER' ? 'Computer wins! 🤖' : 'Player O wins! 🎉';
    showStatus(label, 'lose');
    scores.O++;
    document.getElementById('score-o').textContent = scores.O;
  } else if (status === 'DRAW') {
    showStatus("It's a draw! 🤝", 'draw');
    scores.draws++;
    document.getElementById('score-draws').textContent = scores.draws;
  } else {
    const msg = mode === 'VS_COMPUTER'
      ? 'Your turn (X)'
      : `Player ${currentPlayer}'s turn`;
    showStatus(msg, 'turn');
  }

  document.getElementById('mode-label').textContent =
    mode === 'VS_COMPUTER' ? '🤖 You (X) vs Computer (O)' : '👥 Player X vs Player O';
}

function showStatus(msg, type) {
  const el = document.getElementById('status');
  el.textContent = msg;
  el.className = 'status ' + (type || '');
}

function highlightWinner(player) {
  const cells = document.querySelectorAll('.cell');
  for (const line of WIN_LINES) {
    if (line.every(i => board[i] === player)) {
      line.forEach(i => cells[i].classList.add('winning'));
      break;
    }
  }
}

function updateScoreLabels(m) {
  if (m === 'VS_COMPUTER') {
    document.getElementById('score-x-label').textContent = 'You (X)';
    document.getElementById('score-o-label').textContent = 'Computer';
  } else {
    document.getElementById('score-x-label').textContent = 'Player X';
    document.getElementById('score-o-label').textContent = 'Player O';
  }
}
