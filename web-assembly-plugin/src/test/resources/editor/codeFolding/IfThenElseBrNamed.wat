;;; TOOL: wat2wasm
(<fold text='...' expand='true'>module
  (<fold text='...' expand='true'>func (result i32)
    i32.const 1
    if<fold text='...' expand='true'> $exit (result i32)
      i32.const 1
      br $exit
    else
      i32.const 2 
      br $exit
    </fold>end</fold>)</fold>)