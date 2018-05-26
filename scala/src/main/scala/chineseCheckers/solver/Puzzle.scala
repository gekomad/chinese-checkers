package chineseCheckers.solver

import scala.annotation.tailrec

trait Puzzle extends GameDef {

  def puzzle(bits: Long, nPieces: Int): Option[Long] = {
    require(nPieces > 1)
    require(nPieces < popCount(TERRAIN))

    @tailrec
    def go(bits: Long): Long = {
      val nx = next(bits)
      if (nx == 0) 0 else if (popCount(nx) == nPieces) nx else
        go(nx)
    }

    val l = go(bits)
    if (l == 0) None else Some(l)
  }
}
