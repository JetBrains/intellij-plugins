package {
public class int extends Number {}
public class uint extends Number {}
public native function eval(x:*):Object;
public native function parseInt(string:*, radix:int = 10):Number;
public native function parseFloat(string:*):Number;
public native function isNaN(number:Number):Boolean;
public native function get NaN():Number;
public native function isFinite(number:Number):Boolean;
public native function decodeURI(encodedURI:String):String;
public native function decodeURIComponent(encodedURIComponent:String):String;
public native function get Infinity():Number;
public native function encodeURI(uri:String):String;
public native function encodeURIComponent(uriComponent:String):String;
public native var undefined:*;

public dynamic class Object {
    public native function get constructor():*;
    public native function set constructor(o:*):void;
    public native function get length():Number;
    public native function set length(n:Number):void;
    public native function get prototype():Object;
    public native function set prototype(o:Object):void;

    public native function toLocaleString():Object;

    public native function toSource():Object;
    public native function unwatch(prop:String):void;
    public native function watch(prop:String, handler:Object):void;

    public native function hasOwnProperty(propertyName:String):Boolean;
    public native function isPrototypeOf(o:Object):Boolean;
    public native function propertyIsEnumerable(propertyName:String):Boolean;

    public native function toString():String;
}

public dynamic class Array extends Object {
    native public function Array(... rest):*;
    public native function get index():Number;
    public native function get input():Number;
    public native function concat(... items):Array;
    public native function join(separator:String):String;
    public native function pop():*;
    public native function push(... items):void;
    public native function reverse():Array;
    public native function shift():*;

    native function slice(start:Number = 0, end:Number = Number.MAX_VALUE):Array;
    public native function sort(comparefn:Function = null):Array;
    public native function splice(start:Number,deleteCount:Number = 0, ... items):Array;
    public native function unshift(... items):uint;
    public native function get length():uint;
    private static const AS3:* = "http://adobe.com/AS3/2006/builtin";
}

public class Boolean extends Object {
    public native function valueOf():Boolean;
}

public dynamic class Date extends Object {
    public native function Date(... rest);
    public native function getDate():Number;
    public native function getDay():Number;
    public native function getMonth():Number;
    public native function getFullYear():Number;
    public native function getHours():Number;

    public native function getMilliseconds():Number;
    public native function getMinutes():Number;
    public native function getSeconds():Number;
    public native function getTime():Number;
    public native function getTimezoneOffset():Number;

    public native function getUTCDate():Number;
    public native function getUTCDay():Number;
    public native function getUTCFullYear():Number;
    public native function getUTCHours():Number;
    public native function getUTCMilliseconds():Number;
    public native function getUTCMinutes():Number;

    public native function getUTCMonth():Number;
    public native function getUTCSeconds():Number;
    public native function getYear():Number;
    public static native function parse(s:*):Date;
    public native function setDate(date:*):void;

    public native function setFullYear(year:Number, month: Number = 1, date: Number = 1):void;
    public native function setHours(hour:Number, min: Number = 1, sec: Number = 1, ms:Number = 1):void;
    public native function setMilliseconds(ms: Number):void;

    public native function setMinutes(min: Number, sec: Number = 1, ms:Number = 1):void;
    public native function setMonth(month: Number, date: Number = 1):void;
    public native function setSeconds(sec: Number, ms:Number = 1):void;

    public native function setTime(time: Number):void;
    public native function setUTCDate(date: Number):void;
    public native function setUTCFullYear(year:Number, month: Number = 1, date: Number = 1):void;
    public native function setUTCHours(hour:Number, min: Number = 1, sec: Number = 1, ms:Number = 1):void;

    public native function setUTCMilliseconds(ms: Number):void;
    public native function setUTCMinutes(min: Number, sec: Number = 1, ms:Number = 1):void;
    public native function setUTCMonth(month: Number, date: Number = 1):void;
    public native function setUTCSeconds(sec: Number, ms:Number = 1):void;

    public native function setYear(year: Number):void;
    public native function toGMTString():String;
    public native function toLocaleDateString():String;
    public native function toLocaleTimeString():String;
    public native function toDateString():String;

    public native function toTimeString():String;
    public native function toUTCString():String;
    public native static function UTC(year:Number, month:Number, date:Number = 1, hours:Number = 1, minutes:Number = 1, seconds:Number = 1, ms:Number = 1):String;
    public native function valueOf():Date;
}

class Arguments extends Array {
    public native function get callee():Function;
    public native function set callee(value:Function):void;
    [Deprecated(message="Property caller is obsolete")]
    public native function get caller():Function;
}

public dynamic class Function extends Object {
    public native function get arguments():Arguments;
    public native function get arity():Number;
    public native function apply(thisArg:Object, argArray:Array = null):Object;
    public native function call(thisArg:Object, ...args):Object;
}

public class Math extends Object {
    public native static const E:Number;
    public native static const LN10:Number;
    public native static const LN2:Number;
    public native static const LOG10E:Number;
    public native static const LOG2E:Number;

    public native static const PI:Number;
    public native static const SQRT1_2:Number;
    public native static const SQRT2:Number;

    public native static function abs(x:Number):Number;
    public native static function acos(x:Number):Number;
    public native static function asin(x:Number):Number;
    public native static function atan(x:Number):Number;
    public native static function atan2(y:Number, x:Number):Number;

    public native static function ceil(value:Number):Number;
    public native static function cos(x:Number):Number;
    public native static function exp(x:Number):Number;
    public native static function floor(x:Number):Number;
    public native static function log(x:Number):Number;
    public native static function max(... x/*Number*/):Number;
    public native static function min(... x/*Number*/):Number;

    public native static function pow(x:Number, y:Number):Number;
    public native static function random():Number;
    public native static function round(x:Number):Number;
    public native static function sin(x:Number):Number;
    public native static function sqrt(x:Number):Number;
    public native static function tan(x:Number):Number;
}

public class Number extends Object {
    public static native function get MAX_VALUE():Number
    public static native function get MIN_VALUE():Number
    public static native function get NaN():Number
    public static native function get Infinity():Number
    public static native function get NEGATIVE_INFINITY():Number
    public static native function get POSITIVE_INFINITY():Number

    public native function toExponential(fractionalDigits:Number):String;
    public native function toFixed(fractionalDigits:Number):String;
    public native function toPrecision(precision:Number):String;
    public native function toString(radix:Number = 10):String;
    public native function valueOf():Number;
}

public dynamic class RegExp extends Object {
    public native function RegExp(pattern:String = null, options:String = null);
    public native function get global():Boolean;
    public native function get ignoreCase():Boolean;
    public native function get lastIndex():Number;
    public native function set lastIndex(n:Number):void;

    public native static function get leftContext():String;
    public native static function get input():String;
    public native static function get lastParen():String;
    public native static function get lastMatch():String;
    public native static function get rightContext():String;

    public native function get multiline():Boolean;
    public native function get source():String;

    public native static function get $1():String;
    public native static function get $2():String;
    public native static function get $3():String;
    public native static function get $4():String;
    public native static function get $5():String;

    public native function exec(string:String):*;
    public native function test(string:String):Boolean;
}

public class String extends Object {
    public native function anchor(nameAttribute:String):String;
    public native function big():String;
    public native function blink():String;
    public native function bold():String;
    public native function charAt(pos:Number):String;
    public native function charCodeAt(pos:Number):Number;

    public native function concat(...strings):String;
    public native function fixed():String;
    public native function fontcolor(color:String):String;
    public native function fontsize(size:Number):String;
    public static native function fromCharCode(... chars):String;
    public native function indexOf(searchString:String, position:Number = -1):Number;

    public native function italics():String;
    public native function lastIndexOf(searchString:String, position:Number = -1):Number;
    native public function get length():int;
    public native function link(href:String):String;
    public native function localeCompare(other:String):int;
    public native function match(regexp:RegExp):Array;
    public native function replace(searchValue:*, replaceValue:*):String;

    public native function search(regexp:*):int;
    public native function slice(start:Number, end:Number = Number.MAX_VALUE):String;
    public native function small():String;
    public native function split(separator:*, limit:Number = -1):Array;
    public native function strike():String;
    public native function sub():String;

    public native function substr(start:Number, length:Number = -1):String;
    public native function substring(start:Number, end:Number = -1):String;
    public native function sup():String;
    public native function toLowerCase():String;
    public native function toLocaleLowerCase():String;
    public native function toLocaleUpperCase():String;
    public native function toUpperCase():String;
    public native function valueOf():String;
}

public dynamic class Error extends Object {
    public native function Error(msg:*="", id:*=0);
    public var name:String;
    public var message:String;
    native AS3 function getStackTrace():String;
}

public class EvalError extends Error {}
public class RangeError extends Error {}
public class ReferenceError extends Error {}
public class SyntaxError extends Error {}
public class TypeError extends Error {}
public class URIError extends Error {}
}

/**
 * Determines whether the specified string is a valid name for an XML element or attribute.
 * @param str - A string to evaluate.
 * @return Returns true if the str argument is a valid XML name; false otherwise.
 */
package {
public native function isXMLName(str:String):Boolean

/**
 * An XMLList object is an ordered collection of properties. An XMLList object represents an XML document, an XML fragment,
 * or an arbitrary collection of XML objects.
 * An XMLList object with one XML element is treated the same as an XML object. When there is one XML element, all methods
 * that are available for the XML object are also available for the XMLList object.
 */
public final dynamic class XMLList extends XML {
    /**
     * Creates a new XMLList object.
     * @param value - Any object that can be converted to an XMLList object by using the top-level XMLList() function.
     * @constructor
     */
    public native function XMLList(value:Object = null):XMLList

    /**
     * Calls the attribute() method of each XML object and returns an XMLList object of the results. The results match
     * the given attributeName parameter. If there is no match, the attribute() method returns an empty XMLList object.
     * @param attributeName The name of the attribute that you want to include in an XMLList object.
     * @return
     */
    public native function attribute(attributeName:*):XMLList

    /**
     * Calls the attributes() method of each XML object and returns an XMLList object of attributes for each XML object.
     * @return An XMLList object of attributes for each XML object.
     */
    public native function attributes():XMLList

    /**
     * Calls the child() method of each XML object and returns an XMLList object that contains the results in order.
     * @param propertyName - The element name or integer of the XML child.
     * @return An XMLList object of child nodes that match the input parameter.
     */
    public native function child(propertyName:Object):XMLList

    /**
     * Calls the children() method of each XML object and returns an XMLList object that contains the results.
     * @return An XMLList object of the children in the XML objects.
     */
    public native function children():XMLList

    /**
     * Calls the comments() method of each XML object and returns an XMLList of comments.
     * @return An XMLList of the comments in the XML objects.
     */
    public native function comments():XMLList

    /**
     * Checks whether the XMLList object contains an XML object that is equal to the given value parameter.
     * @param value - An XML object to compare against the current XMLList object.
     * @return If the XMLList contains the XML object declared in the value parameter, then true; otherwise false.
     */
    public native function contains(value:XML):Boolean

    /**
     * Returns a copy of the given XMLList object. The copy is a duplicate of the entire tree of nodes. The copied XML object has no parent
     * and returns null if you attempt to call the parent() method.
     * @return The copy of the XMLList object.
     */
    public native function copy():XMLList

    /**
     *  Returns all descendants (children, grandchildren, great-grandchildren, and so on) of the XML object that have the given name parameter.
     * The name parameter can be a QName object, a String data type, or any other data type that is then converted to a String data type.
     * To return all descendants, use the asterisk (*) parameter. If no parameter is passed, the string "*" is passed and returns all
     * descendants of the XML object.
     * @param name - The name of the element to match, defaults to *.
     */
    public native function descendants(name:Object = *):XMLList

    /**
     * Calls the elements() method of each XML object. The name parameter is passed to the descendants() method. If no parameter is passed,
     * the string "*" is passed to the descendants() method.
     * @param name - the name of the elements to match, defaulting to *.
     * @return An XMLList object of the matching child elements of the XML objects
     */
    public native function elements(name:Object = *):XMLList

    /**
     *  Checks whether the XMLList object contains complex content. An XMLList object is considered to contain complex content if it is not
     * empty and either of the following conditions is true:
     * The XMLList object contains a single XML item with complex content.
     * The XMLList object contains elements.
     * @return  - If the XMLList object contains complex content, then true; otherwise false.
     */
    public native function hasComplexContent():Boolean

    /**
     * Checks for the property specified by p.
     * @param p The property to match.
     * @return  - If the parameter exists, then true; otherwise false.
     */
    public native function hasOwnProperty	(p:String):Boolean

    /**
     *  Checks whether the XMLList object contains simple content. An XMLList object is considered to contain simple content if one or more
     * of the following conditions is true:
     * The XMLList object is empty
     * The XMLList object contains a single XML item with simple content
     * The XMLList object contains no elements
     * @return  - If the XMLList contains simple content, then true; otherwise false.
     */
    public native function hasSimpleContent():Boolean

    /**
     * Returns the number of properties in the XMLList object.
     * @return  - If the parameter exists, then true; otherwise false.
     */
    public native function length	():int

    /**
     * Merges adjacent text nodes and eliminates empty text nodes for each of the following: all text nodes in the XMLList, all the XML
     * objects contained in the XMLList, and the descendants of all the XML objects in the XMLList.
     * @return  - The normalized XMLList object.
     */
    public native function normalize():XMLList

    /**
     * Returns the parent of the XMLList object if all items in the XMLList object have the same parent. If the XMLList object has no parent
     * or different parents, the method returns undefined.
     * @return  - Returns the parent XML object.
     */
    public native function parent():XML

    /**
     * If a name parameter is provided, lists all the children of the XMLList object that contain processing instructions with that name.
     * With no parameters, the method lists all the children of the XMLList object that contain any processing instructions.
     * @param name The name of the processing instructions to match.
     * @return  - An XMLList object that contains the processing instructions for each XML object.
     */
    public native function processingInstructions(name:String = "*"):XMLList

    /**
     * Calls the text() method of each XML object and returns an XMLList object that contains the results.
     * @param name The name of the processing instructions to match.
     * @return  - An XMLList object of all XML properties of the XMLList object that represent XML text nodes.
     */
    public native function text():XMLList

    /**
     * Returns a string representation of all the XML objects in an XMLList object. The rules for this conversion depend on whether the XML
     * object has simple content or complex content:
     * If the XML object has simple content, toString() returns the string contents of the XML object with the following stripped out: the
     * start tag, attributes, namespace declarations, and end tag.
     * If the XML object has complex content, toString() returns an XML encoded string representing the entire XML object, including the start
     *  tag, attributes, namespace declarations, and end tag.
     * To return the entire XML object every time, use the toXMLString() method.
     * @return  - The string representation of the XML object.
     */
    public native function toString():String

    /**
     * Returns a string representation of all the XML objects in an XMLList object. Unlike the toString() method, the toXMLString()
     * method always returns the start tag, attributes, and end tag of the XML object, regardless of whether the XML object has simple content
     *  or complex content. (The toString() method strips out these items for XML objects that contain simple content.)
     * @return  - The string representation of the XML object.
     */
    public native function toXMLString():String

    public native function valueOf():XMLList;
}

/**
 * The XML class contains methods and properties for working with XML objects. The XML class (along with the XMLList, Namespace, and
 * QName classes) implements the powerful XML-handling standards defined in ECMAScript for XML (E4X) specification (ECMA-357 edition 2).
 * Use the toXMLString() method to return a string representation of the XML object regardless of whether the XML object has simple content
 * or complex content.
 */
public dynamic class XML {
    /**
     *  Determines whether XML comments are ignored when XML objects parse the source XML data. By default, the comments are ignored (true).
     * To include XML comments, set this property to false. The ignoreComments property is used only during the XML parsing, not during the call
     * to any method such as myXMLObject.child(*).toXMLString(). If the source XML includes comment nodes, they are kept or discarded during the
     * XML parsing.
     */
    public static native function get ignoreComments():Boolean
    public static native function set ignoreComments(value:Boolean):void

    /**
     * Determines whether XML processing instructions are ignored when XML objects parse the source XML data. By default, the processing
     * instructions are ignored (true). To include XML processing instructions, set this property to false. The ignoreProcessingInstructions
     * property is used only during the XML parsing, not during the call to any method such as myXMLObject.child(*).toXMLString(). If the
     * source XML includes processing instructions nodes, they are kept or discarded during the XML parsing.
     */
    public static native function get ignoreProcessingInstructions():Boolean
    public static native function set ignoreProcessingInstructions(value:Boolean):void

    /**
     * Determines whether white space characters at the beginning and end of text nodes are ignored during parsing. By default, white space
     * is ignored (true). If a text node is 100% white space and the ignoreWhitespace property is set to true, then the node is not created.
     * To show white space in a text node, set the ignoreWhitespace property to false.
     */
    public static native function get ignoreWhitespace():Boolean
    public static native function set ignoreWhitespace(value:Boolean):void

    /**
     * Determines the amount of indentation applied by the toString() and toXMLString() methods when the XML.prettyPrinting property is set
     * to true. Indentation is applied with the space character, not the tab character. The default value is 2.
     */
    public static native function get prettyIndent():int
    public static native function set prettyIndent(value:int):void

    /**
     * Determines whether the toString() and toXMLString() methods normalize white space characters between some tags. The default value is true.
     */
    public static native function get prettyPrinting():Boolean
    public static native function set prettyPrinting(value:Boolean):void

    /**
     * Creates a new XML object. You must use the constructor to create an XML object before you call any of the methods of the XML class.
     * Use the toXMLString() method to return a string representation of the XML object regardless of whether the XML object has simple
     * content or complex content.
     * @param value
     * @constructor
     */
    public native function XML(value:Object = null):XML;

    /**
     * Adds a namespace to the set of in-scope namespaces for the XML object. If the namespace already exists in the in-scope namespaces
     * for the XML object (with a prefix matching that of the given parameter), then the prefix of the existing namespace is set to undefined.
     * If the input parameter is a Namespace object, it's used directly. If it's a QName object, the input parameter's URI is used to create
     * a new namespace; otherwise, it's converted to a String and a namespace is created from the String.
     * @param The namespace to add to the XML object.
     * @return The new XML object, with the namespace added.
     */
    public native function addNamespace(ns:Object):XML

    /**
     * Appends the given child to the end of the XML object's properties. The appendChild() method takes an XML object, an XMLList object,
     * or any other data type that is then converted to a String.
     * Use the delete (XML) operator to remove XML nodes.
     * @param The XML object to append.
     * @return The resulting XML object.
     */
    public native function appendChild(ns:Object):XML

    /**
     * Returns the XML value of the attribute that has the name matching the attributeName  parameter. Attributes are found within XML
     * elements. In the following example, the element has an attribute named "gender" with the value "boy": <first gender="boy">John</first>.
     * The attributeName parameter can be any data type; however, String is the most common data type to use. When passing any object other
     * than a QName object, the attributeName parameter uses the toString() method to convert the parameter to a string.
     * If you need a qualified name reference, you can pass in a QName object. A QName object defines a namespace and the local name, which
     * you can use to define the qualified name of an attribute. Therefore calling attribute(qname) is not the same as calling
     * attribute(qname.toString()).
     * @param attributeName The name of the attribute.
     * @return An XMLList object or an empty XMLList object. Returns an empty XMLList object when an attribute value has not been defined.
     */
    public native function attribute(attributeName:*):XMLList

    /**
     * Returns a list of attribute values for the given XML object. Use the name() method with the attributes() method to return the name
     * of an attribute. Use @* to return the names of all attributes.
     * @return The list of attribute values.
     */
    public native function attributes():XMLList

    /**
     *  Lists the children of an XML object. An XML child is an XML element, text node, comment, or processing instruction.
     * Use the propertyName parameter to list the contents of a specific XML child. For example, to return the contents of a child named
     * <first>, use child.name("first"). You can generate the same result by using the child's index number. The index number identifies
     * the child's position in the list of other XML children. For example, name.child(0) returns the first child in a list.
     * Use an asterisk (*) to output all the children in an XML document. For example, doc.child("*").
     * Use the length() method with the asterisk (*) parameter of the child() method to output the total number of children. For example,
     * numChildren = doc.child("*").length().
     * @param propertyName - The element name or integer of the XML child.
     * @return An XMLList object of child nodes that match the input parameter.
     */
    public native function child(propertyName:Object):XMLList

    /**
     * Identifies the zero-indexed position of this XML object within the context of its parent.
     * @return The position of the object. Returns -1 as well as positive integers.
     */
    public native function childIndex():int

    /**
     * Lists the children of the XML object in the sequence in which they appear. An XML child is an XML element, text node, comment,
     * or processing instruction.
     * @return An XMLList object of the XML object's children.
     */

    public native function children():XMLList

    /**
     * Lists the properties of the XML object that contain XML comments.
     * @return An XMLList object of the properties that contain comments.
     */
    public native function comments():XMLList

    /**
     * Compares the XML object against the given value parameter.
     * @param value - A value to compare against the current XML object.
     * @return If the XML object matches the value parameter, then true; otherwise false.
     */
    public native function contains(value:XML):Boolean

    /**
     * Returns a copy of the given XML object. The copy is a duplicate of the entire tree of nodes. The copied XML object has no parent and
     * returns null if you attempt to call the parent() method.
     * @return The copy of the object.
     */
    public native function copy():XML

    /**
     * Returns an object with the following properties set to the default values: ignoreComments, ignoreProcessingInstructions,
     * ignoreWhitespace, prettyIndent, and prettyPrinting.
     */
    public native static function defaultSettings():Object

    /**
     * Returns all descendants (children, grandchildren, great-grandchildren, and so on) of the XML object that have the given name
     * parameter. The name parameter is optional. The name parameter can be a QName object, a String data type or any other data type that is
     * then converted to a String data type. To return all descendants, use the "*" parameter. If no parameter is passed, the string "*" is
     * passed and returns all descendants of the XML object.
     * @param name The name of the element to match.
     * @return An XMLList object of matching descendants. If there are no descendants, returns an empty XMLList object.
     */
    public native function descendants(name:Object = "*"):XMLList

    /**
     *  Lists the elements of an XML object. An element consists of a start and an end tag; for example <first></first>. The name parameter
     * is optional. The name parameter can be a QName object, a String data type, or any other data type that is then converted to a String
     * data type. Use the name parameter to list a specific element. For example, the element "first" returns "John" in this example:
     * <first>John</first>.
     * To list all elements, use the asterisk (*) as the parameter. The asterisk is also the default parameter. Use the length() method with
     * the asterisk parameter to output the total number of elements. For example, numElement = addressbook.elements("*").length().
     * @param name - The name of the element. An element's name is surrounded by angle brackets. For example, "first" is the name
     * in this example: <first></first>.
     * @return An XMLList object of the element's content. The element's content falls between the start and end tags. If you use the
     * asterisk (*) to call all elements, both the element's tags and content are returned.
     */
    public native function elements(name:Object = "*"):XMLList

    /**
     * Checks to see whether the XML object contains complex content. An XML object contains complex content if it has child elements. XML
     * objects that representing attributes, comments, processing instructions, and text nodes do not have complex content. However, an object
     * that contains these can still be considered to contain complex content (if the object has child elements).
     * @return If the XML object contains complex content, true; otherwise false.
     */
    public native function hasComplexContent():Boolean

    /**
     * Checks to see whether the XML object contains simple content. An XML object contains simple content if it represents a text node, an
     * attribute node, or an XML element that has no child elements. XML objects that represent comments and processing instructions do not
     * contain simple content.
     * @return If the XML object contains simple content, true; otherwise false.
     */
    public native function hasSimpleContent():Boolean

    /**
     * Lists the namespaces for the XML object, based on the object's parent.
     * @return An array of Namespace objects
     */
    [ArrayElementType("Namespace")]
    public native function inScopeNamespaces():Array

    /**
     *  Inserts the given child2 parameter after the child1 parameter in this XML object and returns the resulting object. If the child1
     * parameter is null, the method inserts the contents of child2 before all children of the XML object (in other words, after none).
     * If child1 is provided, but it does not exist in the XML object, the XML object is not modified and undefined is returned.
     * If you call this method on an XML child that is not an element (text, attributes, comments, pi, and so on) undefined is returned.
     * Use the delete (XML) operator to remove XML nodes.
     * @param child1 - The object in the source object that you insert before child2.
     * @param child2 - The object to insert.
     * @return The resulting XML object or undefined.
     */
    public native function insertChildAfter(child1:Object, child2:Object):*

    /**
     *  Inserts the given child2 parameter before the child1 parameter in this XML object and returns the resulting object. If the child1
     * parameter is null, the method inserts the contents of child2 after all children of the XML object (in other words, before none).
     * If child1 is provided, but it does not exist in the XML object, the XML object is not modified and undefined is returned.
     * If you call this method on an XML child that is not an element (text, attributes, comments, pi, and so on) undefined is returned.
     * Use the delete (XML) operator to remove XML nodes.
     * @param child1 - The object in the source object that you insert after child2.
     * @param child2 - The object to insert.
     * @return The resulting XML object or undefined.
     */
    public native function insertChildBefore(child1:Object, child2:Object):*

    /**
     * For XML objects, this method always returns the integer 1. The length() method of the XMLList class returns a value of 1 for an
     * XMLList object that contains only one value.
     * @return Always returns 1 for any XML object.
     */
    public native function length():int

    /**
     * Gives the local name portion of the qualified name of the XML object.
     * @return The local name as either a String or null.
     */
    public native function localName():Object

    /**
     * Gives the qualified name for the XML object.
     * @return The qualified name is either a QName or null.
     */
    public native function name():Object

    /**
     * If no parameter is provided, gives the namespace associated with the qualified name of this XML object. If a prefix parameter is
     * specified, the method returns the namespace that matches the prefix parameter and that is in scope for the XML object.
     * If there is no such namespace, the method returns undefined.
     * @param prefix The prefix you want to match, defaults to null.
     * @return The qualified name is either a QName or null.
     */
    public native function namespace(prefix:String = null):Object

    /**
     * Lists namespace declarations associated with the XML object in the context of its parent.
     * @return An array of Namespace objects.
     */
    public native function namespaceDeclarations():/*Namespace*/ Array

    /**
     * Specifies the type of node: text, comment, processing-instruction, attribute, or element.
     * @return The node type used.
     */
    public native function nodeKind():String

    /**
     * For the XML object and all descendant XML objects, merges adjacent text nodes and eliminates empty text nodes.
     * @return The resulting normalized XML object.
     */
    public native function normalize():XML

    /**
     * Returns the parent of the XML object. If the XML object has no parent, the method returns undefined.
     * @return The parent XML object. Returns either a String or undefined.
     */
    public native function parent():*

    /**
     *  Inserts a copy of the provided child object into the XML element before any existing XML properties for that element. Use the
     * delete (XML) operator to remove XML nodes.
     * @param value - The object to insert.
     * @return The resulting XML object.
     */
    public native function prependChild(value:Object):XML

    /**
     * If a name parameter is provided, lists all the children of the XML object that contain processing instructions with that name.
     * With no parameters, the method lists all the children of the XML object that contain any processing instructions.
     * @param The name of the processing instructions to match, defaulting to "*".
     * @return A list of matching child objects.
     */
    public native function processingInstructions(name:String = "*"):XMLList

    /**
     * Removes the given namespace for this object and all descendants. The removeNamespaces() method does not remove a namespace if it is
     * referenced by the object's qualified name or the qualified name of the object's attributes.
     * @param ns - The namespace to remove.
     * @return A copy of the resulting XML object.
     */
    public native function removeNamespace(ns:Namespace):XML

    /**
     * Replaces the properties specified by the propertyName parameter with the given value parameter. If no properties match propertyName,
     * the XML object is left unmodified.
     * @param propertyName - Can be a numeric value, an unqualified name for a set of XML elements, a qualified name for a set of XML elements,
     * or the asterisk wildcard ("*"). Use an unqualified name to identify XML elements in the default namespace.
     * @param value - The replacement value. This can be an XML object, an XMLList object, or any value that can be converted with toString().
     * @return The resulting XML object, with the matching properties replaced.
     */
    public native function replace(propertyName:*, value:*):XML

    /**
     * Replaces the child properties of the XML object with the specified set of XML properties, provided in the value parameter.
     * @param value - The replacement XML properties. Can be a single XML object or an XMLList object.
     * @return The resulting XML object.
     */
    public native function setChildren(value:Object):XML

    /**
     * Changes the local name of the XML object to the given name parameter.
     * @param name - The replacement name for the local name.
     */
    public native function setLocalName(name:String):void

    /**
     * Sets the name of the XML object to the given qualified name or attribute name.
     * @param name - The new name for the object.
     */
    public native function setName(name:String):void

    /**
     * Sets the namespace associated with the XML object.
     * @param ns - The new namespace.
     */
    public native function setNamespace(name:String):void

    /**
     * Sets values for the following XML properties: ignoreComments, ignoreProcessingInstructions, ignoreWhitespace, prettyIndent, and prettyPrinting.
     * @param rest An object with each of the following properties:
     * ignoreComments
     * ignoreProcessingInstructions
     * ignoreWhitespace
     * prettyIndent
     * prettyPrinting
     */
    public native static function setSettings(... rest):void

    /**
     * Retrieves the following properties: ignoreComments, ignoreProcessingInstructions, ignoreWhitespace, prettyIndent, and prettyPrinting.
     * @return An object with the following XML properties:
     * ignoreComments
     * ignoreProcessingInstructions
     * ignoreWhitespace
     * prettyIndent
     * prettyPrinting
     */
    public native static function settings():Object

    /**
     * Returns an XMLList object of all XML properties of the XML object that represent XML text nodes.
     * @return The list of properties.
     */
    public native function text():XMLList

    /**
     *  Returns a string representation of the XML object. The rules for this conversion depend on whether the XML object has simple content
     * or complex content:
     * If the XML object has simple content, toString() returns the String contents of the XML object with the following stripped out:
     the start tag, attributes, namespace declarations, and end tag.
     * If the XML object has complex content, toString() returns an XML encoded String representing the entire XML object, including
     the start tag, attributes, namespace declarations, and end tag.
     * To return the entire XML object every time, use toXMLString().
     * @return The string representation of the XML object.
     */
    public native function toString():String

    /**
     *  Returns a string representation of the XML object. Unlike the toString() method, the toXMLString() method always returns the start
     * tag, attributes, and end tag of the XML object, regardless of whether the XML object has simple content or complex content.
     * (The toString() method strips out these items for XML objects that contain simple content.)
     * @return The string representation of the XML object.
     */

    public native function toXMLString():String

    public native function valueOf():XML;
}

/**
 * The Namespace class contains methods and properties for defining and working with namespaces. There are three scenarios for using namespaces:<ul>
 * <li>Namespaces of XML objects Namespaces associate a namespace prefix with a Uniform Resource Identifier (URI) that identifies the namespace.
 * The prefix is a string used to reference the namespace within an XML object. If the prefix is undefined, when the XML is converted to a
 * string, a prefix is automatically generated.</li>
 * <li> Namespace to differentiate methods Namespaces can differentiate methods with the same name to perform different tasks. If two methods
 * have the same name but separate namespaces, they can perform different tasks.</li>
 * <li>Namespaces for access control Namespaces can be used to control access to a group of properties and methods in a class. If you place
 * the properties and methods into a private namespace, they are inaccessible to any code that does not have access to that namespace.
 * You can grant access to the group of properties and methods by passing the namespace to other classes, methods or functions.</li>
 *</ul>
 * This class shows two forms of the constructor method because each form accepts different parameters.
 */
public final class Namespace {

    /**
     * The prefix of the namespace.
     */
    public native function get prefix():String
    public native function set prefix(value:String):void

    /**
     * The Uniform Resource Identifier (URI) of the namespace.
     */
    public native function get uri():String
    public native function set uri(value:String):void

    /**
     * Creates a Namespace object according to the values of the prefixValue and uriValue parameters. This constructor requires both parameters.
     * The value of the prefixValue parameter is assigned to the prefix property as follows:<ul>
     * <li>If undefined is passed, prefix is set to undefined.</li>
     * <li>If the value is a valid XML name, as determined by the isXMLName() function, it is converted to a string and assigned to the prefix property.</li>
     * <li>If the value is not a valid XML name, the prefix property is set to undefined.</li>
     * </ul>
     * The value of the uriValue parameter is assigned to the uri property as follows:
     * If a QName object is passed, the uri property is set to the value of the QName object's uri property.
     * If the value is a Namespace object (and first parameter is not specified), a copy of the object is created.
     * Otherwise, the uriValue parameter is converted to a string and assigned to the uri property.
     * If no values is passed, the prefix and uri properties are set to an empty string.
     * @param prefixValue - The prefix to use for the namespace.
     * @param uriValue - The Uniform Resource Identifier (URI) of the namespace.
     * @constructor
     */
    public native function Namespace(prefixValue:* = undefined, uriValue:* = undefined);

    public native function valueOf():Namespace;

    /**
     * Equivalent to the Namespace.uri property.
     * @return The Uniform Resource Identifier (URI) of the namespace, as a string.
     */
    public native function toString():String
}

/**
 * QName objects represent qualified names of XML elements and attributes. Each QName object has a local name and a namespace
 * Uniform Resource Identifier (URI). When the value of the namespace URI is null, the QName object matches any namespace. Use the QName
 * constructor to create a new QName object that is either a copy of another QName object or a new QName object with a uri from a Namespace
 * object and a localName from a QName object.
 */
public final dynamic class QName {
    /**
     * The local name of the QName object.
     */
    public native function get localName():String

    /**
     * The Uniform Resource Identifier (URI) of the QName object.
     */
    public native function get uri():String


    /**
     * Creates a QName object with a URI object from a Namespace object and a localName from a QName object. If either parameter is not the
     * expected data type, the parameter is converted to a string and assigned to the corresponding property of the new QName object.
     * For example, if both parameters are strings, a new QName object is returned with a uri property set to the first parameter and a
     * localName property set to the second parameter. In other words, the following permutations, along with many others, are valid forms of
     * the constructor:
     * QName (uri:Namespace, localName:String);
     * QName (uri:String, localName: QName);
     * QName (uri:String, localName: String);
     * If you pass null for the uri parameter, the uri property of the new QName object is set to null.
     * When uri is not passed then creates a QName object that is a copy of another QName object. If the parameter passed to the constructor
     * is a QName object, a copy of the QName object is created. If the parameter is not a QName object, the parameter is converted to a
     * string and assigned to the localName property of the new QName instance. If the parameter is undefined or unspecified, a new QName
     * object is created with the localName property set to the empty string.
     * @param uri A Namespace object from which to copy the uri value. A parameter of any other type is converted to a string.
     * @param localName The QName object to be copied (when uri is not passed). Objects of any other type are converted to a string that is
     * assigned to the localName property of the new QName object.
     * When uri passed - A QName object from which to copy the localName value. A parameter of any other type is converted to a string.
     */
    public native function QName(uri:* = null, localName:* = null);

    public native function toString():String
}

/**
 * A Class object is created for each class definition in a program. Every Class object is an instance of the Class class. The Class object
 * contains the static properties and methods of the class. The class object creates instances of the class when invoked using the new
 * operator.
 */
public dynamic class Class extends Object {}
}

native public function trace(... rest):void;

package flash.display {
import flash.events.EventDispatcher

[Event(name="mouseDown", type="flash.events.Event")]
[Event(name="mouseUp", type="flash.events.Event")]
[Event(name="mouseMove", type="flash.events.Event")]
public class Sprite extends InteractiveObject {
  [Inspectable(category="General")]
  public native function set layout(s:String);

  [Inspectable(category="General")]
  public native function set currentState(s:String):void;
}

[Event(name="click",type="flash.events.MouseEvent")]
public class InteractiveObject extends DisplayObject{}
public class DisplayObject extends EventDispatcher {
  public native function set width(_:uint):void;
  public native function get width():uint;
}
}

package barxxx {
  class HTTPService {
    [Inspectable(category="General", defaultValue="object", enumeration="object,array,xml,flashvars,text,e4x")]
    native public function set resultFormat(_79:Object):void;
  }
}

package flash.filters {
public class BitmapFilter { }
public class BitmapFilterQuality { }
}

package mx.events {
public class FlexEvent extends flash.events.Event{}
public class DividerEvent extends flash.events.Event{}
}

package flash.events {
public class Event {
  native public function Event(type:String,bubbles:Boolean = false,cancelable:Boolean = false):*;
  public var data;
}

public class KeyboardEvent extends Event {
  public static var TYPED:String = "xxx";
}
public class MouseEvent extends Event {
  public static var CLICK:String = "click";
  public static var DOUBLE_CLICK:String = "doubleClick";
  public static var MOUSE_UP:String = "mouseUp";
  public static var MOUSE_DOWN:String = "mouseDown";
  public static var MOUSE_MOVE:String = "mouseMove";
  public static var MOUSE_IN:String = "mouseIn";
  public static var MOUSE_OUT:String = "mouseOut";
  public static var MOUSE_WHEEL:String = "mouseWheel";
}

public interface IEventDispatcher {
  native function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false) :void
}

public class EventDispatcher implements IEventDispatcher{
  public native function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false) :void
  public native function removeEventListener(type:String, listener:Function, useCapture:Boolean = false) :void
  public native function dispatchEvent(event:flash.events.Event):Boolean
}
}

package mypackage {
import mx.core.UIComponent;
import mx.core.IFactory;
import mx.controls.listClasses.BaseListData;

public function getDefinitionByName(s:String) {}
public function getQualifiedClassName(s:String) {}
public function getClassByQualifiedClassName(s:String):Class {}

  public class ListCollectionView {}
  public class ArrayCollection extends ListCollectionView {
    public function set source(s:Array):void{}
  }

interface IEffect {}

[DefaultProperty("effect")]
class Transition {
    public var effect:IEffect;

    [Inspectable(category="General")]
    public var fromState:String = "*";

    [Inspectable(category="General")]
    public var toState:String = "*";
}

class TweenEffect implements IEffect {
    public var easingFunction:Function = null;
}

class Resize extends TweenEffect {}

interface IFill {}
class SolidColor implements IFill {
  public var color:uint;
}
class Rect {
  public var fill:IFill;
}

interface IOverride {}
interface IDeferredInstance {}

[DefaultProperty("targetFactory")]
class AddChild implements IOverride {
    [Inspectable(category="General")]

    public function get targetFactory():IDeferredInstance
    { return null;}

    /**
     *  @private
     */
    public function set targetFactory(value:IDeferredInstance):void {}
}

public interface IList {}
public interface ICollectionView {}

class SetProperty implements IOverride {
    [Inspectable(category="General")]

    public var name:String;

    //----------------------------------
    //  target
    //----------------------------------

    [Inspectable(category="General")]

    public var target:Object;

    //----------------------------------
    //  value
    //----------------------------------

    [Inspectable(category="General")]

    public var value:*;

}

[DefaultProperty("overrides")]
class State {

    public var basedOn:String;

    [Inspectable(category="General")]

    public var name:String;

    public var overrides:Array /* of IOverride */ = [];

}

[Event(name="initialize", type="flash.events.Event")]
[Event(name="xxx", type="flash.events.KeyboardEvent")]
[Style(name="disabledSkin")]
[Style(name="disabledColor")]
[Style(name="upSkin")]
  public class Application extends mx.core.Container {
      [Inspectable(arrayType="mypackage.Transition")]
        [ArrayElementType("mypackage.Transition")]

        public var transitions:Array /* of Transition */ = [];

      [Inspectable(arrayType="mypackage.State")]
        [ArrayElementType("mypackage.State")]

        public var states:Array /* of State */ = [];
  }

  [Style(name="verticalAlign", type="String", enumeration="bottom,middle,top", inherit="no")]
  public class HBox extends mx.core.Container {
    [Inspectable(category="General", defaultValue="false", enumeration="true,false")]
    public native function set selected(_:Object):void;
  }

  [Style(name="kerning", type="Boolean", inherit="yes")]
  class VBox extends mx.core.Container {
    public native function set selected(_:Boolean):void;
  }

  public interface ResourceBundle {
    function getString(resourceName:String):String;
  }

  public class RegExpValidator {
    public var expression:String;
  }

  public interface IResourceManager {
      function findResourceBundleWithResource(
                        bundleName:String,
                        resourceName:String):*;
      function getString(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):String;
      function getObject(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):*;
      function getClass(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):Class;
      function getStringArray(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):Array /* of String */;

      function getNumber(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):Number;

      function getInt(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):int;
      function getUint(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):uint;

      function getBoolean(bundleName:String, resourceName:String,
                       parameters:Array = null,
                       locale:String = null):Boolean;

      function getResourceBundle(locale:String,
                               bundleName:String):*;
  }

  public class ResourceManager {
    public static native function getInstance():IResourceManager;
  }

/**
 * tabStyleName doc
 */
  [Style(name="tabStyleName", type="String", inherit="no")]
  class TabBar extends mx.core.Container {

  }

  class HGroup extends UIComponent {
    public native override function set width(_:uint):void;
  }

  class CheckBox extends UIComponent {
    public var label:String;
  }

  class ListBase extends UIComponent {
    [Inspectable(enumeration="off,on,auto", defaultValue="auto")]
    public native function get verticalScrollPolicy():String;
    public native function set verticalScrollPolicy(value:String):void;

    [Inspectable(category="Data")]
    public function set itemRenderer(value:IFactory):void{}
  }

  public class List extends ListBase {
    public override native function set verticalScrollPolicy(value:String);
  }

  class Canvas extends UIComponent {
    [Inspectable(category="General")]
    public native function set hints(_:String):void;
  }

  [Exclude(name="resizeEffect", kind="effect")]
  class Text extends UIComponent {
    [Inspectable(category="General", defaultValue="")]
    public native function set text(_:String):void;

    public native function set htmlText(_:String):void;

    // Fake properties
    [Inspectable(category="General")]
    public native function set target(_:DisplayObject):void;

    [Inspectable(category="General")]
    public native function set relativeTo(_:UIComponent);

    [Inspectable(category="General")]
    public native function set relativeTo2(_:Object);
  }

  public class Tree extends UIComponent{}
  public class TextInput extends UIComponent {
    public native function set count(_:Number);
    public native function set text(_:String);
    public native function get text():String;
  }
  class TextArea extends UIComponent{
      public native function set text(_:String);
    public native function get text():String;
  }

  [Style(name="headerColors",type="Array",arrayType="uint",format="Color",inherit="yes")]
  [Style(name="borderAlpha",type="Number",inherit="no")]
  public class Panel extends mx.core.Container {
    public native function set title(_:String):void;
    public native function set statusStyleName(_:String):void;
  }

  [DefaultProperty("dataProvider")]
  class DataGrid extends ListBase {
    [Bindable("columnsChanged")]
    [Inspectable(arrayType="mypackage.dataGridClasses.DataGridColumn")]
    public native function get columns():Array;
    public native function set columns(_:Array);

    [Bindable("collectionChange")]
    [Inspectable(category="Data", defaultValue="undefined")]
    public native function set dataProvider(o:Object);
  }

  /**
   * Item click comment
   */

  [Event(name = "itemClick")]
  [Event(name = "change")]
  [Style(name="disabledColor",type="uint",format="Color",inherit="yes")]
  [Style(name="disabledSkin",type="Class",inherit="no")]
  [Style(name="upSkin",type="Class",inherit="no")]

  /**
   * Button's border color
   */
  [Style(name="borderColor",type="uint",format="Color",inherit="no")]
  [Style(name="borderAlpha")]
  public class Button extends UIComponent {
    public function Button() {}

    [Inspectable(category="General", defaultValue="")]
    public native function set label(_:String);

    [Bindable("click")]
    [Bindable("valueCommit")]
    [Inspectable(category="General", defaultValue="false")]
    public native function set selected(_:Boolean):void;

    [Bindable("dataChange")]
    [Inspectable(environment="none")]
    public native function set listData(value:BaseListData):void;
  }

  public class Label extends UIComponent {
    public function Label():* {}
    public native function set text(_:String);
  }

  public class Accordition extends UIComponent {
    public function set headerRenderer(value:IFactory):void{}
  }

  public class Image extends UIComponent {
    [Inspectable(category="General", defaultValue="", format="File")]
    public native function set source(value:Object);
  }

  class EmailValidator extends UIComponent {
    public native function set source(_:String);
  }

  class WebService {}

  public class StrangeUIComponentOutOfComponentList extends UIComponent {}

  class HTTPService extends barxxx.HTTPService {
      public var url:String;
      public var request:Object;
  }

  class Operation {
    native public function set request(_79:Object):void;
    native public function set name(_79:String):void;
  }

  class RemoteObject {
    native public function set destination(_79:Object):void;
    native public function set fault(_79:Object):void;
    native public function set result(_79:Object):void;
    native public function set source(s:String):void;
  }

  [Style(name="fontWeight",type="String",enumeration="normal,bold",inherit="yes")]

  class RemoteObjectOperation {
    native public function set name(_79:String):void;
    native public function send():void;
    native public function set arguments(a:Array):void;
    native public function get lastResult():Object;
  }

  public class MyEvent extends flash.events.Event {
    public static var TYPED:String = "yyy";
  }

  public class Alert {
    public static function show(s:String):void {}
  }

  public class CSSStyleDeclaration {
      public var defaultFactory:Function;
  }

  class FxContainer {
      public var skinClass:Array;
  }
  class Skin {}
  public class ClassFactory {
    public function ClassFactory(generator:Class) {}
  }
}

package mypackage.dataGridClasses {
import mx.core.IFactory;

  public class DataGridColumn {
    var ccc;

    // heh
    [Inspectable(category="General", defaultValue="")]
    /**
     * AAA
     */
    public var dataField:String;
    public var itemEditor:IFactory;
    public var width:int;
    public var headerText:String;
    public var itemRenderer:IFactory;
    public var headerStyleName:String;
    public var labelFunction:Function;
  }
}

package mx.rpc {
    public class AbstractService {}

    public class AbstractInvoker {}

    public class AbstractOperation {}

}

package mx.rpc.events {
    public class AbstractEvent {}
    public class FaultEvent {}
    public class HeaderEvent {}

}

package mx.rpc.soap {
    public class AbstractWebService {}
}

package mx.utils {
    public class Base64Encoder {}
    public class Base64Decoder {}
}

package mx.messaging.messages {
    public class AbstractMessage {}
}

package mx.messaging {
    public class AbstractProducer {}
    public class AbstractConsumer{}
    public class AbstractMessageStore {}
}

package mx.effects.easing {
    public class Back{}
}

package mx.charts.series {
    public class BarSeries {}
}

package mx.charts {
    public class BarChart {}
}

package mx.binding {
    public class Binding {}
}

package flash.utils {
public class Dictionary {}
}

package mx.core {
    public namespace mx_internal = "http://www.adobe.com/2006/flex/mx/internal";

  public interface IUIComponent {}
  public interface IContainer extends IUIComponent{}

  include "AnchorStyles2.as";
  /**
   * special event
   */
  [Event(name = "creationComplete")]
  include "AnchorStyles.as";
  /** Color of text in the component, including the component label.
   *  @default 0x0B333C
   */
  [Effect(name="resizeEffect", event="resize")]
  [Event(name="xxx")]
  [Event(name="yyy")]
  [Event(name = "onclick")]
  [Style(name="titleBackgroundSkin", type="Class", inherit="no")]
  /**
   * UI component's border color
   */
  [Style(name="borderColor", type="Number")]
  [Event(name="add", type="mx.events.FlexEvent")]
  public class UIComponent extends flash.display.Sprite implements IUIComponent{
    public function UIComponent() {}

    public var resourceManager:mypackage.IResourceManager

    public function get id():String{}
    public function set id(value:String):void{}

    [Inspectable(category="General")]
    [PercentProxy]
    public native function get height():uint;
    public native function set height(_:uint):void;

    private native function set fakePrivateProperty(_:uint):void;
    private native function get fakePrivateProperty():uint;

    static native function set fakeStaticProperty(_:uint):void;
    static native function get fakeStaticProperty():uint;

    final native function set fakeFinalProperty(_:uint):void;
    final native function get fakeFinalProperty():uint;

    [Inspectable(category="General")]
    [PercentProxy]
    public native function set width(_:uint):void;
    public native function get width():uint;

    [Inspectable(category="General")]
    public native function set name(_:String):void;
    public native function get name():String;

    public native function set backgroundColor(_:String):void
    public native function set enabled(_:Boolean):void

    public native function setStyle(styleProp:String, newValue:*):void;

    public native function set styleName(value:Object):void;

    import mx.core.mx_internal;

    mx_internal function updateCallbacks():void {}
  }


  /**
   * Container's border color
   */
  [Style(name="borderColor",type="uint",format="Color",inherit="no")]
  class Container extends UIComponent implements IContainer {
    private var _data:Object;
    public function get data():Object {
      return _data;
    }

    public function set data(value:Object):void {
      _data = value;
    }
  }

public interface IUITextField {}

public interface IFactory {
    function newInstance():*;
}

public class ClassFactory implements IFactory {
    public function ClassFactory(generator:Class = null){}
    public function newInstance():* {}
}

public class DeferredInstanceFromClass {
    public function DeferredInstanceFromClass(generator:Class) {}
}
}

package mx.controls.listClasses {
import mx.core.IUIComponent;
public class BaseListData {
}
}
package mx.controls.treeClasses {
    import mx.core.mx_internal;
    import mx.controls.listClasses.BaseListData;

    public class TreeItemRenderer {
        mx_internal function getLabel():int
        {
        }
    }
    public class TreeListData extends BaseListData {}
}

package flash.ui {
    public class ContextMenu {
        public function clone() {}
    }
}

package mx.controls {
import flash.events.EventDispatcher;
import mx.core.UIComponent;
[Style(name="borderColor",type="uint",format="Color",inherit="no")]
    public class Button extends UIComponent{}
    public class RadioButtonGroup extends EventDispatcher{}
}

[DefaultProperty("source")]
public class mx.collections.XMLListCollection{
  public function set source(s:XMLList):void{}
}

public class flash.utils.Proxy extends Object
{

  native public function Proxy():*;
  native flash_proxy function deleteProperty(name:*):Boolean;
  native flash_proxy function isAttribute(name:*):Boolean;
  native flash_proxy function callProperty(name:*,... rest):*;
  native flash_proxy function nextNameIndex(index:int):int;

  native flash_proxy function nextName(index:int):String;
  native flash_proxy function getDescendants(name:*):*;
  native flash_proxy function getProperty(name:*):*;
  native flash_proxy function nextValue(index:int):*;
  native flash_proxy function setProperty(name:*,value:*):void;

  native flash_proxy function hasProperty(name:*):Boolean;
}

native public const flash.utils.flash_proxy:* = "http://www.adobe.com/2006/actionscript/flash/proxy";

package flash.net {
public function navigateToURL() {}
}

package flash.data {
public class SQLStatement {
    public native function set text(value:String);
}
}

public final dynamic class XML {
    native AS3 function copy():XML;
}

public class flash.xml.XMLNode {
    native public function hasChildNodes():Boolean;
}

native public const NaN:Number;
native public const Infinity:Number;

public interface org.flexunit.runner.IRunner{}

package flash.external {
public class ExternalInterface {
    static native public function call(functionName:String,... rest):*;
}
}