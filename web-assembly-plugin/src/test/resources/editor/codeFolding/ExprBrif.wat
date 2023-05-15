;;; TOOL: wat2wasm
(<fold text='...' expand='true'>module
  (<fold text='...' expand='true'>func (result i32)
    block<fold text='...' expand='true'> $exit (result i32)
      i32.const 0
      i32.const 0
      br_if 0
      drop  
      i32.const 1
    </fold>end</fold>)</fold>)