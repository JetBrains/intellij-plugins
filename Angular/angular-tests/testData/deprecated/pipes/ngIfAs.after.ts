// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Observable} from "rxjs";
import {Component} from '@angular/core';
import {CommonModule} from "@angular/common";

interface Person{
    familyName: string;
}

@Component({
    selector: 'ng-if-as',
    standalone: true,
    imports: [CommonModule],
    template: `
      <div *ngIf="personObservable | async as person">
        Hello {{person.familyName}}!
      </div>
    `
})
export class AsyncPipeExample {
    public personObservable: Observable<Person> = new Observable<Person>();
}