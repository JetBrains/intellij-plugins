;;; TOOL: wat2wasm
(module
  (func (result i32)
    block (result i32)
      nop
      i32.const 1
      return
    end))