;;; TOOL: wat2wasm
(module
  (func
    loop 
      nop
    end
    loop $inner 
      nop
    end))