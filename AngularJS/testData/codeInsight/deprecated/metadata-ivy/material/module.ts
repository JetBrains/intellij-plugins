import {Component, NgModule} from "@angular/core"
import {FormControl} from '@angular/forms';
import {MatFormFieldModule, MatInputModule, MatTabsModule, MatTableModule} from "@angular/material"


@Component({
  template:`
    <mat-tab-group [selectedIndex]="selected.value"
                   (selectedIndexChange)="selected.<weak_warning descr="Argument types do not match parameters">setValue</weak_warning>($event)"
                   <weak_warning descr="Property bar is not provided by any applicable directives nor by mat-tab-group element">[bar]</weak_warning>="12"
    ></mat-tab-group>
    <mat-form-field></mat-form-field>
    <error descr="More than one component is matched on this element: FooComponent (foo) and FooComponent2 (foo)"><foo></error></foo>
    <<warning descr="Unknown html tag bar">bar</warning>></<warning descr="Unknown html tag bar">bar</warning>>
    <table mat-table>
      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
    </table>
  `
})
export class MyComponent {
  selected = new FormControl(0);
  displayedColumns = ['id', 'name'];
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
    MatFormFieldModule,
    MatTableModule
  ]
})
export class MyModule {

}