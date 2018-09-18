// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'app-my-component2',
    templateUrl: './foo.component.html'
})
export class MyComponentComponent implements OnInit {
    @Input() product: Product;
    constructor() { }

    ngOnInit() {
    }

}

export class Product {
    price: number;
}
