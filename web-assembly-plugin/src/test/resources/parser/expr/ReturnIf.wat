;;; TOOL: wat2wasm
(module
  (func (result i32)
    i32.const 1
    if (result i32)
       i32.const 2
    else
       i32.const 3
    end
    return))