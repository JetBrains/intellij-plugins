// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, ViewChild, ViewChildren} from '@angular/core';

@Component({
    selector: 'home',
    templateUrl: './unusedReference.html'
})
export class Home {

    @ViewChild("refUsedInTS")
    view:any;

    @ViewChild("anotherRefUsedInTS")
    view2:any;

    @ViewChildren("yetAnotherRefUsedInTS")
    view3:any;

    @ViewChild("innerRef")
    view3:any;

}
