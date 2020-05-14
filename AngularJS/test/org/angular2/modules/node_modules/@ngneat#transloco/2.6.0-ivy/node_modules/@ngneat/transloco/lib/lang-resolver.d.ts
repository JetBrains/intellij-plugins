declare type LangResolverParams = {
    inline: string | undefined;
    provider: string | undefined;
    active: string | undefined;
};
export declare class LangResolver {
    initialized: boolean;
    resolve({ inline, provider, active }?: LangResolverParams): string;
    /**
     *
     * Resolve the lang
     *
     * @example
     *
     * resolveLangBasedOnScope('todos/en') => en
     * resolveLangBasedOnScope('en') => en
     *
     */
    resolveLangBasedOnScope(lang: string): string;
    /**
     *
     * Resolve the lang path for loading
     *
     * @example
     *
     * resolveLangPath('todos', 'en') => todos/en
     * resolveLangPath('en') => en
     *
     */
    resolveLangPath(lang: string, scope: string | undefined): string;
}
export {};
