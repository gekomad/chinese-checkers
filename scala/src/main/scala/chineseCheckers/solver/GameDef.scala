package chineseCheckers.solver

import java.nio.LongBuffer
import scala.annotation.tailrec
import scala.util.Random

abstract class GameDef(val HASH_SIZE: Int, val TERRAIN: Long) {

  sealed abstract class Move

  case object Left extends Move

  case object Right extends Move

  case object Up extends Move

  case object Down extends Move

  type From     = Long
  type Captured = Long
  type To       = Long

  def next(bits: Long): Long = {

    @tailrec
    def go(bits: Long, count: Int): Long =
      if (bits == 0) 0
      else {
        assert((bits & TERRAIN) == bits)
        val rand: Option[(Captured, From, To)] = randomDirection(bits)
        rand match {
          case None => 0
          case Some(move) =>
            val (captured, from, to) = (move._1, move._2, move._3)
            if ((from & TERRAIN) != 0 && (captured & TERRAIN) != 0 && emptySquare(bits, captured) &&
                emptySquare(bits, from)) (bits | from | captured) & ~to
            else go(bits, count + 1)
        }
      }
    go(bits, 1)
  }

  def random(l: List[Int]): Int = l(Random.nextInt(l.size))

  def randomDirection(board: Long): Option[(Captured, From, To)] = {

    @tailrec
    def go(to: Long, directions: List[Int]): Option[(Captured, From, To)] =
      if (directions.isEmpty) None
      else
        random(directions) match {
          case 0 =>
            if ((to >>> 1 & BOARD_to_LEFT) != 0 && (to >>> 2 & BOARD_to_LEFT) != 0) Some((to >>> 1, to >>> 2, to))
            else go(to, directions diff List(0))
          case 1 =>
            if ((to << 8 & BOARD_to_DOWN) != 0 && (to << 16 & BOARD_to_DOWN) != 0) Some((to << 8, to << 16, to))
            else go(to, directions diff List(1))
          case 2 =>
            if ((to >>> 8 & BOARD_to_UP) != 0 && (to >>> 16 & BOARD_to_UP) != 0) Some((to >>> 8, to >>> 16, to))
            else go(to, directions diff List(2))
          case 3 =>
            if ((to << 1 & BOARD_to_RIGHT) != 0 && (to << 2 & BOARD_to_RIGHT) != 0) Some((to << 1, to << 2, to))
            else go(to, directions diff List(3))
        }

    @tailrec
    def m(randomList: List[Long]): Option[(Captured, From, To)] = {
      if (randomList.isEmpty) None
      else {
        val p = go(randomList.head, List(0, 1, 2, 3))
        if (p.isEmpty) m(randomList.tail) else p
      }
    }

    val bitsList = scala.util.Random.shuffle(allBits(board))

    val move1 = m(bitsList)

    move1
  }

  def randomBit(b: Long): Long = {
    val l = scala.util.Random.shuffle(allBits(b))
    l(Random.nextInt(l.size))
  }

  def allBits(bits: Long): List[Long] =
    if (bits == 0) Nil
    else {
      val from = bits ^ (bits & bits - 1)
      from :: allBits(bits & ~from)
    }

  def printStack(s: List[Long]): Unit = {

    val time = System.currentTimeMillis - startTime
    println(s"\nSolution# $Nsolution ms: $time ----------------- start stack moves ------------------------ ")
    s.reverse.foreach { y =>
      printBoard(y)
      println()
    }
    print(s"\nstack solution: $Nsolution | ")
    s.reverse.foreach(t => print("0x" + t.toHexString + "L, "))

    println(
      s"\n\nSolution# $Nsolution ms: $time Nmoves: $nmoves Hash cuts: $cut (${cut * 100 / nmoves}%) ----------- end stack moves ----------- "
    )
  }

  def emptySquare(a: Long, b: Long): Boolean = (a & b) == 0

  def bitsStream(board: Long, bits: Long, move: Move): LazyList[(Long, (Long, Long))] =
    if (bits == 0)
      LazyList.empty
    else {
      val from = bits ^ (bits & bits - 1)
      val (captured, to) = move match {
        case Right => (from >>> 1, from >>> 2)
        case Up    => (from << 8, from << 16)
        case Down  => (from >>> 8, from >>> 16)
        case Left  => (from << 1, from << 2)
      }
      if (!emptySquare(board, captured) && emptySquare(board, to))
        (from, (captured, to)) #:: bitsStream(board, bits & ~from, move)
      else bitsStream(board, bits & ~from, move)
    }

  def movements(board: Long) =
    List(
      bitsStream(board, board & BOARD_to_RIGHT, Right),
      bitsStream(board, board & BOARD_to_UP, Up),
      bitsStream(board, board & BOARD_to_DOWN, Down),
      bitsStream(board, board & BOARD_to_LEFT, Left)
    )

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

      if ((k - 1) % 8 == 0) println()
      printBoard(board, k - 1)
  }

  final def popCount(board: Long): Int = java.lang.Long.bitCount(board)

  val hashArray: LongBuffer = LongBuffer.allocate(HASH_SIZE)

  val startTime: Long = System.currentTimeMillis

  def pow2(a: Int): Long = math.pow(2, a).toLong

  lazy val BOARD_to_RIGHT: Long = TERRAIN << 2 & 0xfcfcfcfcfcfcfcfcL & TERRAIN

  lazy val BOARD_to_LEFT: Long = TERRAIN >>> 2 & 0x3f3f3f3f3f3f3f3fL & TERRAIN

  lazy val BOARD_to_UP: Long = TERRAIN >> 16 & 0xffffffffffffL & TERRAIN

  lazy val BOARD_to_DOWN: Long = TERRAIN << 16 & 0xffffffffffff0000L & TERRAIN

  //for log
  var nmoves: Long = 0
  var cut: Long    = 0
  var Nsolution    = 0
  var maxSolution  = 100000000
}
