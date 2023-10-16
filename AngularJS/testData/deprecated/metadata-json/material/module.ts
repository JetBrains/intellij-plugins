import {Component, NgModule} from "@angular/core"
import {MatFormFieldModule, MatInputModule} from "@angular/material"


@Component({
  template:`
    <mat-form-field></mat-form-field>
    <error descr="More than one component is matched on this element: FooComponent (foo) and FooComponent2 (foo)"><foo></error></foo>
    <bar></bar>
  `
})
export class MyComponent {

}

@Component({
  selector:"foo",
  template: ``
})
export class FooComponent {

}

@Component({
  selector:"foo",
  template: ``
})
export class FooComponent2 {

}


@NgModule({
  declarations: [
    MyComponent,
    FooComponent,
    FooComponent2
  ],
  imports: [
    MatInputModule,
    MatFormFieldModule
  ]
})
export class MyModule {

}