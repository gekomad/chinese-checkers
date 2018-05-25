package chineseCheckers

import chineseCheckers.solver.{GameDef, Solver}

object Main extends App {

  new GameDef(HASH_SIZE = 1229498, TERRAIN = 0x3838FEFEFE3838L) with Solver {
    val INIT_BOARD = 0x3838FEEEFE3838L
    /* TERRAIN
    # # # # # # # #
    # # O O O # # #
    # # O O O # # #
    O O O O O O O #
    O O O O O O O #
    O O O O O O O #
    # # O O O # # #
    # # O O O # # #
     */

    /* INIT_BOARD
    # # # # # # # #
    # # O O O # # #
    # # O O O # # #
    O O O O O O O #
    O O O - O O O #
    O O O O O O O #
    # # O O O # # #
    # # O O O # # #
     */


    printBoard(INIT_BOARD)
    println("\n------------------------")
    override val startTime = System.currentTimeMillis
    val l: Stream[List[Long]] = gen((INIT_BOARD, List.empty), Stream.empty,popCount(INIT_BOARD))

    l.foreach {
        println("solution:")
        _.reverse.foreach(println)
    }
  }
}
