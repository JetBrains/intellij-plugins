;;; TOOL: wat2wasm
(module
  (memory 1)
  (func
    i32.const 0
    i32.load offset=0
    drop
    i32.const 0
    i64.load offset=1
    drop
    i32.const 0
    i64.load8_s offset=2
    drop
    i32.const 0
    i64.load16_s offset=3
    drop
    i32.const 0
    i64.load32_s offset=4
    drop
    i32.const 0
    i64.load8_u offset=5
    drop 
    i32.const 0
    i64.load16_u offset=6
    drop
    i32.const 0
    i64.load32_u offset=7
    drop
    i32.const 0
    i32.load8_s offset=8
    drop
    i32.const 0
    i32.load16_s offset=9
    drop
    i32.const 0
    i32.load8_u offset=10
    drop
    i32.const 0
    i32.load16_u offset=11
    drop
    i32.const 0
    f32.load offset=12
    drop
    i32.const 0
    f64.load offset=13
    drop
    i32.const 0
    i32.load offset=0 align=1
    drop
    i32.const 0
    i64.load offset=1 align=2
    drop
    i32.const 0
    i64.load8_s offset=2 align=1
    drop
    i32.const 0
    i64.load16_s offset=3 align=1
    drop
    i32.const 0
    i64.load32_s offset=4 align=1
    drop
    i32.const 0
    i64.load8_u offset=5 align=1
    drop
    i32.const 0
    i64.load16_u offset=6 align=2
    drop
    i32.const 0
    i64.load32_u offset=7 align=2
    drop
    i32.const 0
    i32.load8_s offset=8 align=1
    drop
    i32.const 0
    i32.load16_s offset=9 align=2
    drop
    i32.const 0
    i32.load8_u offset=10 align=1
    drop
    i32.const 0
    i32.load16_u offset=11 align=2
    drop
    i32.const 0
    f32.load offset=12 align=4
    drop
    i32.const 0
    f64.load offset=13 align=2 
    drop))