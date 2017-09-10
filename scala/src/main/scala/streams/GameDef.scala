package streams

import java.nio.LongBuffer

import scala.annotation.tailrec

trait GameDef {

  type u64 = Long

  sealed abstract class Move

  case object Left extends Move

  case object Right extends Move

  case object Up extends Move

  case object Down extends Move

  def printStack(s: List[Board]) = {

    val time = System.currentTimeMillis - start_time
    println(s"\nSolution# $Nsolution ms: $time ----------------- start stack moves ------------------------ ")
    s.reverse.foreach { y =>
      y.printBoard
      println
    }
    print(s"\nstack solution: $Nsolution ")
    s.reverse.foreach(t => print("0x" + t.board.toHexString + "L, "))

    println(s"\n\nSolution# $Nsolution ms: $time Nmoves: $nmoves Hash cuts: $cut (${cut * 100 / nmoves}%) ----------- end stack moves ----------- ")
  }

  case class Board(board: u64) {

    def bitsStream(bits: u64, move: Move): Stream[(u64, (u64, u64))] =
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
        if (!freeSquare(captured) && freeSquare(to))
          (from, (captured, to)) #:: bitsStream(bits & ~from, move)
        else bitsStream(bits & ~from, move)
      }

    def movements = List(
      bitsStream(board & BOARD_to_RIGHT, Right),
      bitsStream(board & BOARD_to_UP, Up),
      bitsStream(board & BOARD_to_DOWN, Down),
      bitsStream(board & BOARD_to_LEFT, Left))

    def freeSquare(a: u64) = (a & board) == 0

    def makemove(from: u64, to: u64, capture: u64, stack: List[Board]): (Board, List[Board]) = {
      val x = copy(board = ((board & ~from) | to) & ~capture)
      (x, x :: stack)
    }

    def printBoard: Unit = {
      printBoard(63)
      print("bit mask: 0x" + board.toHexString + "L")
    }

    private def printBoard(k: Int): Unit = k match {
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
        printBoard(k - 1)
    }

    @tailrec
    final def popCount(x: u64 = board, count: Int = 0): Int =
      if (x == 0) count
      else popCount(x & (x - 1), count + 1)

  }

  var nmoves, cut: u64 = 0
  var Nsolution = 0

  val HASH_SIZE = 1229498

  val start_time: Long

  val INIT_BOARD: u64
  val TERRAIN: u64

  val TOT: Int

  val hash_array: LongBuffer

  def pow2(a: Int) = math.pow(2, a).toLong

  lazy val BOARD_to_RIGHT = TERRAIN << 2 & 0xfcfcfcfcfcfcfcfcL

  lazy val BOARD_to_LEFT = TERRAIN >>> 2 & 0x3f3f3f3f3f3f3f3fL

  lazy val BOARD_to_UP = TERRAIN >> 16 & 0xffffffffffffL

  lazy val BOARD_to_DOWN = TERRAIN << 16 & 0xffffffffffff0000L

}