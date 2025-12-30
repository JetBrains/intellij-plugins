// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';

@Component({
    selector: 'dummy-list, dummy-nav-list',
    template: '<ng-content>Dummy</ng-content>',
    styleUrls: ['app/components/dummy/dummy.css'],
    providers: [],
    directives: [],
    pipes: []
})
export class DummyList {
    constructor() {}
    ngOnInit() {
    }
}