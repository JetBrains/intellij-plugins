;;; TOOL: wat2wasm
(module
  ;; loop w/ multiple results
  (func
    loop (result i32 i64)
      i32.const 0
      i64.const 0
    end
    return)

  ;; loop w/ params
  (func
    i64.const 0
    loop (param i64) (result i32)
      drop
      i32.const 1
    end
    return)
)