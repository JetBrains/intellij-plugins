// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {NgForOf} from "@angular/common";

@Component({
   standalone: true,
   template: "<div *ng<caret>For='let item of items'></div>",
   imports: [
     NgForOf
   ]
 })
class MyComponent {

  items: Array<string>

}