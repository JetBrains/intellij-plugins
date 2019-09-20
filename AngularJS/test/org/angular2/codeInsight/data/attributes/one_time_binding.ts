// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input} from "@angular/core";

@Component({selector: 'my-comp'})
class TodoCmp {

    @Input nullBinding;
    @Input anyBinding: any;
    @Input numberBinding: number;
    @Input booleanBinding: boolean;
    @Input stringBinding: string;
    @Input booleanBinding2: boolean;
    @Input objectBinding: { foo: boolean };

}
