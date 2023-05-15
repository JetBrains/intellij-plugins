;;; TOOL: wat2wasm
(module
  ;; if w/ multiple results
  (func
    i32.const 0
    if (result i32 f32 f64)
      i32.const 0
      f32.const 0
      f64.const 0
    else
      i32.const 1
      f32.const 1
      f64.const 1
    end
    return)

  ;; if w/ params
  (func
    i32.const 1  ;; param
    i32.const 0  ;; cond
    if (param i32) (result i64)
      drop
      i64.const 1
    else
      i64.extend_u/i32
    end
    return)
)