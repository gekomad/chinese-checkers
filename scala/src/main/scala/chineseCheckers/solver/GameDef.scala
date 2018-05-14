package chineseCheckers.solver

import java.nio.LongBuffer

import scala.annotation.tailrec

abstract class GameDef(val HASH_SIZE: Int, val INIT_BOARD: Long, val TERRAIN: Long) {

  sealed abstract class Move

  case object Left extends Move

  case object Right extends Move

  case object Up extends Move

  case object Down extends Move

  def printStack(s: List[Long]) = {

    val time = System.currentTimeMillis - startTime
    println(s"\nSolution# $Nsolution ms: $time ----------------- start stack moves ------------------------ ")
    s.reverse.foreach { y =>
      printBoard(y)
      println
    }
    print(s"\nstack solution: $Nsolution | ")
    s.reverse.foreach(t => print("0x" + t.toHexString + "L, "))

    println(s"\n\nSolution# $Nsolution ms: $time Nmoves: $nmoves Hash cuts: $cut (${cut * 100 / nmoves}%) ----------- end stack moves ----------- ")
  }

  def freeSquare(a: Long, b: Long) = (a & b) == 0

  def bitsStream(board: Long, bits: Long, move: Move): Stream[(Long, (Long, Long))] =
    if (bits == 0)
      Stream.empty
    else {
      val from = bits ^ (bits & bits - 1)
      val (captured, to) = move match {
        case Right => (from >>> 1, from >>> 2)
        case Up => (from << 8, from << 16)
        case Down => (from >>> 8, from >>> 16)
        case Left => (from << 1, from << 2)
      }
      if (!freeSquare(board, captured) && freeSquare(board, to))
        (from, (captured, to)) #:: bitsStream(board, bits & ~from, move)
      else bitsStream(board, bits & ~from, move)
    }

  def movements(board: Long) = List(
    bitsStream(board, board & BOARD_to_RIGHT, Right),
    bitsStream(board, board & BOARD_to_UP, Up),
    bitsStream(board, board & BOARD_to_DOWN, Down),
    bitsStream(board, board & BOARD_to_LEFT, Left))

  def printBoard(board: Long): Unit = {
    printBoard(board, 63)
    print("bit mask: 0x" + board.toHexString + "L")
  }

  def printBoard(board: Long, k: Int): Unit = k match {
    case 0 =>
    case _ =>
      if ((board & pow2(k - 1)) == 0) {
        if ((TERRAIN & pow2(k - 1)) == 0)
          print(" \t")
        else
          print("-\t")
      } else
        print("O\t")

      if ((k - 1) % 8 == 0)
        println
      printBoard(board, k - 1)
  }

  @tailrec
  final def popCount(board: Long, count: Int = 0): Int =
    if (board == 0) count
    else popCount(board & (board - 1), count + 1)

  val board = INIT_BOARD
  val TOT: Int = popCount(board)
  val hashArray: LongBuffer = LongBuffer.allocate(HASH_SIZE)

  val startTime: Long = System.currentTimeMillis

  def pow2(a: Int): Long = math.pow(2, a).toLong

  lazy val BOARD_to_RIGHT = TERRAIN << 2 & 0xfcfcfcfcfcfcfcfcL

  lazy val BOARD_to_LEFT = TERRAIN >>> 2 & 0x3f3f3f3f3f3f3f3fL

  lazy val BOARD_to_UP = TERRAIN >> 16 & 0xffffffffffffL

  lazy val BOARD_to_DOWN = TERRAIN << 16 & 0xffffffffffff0000L

  //for log
  var nmoves: Long = 0
  var cut: Long = 0
  var Nsolution = 0

}