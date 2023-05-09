// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from "@angular/forms";

@Component({
             selector: 'app-test',
             standalone: true,
             template: ``
           })
export class TestComponent {
  @Input() item1? = '';
  @Input() item2 = '';
  @Input() item3: string | undefined = '';
}

@Component({
             selector: 'app-root',
             standalone: true,
             template: `
               <div>
                 <app-test [item1]="myItem"></app-test>
                 <app-test [item2]="<error descr="Type string | undefined is not assignable to type string  Type undefined is not assignable to type string">myItem</error>"></app-test>
                 <app-test [item3]="myItem"></app-test>
               </div>
             `,
             imports: [TestComponent, CommonModule, FormsModule]
           })
export class AppComponent {
  myItem: string | undefined;

}
