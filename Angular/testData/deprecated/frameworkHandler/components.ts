// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  registerField1: string
  registerField2: string
}

@Component({
  selector: 'app-register',
  templateUrl: './foo.component.html',
})
export class FooComponent  {
  fooField1: number
  fooField2: string
}
