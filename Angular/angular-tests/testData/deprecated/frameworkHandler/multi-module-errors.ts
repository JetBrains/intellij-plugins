// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import { Component, NgModule } from '@angular/core';

@Component({
  selector: 'app-foo'
})
export class FooComponent  {
}

@Component({
  selector: 'app-bar'
})
export class <error descr="BarComponent is declared in multiple Angular modules: BarModule and FooModule">BarComponent</error>  {
}

@NgModule({
  declarations: [BarComponent, FooComponent]
})
export class BarModule {

}

@NgModule({
  declarations: [BarComponent, FooComponent]
})
export class FooModule {

}