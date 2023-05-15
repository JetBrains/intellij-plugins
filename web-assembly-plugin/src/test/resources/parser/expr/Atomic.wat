;;; TOOL: wat2wasm
;;; ARGS: --enable-threads
(module
  (memory 1 1)
  (func
    i32.const 0 i32.const 0 i64.const 0 i32.atomic.wait drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.wait drop

    i32.const 0 i32.atomic.load drop
    i32.const 0 i64.atomic.load drop
    i32.const 0 i32.atomic.load8_u drop
    i32.const 0 i32.atomic.load16_u drop
    i32.const 0 i64.atomic.load8_u drop
    i32.const 0 i64.atomic.load16_u drop
    i32.const 0 i64.atomic.load32_u drop

    i32.const 0 i32.const 0 i32.atomic.store
    i32.const 0 i64.const 0 i64.atomic.store
    i32.const 0 i32.const 0 i32.atomic.store8
    i32.const 0 i32.const 0 i32.atomic.store16
    i32.const 0 i64.const 0 i64.atomic.store8
    i32.const 0 i64.const 0 i64.atomic.store16
    i32.const 0 i64.const 0 i64.atomic.store32

    i32.const 0 i32.const 0 i32.atomic.rmw.add drop
    i32.const 0 i64.const 0 i64.atomic.rmw.add drop
    i32.const 0 i32.const 0 i32.atomic.rmw8_u.add drop
    i32.const 0 i32.const 0 i32.atomic.rmw16_u.add drop
    i32.const 0 i64.const 0 i64.atomic.rmw8_u.add drop
    i32.const 0 i64.const 0 i64.atomic.rmw16_u.add drop
    i32.const 0 i64.const 0 i64.atomic.rmw32_u.add drop

    i32.const 0 i32.const 0 i32.atomic.rmw.sub drop
    i32.const 0 i64.const 0 i64.atomic.rmw.sub drop
    i32.const 0 i32.const 0 i32.atomic.rmw8_u.sub drop
    i32.const 0 i32.const 0 i32.atomic.rmw16_u.sub drop
    i32.const 0 i64.const 0 i64.atomic.rmw8_u.sub drop
    i32.const 0 i64.const 0 i64.atomic.rmw16_u.sub drop
    i32.const 0 i64.const 0 i64.atomic.rmw32_u.sub drop

    i32.const 0 i32.const 0 i32.atomic.rmw.and drop
    i32.const 0 i64.const 0 i64.atomic.rmw.and drop
    i32.const 0 i32.const 0 i32.atomic.rmw8_u.and drop
    i32.const 0 i32.const 0 i32.atomic.rmw16_u.and drop
    i32.const 0 i64.const 0 i64.atomic.rmw8_u.and drop
    i32.const 0 i64.const 0 i64.atomic.rmw16_u.and drop
    i32.const 0 i64.const 0 i64.atomic.rmw32_u.and drop

    i32.const 0 i32.const 0 i32.atomic.rmw.or drop
    i32.const 0 i64.const 0 i64.atomic.rmw.or drop
    i32.const 0 i32.const 0 i32.atomic.rmw8_u.or drop
    i32.const 0 i32.const 0 i32.atomic.rmw16_u.or drop
    i32.const 0 i64.const 0 i64.atomic.rmw8_u.or drop
    i32.const 0 i64.const 0 i64.atomic.rmw16_u.or drop
    i32.const 0 i64.const 0 i64.atomic.rmw32_u.or drop

    i32.const 0 i32.const 0 i32.atomic.rmw.xor drop
    i32.const 0 i64.const 0 i64.atomic.rmw.xor drop
    i32.const 0 i32.const 0 i32.atomic.rmw8_u.xor drop
    i32.const 0 i32.const 0 i32.atomic.rmw16_u.xor drop
    i32.const 0 i64.const 0 i64.atomic.rmw8_u.xor drop
    i32.const 0 i64.const 0 i64.atomic.rmw16_u.xor drop
    i32.const 0 i64.const 0 i64.atomic.rmw32_u.xor drop

    i32.const 0 i32.const 0 i32.atomic.rmw.xchg drop
    i32.const 0 i64.const 0 i64.atomic.rmw.xchg drop
    i32.const 0 i32.const 0 i32.atomic.rmw8_u.xchg drop
    i32.const 0 i32.const 0 i32.atomic.rmw16_u.xchg drop
    i32.const 0 i64.const 0 i64.atomic.rmw8_u.xchg drop
    i32.const 0 i64.const 0 i64.atomic.rmw16_u.xchg drop
    i32.const 0 i64.const 0 i64.atomic.rmw32_u.xchg drop

    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw.cmpxchg drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw.cmpxchg drop
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw8_u.cmpxchg drop
    i32.const 0 i32.const 0 i32.const 0 i32.atomic.rmw16_u.cmpxchg drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw8_u.cmpxchg drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw16_u.cmpxchg drop
    i32.const 0 i64.const 0 i64.const 0 i64.atomic.rmw32_u.cmpxchg drop

))