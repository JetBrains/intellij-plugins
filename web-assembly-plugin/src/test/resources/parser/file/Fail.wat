(module
  (func (export "fail_me") (result i32)
    i32.const 1
    i32.const 0
    i32.div_s))