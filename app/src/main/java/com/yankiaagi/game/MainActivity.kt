package com.yankiaagi.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            YankiAgiGame()
        }
    }
}

data class Node(
    var top: Boolean,
    var right: Boolean,
    var bottom: Boolean,
    var left: Boolean,
    var rotation: Int = 0
)

@Composable
fun YankiAgiGame() {

    val size = 4

    var moves by remember { mutableIntStateOf(0) }

    var nodes by remember {
        mutableStateOf(createPuzzle(size))
    }

    val connected = calculateConnected(nodes, size)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071018))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "YANKI AĞI",
            color = Color.White,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "SEVİYE 1 • 4×4",
            color = Color(0xFF65D8FF),
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Hamle: $moves",
                color = Color.White,
                fontSize = 16.sp
            )

            Text(
                text = "Bağlantı: $connected%",
                color = Color(0xFF65D8FF),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(25.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {

            for (row in 0 until size) {

                Row(
                    modifier = Modifier.weight(1f)
                ) {

                    for (col in 0 until size) {

                        val index = row * size + col

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .border(
                                    1.dp,
                                    Color(0xFF263946)
                                )
                                .clickable {

                                    val newNodes =
                                        nodes.toMutableList()

                                    newNodes[index] =
                                        rotateNode(newNodes[index])

                                    nodes = newNodes
                                    moves++
                                },
                            contentAlignment = Alignment.Center
                        ) {

                            NodeView(nodes[index])
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        if (connected == 100) {

            Text(
                text = "BÖLÜM TAMAMLANDI!",
                color = Color(0xFF66FFAA),
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    nodes = createPuzzle(size)
                    moves = 0
                }
            ) {
                Text("Yeni Bölüm")
            }
        }
    }
}

@Composable
fun NodeView(node: Node) {

    Box(
        modifier = Modifier
            .size(55.dp)
            .background(
                Color(0xFF10212B),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (node.top)
                Connector()

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (node.left)
                    ConnectorHorizontal()

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(
                            Color(0xFF65D8FF),
                            CircleShape
                        )
                )

                if (node.right)
                    ConnectorHorizontal()
            }

            if (node.bottom)
                Connector()
        }
    }
}

@Composable
fun Connector() {

    Box(
        modifier = Modifier
            .width(5.dp)
            .height(14.dp)
            .background(Color(0xFF65D8FF))
    )
}

@Composable
fun ConnectorHorizontal() {

    Box(
        modifier = Modifier
            .width(14.dp)
            .height(5.dp)
            .background(Color(0xFF65D8FF))
    )
}

fun rotateNode(node: Node): Node {

    return Node(
        top = node.left,
        right = node.top,
        bottom = node.right,
        left = node.bottom
    )
}

fun createPuzzle(size: Int): List<Node> {

    val result = MutableList(size * size) {

        Node(
            top = false,
            right = false,
            bottom = false,
            left = false
        )
    }

    for (row in 0 until size) {

        for (col in 0 until size) {

            val index = row * size + col

            val right =
                col < size - 1 && Random.nextBoolean()

            val bottom =
                row < size - 1 && Random.nextBoolean()

            result[index] =
                Node(
                    top = row > 0 &&
                            result[index - size].bottom,

                    right = right,

                    bottom = bottom,

                    left = col > 0 &&
                            result[index - 1].right
                )
        }
    }

    return result.map {

        var node = it

        repeat(Random.nextInt(4)) {
            node = rotateNode(node)
        }

        node
    }
}

fun calculateConnected(
    nodes: List<Node>,
    size: Int
): Int {

    val total = nodes.size

    if (total == 0) return 0

    val visited = BooleanArray(total)

    val queue = ArrayDeque<Int>()

    queue.add(0)
    visited[0] = true

    while (queue.isNotEmpty()) {

        val index = queue.removeFirst()

        val row = index / size
        val col = index % size

        val node = nodes[index]

        if (node.top && row > 0) {

            val next = index - size

            if (nodes[next].bottom &&
                !visited[next]
            ) {
                visited[next] = true
                queue.add(next)
            }
        }

        if (node.right && col < size - 1) {

            val next = index + 1

            if (nodes[next].left &&
                !visited[next]
            ) {
                visited[next] = true
                queue.add(next)
            }
        }

        if (node.bottom && row < size - 1) {

            val next = index + size

            if (nodes[next].top &&
                !visited[next]
            ) {
                visited[next] = true
                queue.add(next)
            }
        }

        if (node.left && col > 0) {

            val next = index - 1

            if (nodes[next].right &&
                !visited[next]
            ) {
                visited[next] = true
                queue.add(next)
            }
        }
    }

    return visited.count { it } * 100 / total
}
