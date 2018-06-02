package com.github.gekomad.chinesecheckers;

import static java.lang.Math.pow;

class ChineseCheckers {

    private long board;
    private final long[] stack = new long[64];
    private final int TOT;
    private int nstack = 0;

    private final int HASH_SIZE = 1229498;

// Bitboard Calculator: http://cinnamonchess.altervista.org/bitboard_calculator/Calc.html

    public enum Move {
        RIGHT, LEFT, UP, DOWN
    }

    final long TERRAIN;

    long BOARD_to_RIGHT;
    long BOARD_to_LEFT;
    long BOARD_to_UP;
    long BOARD_to_DOWN;
    private int maxSolutions = 1000000000;
    private int Nsolution;
    private final long startTime;

    private final long[] hashArray = new long[HASH_SIZE];

    private long nmoves = 0, cut = 0;


    ChineseCheckers(long i, long t) {
        board = i;
        TOT = popCount(board);
        TERRAIN = t;
        BOARD_to_RIGHT = (TERRAIN << 2) & 0xfcfcfcfcfcfcfcfcL & TERRAIN;
        BOARD_to_LEFT = (TERRAIN >>> 2) & 0x3f3f3f3f3f3f3f3fL & TERRAIN;
        BOARD_to_DOWN = (TERRAIN << 16) & TERRAIN;
        BOARD_to_UP = (TERRAIN >>> 16) & TERRAIN;

        startTime = System.currentTimeMillis();
    }

    static long BITScanForward(long b) {
        return (b ^ (b & b - 1));
    }

    public void setMaxSolutions(int maxSolutions) {
        this.maxSolutions = maxSolutions;
    }

    public static void print(long board, long TERRAIN) {
        for (int k = 63; k >= 1; k--) {
            if ((k == 63 && (0x8000000000000000L & board) == 0) || k != 63 && (board & (long) pow(2, k)) == 0) {
                if ((k == 63 && (0x8000000000000000L & TERRAIN) == 0) || k != 63 && (TERRAIN & (long) pow(2, k)) == 0)
                    System.out.print(" \t");
                else
                    System.out.print("-\t");
            } else
                System.out.print("O\t");
            if ((k) % 8 == 0)
                System.out.println();
        }
        System.out.println(board);
    }

    int popCount(long board) {
        return Long.bitCount(board);
    }

    private void makemove(long from, long to, long capture) {
        stack[nstack] = board;
        board &= ~from;
        board |= to;
        board &= ~capture;
        nstack++;
    }

    private void undomove() {
        nstack--;
        board = stack[nstack];
    }

    void gen() {
        _gen();
    }

    private void _gen() {
        if (Nsolution >= maxSolutions) return;
        long from, bits, to, capture;
        if (0 == ((++nmoves) % 50000000L)) {
            System.out.println("Nmoves: " + nmoves + " cut: " + cut + " " + cut * 100 / nmoves + "%");
        }

        int x = (int) Math.abs(board % HASH_SIZE);

        if (hashArray[x] == board) {
            cut++;
            return;
        }

        if (nstack == TOT - 1) {
            printStack();
            return;
        }

        bits = board & BOARD_to_RIGHT;
        while (bits != 0) {
            from = BITScanForward(bits);
            capture = from >>> 1;
            if ((board & capture) != 0 && (0 == (board & (to = from >>> 2)))) {
                makemove(from, to, capture);
                _gen();
                undomove();
            }
            bits &= ~from;
        }

        bits = board & BOARD_to_UP;
        while (bits != 0) {
            from = BITScanForward(bits);
            capture = from << 8;
            if ((board & capture) != 0 && (0 == (board & (to = from << 16)))) {
                makemove(from, to, capture);
                _gen();
                undomove();
            }
            bits &= ~from;
        }

        bits = board & BOARD_to_DOWN;
        while (bits != 0) {
            from = BITScanForward(bits);
            capture = from >>> 8;
            if ((board & capture) != 0 && (0 == (board & (to = from >>> 16)))) {
                makemove(from, to, capture);
                _gen();
                undomove();
            }
            bits &= ~from;
        }

        bits = board & BOARD_to_LEFT;
        while (bits != 0) {
            from = BITScanForward(bits);
            capture = from << 1;
            if ((board & capture) != 0 && (0 == (board & (to = from << 2)))) {
                makemove(from, to, capture);
                _gen();
                undomove();
            }
            bits &= ~from;
        }

        hashArray[x] = board;
    }

    private void printStack() {
        long time = System.currentTimeMillis() - startTime;
        Nsolution++;
        System.out.println("\nSolution# " + Nsolution + " ms: " + time + " ----------------- start stack moves ------------------------");
        int i;
        for (i = 0; i < nstack; i++) {
            print(stack[i], TERRAIN);
        }
        print(board, TERRAIN);
        System.out.println("\nstack solution: " + Nsolution + " | ");
        for (i = 0; i < nstack; i++) {
            System.out.print("0x" + stack[i] + "L, ");
        }

        System.out.println("Nmoves: " + nmoves + " Hash cut: " + cut + " (" + cut * 100 / nmoves + ") ");
        System.out.println("\nSolution# " + Nsolution + " ms: " + time + " ----------------- end stack moves ------------------------  \n");


    }

};
