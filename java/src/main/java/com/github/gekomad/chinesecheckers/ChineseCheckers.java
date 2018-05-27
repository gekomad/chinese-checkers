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

    final long BOARD_to_RIGHT = 0x2020F8F8F82020L;
    final long BOARD_to_LEFT = 0x8083E3E3E0808L;
    final long BOARD_to_UP = 0x3838FE3838L;
    final long BOARD_to_DOWN = 0x3838FE38380000L;
    private int maxSolutions = 1000000000;
    private int Nsolution;
    private final long startTime;

    private final long[] hashArray = new long[HASH_SIZE];

    private long nmoves = 0, cut = 0;

    ChineseCheckers(long i, long t) {
        board = i;
        TOT = popCount(board);
        TERRAIN = t;


        startTime = System.currentTimeMillis();
    }

    static long BITScanForward(long b) {
        return (b ^ (b & b - 1));
    }

    public void setMaxSolutions(int maxSolutions) {
        this.maxSolutions = maxSolutions;
    }

    void print(long board) {
        for (int k = 63; k >= 1; k--) {
            if ((board & (long) pow(2, k - 1)) == 0) {
                if ((TERRAIN & (long) pow(2, k - 1)) == 0)
                    System.out.print(" \t");
                else
                    System.out.print("-\t");
            } else
                System.out.print("O\t");
            if ((k - 1) % 8 == 0)
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
        if (Nsolution >= maxSolutions) return;
        long from, bits, to, capture;
        if (0 == ((++nmoves) % 50000000L)) {
            System.out.println("Nmoves: " + nmoves + " cut: " + cut + " " + cut * 100 / nmoves + "%");
        }

        if (hashArray[(int) (board % HASH_SIZE)] == board) {
            cut++;
            return;
        }

        if (nstack == TOT - 1) {
            print_stack();
            return;
        }

        bits = board & BOARD_to_RIGHT;
        while (bits != 0) {
            from = BITScanForward(bits);
            capture = from >>> 1;
            if ((board & capture) != 0 && (0 == (board & (to = from >>> 2)))) {
                makemove(from, to, capture);
                gen();
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
                gen();
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
                gen();
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
                gen();
                undomove();
            }
            bits &= ~from;
        }

        hashArray[(int) (board % HASH_SIZE)] = board;
    }

    private void print_stack() {
        long time = System.currentTimeMillis() - startTime;
        Nsolution++;
        System.out.println("\nSolution# " + Nsolution + " ms: " + time + " ----------------- start stack moves ------------------------");
        int i;
        for (i = 0; i < nstack; i++) {
            print(stack[i]);
        }
        print(board);
        System.out.println("\nstack solution: " + Nsolution + " | ");
        for (i = 0; i < nstack; i++) {
            System.out.print("0x" + stack[i] + "L, ");
        }

        System.out.println("Nmoves: " + nmoves + " Hash cut: " + cut + " (" + cut * 100 / nmoves + ") ");
        System.out.println("\nSolution# " + Nsolution + " ms: " + time + " ----------------- end stack moves ------------------------  \n");


    }

};