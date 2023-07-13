// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Observable} from "rxjs";
import {Component} from '@angular/core';

interface Person{
    familyName: string;
}

@Component({
    selector: 'ng-if-as',
    template: `
      <div *ngIf="{foo: foo | async} as r<caret>ec">
      </div>
    `
})
export class AsyncPipeExample {
    public foo: Observable<Person> = new Observable<Person>();
    public directPerson: Person = {
        familyName: 'Doe'
    };
}
