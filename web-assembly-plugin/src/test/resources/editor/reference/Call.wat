(module
  (func (param i32)
    if call 0 else call 1 end)
  (func $f (param i32)
    i32.const 1
    call 0
    call $f)
)