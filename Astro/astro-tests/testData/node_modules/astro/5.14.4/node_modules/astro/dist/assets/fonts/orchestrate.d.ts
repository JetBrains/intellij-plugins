import type { Storage } from 'unstorage';
import type { Logger } from '../../core/logger/core.js';
import type { CssRenderer, FontFileReader, FontMetricsResolver, FontTypeExtractor, Hasher, LocalProviderUrlResolver, RemoteFontProviderResolver, StringMatcher, SystemFallbacksProvider, UrlProxy } from './definitions.js';
import type { ConsumableMap, CreateUrlProxyParams, Defaults, FontFamily, FontFileDataMap, InternalConsumableMap } from './types.js';
/**
 * Manages how fonts are resolved:
 *
 * - families are resolved
 * - unifont providers are extracted from families
 * - unifont is initialized
 *
 * For each family:
 * - We create a URL proxy
 * - We resolve the font and normalize the result
 *
 * For each resolved font:
 * - We generate the CSS font face
 * - We generate optimized fallbacks if applicable
 * - We generate CSS variables
 *
 * Once that's done, the collected data is returned
 */
export declare function orchestrate({ families, hasher, remoteFontProviderResolver, localProviderUrlResolver, storage, cssRenderer, systemFallbacksProvider, fontMetricsResolver, fontTypeExtractor, fontFileReader, logger, createUrlProxy, defaults, bold, stringMatcher, }: {
    families: Array<FontFamily>;
    hasher: Hasher;
    remoteFontProviderResolver: RemoteFontProviderResolver;
    localProviderUrlResolver: LocalProviderUrlResolver;
    storage: Storage;
    cssRenderer: CssRenderer;
    systemFallbacksProvider: SystemFallbacksProvider;
    fontMetricsResolver: FontMetricsResolver;
    fontTypeExtractor: FontTypeExtractor;
    fontFileReader: FontFileReader;
    logger: Logger;
    createUrlProxy: (params: CreateUrlProxyParams) => UrlProxy;
    defaults: Defaults;
    bold: (input: string) => string;
    stringMatcher: StringMatcher;
}): Promise<{
    fontFileDataMap: FontFileDataMap;
    internalConsumableMap: InternalConsumableMap;
    consumableMap: ConsumableMap;
}>;
