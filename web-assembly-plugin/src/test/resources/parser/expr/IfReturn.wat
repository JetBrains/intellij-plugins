;;; TOOL: wat2wasm
(module
  (func (result i32)
    i32.const 1
    if (result i32)
      i32.const 2
      return
    else
      i32.const 3
      return
    end))