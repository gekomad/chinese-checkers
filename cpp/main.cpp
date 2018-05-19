/*
    CHINESE CHECKERS - find all solutions - c source

	run: gcc -O3 main.c -o checkers;./checkers

 	 	O	O	O
 	 	O	O	O
O	O	O	O	O	O	O
O	O	O	-	O	O	O
O	O	O	O	O	O	O
 	 	O	O	O
 	 	O	O	O
 */

#include "stdio.h"
#include "stdlib.h"
#include <assert.h>
#include <sys/timeb.h>
#include <malloc.h>
#include <stdlib.h>
#include "main.h"


int popCount(u64 x) {
    int count = 0;
    while (x) {
        count++;
        x &= x - 1;
    }
    return count;
}

long get_ms() {
    struct timeb timebuffer;
    ftime(&timebuffer);
    return (timebuffer.time * 1000) + timebuffer.millitm;
}

void print(u64 board) {
    int k;
    for (k = 64; k >= 1; k--) {
        if ((board & POW2[k - 1]) == 0) {
            if ((BOARD & POW2[k - 1]) == 0)
                printf(" \t");
            else
                printf("-\t");
        } else
            printf("O\t");
        if ((k - 1) % 8 == 0)
            printf("\n");
    }
}

void ChineseCheckers::makemove(const u64 from, const u64 to, const u64 capture) {
    stack[nstack] = board;
    board &= ~from;
    board |= to;
    board &= ~capture;
    nstack++;
}

void ChineseCheckers::undomove() {
    nstack--;
    board = stack[nstack];
}


void ChineseCheckers::gen() {

    u64 from, bits, to, capture;

    if (!((++nmoves) % 5000000000)) {
        printf("Nmoves: %llu Cut:%llu (%llu%%) ", nmoves, cut, cut * 100 / nmoves);
    }

    if (hash_array[board % HASH_SIZE] == board) {
        cut++;
        return;
    }

    if (nstack == TOT - 1) {
        print_stack();
        return;
    }

    bits = board & BOARD_to_RIGHT;
    while (bits) {
        from = POW2[BITScanForward(bits)];

        capture = from >> 1;
        if (board & capture && !(board & (to = from >> 2))) {
            makemove(from, to, capture);
            gen();
            undomove();
        }
        bits &= ~from;
    };


    bits = board & BOARD_to_UP;
    while (bits) {
        from = POW2[BITScanForward(bits)];
        capture = from << 8;
        if (board & capture && !(board & (to = from << 16))) {
            makemove(from, to, capture);
            gen();
            undomove();
        }
        bits &= ~from;
    };

    bits = board & BOARD_to_DOWN;
    while (bits) {
        from = POW2[BITScanForward(bits)];
        capture = from >> 8;
        if (board & capture && !(board & (to = from >> 16))) {
            makemove(from, to, capture);
            gen();
            undomove();
        }
        bits &= ~from;
    };

    bits = board & BOARD_to_LEFT;
    while (bits) {
        from = POW2[BITScanForward(bits)];
        capture = from << 1;
        if (board & capture && !(board & (to = from << 2))) {
            makemove(from, to, capture);
            gen();
            undomove();
        }
        bits &= ~from;
    };

    hash_array[board % HASH_SIZE] = board;
}

void ChineseCheckers::print_stack() {
    long time = get_ms() - start_time;
    Nsolution++;
    printf("\nSolution# %d ms: %ld ----------------- start stack moves ------------------------  \n",
           Nsolution, time);
    int i;
    for (i = 0; i < nstack; i++) {
        print(stack[i]);
    }
    print(board);
    printf("\nstack solution: %d | ", Nsolution);
    for (i = 0; i < nstack; i++) {
        printf("0x%llxL, ", stack[i]);
    }

    printf("Nmoves: %llu Hash cut: %llu (%llu%%) ", nmoves, cut, cut * 100 / nmoves);
    printf("\nSolution# %d ms: %ld ----------------- end stack moves ------------------------  \n",
           Nsolution, time);

}

ChineseCheckers::ChineseCheckers(u64 i) {
    board = i;
    TOT = popCount(INIT_BOARD);
}


int main(int argc, char *argv[]) {

    hash_array = (u64 *) calloc(HASH_SIZE, sizeof(u64));


    int nTreads = 4;
    if (argc == 2)
        nTreads = atoi(argv[1]);

    print(INIT_BOARD);
    printf("\nn threads: %d", nTreads);

    thread threads[nTreads];
    ChineseCheckers *chineseCheckers[nTreads];

    start_time = get_ms();

    for (int i = 0; i < nTreads; i++) {
        chineseCheckers[i] = new ChineseCheckers(INIT_BOARD);
        threads[i] = chineseCheckers[i]->start();
    }

    for (int i = 0; i < nTreads; i++) {
        threads[i].join();
    }

    for (int i = 0; i < nTreads; i++) {
        free(chineseCheckers[i]);
    }
    free(hash_array);
    return 0;
}
