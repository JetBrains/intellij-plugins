import { InjectionToken } from '@angular/core';
import { TranslocoConfig } from './transloco.config';
export declare const TRANSLOCO_MISSING_HANDLER: InjectionToken<{}>;
export interface TranslocoMissingHandler {
    handle(key: string, config: TranslocoConfig): any;
}
export declare class DefaultHandler implements TranslocoMissingHandler {
    handle(key: string, config: TranslocoConfig): string;
}
