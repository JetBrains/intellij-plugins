;;; TOOL: wat2wasm
(module
  (func (local $foo i32)
    get_local $foo
    drop))