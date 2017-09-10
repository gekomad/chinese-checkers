package streams

import java.nio.LongBuffer

import org.scalatest.FunSuite

class ChineseCheckerSuite extends FunSuite {

  test("shift") {

    new Type1 {
      override val start_time = 0
      val board = Board(0)
      override val TOT = board.popCount()
      override val hash_array = LongBuffer.allocate(HASH_SIZE)
      assert(board.bitsStream(0, Right) == Stream.empty)

      val a = 100L
      val b = a >>> 1
      val c = b << 1
      assert(a == c)

      val bits = 0xc000000000000000L
      val prec = bits >>> 1
      assert(0x6000000000000000L == prec)

    }
  }

  test("bitsStream") {

    new Type1 {
      override val start_time = 0
      val board = Board(100)
      override val TOT = board.popCount()
      override val hash_array = LongBuffer.allocate(HASH_SIZE)
      val o = for {
        l2 <- board.bitsStream(8, Right)
      } yield l2

      assert(List((8, (4, 2))) == o.toList)

    }
  }

  test("solution") {

    new Type1 {

      /**
        * # # - - - # # #
        * # # - - - # # #
        * - - - - - - - #
        * - - - - O O - #
        * - - - - - - - #
        * # # - - - # # #
        * # # - - - # # #
        **/

      override val INIT_BOARD = 0xc000000L
      val board = Board(INIT_BOARD)
      override val TOT = board.popCount()
      override val hash_array = LongBuffer.allocate(HASH_SIZE)

      board.printBoard
      println("\n------------------------")
      override val start_time = System.currentTimeMillis
      val l: Stream[List[Board]] = gen(board, List())

      assert(l.flatten == Stream(Board(33554432), Board(268435456)))

    }
  }


}
