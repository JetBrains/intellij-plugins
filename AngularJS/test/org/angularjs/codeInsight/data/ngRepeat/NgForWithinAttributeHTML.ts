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
    templateUrl: './NgForWithinAttribute.html'
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