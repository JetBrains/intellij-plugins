// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import { Component, NgModule } from '@angular/core';

@Component({
  selector: 'app-template',
  templateUrl: './template.html'
})
export class TemplateComponent {
}

@Component({
  selector: 'app-foo'
})
export class FooComponent  {
}

@Component({
  selector: 'app-bar'
})
export class BarComponent  {
}

@NgModule({
  declarations: [TemplateComponent, BarComponent]
})
export class BarModule {

}

@NgModule({
  declarations: [TemplateComponent, FooComponent]
})
export class FooModule {

}