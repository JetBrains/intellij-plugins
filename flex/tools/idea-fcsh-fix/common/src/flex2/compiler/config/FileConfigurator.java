////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2005-2007 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

// Original file can be found at:
// svn://opensource.adobe.com/svn/opensource/flex/sdk/tags/3.2.0.3958/modules/compiler/src/java/flex2/compiler/config/FileConfigurator.java (revision 2001)

package flex2.compiler.config;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.Locator;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import flex2.compiler.util.CompilerMessage;
import flex2.compiler.util.ThreadLocalToolkit;
import flash.util.FileUtils;
import flash.localization.LocalizationManager;

/**
 * A utility class, which is used to parse an XML file of
 * configuration options and populate a ConfigurationBuffer.  A
 * counterpart of CommandLineConfigurator and
 * SystemPropertyConfigurator.
 *
 * @author Roger Gonzalez
 */
public class FileConfigurator
{
    public static class SAXConfigurationException extends SAXParseException
    {
        private static final long serialVersionUID = -3388781933743434302L;
        SAXConfigurationException( ConfigurationException e, Locator locator )
        {
            super( null, locator ); // ?
            this.innerException = e;
        }
        public ConfigurationException innerException;
    }

    /**
     * @deprecated
     */
    public static void load( final ConfigurationBuffer buffer, final Reader r, final String path, String rootElement ) throws ConfigurationException
    {
        load( buffer, r, path, null, rootElement );
    }

    /**
     * @deprecated
     */
    public static void load( final ConfigurationBuffer buffer, final Reader r, final String path, final String context, String rootElement ) throws ConfigurationException
    {
        ThreadLocalToolkit.log( new LoadingConfiguration(path) );
        Handler h = new Handler( buffer, path, context, rootElement, false );

        SAXParserFactory factory = SAXParserFactory.newInstance();

        try
        {
            SAXParser parser = factory.newSAXParser();
            InputSource source = new InputSource( r );
            parser.parse( source, h );
        }
        catch (SAXConfigurationException e)
        {
            throw e.innerException;
        }
        catch (SAXParseException e)
        {
            throw new ConfigurationException.OtherThrowable( e, null, path, e.getLineNumber() );
        }
        catch (Exception e)
        {
            throw new ConfigurationException.OtherThrowable( e, null, path, -1 );
        }
    }

    public static void load( final ConfigurationBuffer buffer, final InputStream r, final String path, String rootElement ) throws ConfigurationException
    {
        load( buffer, r, path, null, rootElement, false );
    }

    public static void load( final ConfigurationBuffer buffer, final InputStream r, final String path, 
    						 final String context, String rootElement, boolean ignoreUnknownItems ) throws ConfigurationException
    {
        ThreadLocalToolkit.log( new LoadingConfiguration(path) );
        Handler h = new Handler( buffer, path, context, rootElement, ignoreUnknownItems );

        SAXParserFactory factory = SAXParserFactory.newInstance();

        try
        {
            SAXParser parser = factory.newSAXParser();
            InputSource source = new InputSource( r );
            parser.parse( source, h );
        }
        catch (SAXConfigurationException e)
        {
            throw e.innerException;
        }
        catch (SAXParseException e)
        {
            throw new ConfigurationException.OtherThrowable( e, null, path, e.getLineNumber() );
        }
        catch (Exception e)
        {
            throw new ConfigurationException.OtherThrowable( e, null, path, -1 );
        }
    }

    public static void load( ConfigurationBuffer buffer, String path, String rootElement ) throws ConfigurationException
    {
        load( buffer, path, null, -1, rootElement );
    }

    public static void load( ConfigurationBuffer buffer, String path, String contextPath, int line, String rootElement ) throws ConfigurationException
    {
        try
        {
            File f = new File( getFilePath( path, contextPath ) );
            InputStream r = new BufferedInputStream( new FileInputStream( f ) );

            load( buffer, r, f.getAbsolutePath(), f.getParent(), rootElement, false );

            try
            {
                r.close();
            }
            catch(IOException e)
            {
                //
            }
        }
        catch (FileNotFoundException e)
        {
            throw new ConfigurationException.ConfigurationIOError( path, null, contextPath, line );
        }
    }

    // file to load should either be an absolute path or else relative to this config file
    private static String getFilePath( String path, String contextPath )
    {
        File f = new File( path );
        // file to load should either be an absolute path or else relative to this config file
        if ( (contextPath != null) && ( !FileUtils.exists(f) || !FileUtils.isAbsolute(f) ) )
        {
            f = new File( contextPath + File.separator + path );
        }

        return f.getAbsolutePath();
    }

    private static class Handler extends DefaultHandler
    {
        public Handler( ConfigurationBuffer buffer, String source, String contextPath, String rootElement, 
        				boolean ignoreUnknownItems )
        {
            this.cfgbuf = buffer;
            this.source = source;
            this.contextPath = contextPath;
            this.rootElement = rootElement;
            this.ignoreUnknownItems = ignoreUnknownItems;
        }
        public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
        {
            String element = qName;
            if (contextStack.size() == 0)
            {
                if (!element.equals( rootElement ))
                {
                    throw new SAXConfigurationException(
                            new ConfigurationException.IncorrectElement( rootElement, qName, this.source, locator.getLineNumber() ),
                            locator );
                }
                ParseContext ctx = new ParseContext();
                contextStack.push( ctx );
                return;
            }

            ParseContext ctx = contextStack.peek();

            if (ctx.ignore)
            {
            	// ignore starting new elements
            	return;
            }
            
            final String trimmedText = text.toString().trim();
            if (trimmedText.length() > 0)
            {
                throw new SAXConfigurationException(
                        new ConfigurationException.UnexpectedCDATA( this.source, locator.getLineNumber() ),
                        locator );
            }

            String fullname = name( element, ctx.base );

            if (ctx.item != null)
            {
                throw new SAXConfigurationException(
                        new ConfigurationException.UnexpectedElement( element, contextPath, locator.getLineNumber() ),
                        locator );
            }
            else if (ctx.var != null)
            {
                // we're setting values for a variable

                if (ctx.varArgCount == 1)
                {
                    // oops, we weren't expecting more than one value!

                    throw new SAXConfigurationException(
                            new ConfigurationException.UnexpectedElement( element, source, locator.getLineNumber() ),
                            locator );
                }
                ctx.item = element;
            }
            else if (cfgbuf.isValidVar( fullname ))
            {
                ctx.var = fullname;
                ctx.varArgCount = cfgbuf.getVarArgCount( ctx.var );
                ctx.append = false;
                
                String a = attributes.getValue( "", "append" );
                if (a != null)
                {
                    try
                    {
                        if (a.equalsIgnoreCase( "true" ) || a.equalsIgnoreCase( "false" ))
                            ctx.append = Boolean.valueOf( a ).booleanValue();
                        else
                            throw new SAXConfigurationException( new ConfigurationException.BadAppendValue( ctx.var, source, locator.getLineNumber() ),
                                                                 locator );

                    }
                    catch (Exception e)
                    {
                        throw new SAXConfigurationException( new ConfigurationException.BadAppendValue( ctx.var, source, locator.getLineNumber() ),
                                                             locator );
                    }
                }
            }
            else if (cfgbuf.isChildConfig( fullname ))
            {
                String src = attributes.getValue( "", "file-path" );
                if (src != null)
                {
                    try
                    {
                        Class childClass = cfgbuf.getChildConfigClass( fullname );
                        ConfigurationBuffer childBuf = new ConfigurationBuffer( childClass );
                        // keep track of the file-path name
                        cfgbuf.setVar( element + "-file-path", getFilePath( src, contextPath ), contextPath, locator.getLineNumber() );
                        FileConfigurator.load( childBuf, src, contextPath, locator.getLineNumber(), element );
                        cfgbuf.mergeChild( element, childBuf );
                    }
                    catch (final ConfigurationException e)
                    {
                        throw new SAXConfigurationException( e, locator );
                    }
                }


                ParseContext newctx = new ParseContext();
                newctx.base = fullname;
                contextStack.push( newctx );
            }
            else
            {
            	if (ignoreUnknownItems)
            	{
            		// push a new context and ignore everything until we get the end 
            		// of this element.
                    ParseContext newctx = new ParseContext();
                    newctx.item = element;
                    newctx.ignore = true;
                    contextStack.push( newctx );
            		return;
            	}
                throw new SAXConfigurationException(
                        new ConfigurationException.UnknownVariable(
                                                    fullname, source, locator.getLineNumber() ),
                        locator );
            }
        }

        public void endElement( String uri, String localName, String qName ) throws SAXException
        {
            String element = qName;

            ParseContext ctx = contextStack.peek();

            if (ctx.ignore)
            {
            	// if found the matching end element, then pop the context and stop ignoring input
            	if (ctx.item.equals(element))
            	{
            		contextStack.pop();
            		text = new StringBuilder();	// ignore any text read
            	}
            	
            	return;
            }
            
            // There are four possible states here;
            // 1. localname==rootElement -> end of file, pop, we're done
            // 2. localname==itemElement -> finished gathering text, push onto arglist
            // 2. var is set -> set the var to the argList, pop
            // 3. var is null -> we're finishing a child config, pop

            final String trimmedText = text.toString().trim();
            
            if (element.equals( rootElement ))
            {
                // Finished with the file!
            }
            else if (ctx.item != null)
            {
                // Finished with the current item.

                ParseValue v = new ParseValue();
                v.name = element;
                v.value = trimmedText;
                v.line = locator.getLineNumber();
                ctx.argList.add( v );
                text = new StringBuilder();
                ctx.item = null;
            }
            else if (ctx.var != null)
            {
                if ((ctx.varArgCount > 1) && (ctx.argList.size() == 0))
                {
                    throw new SAXConfigurationException(
                            new ConfigurationException.IncorrectArgumentCount( ctx.varArgCount, 0,
                                                                               ctx.var, source, locator.getLineNumber() ),
                            locator );
                }
                if (ctx.varArgCount == 1)
                {
                    ParseValue v = new ParseValue();
                    v.name = null;
                    v.value = trimmedText;
                    v.line = locator.getLineNumber();
                    ctx.argList.add( v );
                    text = new StringBuilder();
                }
                else
                {
                    if (trimmedText.length() > 0)
                    {
                        // "unexpected CDATA encountered, " + ctx.var + " requires named arguments.", locator );
                        throw new SAXConfigurationException(
                                new ConfigurationException.UnexpectedCDATA( source, locator.getLineNumber() ),
                                locator );

                    }
                }
                // Finished with the current var, save the current list
                try
                {
                    setVar( ctx.var, ctx.argList, locator.getLineNumber(), ctx.append );
                    ctx.var = null;
                    ctx.argList.clear();
                    ctx.item = null;
                    ctx.append = false;
                }
                catch (ConfigurationException e)
                {
                    throw new SAXConfigurationException( e, locator );
                }
            }
            else
            {
                // done with a child config
                contextStack.pop();
            }
        }

        public void setVar( String var, List<ParseValue> argList, int line, boolean append ) throws ConfigurationException
        {
            int varArgCount = cfgbuf.getVarArgCount( var );

            Map<String, String> items = new HashMap<String, String>();

            boolean byName = (varArgCount > 1);

            if (byName)
            {
                for (Iterator<ParseValue> it = argList.iterator(); it.hasNext();)
                {
                    ParseValue v = it.next();

                    if (items.containsKey( v.name ))
                    {
                        byName = false;     // can't support byName, duplicate item name!
                        break;
                    }
                    else
                    {
                        items.put( v.name, v.value );
                    }
                }
            }
            List<String> args = new LinkedList<String>();

            if (byName)
            {
                int argc = 0;

                while (args.size() < items.size())
                {
                    String name = cfgbuf.getVarArgName( var, argc++ );
                    String val = items.get( name );
                    if (val == null)
                    {
                        throw new ConfigurationException.MissingArgument( name, var, source, line );
                    }
                    args.add( val );
                }
            }
            else
            {
                Iterator<ParseValue> it = argList.iterator();
                int argc = 0;
                while (it.hasNext())
                {
                    ParseValue v = it.next();
                    String name = cfgbuf.getVarArgName( var, argc++ );
                    if ((v.name != null) && !name.equals( v.name ))
                    {
                        throw new ConfigurationException.UnexpectedArgument( name, v.name, var, source, v.line );
                    }
                    args.add( v.value );
                }
            }
            cfgbuf.setVar( var, args, source, line, contextPath, append );
        }

        public void characters( char ch[], int start, int length )
        {
            String chars = new String( ch, start, length );
            text.append( chars );
        }
        public void setDocumentLocator( Locator locator )
        {
            this.locator = locator;
        }

        private Stack<ParseContext> contextStack = new Stack<ParseContext>();
        private final ConfigurationBuffer cfgbuf;
        private final String source;
        private final String contextPath;
        private final String rootElement;
        
        /**
         * 	if true, do not throw an error if a config var is found that is not defined in the config buffer.
         */
        private final boolean ignoreUnknownItems;
        
        private Locator locator;
        StringBuilder text = new StringBuilder();
    }

    private static String name( String var, String base )
    {
        return (base == null)? var : (base + "." + var);
    }

    private static class ParseContext
    {
        ParseContext()
        {
            this.base = null;
            this.var = null;
            this.varArgCount = -2;
            this.argList = new LinkedList<ParseValue>();
            this.append = false;
            this.ignore = false;
        }

        public String localVar;
        public String var;
        public String base;
        public String item;
        public int varArgCount;
        public boolean append;
        public List<ParseValue> argList;
        public boolean ignore;	// ignore this variable, do not put in config buffer
    }
    
    private static class ParseValue
    {
        public String name;
        public String value;
        public int line;
    }

    private static class FormatNode
    {
        public String fullname;
        public String shortname;
        public ConfigurationInfo info;
        public List values;

        public TreeMap<String, FormatNode> children;   // only for configs
    }

    static final String pad = "   ";
    private static String classToArgName( Class c )
    {
        // we only support builtin classnames!

        String className = c.getName();
        if (className.startsWith( "java.lang." ))
            className = className.substring( "java.lang.".length() );

        return className.toLowerCase();
    }

    private static String formatBuffer1( ConfigurationBuffer cfgbuf,
                                         FormatNode node,
                                         String indent,
                                         LocalizationManager lmgr,
                                         String prefix )
    {
        StringBuilder buf = new StringBuilder( 1024 );

        buf.append( indent + "<" + node.shortname + ">\n" );
        if (node.children != null) {
        for (Iterator it = node.children.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry e = (Map.Entry) it.next();
            FormatNode child = (FormatNode) e.getValue();

            if (child.children != null) // its a config
            {
                buf.append( formatBuffer1( cfgbuf, child, indent + pad, lmgr, prefix ) );
            }
            else
            {
                String description = lmgr.getLocalizedTextString( prefix + "." + child.fullname );

                if (description != null)
                    buf.append( indent + pad + "<!-- " + child.fullname + ": " + description + "-->\n" );

                if ((child.values == null) || !child.info.isDisplayed())
                {
                    boolean newline = false;
                    buf.append( indent + pad + "<!-- " + child.fullname + " usage:\n" );
                    buf.append( indent + pad + "<" + child.shortname + ">");

                    int i = 0;
                    while (true)
                    {
                        if (child.info.getArgCount() == 1)
                        {
                            buf.append( child.info.getArgName( i ) );
                            break;
                        }
                        else
                        {
                            buf.append( "\n" + indent + pad + pad + "<" + child.info.getArgName( i ) + ">" + classToArgName( child.info.getArgType( i ) ) + "</" + child.info.getArgName( i ) + ">");
                            newline = true;
                        }
                        if (child.info.getArgCount() == -1)
                        {
                            if (i > 0) 
                            {
                            	// stop iterating thru arguments when an arg name
                            	// matches a previously used arg name.
                            	boolean found = false;	// true if found argName in the arg list
                            	String argName = child.info.getArgName(i + 1);
                            	for (int j = i; j >= 0; j--)
                            	{
                            		if (child.info.getArgName(j).equals( argName ))
                            		{
                            			found = true;
                            			break;
                            		}
                            	}
                            	if (found)
                            	{
                            		break;
                            	}
                            }
                        }
                        else if (i >= child.info.getArgCount())
                        {
                            break;
                        }
                        ++i;
                    }
                    if (newline)
                        buf.append( "\n" + indent + pad);

                    buf.append( "</" + child.shortname + ">\n");
                    buf.append( indent + pad + "-->\n" );
                }
                else
                {
                    // var may be set multiple times...
                    boolean newline = false;
                    for (Iterator valit = child.values.iterator(); valit.hasNext();)
                    {
                        ConfigurationValue cv = (ConfigurationValue) valit.next();

                        buf.append( indent + pad + "<" + child.shortname + ">" );

                        int argCount = child.info.getArgCount();
                        // var may have multiple values...
                        int argc = 0;
                        for (Iterator argit = cv.getArgs().iterator(); argit.hasNext();)
                        {
                            String arg = (String) argit.next();

                            if (argCount == 1)
                            {
                                buf.append( arg );
                                break;
                            }
                            else
                            {
                                String argname = child.info.getArgName( argc++ );
                                newline = true;
                                buf.append( "\n" + indent + pad + pad + "<" + argname + ">" + arg + "</" + argname + ">" );
                            }
                        }
                        if (newline)
                            buf.append( "\n" + indent + pad);
                        buf.append( "</" + child.shortname + ">\n" );
                    }
                }
            }
        }
        }
        buf.append( indent + "</" + node.shortname + ">\n" );

        return buf.toString();
    }
    private static void addNode( ConfigurationBuffer cfgbuf, String var, FormatNode root )
    {
        String name = null;
        StringTokenizer t = new StringTokenizer( var, "." );

        FormatNode current = root;

        while (t.hasMoreTokens())
        {
            String token = t.nextToken();

            if (name == null)
                name = token;
            else
                name += "." + token;

            if (current.children == null)
                current.children = new TreeMap<String, FormatNode>();

            if (cfgbuf.isChildConfig( name ))
            {
                if (!current.children.containsKey( token ))
                {
                    FormatNode node = new FormatNode();
                    node.fullname = name;
                    node.shortname = token;
                    node.children = new TreeMap<String, FormatNode>();
                    current.children.put( token, node );
                    current = node;
                }
                else
                {
                    current = current.children.get( token );
                }
            }
            else if (cfgbuf.isValidVar( name ))
            {
                FormatNode node = new FormatNode();
                node.fullname = name;
                node.shortname = token;
                node.info = cfgbuf.getInfo( name );
                node.values = cfgbuf.getVar( name );
                current.children.put( token, node );
            }
        }
    }

    public static String formatBuffer( ConfigurationBuffer cfgbuf,
                                       String rootElement,
                                       LocalizationManager lmgr,
                                       String prefix )
    {
        FormatNode root = new FormatNode();
        root.shortname = rootElement;
        for (Iterator it = cfgbuf.getVarIterator(); it.hasNext(); )
        {
            String var = (String) it.next();
            // if var is a 'hidden' parameter, don't dump.
            ConfigurationInfo info = cfgbuf.getInfo(var);
            if (info != null && (info.isHidden() || !info.isDisplayed()))
            {
            	continue;
            }
            addNode( cfgbuf, var, root );
        }

        return formatBuffer1( cfgbuf, root, "", lmgr, prefix );
    }

    public static String formatBuffer( ConfigurationBuffer cfgbuf, String rootElement)
    {
        return formatBuffer( cfgbuf, rootElement, null, null );
    }

	public static class LoadingConfiguration extends CompilerMessage.CompilerInfo
	{
        private static final long serialVersionUID = 7288323144791549482L;
        public String file;

		public LoadingConfiguration(String file)
		{
            this.file = file;
		}
	}
}
