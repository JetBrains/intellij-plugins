// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';
import {Subject} from 'rxjs/Subject';

@Component({
    selector: 'ng-if-let',
    template: `
    <button (click)="nextUser()">Next User</button>
    <br>
    <div *ngIf="userObservable | async as my_user; else loading">
      Hello {{my_u<caret>ser.last}}, {{my_user.first}}!
    </div>
    <ng-template #loading let-my_user>Waiting... (User is {{my_user | json}})</ng-template>
  `
})
export class NgIfAs {
    userObservable = new Subject<{ first: string, last: string }>();
    first = ['John', 'Mike', 'Mary', 'Bob'];
    firstIndex = 0;
    last = ['Smith', 'Novotny', 'Angular'];
    lastIndex = 0;

    nextUser() {
        const first = this.first[this.firstIndex++];
        if (this.firstIndex >= this.first.length) {this.firstIndex = 0; }
        const last = this.last[this.lastIndex++];
        if (this.lastIndex >= this.last.length) {this.lastIndex = 0; }
        this.userObservable.next({first, last});
    }
}