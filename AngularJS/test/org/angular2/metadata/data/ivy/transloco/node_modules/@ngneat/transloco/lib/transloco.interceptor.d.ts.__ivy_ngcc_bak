import { InjectionToken } from '@angular/core';
import { Translation } from './types';
export declare const TRANSLOCO_INTERCEPTOR: InjectionToken<{}>;
export interface TranslocoInterceptor {
    preSaveTranslation(translation: Translation, lang: string): Translation;
    preSaveTranslationKey(key: string, value: string, lang: string): string;
}
export declare class DefaultInterceptor implements TranslocoInterceptor {
    preSaveTranslation(translation: Translation, lang: string): Translation;
    preSaveTranslationKey(key: string, value: string, lang: string): string;
}
