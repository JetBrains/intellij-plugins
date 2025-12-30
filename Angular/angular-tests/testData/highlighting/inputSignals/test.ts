// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ɵinput as input} from '@angular/core';
import {TodoCmp} from "./signalInputs";

@Component({
    selector: 'test',
    templateUrl: "test.html",
    imports: [
        TodoCmp
    ],
    standalone: true
})
export class TestCmp {

}
