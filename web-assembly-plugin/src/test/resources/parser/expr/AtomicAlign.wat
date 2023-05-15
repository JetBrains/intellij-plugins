;;; TOOL: wat2wasm
;;; ARGS: --enable-threads
(module
  (memory 1 1)
  (func
    i32.const 0 i32.const 0 i64.const 0 i32.atomic.wait align=4 drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.wait align=8 drop

    i32.const 0 i32.atomic.load align=4 drop
    i32.const 0 i64.atomic.load align=8 drop
    i32.const 0 i32.atomic.load8_u align=1 drop
    i32.const 0 i32.atomic.load16_u align=2 drop
    i32.const 0 i64.atomic.load8_u align=1 drop
    i32.const 0 i64.atomic.load16_u align=2 drop
    i32.const 0 i64.atomic.load32_u align=4 drop

    i32.const 0 i32.const 0 i32.atomic.store align=4
    i32.const 0 i64.const 0 i64.atomic.store align=8
    i32.const 0 i32.const 0 i32.atomic.store8 align=1
    i32.const 0 i32.const 0 i32.atomic.store16 align=2
    i32.const 0 i64.const 0 i64.atomic.store8 align=1
    i32.const 0 i64.const 0 i64.atomic.store16 align=2
    i32.const 0 i64.const 0 i64.atomic.store32 align=4

    i32.const 0 i32.const 0 i32.atomic.rmw.add align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw.add align=8 drop
    i32.const 0 i32.const 0 i32.atomic.rmw8_u.add align=1 drop
    i32.const 0 i32.const 0 i32.atomic.rmw16_u.add align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw8_u.add align=1 drop
    i32.const 0 i64.const 0 i64.atomic.rmw16_u.add align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw32_u.add align=4 drop

    i32.const 0 i32.const 0 i32.atomic.rmw.sub align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw.sub align=8 drop
    i32.const 0 i32.const 0 i32.atomic.rmw8_u.sub align=1 drop
    i32.const 0 i32.const 0 i32.atomic.rmw16_u.sub align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw8_u.sub align=1 drop
    i32.const 0 i64.const 0 i64.atomic.rmw16_u.sub align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw32_u.sub align=4 drop

    i32.const 0 i32.const 0 i32.atomic.rmw.and align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw.and align=8 drop
    i32.const 0 i32.const 0 i32.atomic.rmw8_u.and align=1 drop
    i32.const 0 i32.const 0 i32.atomic.rmw16_u.and align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw8_u.and align=1 drop
    i32.const 0 i64.const 0 i64.atomic.rmw16_u.and align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw32_u.and align=4 drop

    i32.const 0 i32.const 0 i32.atomic.rmw.or align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw.or align=8 drop
    i32.const 0 i32.const 0 i32.atomic.rmw8_u.or align=1 drop
    i32.const 0 i32.const 0 i32.atomic.rmw16_u.or align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw8_u.or align=1 drop
    i32.const 0 i64.const 0 i64.atomic.rmw16_u.or align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw32_u.or align=4 drop

    i32.const 0 i32.const 0 i32.atomic.rmw.xor align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw.xor align=8 drop
    i32.const 0 i32.const 0 i32.atomic.rmw8_u.xor align=1 drop
    i32.const 0 i32.const 0 i32.atomic.rmw16_u.xor align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw8_u.xor align=1 drop
    i32.const 0 i64.const 0 i64.atomic.rmw16_u.xor align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw32_u.xor align=4 drop

    i32.const 0 i32.const 0 i32.atomic.rmw.xchg align=4 drop
    i32.const 0 i64.const 0 i64.atomic.rmw.xchg align=8 drop
    i32.const 0 i32.const 0 i32.atomic.rmw8_u.xchg align=1 drop
    i32.const 0 i32.const 0 i32.atomic.rmw16_u.xchg align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw8_u.xchg align=1 drop
    i32.const 0 i64.const 0 i64.atomic.rmw16_u.xchg align=2 drop
    i32.const 0 i64.const 0 i64.atomic.rmw32_u.xchg align=4 drop

    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw.cmpxchg align=4 drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw.cmpxchg align=8 drop
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw8_u.cmpxchg align=1 drop
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw16_u.cmpxchg align=2 drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw8_u.cmpxchg align=1 drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw16_u.cmpxchg align=2 drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw32_u.cmpxchg align=4 drop

))