export declare var google: any;
export interface GoogleMap extends MVCObject {
    data?: Data;
    constructor(el: HTMLElement, opts?: MapOptions): void;
    panTo(latLng: LatLng | LatLngLiteral): void;
    panBy(x: number, y: number): void;
    setZoom(zoom: number): void;
    getCenter(): LatLng;
    setCenter(latLng: LatLng | LatLngLiteral): void;
    getBounds(): LatLngBounds;
    getMapTypeId(): MapTypeId;
    getZoom(): number;
    setOptions(options: MapOptions): void;
    panToBounds(latLngBounds: LatLngBounds | LatLngBoundsLiteral): void;
    fitBounds(bounds: LatLngBounds | LatLngBoundsLiteral): void;
}
export interface LatLng {
    constructor(lat: number, lng: number): void;
    lat(): number;
    lng(): number;
    toString(): string;
}
export interface Marker extends MVCObject {
    constructor(options?: MarkerOptions): void;
    setMap(map: GoogleMap): void;
    setPosition(latLng: LatLng | LatLngLiteral): void;
    setTitle(title: string): void;
    setLabel(label: string | MarkerLabel): void;
    setDraggable(draggable: boolean): void;
    setIcon(icon: string): void;
    setOpacity(opacity: number): void;
    setVisible(visible: boolean): void;
    setZIndex(zIndex: number): void;
    setAnimation(animation: any): void;
    getLabel(): MarkerLabel;
    setClickable(clickable: boolean): void;
}
export interface MarkerOptions {
    position: LatLng | LatLngLiteral;
    title?: string;
    map?: GoogleMap;
    label?: string | MarkerLabel;
    draggable?: boolean;
    icon?: string;
    opacity?: number;
    visible?: boolean;
    zIndex?: number;
    clickable: boolean;
    animation?: any;
}
export interface MarkerLabel {
    color: string;
    fontFamily: string;
    fontSize: string;
    fontWeight: string;
    text: string;
}
export interface Circle extends MVCObject {
    getBounds(): LatLngBounds;
    getCenter(): LatLng;
    getDraggable(): boolean;
    getEditable(): boolean;
    getMap(): GoogleMap;
    getRadius(): number;
    getVisible(): boolean;
    setCenter(center: LatLng | LatLngLiteral): void;
    setDraggable(draggable: boolean): void;
    setEditable(editable: boolean): void;
    setMap(map: GoogleMap): void;
    setOptions(options: CircleOptions): void;
    setRadius(radius: number): void;
    setVisible(visible: boolean): void;
}
export interface CircleOptions {
    center?: LatLng | LatLngLiteral;
    clickable?: boolean;
    draggable?: boolean;
    editable?: boolean;
    fillColor?: string;
    fillOpacity?: number;
    map?: GoogleMap;
    radius?: number;
    strokeColor?: string;
    strokeOpacity?: number;
    strokePosition?: 'CENTER' | 'INSIDE' | 'OUTSIDE';
    strokeWeight?: number;
    visible?: boolean;
    zIndex?: number;
}
export interface Rectangle extends MVCObject {
    getBounds(): LatLngBounds;
    getDraggable(): boolean;
    getEditable(): boolean;
    getMap(): GoogleMap;
    getVisible(): boolean;
    setBounds(bounds: LatLngBounds | LatLngBoundsLiteral): void;
    setDraggable(draggable: boolean): void;
    setEditable(editable: boolean): void;
    setMap(map: GoogleMap): void;
    setOptions(options: RectangleOptions): void;
    setVisible(visible: boolean): void;
}
export interface RectangleOptions {
    bounds?: LatLngBounds | LatLngBoundsLiteral;
    clickable?: boolean;
    draggable?: boolean;
    editable?: boolean;
    fillColor?: string;
    fillOpacity?: number;
    map?: GoogleMap;
    strokeColor?: string;
    strokeOpacity?: number;
    strokePosition?: 'CENTER' | 'INSIDE' | 'OUTSIDE';
    strokeWeight?: number;
    visible?: boolean;
    zIndex?: number;
}
export interface LatLngBounds {
    contains(latLng: LatLng): boolean;
    equals(other: LatLngBounds | LatLngBoundsLiteral): boolean;
    extend(point: LatLng | LatLngLiteral): void;
    getCenter(): LatLng;
    getNorthEast(): LatLng;
    getSouthWest(): LatLng;
    intersects(other: LatLngBounds | LatLngBoundsLiteral): boolean;
    isEmpty(): boolean;
    toJSON(): LatLngBoundsLiteral;
    toSpan(): LatLng;
    toString(): string;
    toUrlValue(precision?: number): string;
    union(other: LatLngBounds | LatLngBoundsLiteral): LatLngBounds;
}
export interface LatLngBoundsLiteral {
    east: number;
    north: number;
    south: number;
    west: number;
}
export interface LatLngLiteral {
    lat: number;
    lng: number;
}
export interface MouseEvent {
    latLng: LatLng;
}
export interface MapOptions {
    center?: LatLng | LatLngLiteral;
    zoom?: number;
    minZoom?: number;
    maxZoom?: number;
    disableDoubleClickZoom?: boolean;
    disableDefaultUI?: boolean;
    scrollwheel?: boolean;
    backgroundColor?: string;
    draggable?: boolean;
    draggableCursor?: string;
    draggingCursor?: string;
    keyboardShortcuts?: boolean;
    styles?: MapTypeStyle[];
    zoomControl?: boolean;
    zoomControlOptions?: ZoomControlOptions;
    streetViewControl?: boolean;
    streetViewControlOptions?: StreetViewControlOptions;
    scaleControl?: boolean;
    scaleControlOptions?: ScaleControlOptions;
    mapTypeControl?: boolean;
    mapTypeControlOptions?: MapTypeControlOptions;
    panControl?: boolean;
    panControlOptions?: PanControlOptions;
    rotateControl?: boolean;
    rotateControlOptions?: RotateControlOptions;
    fullscreenControl?: boolean;
    fullscreenControlOptions?: FullscreenControlOptions;
    mapTypeId?: string | MapTypeId;
    clickableIcons?: boolean;
    gestureHandling?: 'cooperative' | 'greedy' | 'none' | 'auto';
}
export interface MapTypeStyle {
    elementType?: 'all' | 'geometry' | 'geometry.fill' | 'geometry.stroke' | 'labels' | 'labels.icon' | 'labels.text' | 'labels.text.fill' | 'labels.text.stroke';
    featureType?: 'administrative' | 'administrative.country' | 'administrative.land_parcel' | 'administrative.locality' | 'administrative.neighborhood' | 'administrative.province' | 'all' | 'landscape' | 'landscape.man_made' | 'landscape.natural' | 'landscape.natural.landcover' | 'landscape.natural.terrain' | 'poi' | 'poi.attraction' | 'poi.business' | 'poi.government' | 'poi.medical' | 'poi.park' | 'poi.place_of_worship' | 'poi.school' | 'poi.sports_complex' | 'road' | 'road.arterial' | 'road.highway' | 'road.highway.controlled_access' | 'road.local' | 'transit' | 'transit.line' | 'transit.station' | 'transit.station.airport' | 'transit.station.bus' | 'transit.station.rail' | 'water';
    stylers: MapTypeStyler[];
}
/**
 *  If more than one key is specified in a single MapTypeStyler, all but one will be ignored.
 */
export interface MapTypeStyler {
    color?: string;
    gamma?: number;
    hue?: string;
    invert_lightness?: boolean;
    lightness?: number;
    saturation?: number;
    visibility?: string;
    weight?: number;
}
export interface InfoWindow extends MVCObject {
    constructor(opts?: InfoWindowOptions): void;
    close(): void;
    getContent(): string | Node;
    getPosition(): LatLng;
    getZIndex(): number;
    open(map?: GoogleMap, anchor?: MVCObject): void;
    setContent(content: string | Node): void;
    setOptions(options: InfoWindowOptions): void;
    setPosition(position: LatLng | LatLngLiteral): void;
    setZIndex(zIndex: number): void;
}
export interface MVCObject {
    addListener(eventName: string, handler: Function): MapsEventListener;
}
export interface MapsEventListener {
    remove(): void;
}
export interface Size {
    height: number;
    width: number;
    constructor(width: number, height: number, widthUnit?: string, heightUnit?: string): void;
    equals(other: Size): boolean;
    toString(): string;
}
export interface InfoWindowOptions {
    content?: string | Node;
    disableAutoPan?: boolean;
    maxWidth?: number;
    pixelOffset?: Size;
    position?: LatLng | LatLngLiteral;
    zIndex?: number;
}
export interface Point {
    x: number;
    y: number;
    equals(other: Point): boolean;
    toString(): string;
}
export interface GoogleSymbol {
    anchor?: Point;
    fillColor?: string;
    fillOpacity?: string;
    labelOrigin?: Point;
    path?: string;
    rotation?: number;
    scale?: number;
    strokeColor?: string;
    strokeOpacity?: number;
    strokeWeight?: number;
}
export interface IconSequence {
    fixedRotation?: boolean;
    icon?: GoogleSymbol;
    offset?: string;
    repeat?: string;
}
export interface PolylineOptions {
    clickable?: boolean;
    draggable?: boolean;
    editable?: boolean;
    geodesic?: boolean;
    icons?: Array<IconSequence>;
    map?: GoogleMap;
    path?: Array<LatLng> | Array<LatLng | LatLngLiteral>;
    strokeColor?: string;
    strokeOpacity?: number;
    strokeWeight?: number;
    visible?: boolean;
    zIndex?: number;
}
export interface Polyline extends MVCObject {
    getDraggable(): boolean;
    getEditable(): boolean;
    getMap(): GoogleMap;
    getPath(): Array<LatLng>;
    getVisible(): boolean;
    setDraggable(draggable: boolean): void;
    setEditable(editable: boolean): void;
    setMap(map: GoogleMap): void;
    setOptions(options: PolylineOptions): void;
    setPath(path: Array<LatLng | LatLngLiteral>): void;
    setVisible(visible: boolean): void;
}
/**
 * PolyMouseEvent gets emitted when the user triggers mouse events on a polyline.
 */
export interface PolyMouseEvent extends MouseEvent {
    edge: number;
    path: number;
    vertex: number;
}
export interface PolygonOptions {
    clickable?: boolean;
    draggable?: boolean;
    editable?: boolean;
    fillColor?: string;
    fillOpacity?: number;
    geodesic?: boolean;
    icon?: Array<IconSequence>;
    map?: GoogleMap;
    paths?: Array<LatLng | LatLngLiteral> | Array<Array<LatLng | LatLngLiteral>>;
    strokeColor?: string;
    strokeOpacity?: number;
    strokeWeight?: number;
    visible?: boolean;
    zIndex?: number;
}
export interface Polygon extends MVCObject {
    getDraggable(): boolean;
    getEditable(): boolean;
    getMap(): GoogleMap;
    getPath(): Array<LatLng>;
    getPaths(): Array<Array<LatLng>>;
    getVisible(): boolean;
    setDraggable(draggable: boolean): void;
    setEditable(editable: boolean): void;
    setMap(map: GoogleMap): void;
    setPath(path: Array<LatLng> | Array<LatLng | LatLngLiteral>): void;
    setOptions(options: PolygonOptions): void;
    setPaths(paths: Array<Array<LatLng | LatLngLiteral>> | Array<LatLng | LatLngLiteral>): void;
    setVisible(visible: boolean): void;
}
export interface KmlLayer extends MVCObject {
    getDefaultViewport(): LatLngBounds;
    getMap(): GoogleMap;
    getMetadata(): KmlLayerMetadata;
    getStatus(): KmlLayerStatus;
    getUrl(): string;
    getZIndex(): number;
    setMap(map: GoogleMap): void;
    setOptions(options: KmlLayerOptions): void;
    setUrl(url: string): void;
    setZIndex(zIndex: number): void;
}
/**
 * See: https://developers.google.com/maps/documentation/javascript/reference?hl=de#KmlLayerStatus
 */
export declare type KmlLayerStatus = 'DOCUMENT_NOT_FOUND' | 'DOCUMENT_TOO_LARGE' | 'FETCH_ERROR' | 'INVALID_DOCUMENT' | 'INVALID_REQUEST' | 'LIMITS_EXCEEDED' | 'OK' | 'TIMED_OUT' | 'UNKNOWN';
/**
 * See: https://developers.google.com/maps/documentation/javascript/reference?hl=de#KmlLayerMetadata
 */
export interface KmlLayerMetadata {
    author: KmlAuthor;
    description: string;
    hasScreenOverlays: boolean;
    name: string;
    snippet: string;
}
export interface KmlAuthor {
    email: string;
    name: string;
    uri: string;
}
export interface KmlLayerOptions {
    clickable?: boolean;
    map?: GoogleMap;
    preserveViewport?: boolean;
    screenOverlays?: boolean;
    suppressInfoWindows?: boolean;
    url?: string;
    zIndex?: number;
}
export interface KmlFeatureData {
    author: KmlAuthor;
    description: string;
    id: string;
    infoWindowHtml: string;
    name: string;
    snippet: string;
}
export interface KmlMouseEvent extends MouseEvent {
    featureData: KmlFeatureData;
    pixelOffset: Size;
}
export interface Data extends MVCObject {
    features: Feature[];
    constructor(options?: DataOptions): void;
    addGeoJson(geoJson: Object, options?: GeoJsonOptions): Feature[];
    remove(feature: Feature): void;
    setControlPosition(controlPosition: ControlPosition): void;
    setControls(controls: string[]): void;
    setDrawingMode(drawingMode: string): void;
    setMap(map: GoogleMap): void;
    setStyle(style: () => void): void;
    forEach(callback: (feature: Feature) => void): void;
    loadGeoJson(url: string, options?: GeoJsonOptions, callback?: (feats: Feature[]) => void): void;
}
export interface Feature extends MVCObject {
    id?: number | string | undefined;
    geometry: Geometry;
    properties: any;
}
export interface DataOptions {
    controlPosition?: ControlPosition;
    controls?: string[];
    drawingMode?: string;
    featureFactory?: (geometry: Geometry) => Feature;
    map?: GoogleMap;
    style?: () => void;
}
export interface DataMouseEvent extends MouseEvent {
    feature: Feature;
}
export interface GeoJsonOptions {
    idPropertyName: string;
}
export interface Geometry {
    type: string;
}
/**
 * Identifiers used to specify the placement of controls on the map. Controls are
 * positioned relative to other controls in the same layout position. Controls that
 * are added first are positioned closer to the edge of the map.
 */
export declare enum ControlPosition {
    RIGHT_BOTTOM = 0,
    TOP_LEFT = 1,
    TOP_CENTER = 2,
    TOP_RIGHT = 3,
    LEFT_CENTER = 4,
    LEFT_TOP = 5,
    LEFT_BOTTOM = 6,
    RIGHT_TOP = 7,
    RIGHT_CENTER = 8,
    BOTTOM_RIGHT = 9,
    BOTTOM_LEFT = 10,
    BOTTOM_CENTER = 11,
}
export declare enum MapTypeId {
    /** This map type displays a transparent layer of major streets on satellite images. */
    HYBRID = 0,
    /** This map type displays a normal street map. */
    ROADMAP = 1,
    /** This map type displays satellite images. */
    SATELLITE = 2,
    /** This map type displays maps with physical features such as terrain and vegetation. */
    TERRAIN = 3,
}
/***** Controls *****/
/** Options for the rendering of the map type control. */
export interface MapTypeControlOptions {
    /** IDs of map types to show in the control. */
    mapTypeIds?: (MapTypeId | string)[];
    /**
     * Position id. Used to specify the position of the control on the map.
     * The default position is TOP_RIGHT.
     */
    position?: ControlPosition;
    /** Style id. Used to select what style of map type control to display. */
    style?: MapTypeControlStyle;
}
export declare enum MapTypeControlStyle {
    DEFAULT = 0,
    DROPDOWN_MENU = 1,
    HORIZONTAL_BAR = 2,
}
export interface OverviewMapControlOptions {
    opened?: boolean;
}
/** Options for the rendering of the pan control. */
export interface PanControlOptions {
    /**
     * Position id. Used to specify the position of the control on the map.
     * The default position is TOP_LEFT.
     */
    position?: ControlPosition;
}
/** Options for the rendering of the rotate control. */
export interface RotateControlOptions {
    /**
     * Position id. Used to specify the position of the control on the map.
     * The default position is TOP_LEFT.
     */
    position?: ControlPosition;
}
/** Options for the rendering of the scale control. */
export interface ScaleControlOptions {
    /** Style id. Used to select what style of scale control to display. */
    style?: ScaleControlStyle;
}
export declare enum ScaleControlStyle {
    DEFAULT = 0,
}
/** Options for the rendering of the Street View pegman control on the map. */
export interface StreetViewControlOptions {
    /**
     * Position id. Used to specify the position of the control on the map. The
     * default position is embedded within the navigation (zoom and pan) controls.
     * If this position is empty or the same as that specified in the
     * zoomControlOptions or panControlOptions, the Street View control will be
     * displayed as part of the navigation controls. Otherwise, it will be displayed
     * separately.
     */
    position?: ControlPosition;
}
/** Options for the rendering of the zoom control. */
export interface ZoomControlOptions {
    /**
     * Position id. Used to specify the position of the control on the map.
     * The default position is TOP_LEFT.
     */
    position?: ControlPosition;
    style?: ZoomControlStyle;
}
export declare enum ZoomControlStyle {
    DEFAULT = 0,
    LARGE = 1,
    SMALL = 2,
}
/** Options for the rendering of the fullscreen control. */
export interface FullscreenControlOptions {
    /**
     * Position id. Used to specify the position of the control on the map.
     * The default position is RIGHT_TOP.
     */
    position?: ControlPosition;
}
