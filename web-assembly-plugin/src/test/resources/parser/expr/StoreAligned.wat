;;; TOOL: wat2wasm
(module
  (memory 1)
  (func
    i32.const 0
    i32.const 0
    i32.store8 align=1
    i32.const 0
    i32.const 0
    i32.store16 align=2
    i32.const 0
    i32.const 0
    i32.store align=4
    i32.const 0
    i64.const 0
    i64.store align=8
    i32.const 0
    i64.const 0
    i64.store8 align=1
    i32.const 0
    i64.const 0
    i64.store16 align=2
    i32.const 0
    i64.const 0
    i64.store32 align=4
    i32.const 0
    f32.const 0
    f32.store align=4
    i32.const 0
    f64.const 0
    f64.store align=8 ))