import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { Translation } from './types';
export interface TranslocoLoader {
    getTranslation(lang: string): Observable<Translation> | Promise<Translation>;
}
export declare class DefaultLoader implements TranslocoLoader {
    private translations;
    constructor(translations: Map<string, Translation>);
    getTranslation(lang: string): Observable<Translation>;
}
export declare const TRANSLOCO_LOADER: InjectionToken<import("./types").HashMap<any>>;
