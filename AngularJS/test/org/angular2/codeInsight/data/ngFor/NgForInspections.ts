// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';

interface MiniContact {
    username: string;
    is_hidden: boolean;
}

interface Contact extends MiniContact {
    email: string;
    created_at: string;
    updated_at: string;
}

type Contacts = Contact[];

@Component({
    selector: 'my-app',
    template: `
      <table>
        <thead>
        <tr>
          <th style="width: 1%;text-align: left;">#</th>
          <th style="width: 1%;text-align: left;">Username</th>
          <th style="text-align: left;">Email</th>
        </tr>
        </thead>
        <tbody>
        <tr *ngFor="let contact of contacts; index as i; trackBy: <error descr="Unresolved variable or type foo">foo</error>">
            <td>{{i + 1}}</td>
            <td>{{i.toExponential()}}</td>
            <td>{{i.<error descr="Unresolved function or method big()">big</error>()}}</td>
            <td>{{contact}}</td>
            <td>{{contact.username}}</td>
            <td>{{contact.<error descr="Unresolved variable foo">foo</error>}}</td>
        </tr>
        </tbody>
      </table>
    `
})
export class AppComponent {
    public contacts: Contacts = [{
        username: 'Andrey',
        is_hidden: false,
        email: 'andrey@post.com',
        created_at: '',
        updated_at: '',
    }, {
        username: 'Silly',
        is_hidden: false,
        email: 'silly@post.com',
        created_at: '',
        updated_at: '',
    }];
}