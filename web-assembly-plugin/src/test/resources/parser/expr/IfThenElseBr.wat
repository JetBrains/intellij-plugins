;;; TOOL: wat2wasm
(module
  (func (result i32)
    i32.const 1
    if (result i32)
      i32.const 1
      br 0
    else
      i32.const 1
      br 0
    end))