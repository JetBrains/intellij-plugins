;;; TOOL: wat2wasm
(module
  (func (param i32) (param $n f32)
    (local i32 i64)
    (local $m f64)
    get_local 0
    drop
    get_local 1
    drop
    get_local $n  ;; 1
    drop
    get_local 2
    drop
    get_local 3
    drop
    get_local $m  ;; 4
    drop 
    get_local 4
    drop))