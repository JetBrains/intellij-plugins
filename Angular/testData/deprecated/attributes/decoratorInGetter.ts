// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'app-my',
    template: `<app-my <caret></app-my>`,
    styleUrls: ['./my.component.css']
})
export class MyComponent implements OnInit {

    private _age: number;

    public get age(): number {
        return this._age;
    }

    @Input()
    public set age(value: number) {
        this._age = value;
    }

    constructor() {
    }
    ngOnInit() {
    }
}