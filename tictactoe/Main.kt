package tictactoe

import java.lang.Exception
import java.lang.NumberFormatException
import kotlin.math.abs

data class Coordinate(val x: Int, val y: Int)

sealed class Result<T, E> {
    class Success<T, E>(val value: T): Result<T, E>()
    class Continue<T, E>(val value: T): Result<T, E>()
    class Error<T, E>(val error: Throwable): Result<T, E>()
}

class ImpossibleGameField: Exception("Impossible")
class DrawGameField: Exception("Draw")
class NonDigitCoordinate: Exception("You should enter numbers!")
class CoordinateOutField: Exception("Coordinates should be from 1 to 3!")
class CellOccupied: Exception("This cell is occupied! Choose another one!")

class GameField(private val field: Array<CharArray>) {

    private var gameIsRunning = false
    private val emptyChar: Char = ' '

    private fun printGameField() {
        println("---------")
        for (line in field) {
            print("| ")
            for (c in line) {
                print("$c ")
            }
            println("|")
        }
        println("---------")
    }

    fun runGame() {
        printGameField()

        when (val result = analyzeGameField()) {
            is Result.Success -> {
                println(result.value)
            }
            is Result.Error -> {
                throw result.error
            }
            is Result.Continue -> {
                start()
            }
        }
    }

    private fun start() {
        gameIsRunning = true
        var round = 2
        while (gameIsRunning) {
            try {
                val coordinate = readCoordinate()
                val player = if (round % 2 == 0) 'X' else 'O'
                move(player, coordinate)
                printGameField()
                when (val result = analyzeGameField()) {
                    is Result.Success -> {
                        gameIsRunning = false
                        println(result.value)
                    }
                    is Result.Error -> {
                        throw result.error
                    }
                    is Result.Continue -> {
                        ++round
                    }
                }

            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    private fun move(char: Char, coordinate: Coordinate): Boolean {
        if (field.indices.contains(coordinate.y)) {
            if (field[coordinate.y].indices.contains(coordinate.x)) {
                field[coordinate.y][coordinate.x] = char
                return true
            }
        }
        return false
    }

    private fun readCoordinate(): Coordinate {
        try {
            val (y, x) = readln().split(" ")
            val coordinate = Coordinate(x.toInt()-1, y.toInt()-1)
            if (!coordinateInField(coordinate)) {
                throw CoordinateOutField()
            }
            if (!checkEmptyCell(coordinate)) {
                throw CellOccupied()
            }
            return coordinate
        } catch (e: NumberFormatException) {
            throw NonDigitCoordinate()
        }
    }

    private fun analyzeGameField(): Result<String, Throwable> = when {
        checkImpossible() -> Result.Error(ImpossibleGameField())
        checkWin('X') -> Result.Success("X wins")
        checkWin('O') -> Result.Success("O wins")
        checkEmptyCell() -> Result.Continue("Game not finished")
        else -> Result.Error(DrawGameField())
    }

    private fun checkWin(char: Char): Boolean {
        for (y in field.indices) {
            val line = field[y]
            for (x in line.indices) {
                if (char == line[x]) {
                    if (checkWinVertically(char, Coordinate(x, y))) {
                        println("$char won vertically")
                        return true
                    } else if (checkWinHorizontally(char, Coordinate(x, y))) {
                        println("$char won horizontally")
                        return true
                    } else if (checkWinDiagonally(char, Coordinate(x, y))) {
                        println("$char won diagonally")
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun checkImpossible(): Boolean {
        if (checkDiff()) {
            return true
        } else if (checkWin('X') && checkWin('O')) {
            return true
        }
        return false
    }

    private fun checkEmptyCell(): Boolean {
        for (line in field) {
            if (line.contains(emptyChar)) return true
        }
        return false
    }

    private fun checkEmptyCell(coordinate: Coordinate): Boolean {
        if (field.indices.contains(coordinate.y)) {
            if (field[coordinate.y].indices.contains(coordinate.x)) {
                return field[coordinate.y][coordinate.x] == emptyChar
            }
        }
        return false
    }

    private fun checkDiff(): Boolean {
        val countX = field.fold(0) { acc, s ->
            acc + s.count { it == 'X' }
        }

        val countO = field.fold(0) { acc, s ->
            acc + s.count { it == 'O' }
        }
        return abs(countX - countO) > 1
    }

    private fun coordinateInField(coordinate: Coordinate): Boolean {
        val containY = field.indices.contains(coordinate.y)
        var containX = false
        for (line in field) {
            containX = line.indices.contains(coordinate.x)
            if (containX) break
        }
        return containX && containY
    }

    private fun checkWinDiagonally(char: Char, coordinate: Coordinate): Boolean {
        var x = coordinate.x
        var count = 0
        for (y in coordinate.y..coordinate.y+2) {
            if (coordinateInField(Coordinate(x, y)) && field[y][x] == char) {
                ++x
                ++count
            } else {
                break
            }
        }

        if (count == 3) {
            return true
        }

        x = coordinate.x
        count = 0
        for (y in coordinate.y..coordinate.y+2) {
            if (coordinateInField(Coordinate(x, y)) && field[y][x] == char) {
                --x
                ++count
            } else {
                break
            }
        }
        return count == 3
    }

    private fun checkWinVertically(char: Char, coordinate: Coordinate): Boolean {
        for (y in coordinate.y..coordinate.y+2) {
            if (coordinateInField(Coordinate(coordinate.x, y)) && field[y][coordinate.x] == char) {
                continue
            } else {
                return false
            }
        }
        return true
    }

    private fun checkWinHorizontally(char: Char, coordinate: Coordinate): Boolean {
        for (x in coordinate.x..coordinate.x+2) {
            if (coordinateInField(Coordinate(x, coordinate.y)) && field[coordinate.y][x] == char) {
                continue
            } else {
                return false
            }
        }
        return true
    }
}

fun main() {
//    val gameField = GameField(readln().chunked(3).map { it.toCharArray() }.toTypedArray())
    val gameField = GameField(Array(3) { CharArray(3) { ' ' } })

    try {
        gameField.runGame()
    } catch (e: Exception) {
        println(e.message)
    }
}