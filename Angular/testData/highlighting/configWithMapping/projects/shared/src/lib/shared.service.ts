import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SharedService {

  constructor() { }

  greet(name: string) {
    return `Hello ${name}!`;
  }
}
