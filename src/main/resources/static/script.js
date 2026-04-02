/* global state */
let gameId = null;
let currentMode = null;
let boardLocked = false;

const scores = { X: 0, O: 0, draws: 0 };

/* -------- API helpers -------- */

async function apiPost(url, data) {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  return res.json();
}

async function apiGet(url) {
  const res = await fetch(url);
  return res.json();
}

/* -------- Screen helpers -------- */

function showScreen(id) {
  document.querySelectorAll('.screen').forEach(s => s.classList.add('hidden'));
  document.getElementById(id).classList.remove('hidden');
}

function goHome() {
  showScreen('mode-screen');
  gameId = null;
  currentMode = null;
  boardLocked = false;
}

/* -------- Game flow -------- */

async function startGame(mode) {
  currentMode = mode;
  const data = await apiPost('/api/game/new', { mode });
  if (data.error) { alert(data.error); return; }
  gameId = data.gameId;
  boardLocked = false;
  updateScoreLabels(mode);
  showScreen('game-screen');
  renderGame(data);
}

async function restartGame() {
  if (!currentMode) return;
  await startGame(currentMode);
}

async function handleClick(cellIndex) {
  if (boardLocked || !gameId) return;

  const data = await apiPost(`/api/game/${gameId}/move`, { cellIndex });
  if (data.error) {
    showStatus('⚠️ ' + data.error, 'error');
    return;
  }
  renderGame(data);
}

/* -------- Rendering -------- */

function renderGame(data) {
  const board = data.board;  // array of 9 strings: "X", "O", or ""
  const status = data.status;
  const currentPlayer = data.currentPlayer;
  const mode = data.mode;

  // Render cells
  const cells = document.querySelectorAll('.cell');
  cells.forEach((cell, i) => {
    const val = board[i];
    cell.textContent = val || '';
    cell.className = 'cell';
    if (val === 'X') cell.classList.add('taken', 'x');
    else if (val === 'O') cell.classList.add('taken', 'o');
  });

  // Highlight winning cells
  if (status === 'X_WINS' || status === 'O_WINS') {
    highlightWinner(board, status === 'X_WINS' ? 'X' : 'O');
  }

  // Status message
  boardLocked = false;
  if (status === 'X_WINS') {
    const label = mode === 'VS_COMPUTER' ? 'You win! 🎉' : 'Player X wins! 🎉';
    showStatus(label, 'win');
    scores.X++;
    document.getElementById('score-x').textContent = scores.X;
    boardLocked = true;
  } else if (status === 'O_WINS') {
    const label = mode === 'VS_COMPUTER' ? 'Computer wins! 🤖' : 'Player O wins! 🎉';
    showStatus(label, 'lose');
    scores.O++;
    document.getElementById('score-o').textContent = scores.O;
    boardLocked = true;
  } else if (status === 'DRAW') {
    showStatus("It's a draw! 🤝", 'draw');
    scores.draws++;
    document.getElementById('score-draws').textContent = scores.draws;
    boardLocked = true;
  } else {
    if (mode === 'VS_COMPUTER') {
      showStatus("Your turn (X)", 'turn');
    } else {
      showStatus(`Player ${currentPlayer}'s turn`, 'turn');
    }
  }

  // Mode label
  document.getElementById('mode-label').textContent =
    mode === 'VS_COMPUTER' ? '🤖 You (X) vs Computer (O)' : '👥 Player X vs Player O';
}

function showStatus(msg, type) {
  const el = document.getElementById('status');
  el.textContent = msg;
  el.className = 'status ' + (type || '');
}

const WIN_LINES = [
  [0,1,2],[3,4,5],[6,7,8],
  [0,3,6],[1,4,7],[2,5,8],
  [0,4,8],[2,4,6]
];

function highlightWinner(board, player) {
  for (const line of WIN_LINES) {
    if (line.every(i => board[i] === player)) {
      line.forEach(i => {
        document.querySelectorAll('.cell')[i].classList.add('winning');
      });
      break;
    }
  }
}

function updateScoreLabels(mode) {
  if (mode === 'VS_COMPUTER') {
    document.getElementById('score-x-label').textContent = 'You (X)';
    document.getElementById('score-o-label').textContent = 'Computer';
  } else {
    document.getElementById('score-x-label').textContent = 'Player X';
    document.getElementById('score-o-label').textContent = 'Player O';
  }
  // Reset scores on mode change only when going home
}
