;;; TOOL: wat2wasm
(module
  (func
    block $exit1
      br 0
    end
    block $exit2
      br $exit2
    end))