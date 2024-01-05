// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from '@angular/core';
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";

@Component({
   selector: 'test',
   standalone: true,
   imports: [CommonModule, FormsModule],
   template: `
        <form <warning descr="Attribute ngFoo is not allowed here">ngFoo</warning>></form>
        <form ngNativeValidate></form>
    `
 })
export class TestComp {
}
