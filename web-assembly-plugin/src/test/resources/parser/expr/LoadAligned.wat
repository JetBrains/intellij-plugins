;;; TOOL: wat2wasm
(module
  (memory 1)
  (func
    i32.const 0
    i32.load align=4
    drop
    i32.const 0
    i64.load align=4
    drop
    i32.const 0
    i64.load8_s align=1
    drop
    i32.const 0
    i64.load16_s align=2
    drop
    i32.const 0
    i64.load32_s align=4
    drop
    i32.const 0
    i64.load8_u align=1
    drop
    i32.const 0
    i64.load16_u align=2
    drop
    i32.const 0
    i64.load32_u align=4
    drop
    i32.const 0
    i32.load8_s align=1
    drop
    i32.const 0
    i32.load16_s align=2
    drop
    i32.const 0
    i32.load8_u align=1
    drop
    i32.const 0
    i32.load16_u align=2
    drop
    i32.const 0
    f32.load align=2
    drop
    i32.const 0
    f64.load align=8 
    drop))