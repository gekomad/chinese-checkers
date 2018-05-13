/*
    CHINESE CHECKERS - find all solutions - rust source

	run: cargo test --release
	run: cargo run --release

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

const HASH_SIZE: usize = 1229498;
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
    nsolution: isize,
    nstack: isize,
    tot: isize,
    start_time: u64,
    hash_array: Vec<Bits>,
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
        self.trailing_zeros() as usize
    }
}

fn undomove(global: &mut Global) {
    global.nstack = global.nstack - 1;
    debug_assert!(global.nstack >= 0);

    global.board = global.stack[global.nstack as usize]
}

fn makemove(from: Bits, to: Bits, capture: Bits, global: &mut Global) {
    global.stack[global.nstack as usize] = global.board;

    global.board &= !from;
    global.board |= to;
    global.board &= !capture;

    debug_assert!(global.nstack < global.tot);
    global.nstack = global.nstack + 1
}

fn pop_count(x: Bits) -> isize {
    fn go(x: Bits, count: isize) -> isize {
        if x == 0 { count } else {
            go(x & (x - 1), count + 1)
        }
    }

    go(x, 0)
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
    print!("\n")
}

fn print_stack(global: &mut Global) {
    if global.nmoves != 0 {
        let time = (get_ns() - global.start_time) / 1000000;
        global.nsolution = global.nsolution + 1;
        println!("\nSolution# {} ms: {} ----------------- start stack moves ------------------------  ", global.nsolution, time);
        for i in 0..global.nstack {
            print(global.stack[i as usize]);
        }
        print(global.board);
        print!("Nmoves: {} Hash cut: {} ({}%) ", global.nmoves, global.cut, global.cut * 100 / global.nmoves);
        println!("\nSolution# {} ms: {} ----------------- end stack moves ------------------------  ", global.nsolution, time)
    }
}

fn gen(global: &mut Global) {
    global.nmoves = global.nmoves + 1;
    if global.nmoves % 5000000000 == 0 {
        print!("Nmoves: {} Cut:{} ({}%) ", global.nmoves, global.cut, global.cut * 100 / global.nmoves);
    }

    if global.hash_array[(global.board % HASH_SIZE as Bits) as usize] == global.board {
        global.cut = global.cut + 1;
    } else {
        if global.nstack == global.tot - 1 {
            print_stack(global);
        } else {
            let mut bits = global.board & BOARD_TO_RIGHT;
            while bits != 0 {
                let from;

                from = POW2[bits.bitscan_forward()];

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
            }

            bits = global.board & BOARD_TO_UP;
            while bits != 0 {
                let from;

                from = POW2[bits.bitscan_forward()];

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
            }

            bits = global.board & BOARD_TO_DOWN;
            while bits != 0 {
                let from;

                from = POW2[bits.bitscan_forward()];

                let capture = from >> 8;
                if (global.board & capture) != 0 {
                    let to = from >> 16;
                    if (global.board & to) == 0 {
                        makemove(from, to, capture, global);
                        gen(global);
                        undomove(global)
                    }
                }
                bits &= !from
            }


            bits = global.board & BOARD_TO_LEFT;
            while bits != 0 {
                let from;

                from = POW2[bits.bitscan_forward()];

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
            }

            global.hash_array[(global.board % HASH_SIZE as Bits) as usize] = global.board
        }
    }
}

fn main() {
    let mut global = Global {
        board: INIT_BOARD,
        nmoves: 0,
        cut: 0,
        nsolution: 0,
        nstack: 0,
        tot: pop_count(INIT_BOARD),
        start_time: time::precise_time_ns(),
        hash_array: vec![0; HASH_SIZE],
        stack: [0; 64],
    };

    print(global.board);
    global.start_time = time::precise_time_ns();
    gen(&mut global)
}

#[test]
fn makemove_test() {
    let mut global = Global {
        board: INIT_BOARD,
        nmoves: 0,
        cut: 0,
        nsolution: 0,
        nstack: 0,
        tot: pop_count(INIT_BOARD),
        start_time: 0,
        hash_array: vec![0; 0],
        stack: [0; 64],
    };
    let from: Bits = 0x1000;
    let to: Bits = 0x10000000;
    let capture: Bits = 0x100000;
    assert!(0 == global.nstack);
    makemove(from, to, capture, &mut global);
    assert!(0x3838fefeee2838 == global.board);
    assert!(1 == global.nstack);

    undomove(&mut global);
    assert!(0 == global.nstack);
    assert!(INIT_BOARD == global.board)
}
