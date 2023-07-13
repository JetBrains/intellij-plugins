// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
interface Person{
    familyName: string;
}

import {Component} from '@angular/core';
import {Subject} from 'rxjs/Subject';

@Component({
    selector: 'ng-if-as',
    template: `
      <div *ngIf="directPerson as person">
        Hello {{person.familyName}}!
      </div>
    `
})
export class AsyncPipeExample {
    public directPerson: Person = {
        familyName: 'Doe'
    };
}