;;; TOOL: wat2wasm
(module
  (func
    i32.const 0
    f32.reinterpret/i32
    drop
    f32.const 0
    i32.reinterpret/f32 
    drop
    i64.const 0
    f64.reinterpret/i64
    drop
    f64.const 0
    i64.reinterpret/f64
    drop))