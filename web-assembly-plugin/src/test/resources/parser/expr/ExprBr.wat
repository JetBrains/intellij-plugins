;;; TOOL: wat2wasm
(module
  (func (result i32)
    block $exit (result i32)
      i32.const 0
      br 0 
    end))