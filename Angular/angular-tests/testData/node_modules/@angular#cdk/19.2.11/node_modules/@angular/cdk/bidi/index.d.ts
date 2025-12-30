export { BidiModule, Dir, Direction, Directionality } from '../bidi-module.d-BSI86Zrk.js';
import { InjectionToken } from '@angular/core';

/**
 * Injection token used to inject the document into Directionality.
 * This is used so that the value can be faked in tests.
 *
 * We can't use the real document in tests because changing the real `dir` causes geometry-based
 * tests in Safari to fail.
 *
 * We also can't re-provide the DOCUMENT token from platform-browser because the unit tests
 * themselves use things like `querySelector` in test code.
 *
 * This token is defined in a separate file from Directionality as a workaround for
 * https://github.com/angular/angular/issues/22559
 *
 * @docs-private
 */
declare const DIR_DOCUMENT: InjectionToken<Document>;

export { DIR_DOCUMENT };
