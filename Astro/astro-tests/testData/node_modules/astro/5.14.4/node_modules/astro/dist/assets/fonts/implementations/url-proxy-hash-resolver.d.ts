import type { Hasher, UrlProxyContentResolver, UrlProxyHashResolver } from '../definitions.js';
export declare function createBuildUrlProxyHashResolver({ hasher, contentResolver, }: {
    hasher: Hasher;
    contentResolver: UrlProxyContentResolver;
}): UrlProxyHashResolver;
export declare function createDevUrlProxyHashResolver({ baseHashResolver, }: {
    baseHashResolver: UrlProxyHashResolver;
}): UrlProxyHashResolver;
