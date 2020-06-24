// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from '@angular/core';
import {Observable} from "rxjs";

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
    templateUrl: "./asyncPipe.html"
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

    makePromise(): Promise<Contact> {
        return Promise.resolve(this.contacts[0]);
    }
    makeObservable(): Observable<Contacts> {
        return null
    }
}