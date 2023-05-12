(<fold text='...' expand='true'>module $color-painter
  (<fold text='...' expand='true'>export "getHeight" (func $getHeight)</fold>)
  (<fold text='...' expand='true'>export "getWidth" (func $getWidth)</fold>)
  (<fold text='...' expand='true'>export "memory" (memory $mem)</fold>)
  (<fold text='...' expand='true'>export "setDimensions" (func $setDimensions)</fold>)

  (<fold text='...' expand='true'>type $getI32 (func (result i32))</fold>)

  (<fold text='...' expand='true'>memory $mem 4</fold>)

  (<fold text='...' expand='true'>global $bytesPerPage i32 i32.const 0x10000</fold>)
  (<fold text='...' expand='true'>global $bytesPerPixel i32 i32.const 4</fold>)
  (<fold text='...' expand='true'>global $false i32 i32.const 0</fold>)
  (<fold text='...' expand='true'>global $true i32 i32.const 1</fold>)

  (<fold text='...' expand='true'>global $height (mut i32) (<fold text='...' expand='true'>i32.const 0</fold>)</fold>)
  (<fold text='...' expand='true'>global $width (mut i32) (<fold text='...' expand='true'>i32.const 0</fold>)</fold>)

  (<fold text='...' expand='true'>func $getHeight (type $getI32) (<fold text='...' expand='true'>global.set $height</fold>)</fold>)
  (<fold text='...' expand='true'>func $getWidth (type $getI32) (<fold text='...' expand='true'>global.set $width</fold>)</fold>)

  (<fold text='...' expand='true'>func $setDimentions (param $width i32) (param $height i32) (result i32)
    (local $bytes i32)
    (local $pixels i32)
    (local $pages i32)
    (local $success i32)

    (<fold text='...' expand='true'>local.set $success (global.get $true)</fold>)

    (<fold text='...' expand='true'>local.set $pixels
      (i32.mul
        (local.get $width)
        (local.get $height))</fold>)
  </fold>)
</fold>)