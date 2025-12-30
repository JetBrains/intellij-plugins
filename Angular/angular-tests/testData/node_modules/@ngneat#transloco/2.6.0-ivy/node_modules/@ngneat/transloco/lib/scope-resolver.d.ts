import { TranslocoScope } from './types';
import { TranslocoService } from './transloco.service';
declare type ScopeResolverParams = {
    inline: string | undefined;
    provider: TranslocoScope;
};
export declare class ScopeResolver {
    private translocoService;
    constructor(translocoService: TranslocoService);
    resolve({ inline, provider }?: ScopeResolverParams): string;
}
export {};
