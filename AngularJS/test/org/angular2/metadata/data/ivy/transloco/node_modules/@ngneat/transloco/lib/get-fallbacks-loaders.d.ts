import { TranslocoLoader } from './transloco.loader';
import { InlineLoader } from './types';
export declare function getFallbacksLoaders(mainPath: string, fallbackPath: string, mainLoader: TranslocoLoader, inlineLoader: InlineLoader): import("rxjs").Observable<{
    translation: any;
    lang: string;
}>[];
