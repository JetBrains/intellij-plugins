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
  @Input() item4?: string;
}

@Component({
             selector: 'app-root',
             standalone: true,
             template: `
               <div>
                 <app-test [item1]="myItem1"></app-test>
                 <app-test [item2]="<error descr="Type string | undefined is not assignable to type string  Type undefined is not assignable to type string">myItem1</error>"></app-test>
                 <app-test [item3]="myItem1"></app-test>
                 <app-test [item4]="myItem1"></app-test>
               </div>
               <div>
                 <app-test [item1]="myItem2"></app-test>
                 <app-test [item2]="<error descr="Type string | undefined is not assignable to type string  Type undefined is not assignable to type string">myItem2</error>"></app-test>
                 <app-test [item3]="myItem2"></app-test>
                 <app-test [item4]="myItem2"></app-test>
               </div>
               <div>
                 <app-test [item1]="myItem3"></app-test>
                 <app-test [item2]="<error descr="Type string | undefined is not assignable to type string  Type undefined is not assignable to type string">myItem3</error>"></app-test>
                 <app-test [item3]="myItem3"></app-test>
                 <app-test [item4]="myItem3"></app-test>
               </div>
               <div>
                 <app-test [item1]="myItem4"></app-test>
                 <app-test [item2]="myItem4"></app-test>
                 <app-test [item3]="myItem4"></app-test>
                 <app-test [item4]="myItem4"></app-test>
               </div>
             `,
             imports: [TestComponent, CommonModule, FormsModule]
           })
export class AppComponent {
  myItem1: string | undefined;
  myItem2?: string;
  myItem3? = "infer";
  myItem4 = "";

}
