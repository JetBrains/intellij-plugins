;;; TOOL: wat2wasm
(module
  (memory 1)
  (func
    i32.const 0
    i32.load
    drop
    i32.const 0
    i32.load8_s
    drop
    i32.const 0
    i32.load16_s
    drop
    i32.const 0
    i32.load8_u
    drop
    i32.const 0
    i32.load16_u
    drop
    i32.const 0
    i64.load
    drop
    i32.const 0
    i64.load8_s
    drop
    i32.const 0
    i64.load16_s
    drop
    i32.const 0
    i64.load32_s
    drop
    i32.const 0
    i64.load8_u
    drop
    i32.const 0
    i64.load16_u
    drop
    i32.const 0
    i64.load32_u
    drop
    i32.const 0
    f32.load
    drop
    i32.const 0
    f64.load
    drop))