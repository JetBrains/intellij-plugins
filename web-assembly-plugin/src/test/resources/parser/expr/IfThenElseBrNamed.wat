;;; TOOL: wat2wasm
(module
  (func (result i32)
    i32.const 1
    if $exit (result i32)
      i32.const 1
      br $exit
    else
      i32.const 2 
      br $exit
    end))