// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

/// <reference path="../../typings/tsd.d.ts" />

import {Component, View} from '@angular/core';
import {coreDirectives} from 'angular2/directives';
import {status, text} from '../utils/fetch'
import {Router} from 'angular2/router';

let styles   = require('./home.css');
let template = require('./home.html');


@Component({
    selector: 'home',
    templateUrl: "./event_private.html",
})
@View({
    styles: [ styles ],
    template: template,
    directives: [ coreDirectives ]
})
export class Home {
    jwt: string;
    decodedJwt: string;
    response: string;
    api: string;

    constructor(public router: Router) {
        this.jwt = localStorage.getItem('jwt');
        this.decodedJwt = this.jwt && window.jwt_decode(this.jwt);
    }

    logout() {
        localStorage.removeItem('jwt');
        this.router.parent.navigate('/login');
    }

    private callAnonymousApi() {
        this._callApi('Anonymous', 'http://localhost:3001/api/random-quote');
    }

    callSecuredApi() {
        this._callApi('Secured', 'http://localhost:3001/api/protected/random-quote');
    }
    _callApi(type, url) {
        this.response = null;
        this.api = type;
        window.fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Authorization': 'bearer ' + this.jwt
            }
        })
            .then(status)
            .then(text)
            .then((response) => {
            this.response = response;
        })
            .catch((error) => {
            this.response = error.message;
        });
    }

    callZ() {

    }

    private callA() {

    }

}
