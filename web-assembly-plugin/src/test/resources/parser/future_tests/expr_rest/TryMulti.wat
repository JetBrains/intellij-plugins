;;; TOOL: wat2wasm
;;; ARGS: --enable-exceptions
(module
  ;; try w/ multiple results
  (func
    try (result f32 f32)
      f32.const 0
      f32.const 1
    catch
      drop
      f32.const 2
      f32.const 3
    end
    return)

  ;; try w/ params
  (func
    i32.const 0
    try (param i32) (result i32)
      i32.eqz
    catch
      drop  ;; no i32 param, just exnref
      i32.const 0
    end
    return)
)
