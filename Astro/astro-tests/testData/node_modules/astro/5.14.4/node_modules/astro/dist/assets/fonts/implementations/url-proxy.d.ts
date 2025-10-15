import type { DataCollector, UrlProxy, UrlProxyHashResolver, UrlResolver } from '../definitions.js';
export declare function createUrlProxy({ hashResolver, dataCollector, urlResolver, cssVariable, }: {
    hashResolver: UrlProxyHashResolver;
    dataCollector: DataCollector;
    urlResolver: UrlResolver;
    cssVariable: string;
}): UrlProxy;
