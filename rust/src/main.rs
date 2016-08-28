/*
    CHINESE CHECKERS - find all solutions - rust source

	run: cargo build --release; ./target/release/checkers

 	 	O	O	O
 	 	O	O	O
O	O	O	O	O	O	O
O	O	O	-	O	O	O
O	O	O	O	O	O	O
 	 	O	O	O
 	 	O	O	O
 */


extern crate time;

type Bits = u64;

const HASH_SIZE: usize = 122949823;
const INIT_BOARD: Bits = 0x3838FEEEFE3838;
const BOARD: Bits = 0x3838FEFEFE3838;
const BOARD_TO_RIGHT: Bits = 0x2020F8F8F82020;
const BOARD_TO_LEFT: Bits = 0x8083E3E3E0808;
const BOARD_TO_UP: Bits = 0x3838FE3838;
const BOARD_TO_DOWN: Bits = 0x3838FE38380000;

struct Global {
    board: Bits,
    nmoves: u64,
    cut: u64,
    nsolution: i32,
    count: i32,
    nstack: i32,
    tot: i32,
    start_time: u64,
    hash_array: Box<[Bits]>,
    stack: [Bits; 64],
}

const POW2: [u64; 64] = [0x1, 0x2, 0x4, 0x8, 0x10, 0x20,
    0x40, 0x80, 0x100, 0x200, 0x400, 0x800, 0x1000,
    0x2000, 0x4000, 0x8000, 0x10000, 0x20000, 0x40000,
    0x80000, 0x100000, 0x200000, 0x400000, 0x800000,
    0x1000000, 0x2000000, 0x4000000, 0x8000000, 0x10000000,
    0x20000000, 0x40000000, 0x80000000, 0x100000000,
    0x200000000, 0x400000000, 0x800000000, 0x1000000000,
    0x2000000000, 0x4000000000, 0x8000000000, 0x10000000000,
    0x20000000000, 0x40000000000, 0x80000000000,
    0x100000000000, 0x200000000000, 0x400000000000,
    0x800000000000, 0x1000000000000, 0x2000000000000,
    0x4000000000000, 0x8000000000000, 0x10000000000000,
    0x20000000000000, 0x40000000000000, 0x80000000000000,
    0x100000000000000, 0x200000000000000, 0x400000000000000,
    0x800000000000000, 0x1000000000000000, 0x2000000000000000,
    0x4000000000000000, 0x8000000000000000];

trait BitOperations {
    fn bitscan_forward(&self) -> usize;
}

impl BitOperations for Bits {
    fn bitscan_forward(&self) -> usize {
        const LSB_64_TABLE: [usize; 64] = [63, 30, 3, 32, 59, 14, 11, 33, 60, 24, 50, 9, 55, 19, 21, 34, 61, 29, 2, 53, 51, 23, 41, 18, 56, 28, 1, 43, 46, 27, 0, 35, 62, 31, 58, 4, 5, 49, 54, 6, 15, 52, 12, 40, 7, 42, 45, 16, 25, 57, 48, 13, 10, 39, 8, 44, 20, 47, 38, 22, 17, 37, 36, 26];

        //  @author Matt Taylor (2003)
        let bb = self ^ (self - 1);
        let folded = (((bb as u32 ^ ((bb >> 32) as u32)) as Bits) * 0x78291ACF) as u32;
        LSB_64_TABLE[(folded >> 26) as usize]
    }
}

fn undomove(global: &mut Global) {
    global.nstack = global.nstack - 1;
    debug_assert!(global.nstack >= 0);
    global.board = global.stack[global.nstack as usize];
}

fn makemove(from: Bits, to: Bits, capture: Bits, global: &mut Global) {
    global.stack[global.nstack as usize] = global.board;
    global.board &= !from;
    global.board |= to;
    global.board &= !capture;

    debug_assert!(global.nstack < global.tot);
    global.nstack = global.nstack + 1;
}

fn pop_count(mut x: Bits) -> i32 {
    let mut count: i32 = 0;
    while x != 0 {
        count = count + 1;
        x &= x - 1;
    }
    count
}

fn get_ns() -> u64 {
    time::precise_time_ns()
}

fn print(board: Bits) {
    for k in (0..64).rev() {
        if 0 == (board & POW2[k]) {
            if (BOARD & POW2[k]) == 0 {
                print!(" \t");
            } else {
                print!("-\t");
            }
        } else {
            print!("O\t");
        }
        if (k) % 8 == 0 {
            print!("\n");
        }
    }
    print!("\n");
}

fn print_stack(global: &mut Global) {
    if global.nmoves == 0 {
        return;
    }
    let time = (get_ns() - global.start_time) / 1000000;
    global.nsolution = global.nsolution + 1;
    println!("\nSolution# {} ms: {} ----------------- start stack moves ------------------------  ", global.nsolution, time);
    for i in 0..global.nstack {
        print(global.stack[i as usize]);
    }
    print(global.board);
    print!("Nmoves: {} Hash cut: {} ({}%) ", global.nmoves, global.cut, global.cut * 100 / global.nmoves);
    println!("\nSolution# {} ms: {} ----------------- end stack moves ------------------------  ", global.nsolution, time);
}

fn gen(global: &mut Global) {
    let mut found = false;

    global.nmoves = global.nmoves + 1;
    if global.nmoves % 5000000000 == 0 {
        print!("Nmoves: {} Cut:{} ({}%) ", global.nmoves, global.cut, global.cut * 100 / global.nmoves);
    }

    if global.hash_array[(global.board % HASH_SIZE as Bits) as usize] == global.board {
        global.cut = global.cut + 1;
        return;
    }

    if global.nstack >= global.count {
        global.count = global.nstack;
        if global.nstack == global.tot - 1 {
            found = true;
            print_stack(global);
        }
    }

    let mut bits = global.board & BOARD_TO_LEFT;
    while bits != 0 {
        let from = POW2[bits.bitscan_forward()];
        let capture = from << 1;
        if (global.board & capture) != 0 {
            let to = from << 2;
            if (global.board & to) == 0 {
                makemove(from, to, capture, global);
                gen(global);
                undomove(global);
            }
        }
        bits &= !from;
    };

    bits = global.board & BOARD_TO_RIGHT;
    while bits != 0 {
        let from = POW2[bits.bitscan_forward()];
        let capture = from >> 1;
        if (global.board & capture) != 0 {
            let to = from >> 2;
            if (global.board & to) == 0 {
                makemove(from, to, capture, global);
                gen(global);
                undomove(global);
            }
        }
        bits &= !from;
    };

    bits = global.board & BOARD_TO_DOWN;
    while bits != 0 {
        let from = POW2[bits.bitscan_forward()];
        let capture = from >> 8;
        if (global.board & capture) != 0 {
            let to = from >> 16;
            if (global.board & to) == 0 {
                makemove(from, to, capture, global);
                gen(global);
                undomove(global);
            }
        }
        bits &= !from;
    };

    bits = global.board & BOARD_TO_UP;
    while bits != 0 {
        let from = POW2[bits.bitscan_forward()];
        let capture = from << 8;

        if (global.board & capture) != 0 {
            let to = from << 16;
            if (global.board & to) == 0 {
                makemove(from, to, capture, global);
                gen(global);
                undomove(global);
            }
        }
        bits &= !from;
    };

    if found == false {
        global.hash_array[(global.board % HASH_SIZE as Bits) as usize] = global.board;
    }
}

fn main() {
    let mut global = Global {
        board: INIT_BOARD,
        nmoves: 0,
        cut: 0,
        nsolution: 0,
        count: 0,
        nstack: 0,
        tot: pop_count(INIT_BOARD),
        start_time: time::precise_time_ns(),
        hash_array: Box::new([0; HASH_SIZE]),
        stack: [0; 64],
    };

    print(global.board);
    gen(&mut global);
}

#[test]
fn makemove_test() {
    let mut global = Global {
        board: INIT_BOARD,
        nmoves: 0,
        cut: 0,
        nsolution: 0,
        count: 0,
        nstack: 0,
        tot: pop_count(INIT_BOARD),
        start_time: 0,
        hash_array: Box::new([0; 0]),
        stack: [0; 64],
    };
    let from: Bits = 0x1000;
    let to: Bits = 0x10000000;
    let capture: Bits = 0x100000;
    assert!(0 == global.nstack );
    makemove(from, to, capture, &mut global);
    assert!(0x3838fefeee2838 == global.board);
    assert!(1 == global.nstack );

    undomove(&mut global);
    assert!(0 == global.nstack );
    assert!(INIT_BOARD == global.board);
}
