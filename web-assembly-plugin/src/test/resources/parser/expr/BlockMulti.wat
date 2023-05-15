;;; TOOL: wat2wasm
(module
  ;; block w/ multiple results
  (func
    block (result i32 i64)
      i32.const 0
      i64.const 0
    end
    return)

  ;; block w/ params
  (func
    i32.const 0
    block (param i32) (result i32 i32)
      i32.const 1
    end
    return)
)