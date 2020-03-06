import { ModuleWithProviders } from '@angular/core';
import { TranslocoLoader } from './transloco.loader';
import { HashMap, Translation } from './types';
import { Observable } from 'rxjs';
import { TranslocoConfig } from './transloco.config';
export declare class TestingLoader implements TranslocoLoader {
    private langs;
    constructor(langs: HashMap<Translation>);
    getTranslation(lang: string): Observable<Translation> | Promise<Translation>;
}
export declare class TranslocoTestingModule {
    static withLangs(langs: HashMap<Translation>, config?: Partial<TranslocoConfig>): ModuleWithProviders;
}
