;;; TOOL: wat2wasm
(module (func
  block $foo
    br $foo
  end))