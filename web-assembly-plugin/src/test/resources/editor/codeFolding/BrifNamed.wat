;;; TOOL: wat2wasm
(<fold text='...' expand='true'>module
  (<fold text='...' expand='true'>func
    block<fold text='...' expand='true'> $foo
      i32.const 1
      br_if $foo
    </fold>end</fold>)</fold>)