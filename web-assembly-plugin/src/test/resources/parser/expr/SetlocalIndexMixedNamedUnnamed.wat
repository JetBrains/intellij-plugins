;;; TOOL: wat2wasm
(module
  (func (param i32) (param $n f32)
    (local i32 i64)
    (local $m f64)
    i32.const 0
    set_local 0
    f32.const 0
    set_local 1
    f32.const 0
    set_local $n ;; 1
    i32.const 0
    set_local 2
    i64.const 0
    set_local 3
    f64.const 0
    set_local $m ;; 4
    f64.const 0
    set_local 4))