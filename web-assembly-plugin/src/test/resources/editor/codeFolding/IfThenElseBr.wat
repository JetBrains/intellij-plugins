;;; TOOL: wat2wasm
(<fold text='...' expand='true'>module
  (<fold text='...' expand='true'>func (result i32)
    i32.const 1
    if<fold text='...' expand='true'> (result i32)
      i32.const 1
      br 0
    else
      i32.const 1
      br 0
    </fold>end</fold>)</fold>)