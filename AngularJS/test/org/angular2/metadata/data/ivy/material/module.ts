import {Component, NgModule} from "@angular/core"
import {FormControl} from '@angular/forms';
import {MatFormFieldModule, MatInputModule, MatTabsModule} from "@angular/material"


@Component({
  template:`
    <mat-tab-group [selectedIndex]="selected.value"
                   (selectedIndexChange)="selected.<weak_warning descr="Unresolved function or method setValue()">setValue</weak_warning>($event)"
                   <weak_warning descr="Property bar is not provided by any applicable directives nor by mat-tab-group element">[bar]</weak_warning>="12"
    ></mat-tab-group>
    <mat-form-field></mat-form-field>
    <error descr="More than one component is matched on this element: FooComponent (foo) and FooComponent2 (foo)"><foo></error></foo>
    <bar></bar>
  `
})
export class MyComponent {
  selected = new FormControl(0);

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
    MatTabsModule,
    MatFormFieldModule
  ]
})
export class MyModule {

}