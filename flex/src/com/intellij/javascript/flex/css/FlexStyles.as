/**
 * Indicates whether to embed an FTE-enabled font for components. Flash Text Engine (FTE) is a library that provides text controls with a rich set of formatting options.
 * For Flex 4 and later, the default value is true. If you set the compatibility-version compiler option to 3.0.0, then the default value is false.
 * If you set the embedAsCFF property to true, then you can use the advanced formatting features of FTE such as bidirectional text, kerning, and ligatures. If you set the value of embedAsCFF to false, then the embedded font does not support FTE, and works only with the MX text components.
 * For information on using FTE-based classes for text rendering in your MX text controls, see <a href="http://help.adobe.com/en_US/flex/using/WS0FA8AEDB-C69F-4f19-ADA5-AA5757217624.html">Embedding fonts with MX components</a>.
 */
[Style(name="embedAsCFF", type="Boolean", inherit="yes")]


/**
 * Determines whether to include the advanced anti-aliasing information when embedding the font.
 * This property is optional and only used for legacy fonts. This property is ignored if you embed a font with the embedAsCFF property set to true.
 * You cannot use this option when embedding fonts from a SWF file because this option requires access to the original, raw font file to pre-calculate anti-aliasing information at compile time.
 * For more information on using advanced anti-aliasing, see <a href="http://help.adobe.com/en_US/flex/using/WS2db454920e96a9e51e63e3d11c0bf69084-7e0d.html">Using advanced anti-aliasing with non-CFF based fonts</a>.
 */
[Style(name="advancedAntiAliasing", type="Boolean", inherit="yes")]
