import { MapsAPILoader } from './maps-api-loader';
/**
 * When using the NoOpMapsAPILoader, the Google Maps API must be added to the page via a `<script>`
 * Tag.
 * It's important that the Google Maps API script gets loaded first on the page.
 */
export declare class NoOpMapsAPILoader implements MapsAPILoader {
    load(): Promise<void>;
}
