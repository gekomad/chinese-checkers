package com.github.gekomad.chinesecheckers;

import java.util.*;


class Puzzle extends ChineseCheckers {

    private final int MAX_PIECES;
    private long found;
    private final List<Move> moves = new ArrayList<Move>(4);
    private long start;

    public Puzzle(long terrain, int maxPieces) {
        super(0, terrain);
        this.MAX_PIECES = maxPieces;
        moves.add(Move.UP);
        moves.add(Move.DOWN);
        moves.add(Move.LEFT);
        moves.add(Move.RIGHT);
    }

    private static List<Long> shufflePieces(long bits) {
        List<Long> l = new ArrayList<Long>();
        while (bits != 0) {
            long from = BITScanForward(bits);
            l.add(from);
            bits &= ~from;
        }
        Collections.shuffle(l);
        return l;
    }

    // -1 invalid 1 piece 0 no piece
    private int inBoard(long bit, long board,long TERRAIN) {
        if ((bit & TERRAIN) == 0) return -1;
        if ((bit & board) == 0) return 0;
        return 1;
    }

    private FromToCaptured randomMove(long piece, List<ChineseCheckers.Move> moves, long board) {
        if (moves.isEmpty()) return null;
        ChineseCheckers.Move n = moves.get(new Random().nextInt(moves.size()));
        switch (n) {
            case RIGHT: {
                int to = inBoard(piece >>> 2, board & BOARD_to_LEFT,TERRAIN & BOARD_to_LEFT);
                int captured = inBoard(piece >>> 1, board & BOARD_to_LEFT,TERRAIN & BOARD_to_LEFT);
                if (to != -1 && to == 0 && captured != -1 && captured == 0)
//                    if ((piece >>> 1 & BOARD_to_LEFT) != 0 && (piece >>> 1 & TERRAIN) != 0 && (piece >>> 2 & BOARD_to_LEFT) != 0 && (piece >>> 2 & TERRAIN) != 0)
                    return new FromToCaptured(piece >>> 1, piece >>> 2, piece);
                else {
                    moves.remove(ChineseCheckers.Move.RIGHT);
                    randomMove(piece, moves, board);
                }
                break;
            }
            case UP: {
                int to = inBoard(piece << 8, board & BOARD_to_DOWN,TERRAIN & BOARD_to_DOWN);
                int captured = inBoard(piece << 16, board & BOARD_to_DOWN,TERRAIN & BOARD_to_DOWN);
                if (to != -1 && to == 0 && captured != -1 && captured == 0)
//                if ((piece << 8 & BOARD_to_DOWN) != 0 && (piece << 8 & TERRAIN) != 0 && (piece << 16 & BOARD_to_DOWN) != 0 && (piece << 16 & TERRAIN) != 0)
                    return new FromToCaptured(piece << 8, piece << 16, piece);
                else {
                    moves.remove(ChineseCheckers.Move.UP);
                    randomMove(piece, moves, board);
                }
                break;
            }
            case DOWN: {
                int to = inBoard(piece >> 8, board & BOARD_to_UP,TERRAIN & BOARD_to_UP);
                int captured = inBoard(piece >>> 16, board & BOARD_to_UP,TERRAIN & BOARD_to_UP);
                if (to != -1 && to == 0 && captured != -1 && captured == 0)
//                if ((piece >>> 8 & BOARD_to_UP) != 0 && (piece >>> 8 & TERRAIN) != 0 && (piece >>> 16 & BOARD_to_UP) != 0 && (piece >>> 16 & TERRAIN) != 0)
                    return new FromToCaptured(piece >>> 8, piece >>> 16, piece);
                else {
                    moves.remove(Move.DOWN);
                    randomMove(piece, moves, board);
                }
                break;
            }
            case LEFT: {
                int to = inBoard(piece << 1, board & BOARD_to_UP,TERRAIN & BOARD_to_UP);
                int captured = inBoard(piece << 2, board & BOARD_to_UP,TERRAIN & BOARD_to_UP);
                if (to != -1 && to == 0 && captured != -1 && captured == 0)
//                if ((piece << 1 & BOARD_to_RIGHT) != 0 && (piece << 1 & TERRAIN) != 0 && (piece << 2 & BOARD_to_RIGHT) != 0 && (piece << 2 & TERRAIN) != 0)
                    return new FromToCaptured(piece << 1, piece << 2, piece);
                else {
                    moves.remove(Move.LEFT);
                    randomMove(piece, moves, board);
                }
                break;
            }
            default:
                return null;
        }
        return null;
    }

    long puzzle() {
        found = 0;
        start = new Date().getTime();
        for (long m : Puzzle.shufflePieces(TERRAIN)) {
            puzzle(m, 0);
            if (found != 0)
                break;
        }
        if (found != 0) print(found);
        else System.out.println("no solutions");
        return found;
    }

    private void puzzle(long bits, int count) {

        System.out.println("count " + count);
        print(bits);
        //if (count > 200 || exceedTime() || found != 0) return;
        if (popCount(bits) >= MAX_PIECES) {
            found = bits;
            return;
        }
        List<Long> ll = shufflePieces(bits);

        for (long l : ll) {

            //print(l);
            if (found == 0) {
                List<Move> moves2 = new ArrayList<Move>(moves);

                FromToCaptured fromTo = randomMove(l, moves2, bits);
                if (fromTo != null) {
                    bits |= fromTo.getFrom();
                    bits |= fromTo.getCaptured();
                    bits &= ~fromTo.getTo();
                    puzzle(bits, count + 1);
                }
            }
        }
    }

    private boolean exceedTime() {
        int seconds = 5 * 1000;
        return ((new Date()).getTime() - start) > seconds;
    }

}
