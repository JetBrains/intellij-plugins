;;; TOOL: wat2wasm
(module
  (memory 1)
  (func
    i32.const 0
    i32.const 0
    i32.store8 offset=0
    i32.const 0
    i32.const 0
    i32.store16 offset=1
    i32.const 0
    i32.const 0
    i32.store offset=2
    i32.const 0
    i64.const 0
    i64.store offset=3
    i32.const 0
    i64.const 0
    i64.store8 offset=4
    i32.const 0 
    i64.const 0
    i64.store16 offset=5
    i32.const 0
    i64.const 0
    i64.store32 offset=6
    i32.const 0
    f32.const 0
    f32.store offset=7
    i32.const 0
    f64.const 0
    f64.store offset=8

    ;; alignment must come after
    i32.const 0
    i32.const 0
    i32.store8 offset=0 align=1
    i32.const 0
    i32.const 0
    i32.store16 offset=1 align=2
    i32.const 0
    i32.const 0
    i32.store offset=2 align=4
    i32.const 0
    i64.const 0
    i64.store offset=3 align=8
    i32.const 0
    i64.const 0
    i64.store8 offset=4 align=1
    i32.const 0
    i64.const 0
    i64.store16 offset=5 align=1
    i32.const 0
    i64.const 0
    i64.store32 offset=6 align=4
    i32.const 0
    f32.const 0
    f32.store offset=7 align=2
    i32.const 0
    f64.const 0
    f64.store offset=8 align=1))