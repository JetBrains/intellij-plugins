// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from '@angular/core';

@Component({
    selector: 'todo-cmp',
    templateUrl: "./boundClassWithSlash.html",
    styleUrls: ["./boundClassWithSlash.css"]
})
export class TodoCmp {
    first: boolean = true;
}
