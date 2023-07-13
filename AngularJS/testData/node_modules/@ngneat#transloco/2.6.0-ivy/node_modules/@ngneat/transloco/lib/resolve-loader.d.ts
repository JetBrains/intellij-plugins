import { TranslocoLoader } from './transloco.loader';
import { InlineLoader } from './types';
export declare function resolveLoader(path: string, mainLoader: TranslocoLoader, inlineLoader: InlineLoader): Promise<any> | import("rxjs").Observable<import("./types").HashMap<any>>;
