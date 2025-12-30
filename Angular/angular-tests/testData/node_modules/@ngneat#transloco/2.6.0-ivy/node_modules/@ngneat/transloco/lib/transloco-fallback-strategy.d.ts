import { InjectionToken } from '@angular/core';
import { TranslocoConfig } from './transloco.config';
export declare const TRANSLOCO_FALLBACK_STRATEGY: InjectionToken<TranslocoFallbackStrategy>;
export interface TranslocoFallbackStrategy {
    getNextLangs(failedLang: string): string[];
}
export declare class DefaultFallbackStrategy implements TranslocoFallbackStrategy {
    private userConfig;
    constructor(userConfig: TranslocoConfig);
    getNextLangs(failedLang: string): string[];
}
