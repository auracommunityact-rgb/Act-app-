package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

// --- 1. NEON SNAKE ARCADE (Classic Snake Game in Compose) ---

@Composable
fun NeonSnakeGame(
    onGameFinished: (Int) -> Unit
) {
    var score by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(0) }
    var snake by remember { mutableStateOf(listOf(Pair(10, 10), Pair(10, 11), Pair(10, 12))) }
    var food by remember { mutableStateOf(Pair(5, 5)) }
    var direction by remember { mutableStateOf(Pair(0, -1)) } // (dx, dy) moving UP initially
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    val gridSize = 20

    // Core Game Tick Loop
    LaunchedEffect(isPlaying, direction) {
        while (isPlaying && !isGameOver) {
            delay(150 - (score * 4).coerceAtMost(80).toLong()) // Acceleration as score grows

            val head = snake.first()
            val nextHead = Pair(
                (head.first + direction.first + gridSize) % gridSize,
                (head.second + direction.second + gridSize) % gridSize
            )

            // Self collision check
            if (snake.contains(nextHead)) {
                isGameOver = true
                isPlaying = false
                if (score > highScore) highScore = score
                onGameFinished(score)
            } else {
                val nextBody = mutableListOf(nextHead)
                if (nextHead == food) {
                    nextBody.addAll(snake) // Grow
                    score += 10
                    // Randomize food to unoccupied block
                    var validFood = false
                    while (!validFood) {
                        val potentialFood = Pair(Random.nextInt(gridSize), Random.nextInt(gridSize))
                        if (!snake.contains(potentialFood) && potentialFood != nextHead) {
                            food = potentialFood
                            validFood = true
                        }
                    }
                } else {
                    nextBody.addAll(snake.dropLast(1)) // Travel forward
                }
                snake = nextBody
            }
        }
    }

    fun handleReset() {
        snake = listOf(Pair(10, 10), Pair(10, 11), Pair(10, 12))
        direction = Pair(0, -1)
        score = 0
        isGameOver = false
        isPlaying = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("neon_snake_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🐍 NEON SNAKE",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Score: $score  High: $highScore",
                    color = Color(0xFFBD00FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Game Playfield Canvas
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF07090F))
                    .border(2.dp, Color(0xFF2B3245))
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scaleX = size.width / gridSize
                    val scaleY = size.height / gridSize

                    // Draw Food Pellet (Neon Pink Glow)
                    drawCircle(
                        color = Color(0xFFFF007A),
                        radius = (scaleX * 0.4f),
                        center = Offset(
                            food.first * scaleX + scaleX / 2f,
                            food.second * scaleY + scaleY / 2f
                        )
                    )

                    // Draw Snake segments
                    snake.forEachIndexed { index, segment ->
                        val color = if (index == 0) Color(0xFF00E5FF) else Color(0xFF00B2CC)
                        val inset = scaleX * 0.05f
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(segment.first * scaleX + inset, segment.second * scaleY + inset),
                            size = Size(scaleX - inset*2, scaleY - inset*2),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }
                }

                if (!isPlaying && !isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SNAKE ARENA", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { isPlaying = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                                modifier = Modifier.testTag("snake_start_btn")
                            ) {
                                Text("Launch Match", color = Color.White)
                            }
                        }
                    }
                }

                if (isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💥 OUT OF BOUNDS / CRASH", color = Color(0xFFFF007A), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Level Points Captured: $score", color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { handleReset() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                modifier = Modifier.testTag("snake_retry_btn")
                            ) {
                                Text("Respawn Ship", color = Color.Black)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controller D-Pad Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // UP
                IconButton(
                    onClick = { if (direction.second == 0) direction = Pair(0, -1) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF1D2230), CircleShape)
                        .testTag("dpad_up")
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = Color(0xFF00E5FF))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // LEFT
                    IconButton(
                        onClick = { if (direction.first == 0) direction = Pair(-1, 0) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF1D2230), CircleShape)
                            .testTag("dpad_left")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Move Left", tint = Color(0xFF00E5FF))
                    }

                    Spacer(modifier = Modifier.width(36.dp))

                    // RIGHT
                    IconButton(
                        onClick = { if (direction.first == 0) direction = Pair(1, 0) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF1D2230), CircleShape)
                            .testTag("dpad_right")
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Move Right", tint = Color(0xFF00E5FF))
                    }
                }

                // DOWN
                IconButton(
                    onClick = { if (direction.second == 0) direction = Pair(0, 1) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF1D2230), CircleShape)
                        .testTag("dpad_down")
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = Color(0xFF00E5FF))
                }
            }
        }
    }
}

// --- 2. CYBER STREET RACER (Interactive 2D Dodge Obstacles in Compose) ---

data class RaceBar(
    val id: Int,
    val lane: Int, // 0, 1, or 2
    var progressY: Float // 0.0 to 1.0 (reaches player at 0.85)
)

@Composable
fun CyberStreetRacerGame(
    onGameFinished: (Int) -> Unit
) {
    var score by remember { mutableStateOf(0) }
    var playerLane by remember { mutableStateOf(1) } // Default Middle Lane
    var barriers by remember { mutableStateOf(listOf<RaceBar>()) }
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var tickCounter by remember { mutableStateOf(0) }

    // Fast animation loop
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(50)
            tickCounter++

            // Move barriers down
            val updated = barriers.map {
                it.copy(progressY = it.progressY + 0.05f)
            }.filter {
                // If passed the screen border, score points
                if (it.progressY >= 1.0f) {
                    score += 5
                    false
                } else true
            }

            // Spawn barrier occasionally
            val current = updated.toMutableList()
            if (tickCounter % 15 == 0) {
                current.add(RaceBar(id = Random.nextInt(100000), lane = Random.nextInt(3), progressY = 0f))
            }

            // Crash Check
            val crashed = current.any { b ->
                b.progressY in 0.76f .. 0.88f && b.lane == playerLane
            }

            if (crashed) {
                isGameOver = true
                isPlaying = false
                onGameFinished(score)
            } else {
                barriers = current
            }
        }
    }

    fun handleReset() {
        barriers = listOf(RaceBar(1, 0, 0.1f), RaceBar(2, 2, 0.4f))
        playerLane = 1
        score = 0
        isGameOver = false
        isPlaying = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cyber_racer_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
        border = BorderStroke(1.dp, Color(0xFFBD00FF).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏎️ CYBER STREET RACER",
                    color = Color(0xFFBD00FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Km/H: ${100 + score} Score: $score",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Racing Screen Layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color(0xFF0A0F1D))
                    .border(2.dp, Color(0xFF2B3245))
                    .clip(RoundedCornerShape(8.dp))
            ) {
                // Lines / Lanes
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw Lane Dividers (Dotted neon blue lines)
                    drawLine(Color(0xFF2B3245), Offset(w/3f, 0f), Offset(w/3f, h), strokeWidth = 3f)
                    drawLine(Color(0xFF2B3245), Offset((w*2f)/3f, 0f), Offset((w*2f)/3f, h), strokeWidth = 3f)

                    // Draw Enemy obstacles (Chroma red gates)
                    barriers.forEach { b ->
                        val startX = (b.lane * w/3f) + (w/6f)
                        val sizeBarrierY = 24.dp.toPx()
                        val posBarrierY = b.progressY * (h - sizeBarrierY)
                        drawRoundRect(
                            color = Color(0xFFFF007A),
                            topLeft = Offset(startX - 25.dp.toPx(), posBarrierY),
                            size = Size(50.dp.toPx(), sizeBarrierY),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                        )
                    }

                    // Draw Player car (Cyan speed jet)
                    val playerX = (playerLane * w/3f) + (w/6f)
                    val carY = h * 0.8f
                    drawRect(
                        color = Color(0xFF00E5FF),
                        topLeft = Offset(playerX - 18.dp.toPx(), carY),
                        size = Size(36.dp.toPx(), 30.dp.toPx())
                    )
                    // Draw neon light trail
                    drawRect(
                        color = Color(0xFFBD00FF).copy(alpha = 0.5f),
                        topLeft = Offset(playerX - 12.dp.toPx(), carY + 30.dp.toPx()),
                        size = Size(24.dp.toPx(), 12.dp.toPx())
                    )
                }

                if (!isPlaying && !isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CYBER TRACK LOBBY", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Dodge hot-pink barriers! Switch lanes rapidly.", color = Color.LightGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { handleReset() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD00FF)),
                                modifier = Modifier.testTag("racer_start_btn")
                            ) {
                                Text("Engines ON", color = Color.White)
                            }
                        }
                    }
                }

                if (isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💥 VEHICLE COLLIDED!", color = Color(0xFFFF007A), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Championship Score: $score", color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { handleReset() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                modifier = Modifier.testTag("racer_retry_btn")
                            ) {
                                Text("Deploy New Car", color = Color.Black)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lateral Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { if (playerLane > 0) playerLane-- },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2230)),
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp).testTag("btn_steer_left"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF))
                ) {
                    Text("◀ STEER LEFT", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { if (playerLane < 2) playerLane++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D2230)),
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp).testTag("btn_steer_right"),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFBD00FF))
                ) {
                    Text("STEER RIGHT ▶", color = Color(0xFFBD00FF), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 3. MEMORY MATCH TILES (Tile Matching Brain Game) ---

data class MemoryTile(
    val index: Int,
    val value: String,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

@Composable
fun MemoryMatchTilesGame(
    onGameFinished: (Int) -> Unit
) {
    var tiles by remember { mutableStateOf(listOf<MemoryTile>()) }
    var selectedIndexes by remember { mutableStateOf(listOf<Int>()) }
    var moves by remember { mutableStateOf(0) }
    var matchesFound by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    // Standard gaming items represented by cool emoji representations
    val symbols = listOf("🎮", "📞", "👑", "🗡️", "🔑", "🛡️", "💎", "🏆")

    fun initializeGame() {
        val pairs = (symbols + symbols).shuffled()
        tiles = pairs.mapIndexed { idx, symbol ->
            MemoryTile(idx, symbol)
        }
        selectedIndexes = emptyList()
        moves = 0
        matchesFound = 0
        isGameOver = false
    }

    LaunchedEffect(Unit) {
        initializeGame()
    }

    // Handles flip comparisons
    fun handleTileClick(index: Int) {
        val clickedTile = tiles[index]
        if (clickedTile.isFlipped || clickedTile.isMatched || selectedIndexes.size >= 2) return

        // Flip the clicked tile
        tiles = tiles.map {
            if (it.index == index) it.copy(isFlipped = true) else it
        }

        val nextSelection = selectedIndexes + index
        selectedIndexes = nextSelection

        if (nextSelection.size == 2) {
            moves++
            val first = tiles[nextSelection[0]]
            val second = tiles[nextSelection[1]]

            if (first.value == second.value) {
                // Match Found!
                tiles = tiles.map {
                    if (it.index == first.index || it.index == second.index) {
                        it.copy(isMatched = true)
                    } else it
                }
                matchesFound++
                selectedIndexes = emptyList()
                if (matchesFound == symbols.size) {
                    isGameOver = true
                    onGameFinished(200 - (moves * 5).coerceAtLeast(0))
                }
            } else {
                // Flash flip back
                // Delay 1 second, then flip both back
            }
        }
    }

    // Delayed flip back effect
    LaunchedEffect(selectedIndexes) {
        if (selectedIndexes.size == 2) {
            delay(1000)
            val first = tiles[selectedIndexes[0]]
            val second = tiles[selectedIndexes[1]]
            if (first.value != second.value) {
                tiles = tiles.map {
                    if (it.index == first.index || it.index == second.index) {
                        it.copy(isFlipped = false)
                    } else it
                }
            }
            selectedIndexes = emptyList()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("memory_match_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121620)),
        border = BorderStroke(1.dp, Color(0xFF00FF85).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🧠 MEMORY MASTER TILES",
                    color = Color(0xFF00FF85),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Moves: $moves Matches: $matchesFound/8",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (tiles.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(220.dp)
                ) {
                    items(tiles.size) { index ->
                        val tile = tiles[index]
                        val showSymbol = tile.isFlipped || tile.isMatched
                        val bgColor = when {
                            tile.isMatched -> Color(0xFF00FF85).copy(alpha = 0.35f)
                            showSymbol -> Color(0xFF1D2230)
                            else -> Color(0xFF2B3245)
                        }
                        val borderColor = when {
                            tile.isMatched -> Color(0xFF00FF85)
                            showSymbol -> Color(0xFF00E5FF)
                            else -> Color(0xFF121620)
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                .clickable { handleTileClick(index) }
                                .testTag("memory_tile_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (showSymbol) {
                                Text(tile.value, fontSize = 24.sp)
                            } else {
                                Text(
                                    "?",
                                    color = Color(0xFF8E9AA8),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isGameOver) {
                Text(
                    text = "🎉 VICTORY! ALL MATCHES SECURED!",
                    color = Color(0xFF00FF85),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { initializeGame() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF85)),
                modifier = Modifier.fillMaxWidth().testTag("memory_restart_btn")
            ) {
                Text("Shuffle Board & Restart", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
