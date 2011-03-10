////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2003-2007 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

// Original file can be found at:
// svn://opensource.adobe.com/svn/opensource/flex/sdk/branches/4.0.0/modules/debugger/src/java/flex/tools/debugger/cli/DebugCLI.java (revision 14159)

package flex.tools.debugger.cli;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import flash.localization.LocalizationManager;
import flash.tools.debugger.Bootstrap;
import flash.tools.debugger.DebuggerLocalizer;
import flash.tools.debugger.Frame;
import flash.tools.debugger.InProgressException;
import flash.tools.debugger.Location;
import flash.tools.debugger.NoResponseException;
import flash.tools.debugger.NotConnectedException;
import flash.tools.debugger.NotSupportedException;
import flash.tools.debugger.NotSuspendedException;
import flash.tools.debugger.PlayerDebugException;
import flash.tools.debugger.Session;
import flash.tools.debugger.SessionManager;
import flash.tools.debugger.SourceFile;
import flash.tools.debugger.SourceLocator;
import flash.tools.debugger.SuspendReason;
import flash.tools.debugger.SuspendedException;
import flash.tools.debugger.SwfInfo;
import flash.tools.debugger.Value;
import flash.tools.debugger.Variable;
import flash.tools.debugger.VariableAttribute;
import flash.tools.debugger.VariableType;
import flash.tools.debugger.VersionException;
import flash.tools.debugger.Watch;
import flash.tools.debugger.WatchKind;
import flash.tools.debugger.events.BreakEvent;
import flash.tools.debugger.events.ConsoleErrorFault;
import flash.tools.debugger.events.DebugEvent;
import flash.tools.debugger.events.DivideByZeroFault;
import flash.tools.debugger.events.ExceptionFault;
import flash.tools.debugger.events.FaultEvent;
import flash.tools.debugger.events.FileListModifiedEvent;
import flash.tools.debugger.events.FunctionMetaDataAvailableEvent;
import flash.tools.debugger.events.InvalidTargetFault;
import flash.tools.debugger.events.InvalidURLFault;
import flash.tools.debugger.events.InvalidWithFault;
import flash.tools.debugger.events.ProtoLimitFault;
import flash.tools.debugger.events.RecursionLimitFault;
import flash.tools.debugger.events.ScriptTimeoutFault;
import flash.tools.debugger.events.StackUnderFlowFault;
import flash.tools.debugger.events.SwfLoadedEvent;
import flash.tools.debugger.events.SwfUnloadedEvent;
import flash.tools.debugger.events.TraceEvent;
import flash.tools.debugger.expression.ECMA;
import flash.tools.debugger.expression.NoSuchVariableException;
import flash.tools.debugger.expression.PlayerFaultException;
import flash.tools.debugger.expression.ValueExp;
import flash.util.FieldFormat;
import flash.util.Trace;
import flex.tools.debugger.cli.ExpressionCache.EvaluationResult;

/**
 * This is a front end command line interface to the Flash Debugger
 * Player.
 *<p>
 * This tool utilizes the Debugger Java API (DJAPI) for Flash 
 * Player that exists in flash.tools.debuggger.
 *<p> 
 * This tool is not completely compliant with the API, since
 * some commands expose implementation specific information for
 * debugging purposes.  Instances where this occurs are kept to a
 * minimum and are isolated in a special class called Extensions.  
 * If you wish to build a version that is completely API 
 * compatible.  Replace Extensions with ExtensionsDisabled in 
 * the static method calls at the end of this file. 
 */
public class DebugCLI implements Runnable, SourceLocator
{
	public static final String VERSION			= "82"; //$NON-NLS-1$

	public static final int CMD_UNKNOWN			= 0;
	public static final int CMD_QUIT			= 1;
	public static final int CMD_CONTINUE		= 2;
	public static final int CMD_STEP			= 3;
	public static final int CMD_NEXT			= 4;
	public static final int CMD_FINISH			= 5;
	public static final int CMD_BREAK			= 6;
	public static final int CMD_SET				= 7;
	public static final int CMD_LIST			= 8;
	public static final int CMD_PRINT			= 9;
	public static final int CMD_TUTORIAL		= 10;
	public static final int CMD_INFO			= 11;
	public static final int CMD_HOME			= 12;
	public static final int CMD_RUN				= 13;
	public static final int CMD_FILE			= 14;
	public static final int CMD_DELETE			= 15;
	public static final int CMD_SOURCE			= 16;
	public static final int CMD_COMMENT			= 17;
	public static final int CMD_CLEAR			= 18;
	public static final int CMD_HELP			= 19;
	public static final int CMD_SHOW			= 20;
	public static final int CMD_KILL			= 21;
	public static final int CMD_HANDLE			= 22;
	public static final int CMD_ENABLE			= 23;
	public static final int CMD_DISABLE			= 24;
	public static final int CMD_DISPLAY			= 25;
	public static final int CMD_UNDISPLAY		= 26;
 	public static final int CMD_COMMANDS		= 27;
    public static final int CMD_PWD             = 28;
    public static final int CMD_CF              = 29;
    public static final int CMD_CONDITION		= 30;
	public static final int CMD_AWATCH			= 31;
	public static final int CMD_WATCH			= 32;
	public static final int CMD_RWATCH			= 33;
	public static final int CMD_WHAT			= 34;
	public static final int CMD_DISASSEMBLE		= 35;
	public static final int CMD_HALT			= 36;
	public static final int CMD_MCTREE			= 37;
	public static final int CMD_VIEW_SWF		= 38;
	public static final int CMD_DOWN			= 39;
	public static final int CMD_UP				= 40;
	public static final int CMD_FRAME			= 41;
	public static final int CMD_DIRECTORY		= 42;
	public static final int CMD_CATCH			= 43;

	/* info sub commands */
	public static final int INFO_UNKNOWN_CMD	= 100;
	public static final int INFO_ARGS_CMD		= 101;
	public static final int INFO_BREAK_CMD		= 102;
	public static final int INFO_FILES_CMD		= 103;
	public static final int INFO_HANDLE_CMD		= 104;
	public static final int INFO_FUNCTIONS_CMD	= 105;
	public static final int INFO_LOCALS_CMD		= 106;
	public static final int INFO_SCOPECHAIN_CMD	= 107;
	public static final int INFO_SOURCES_CMD	= 108;
	public static final int INFO_STACK_CMD		= 109;
	public static final int INFO_VARIABLES_CMD	= 110;
	public static final int INFO_DISPLAY_CMD	= 111;
    public static final int INFO_TARGETS_CMD    = 112;
    public static final int INFO_SWFS_CMD		= 113;

	/* show subcommands */
	public static final int SHOW_UNKNOWN_CMD	= 200;
	public static final int SHOW_NET_CMD		= 201;
	public static final int SHOW_FUNC_CMD		= 202;
	public static final int SHOW_URI_CMD		= 203;
	public static final int SHOW_PROPERTIES_CMD	= 204;
	public static final int SHOW_FILES_CMD		= 205;
	public static final int SHOW_BREAK_CMD		= 206;
	public static final int SHOW_VAR_CMD		= 207;
	public static final int SHOW_MEM_CMD		= 208;
	public static final int SHOW_LOC_CMD		= 209;
	public static final int SHOW_DIRS_CMD		= 210;

	/* misc subcommands */
	public static final int ENABLE_ONCE_CMD		= 301;

    // default metadata retry count 8 attempts per waitForMetadata() call * 5 calls
    public static final int METADATA_RETRIES    = 8*5;

	Stack<LineNumberReader> m_readerStack = new Stack<LineNumberReader>();
	PrintStream m_err;
	PrintStream m_out;
	Session		m_session;
	String		m_launchURI;
	boolean		m_fullnameOption; // emacs mode
	String		m_cdPath;
	String		m_mruURI;
	public final static String m_newline = System.getProperty("line.separator"); //$NON-NLS-1$

	private final static LocalizationManager m_localizationManager = new LocalizationManager();

	List<String>	m_sourceDirectories; // List of String
	int				m_sourceDirectoriesChangeCount;
	private File	m_flexHomeDirectory; // <application.home>/frameworks/projects/*/src always goes in m_sourceDirectories
	private boolean	m_initializedFlexHomeDirectory;

	// context information for our current session
	FileInfoCache	m_fileInfo;
	ExpressionCache m_exprCache;
	FaultActions	m_faultTable;
	Vector<BreakAction>	        m_breakpoints;
	Vector<WatchAction>			m_watchpoints;
	Vector<CatchAction>			m_catchpoints;
	ArrayList<DisplayAction>	m_displays;
	boolean			m_requestResume;
	boolean			m_requestHalt;
	boolean			m_stepResume;

	/* our current input processing context */
	LineNumberReader	m_in;
	LineNumberReader	m_keyboardStream;
	Vector<String>		m_keyboardInput;
	boolean				m_keyboardReadRequest;
	StringTokenizer		m_currentTokenizer;
	String				m_currentToken;
	String				m_currentLine;
	public String		m_repeatLine;

	/**
	 * The module that the next "list" command should display if no
	 * module is explicitly specified.
	 */
	public static final String LIST_MODULE = "$listmodule"; //$NON-NLS-1$

	/**
	 * The line number at which the next "list" command should begin if no
	 * line number is explicitly specified.
	 */
	public static final String LIST_LINE = "$listline"; //$NON-NLS-1$

	/**
	 * The number of lines displayed by the "list" command.
	 */
	private static final String LIST_SIZE = "$listsize"; //$NON-NLS-1$

	private static final String COLUMN_WIDTH = "$columnwidth"; //$NON-NLS-1$

	private static final String UPDATE_DELAY = "$updatedelay"; //$NON-NLS-1$

	private static final String HALT_TIMEOUT = "$halttimeout"; //$NON-NLS-1$

	/**
	 * Current breakpoint number.
	 */
	private static final String BPNUM = "$bpnum"; //$NON-NLS-1$

	/**
	 * Used to determine how much context information should be displayed.
	 */
	private static final String LAST_FRAME_DEPTH = "$lastframedepth"; //$NON-NLS-1$

	/**
	 * Used to determine how much context information should be displayed.
	 */
	private static final String CURRENT_FRAME_DEPTH = "$currentframedepth"; //$NON-NLS-1$

	/**
	 * The current frame we are viewing -- controlled by the "up", "down", and "frame" commands.
	 */
	public static final String DISPLAY_FRAME_NUMBER = "$displayframenumber"; //$NON-NLS-1$

	private static final String FILE_LIST_WRAP = "$filelistwrap"; //$NON-NLS-1$

	private static final String NO_WAITING = "$nowaiting"; //$NON-NLS-1$

	/**
	 * Show this pointer for info stack.
	 */
	private static final String INFO_STACK_SHOW_THIS = "$infostackshowthis"; //$NON-NLS-1$

	/**
	 * Number of milliseconds to wait for metadata.
	 */
	private static final String METADATA_ATTEMPTS_PERIOD = "$metadataattemptsperiod"; //$NON-NLS-1$

	private static final String METADATA_NOT_AVAILABLE = "$metadatanotavailable"; //$NON-NLS-1$

	/**
	 * How many times we should try to get metadata.
	 */
	private static final String METADATA_ATTEMPTS = "$metadataattempts"; //$NON-NLS-1$

	private static final String PLAYER_FULL_SUPPORT = "$playerfullsupport"; //$NON-NLS-1$

	/**
	 * Whether the "print" command will display attributes of members.
	 */
	public static final String DISPLAY_ATTRIBUTES = "$displayattributes"; //$NON-NLS-1$
	
	/* class's static init */
	static
	{
        // set up for localizing messages
        m_localizationManager.addLocalizer( new DebuggerLocalizer("flex.tools.debugger.cli.fdb.") ); //$NON-NLS-1$
	}

	public static void main(String[] args)
	{
		DebugCLI cli = new DebugCLI();

		/* attach our 'main' input method and out/err*/
		cli.m_err = System.err;
		cli.m_out = System.out;

		// get the default <application.home>/projects/frameworks/*/src entries into the source path
		cli.initSourceDirectoriesList();

		// a big of wrangling for our keyboard input stream since its special
		cli.m_keyboardStream = new LineNumberReader(new InputStreamReader(System.in));
		cli.pushStream(cli.m_keyboardStream);

		/* iterate through the args list */
		cli.processArgs(args);

		/* figure out $HOME and the current directory */
		String userHome = System.getProperty("user.home"); //$NON-NLS-1$
		String userDir = System.getProperty("user.dir"); //$NON-NLS-1$

		/*
		 * If the current directory is not $HOME, and a .fdbinit file exists in the current directory,
		 * then push it onto the stack of files to read.
		 * 
		 * Note, we want ./.fdbinit to be read AFTER $HOME/.fdbinit, but we push them in reverse
		 * order, because they're going onto a stack.  If we push them in reverse order, then they
		 * will be read in the correct order (last one pushed is the first one read).
		 */
		if (userDir != null && !userDir.equals(userHome))
		{
			try
			{
				FileReader sr = new FileReader(new File(userDir, ".fdbinit")); //$NON-NLS-1$
				cli.pushStream( new LineNumberReader(sr) );
			}
			catch(FileNotFoundException fnf) {}
		}

		/*
		 * If a .fdbinit file exists in the $HOME directory, then push it onto the stack of files
		 * to read.
		 * 
		 * Note, we want ./.fdbinit to be read AFTER $HOME/.fdbinit, but we push them in reverse
		 * order, because they're going onto a stack.  If we push them in reverse order, then they
		 * will be read in the correct order (last one pushed is the first one read).
		 */
		if (userHome != null)
		{
			try
			{
				FileReader sr = new FileReader(new File(userHome, ".fdbinit")); //$NON-NLS-1$
				cli.pushStream( new LineNumberReader(sr) );
			}
			catch(FileNotFoundException fnf) {}
		}

		cli.execute();
	}

	public DebugCLI()
	{
		m_fullnameOption = false;
		m_exprCache = new ExpressionCache(this);
		m_faultTable = new FaultActions();
		m_breakpoints = new Vector<BreakAction>();
		m_watchpoints = new Vector<WatchAction>();
		m_catchpoints = new Vector<CatchAction>();
		m_displays = new ArrayList<DisplayAction>();
		m_keyboardInput = new Vector<String>();
		m_mruURI = null;
		m_sourceDirectories = new LinkedList<String>();

		initProperties();
		populateFaultTable();
	}

	public static LocalizationManager getLocalizationManager() { return m_localizationManager; }
	public Session				getSession()	{ return m_session; }
	public FileInfoCache		getFileCache()	{ return m_fileInfo; }

	/**
	 * Convert a module to class name.  This is used
	 * by the ExpressionCache to find variables
	 * that live at royale package scope.   That
	 * is variables such as mx.core.Component.
	 */
	public String module2ClassName(int moduleId)
	{
		String pkg = null;
		try
		{
			SourceFile file = m_fileInfo.getFile(moduleId);
			pkg = file.getPackageName();
		}
		catch(Exception npe)
		{
			// didn't work ignore it.
		}
		return pkg;
	}

	LineNumberReader	popStream()						{ return m_readerStack.pop(); }
	void				pushStream(LineNumberReader r)  { m_readerStack.push(r); }
	boolean				haveStreams()					{ return !m_readerStack.empty(); }

	void processArgs(String[] args)
	{
		for(int i=0; i<args.length; i++)
		{
			String arg = args[i];
//			System.out.println("arg["+i+"]= '"+arg+"'");
			if (arg.charAt(0) == '-')
			{
				// its an option
				if (arg.equals("-unit")) // unit-testing mode //$NON-NLS-1$
				{
					System.setProperty("fdbunit", ""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				else if (arg.equals("-fullname") || arg.equals("-f")) //$NON-NLS-1$ //$NON-NLS-2$
				{
					m_fullnameOption = true; // emacs mode
				}
				else if (arg.equals("-cd")) //$NON-NLS-1$
				{
					// consume the path
					if (i+1 < args.length)
						m_cdPath = args[i++];
				}
				else
				{
					err("Unknown command-line argument: " + arg);
				}
			}
			else
			{
				// its a URI to run
				StringReader sr = new StringReader("run "+arg+m_newline); //$NON-NLS-1$
				pushStream( new LineNumberReader(sr) );
			}
		}
	}

	/**
	 * Dispose of the current line and read the next from the current stream, if its an empty
	 * line and we are console then repeat last line.
	 */
	String readLine() throws IOException
	{
		String line = null;
		if (haveStreams())
			line = m_in.readLine();
		else
			line = keyboardReadLine();

		setCurrentLine(line);
		return line;
	}

	/**
	 * The reader portion of our keyboard input routine
	 * Block until input arrives.
	 */
	synchronized String keyboardReadLine()
	{
		// enable a request then block on the queue
		m_keyboardReadRequest = true;
		try { wait(); } catch(InterruptedException ie) {}

		// pull from the front of the queue
		return m_keyboardInput.remove(0);
	}

	/**
	 * A seperate thread collects our input so that we can
	 * block in the doContinue on the main thread and then
	 * allow the user to interrupt us via keyboard input
	 * on this thread.
	 *
	 * We built the stupid thing in this manner, since readLine()
	 * will block no matter what and if we 'quit' we can't
	 * seem to kill this thread.  .close() doesn't work
	 * and Thread.stop(), etc. all fail to do the job.
	 *
	 * Thus we needed to take a request response approach
	 * so that we only block when requested to do so.
	 */
	public void run()
	{
		// while we have this stream
		while(m_keyboardStream != null)
		{
			try
			{
				// only if someone is requesting us to read do we do so...
				if (m_keyboardReadRequest)
				{
					// block on keyboard input and put it onto the end of the queue
					String s = m_keyboardStream.readLine();
					m_keyboardInput.add(s);

					// fullfilled request, now notify blocking thread.
					m_keyboardReadRequest = false;
					synchronized(this) { notifyAll(); }
				}
				else
					try { Thread.sleep(50); } catch(InterruptedException ie) {}
			}
			catch(IOException io)
			{
//				io.printStackTrace();
			}
		}
	}

	void setCurrentLine(String s)
	{
		m_currentLine = s;
		if (m_currentLine == null)
			m_currentTokenizer = null;   /* eof */
		else
		{
			m_currentLine = m_currentLine.trim();

			/* if nothing provided on this command then pull our 'repeat' command  */
			if (m_repeatLine != null && !haveStreams() && m_currentLine.length() == 0)
				m_currentLine = m_repeatLine;

			m_currentTokenizer = new StringTokenizer(m_currentLine, " \n\r\t"); //$NON-NLS-1$
		}
	}

	/* Helpers for extracting tokens from the current line */
	public boolean		hasMoreTokens()									{ return m_currentTokenizer.hasMoreTokens(); }
	public String		nextToken()										{ m_currentToken = m_currentTokenizer.nextToken(); return m_currentToken; }
	public int			nextIntToken() throws NumberFormatException		{ nextToken(); return Integer.parseInt(m_currentToken);	}
	public long			nextLongToken() throws NumberFormatException	{ nextToken(); return Long.parseLong(m_currentToken);	}
	public String		restOfLine()									{ return m_currentTokenizer.nextToken("").trim(); } //$NON-NLS-1$

	public void execute()
	{
		/* dump console message */
		displayStartMessage();
		
		/* now fire our keyboard input thread */
		Thread t = new Thread(this, "Keyboard input"); //$NON-NLS-1$
		t.start();

		/* keep processing streams until we have no more to do */
		while(haveStreams())
		{
			try
			{
				m_in = popStream();
				process();
			}
			catch(EOFException eof)
			{
				; /* quite allright */
			}
			catch(IOException io)
			{
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("exceptionMessage", io); //$NON-NLS-1$
				err(getLocalizationManager().getLocalizedTextString("errorWhileProcessingFile", args)); //$NON-NLS-1$
			}
		}

		/* we done kill everything */
		exitSession();

		// clear this thing, which also halts our other thread.
		m_keyboardStream = null;
	}

	public PrintStream getOut() { return m_out; }

	private void displayStartMessage()
	{
        String build = getLocalizationManager().getLocalizedTextString("defaultBuildName"); //$NON-NLS-1$

        try
        {
            Properties p = new Properties();
            p.load(this.getClass().getResourceAsStream("version.properties")); //$NON-NLS-1$
            String buildString = p.getProperty("build"); //$NON-NLS-1$
            if ((buildString != null) && (! buildString.equals(""))) //$NON-NLS-1$
            {
                build = buildString;
            }
        }
        catch (Throwable t)
        {
            // ignore
        }

        Map<String, Object> aboutMap = new HashMap<String, Object>(); aboutMap.put("build", build); //$NON-NLS-1$
        out(getLocalizationManager().getLocalizedTextString("about", aboutMap)); //$NON-NLS-1$
		out(getLocalizationManager().getLocalizedTextString("copyright")); //$NON-NLS-1$
	}

	void displayPrompt()
	{
		m_out.print("(fdb) "); //$NON-NLS-1$
	}

	void displayCommandPrompt()
	{
		m_out.print(">"); //$NON-NLS-1$
	}

	// add the given character n times to sb
	void repeat(StringBuilder sb, char c, int n)
	{
		while(n-- > 0)
			sb.append(c);
	}

	// Prompt the user to respond to a yes or no type question
	boolean yesNoQuery(String prompt) throws IOException
	{
		boolean result = false;
		m_out.print(prompt);
		m_out.print(getLocalizationManager().getLocalizedTextString("yesOrNoAppendedToAllQuestions")); //$NON-NLS-1$

		String in = readLine();
		if (in != null && in.equals(getLocalizationManager().getLocalizedTextString("singleCharacterUserTypesForYes"))) //$NON-NLS-1$
			result = true;
		else if (in != null && in.equals("escape")) //$NON-NLS-1$
			throw new IllegalArgumentException("escape"); //$NON-NLS-1$
		else
			out(getLocalizationManager().getLocalizedTextString("yesNoQueryNotConfirmed")); //$NON-NLS-1$
		return result;
	}

	public void err(String s)
	{
		// Doesn't make sense to send messages to stderr, because this is
		// an interactive application; and besides that, sending a combination
		// of interwoven but related messages to both stdout and stderr causes
		// the output to be in the wrong order sometimes.
		out(s);
	}

	public void out(String s)
	{
		if (s.length() > 0 && (s.charAt(s.length()-1) == '\n') )
			m_out.print(s);
		else
			m_out.println(s);
	}

	static String uft()
	{
		Runtime rt = Runtime.getRuntime();
		long free = rt.freeMemory(), total = rt.totalMemory(), used =  total - free;
//		long max = rt.maxMemory();
		java.text.NumberFormat nf = java.text.NumberFormat.getInstance() ;
//        System.out.println("used: "+nf.format(used)+" free: "+nf.format(free)+" total: "+nf.format(total)+" max: "+nf.format(max));
        return "Used "+nf.format(used)+" - free "+nf.format(free)+" - total "+nf.format(total); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

	/**
	 * Add all properties that we know about
	 */
	void initProperties()
	{
		propertyPut(LIST_SIZE, 10);
		propertyPut(LIST_LINE, 1);
		propertyPut(LIST_MODULE, 1);  // default to module #1
		propertyPut(COLUMN_WIDTH, 70);
		propertyPut(UPDATE_DELAY, 25);
		propertyPut(HALT_TIMEOUT, 7000);
		propertyPut(BPNUM, 0);				// set current breakpoint number as something bad
		propertyPut(LAST_FRAME_DEPTH, 0);		// used to determine how much context information should be displayed
		propertyPut(CURRENT_FRAME_DEPTH, 0);   // used to determine how much context information should be displayed
		propertyPut(DISPLAY_FRAME_NUMBER, 0);  // houses the current frame we are viewing
		propertyPut(FILE_LIST_WRAP, 999999);   // default 1 file name per line
		propertyPut(NO_WAITING, 0);
		propertyPut(INFO_STACK_SHOW_THIS, 1); // show this pointer for info stack
	}

	// getter/setter for properties; in the expression cache, so that they can be used in expressions!
	public void propertyPut(String k, int v)  { m_exprCache.put(k,v); }
	public int  propertyGet(String k)		  { return ((Integer)m_exprCache.get(k)).intValue(); }
	public Set<String>  propertyKeys()		  { return m_exprCache.keySet(); }

	/**
	 * Process this reader until its done
	 */
	void process() throws IOException
	{
		boolean done = false;
		while(!done)
		{
			try
			{
				/**
				 * Now if we are in a session and that session is suspended then we go
				 * into a state where we wait for some user interaction to get us out
				 */
				runningLoop();

				/* if we are in the stdin then put out a prompt */
				if (!haveStreams())
					displayPrompt();

				/* now read in the next line */
				readLine();
				if (m_currentLine == null)
					break;

				done = processLine();
			}
			catch(NoResponseException nre)
			{
				err(getLocalizationManager().getLocalizedTextString("noResponseException")); //$NON-NLS-1$
			}
			catch(NotSuspendedException nse)
			{
				err(getLocalizationManager().getLocalizedTextString("notSuspendedException")); //$NON-NLS-1$
			}
			catch(AmbiguousException ae)
			{
				// we already put up a warning for the user
			}
			catch(IllegalStateException ise)
			{
				err(getLocalizationManager().getLocalizedTextString("illegalStateException")); //$NON-NLS-1$
			}
			catch(IllegalMonitorStateException ime)
			{
				err(getLocalizationManager().getLocalizedTextString("illegalMonitorStateException")); //$NON-NLS-1$
			}
			catch(NoSuchElementException nse)
			{
				err(getLocalizationManager().getLocalizedTextString("noSuchElementException")); //$NON-NLS-1$
			}
			catch(NumberFormatException nfe)
			{
				err(getLocalizationManager().getLocalizedTextString("numberFormatException")); //$NON-NLS-1$
			}
			catch(SocketException se)
			{
				Map<String, Object> socketArgs = new HashMap<String, Object>();
				socketArgs.put("message", se.getMessage()); //$NON-NLS-1$
				err(getLocalizationManager().getLocalizedTextString("socketException", socketArgs)); //$NON-NLS-1$
			}
			catch(VersionException ve)
			{
				err(getLocalizationManager().getLocalizedTextString("versionException")); //$NON-NLS-1$
			}
			catch(NotConnectedException nce)
			{
				// handled by isConnectionLost()
			}
			catch(Exception e)
			{
				err(getLocalizationManager().getLocalizedTextString("unexpectedError")); //$NON-NLS-1$
				err(getLocalizationManager().getLocalizedTextString("stackTraceFollows")); //$NON-NLS-1$
				e.printStackTrace();
			}

			// check for a lost connection and if it is clean-up!
			if (isConnectionLost())
			{
				try
				{
					dumpHaltState(false);
				}
				catch(PlayerDebugException pde)
				{
					err(getLocalizationManager().getLocalizedTextString("sessionEndedAbruptly")); //$NON-NLS-1$
				}
			}
		}
	}

	// check if we have lost the connect without our help...
	boolean isConnectionLost()
	{
		boolean lost = false;

		if (m_session != null && !m_session.isConnected())
			lost = true;

		return lost;
	}

	boolean haveConnection()
	{
		boolean have = false;

		if (m_session != null && m_session.isConnected())
			have = true;

		return have;
	}

	void doShow() throws AmbiguousException, PlayerDebugException
	{
		/* show without any args brings up help */
		if (!hasMoreTokens())
			out( getHelpTopic("show") ); //$NON-NLS-1$
		else
		{
			/* otherwise we have a boatload of options */
			String subCmdString = nextToken();
			int subCmd = showCommandFor(subCmdString);
			switch(subCmd)
			{
				case SHOW_NET_CMD:
					doShowStats();
					break;

				case SHOW_FUNC_CMD:
					doShowFuncs();
					break;

				case SHOW_URI_CMD:
					doShowUri();
					break;

				case SHOW_PROPERTIES_CMD:
					doShowProperties();
					break;

				case SHOW_FILES_CMD:
					doShowFiles();
					break;

				case SHOW_BREAK_CMD:
					doShowBreak();
					break;

				case SHOW_VAR_CMD:
					doShowVariable();
					break;

				case SHOW_MEM_CMD:
					doShowMemory();
					break;

				case SHOW_LOC_CMD:
					doShowLocations();
					break;

				case SHOW_DIRS_CMD:
					doShowDirectories();
					break;

				default:
					doUnknown("show", subCmdString); //$NON-NLS-1$
					break;
			}
		}
	}

	void doShowUri()
	{
		// dump the URI that the player has sent us
		try
		{
			StringBuilder sb = new StringBuilder();
			sb.append("URI = "); //$NON-NLS-1$
			sb.append( m_session.getURI() );
			out( sb.toString() );
		}
		catch(Exception e)
		{
			err(getLocalizationManager().getLocalizedTextString("noUriReceived")); //$NON-NLS-1$
		}
	}

	/**
	 * Dump the content of files in a raw format
	 */
	void doShowFiles()
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			Iterator itr = m_fileInfo.getAllFiles();

			while(itr.hasNext())
			{
				SourceFile m = (SourceFile) ((Map.Entry)itr.next()).getValue();

				String name = m.getName();
				int id = m.getId();
				String path = m.getFullPath();

				sb.append(id);
				sb.append(' ');
				sb.append(path);
				sb.append(", "); //$NON-NLS-1$
				sb.append(name);
				sb.append(m_newline);
			}
			out( sb.toString() );
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("noSourceFilesFound")); //$NON-NLS-1$
		}
	}

	void doShowMemory()
	{
		out(uft());
	}

	void doShowLocations()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Num Type           Disp Enb Address    What"+m_newline);

		// our list of breakpoints
		int count = breakpointCount();
		for(int i=0; i<count; i++)
		{
			BreakAction b = breakpointAt(i);
			int num = b.getId();

			FieldFormat.formatLong(sb, num, 3);
			sb.append(" breakpoint     ");

			if (b.isAutoDisable())
				sb.append("dis  ");
			else if (b.isAutoDelete())
				sb.append("del  ");
			else
				sb.append("keep ");

			if (b.isEnabled())
				sb.append("y   ");
			else
				sb.append("n   ");

			Iterator<Location> itr = b.getLocations().iterator();
			while(itr.hasNext())
			{
				Location l = itr.next();
				SourceFile file = l.getFile();
				String funcName = (file == null)
					? getLocalizationManager().getLocalizedTextString("unknownBreakpointLocation") //$NON-NLS-1$
					: file.getFunctionNameForLine(m_session, l.getLine()) ;
				int offset = adjustOffsetForUnitTests((file == null) ? 0 : file.getOffsetForLine(l.getLine()));

				sb.append("0x"); //$NON-NLS-1$
				FieldFormat.formatLongToHex(sb, offset, 8);
				sb.append(' ');

				if (funcName != null)
				{
					Map<String, Object> funcArgs = new HashMap<String, Object>();
					funcArgs.put("functionName", funcName); //$NON-NLS-1$
					sb.append(getLocalizationManager().getLocalizedTextString("inFunctionAt", funcArgs)); //$NON-NLS-1$
				}

				sb.append(file.getName());
				if (file != null)
				{
					sb.append("#"); //$NON-NLS-1$
					sb.append(file.getId());
				}
				sb.append(':');
				sb.append(l.getLine());

				try
				{
					SwfInfo info = m_fileInfo.swfForFile(file);
					Map<String, Object> swfArgs = new HashMap<String, Object>();
					swfArgs.put("swf", FileInfoCache.shortNameOfSwf(info)); //$NON-NLS-1$
					sb.append(getLocalizationManager().getLocalizedTextString("inSwf", swfArgs)); //$NON-NLS-1$
				}
				catch(NullPointerException npe)
				{
					// can't find the swf
					sb.append(getLocalizationManager().getLocalizedTextString("nonRestorable")); //$NON-NLS-1$
				}
				sb.append(m_newline);
				if (itr.hasNext())
					sb.append("                            "); //$NON-NLS-1$
			}
		}
		out(sb.toString());
	}

	/**
	 * When running unit tests, we want byte offsets into the file to
	 * always be displayed as zero, so that the unit test expected
	 * results will match up with the actual results.  This is just a
	 * simple helper function that deals with that.
	 */
	private int adjustOffsetForUnitTests(int offset)
	{
		if (System.getProperty("fdbunit")==null) //$NON-NLS-1$
			return offset;
		else
			return 0;
	}

	void doShowDirectories()
	{
		out(getLocalizationManager().getLocalizedTextString("sourceDirectoriesSearched")); //$NON-NLS-1$
		Iterator<String> iter = m_sourceDirectories.iterator();
		while (iter.hasNext())
		{
			String dir = iter.next();
			out("  " + dir); //$NON-NLS-1$
		}
	}

	void doHalt() throws SuspendedException, NotConnectedException, NoResponseException
	{
		out(getLocalizationManager().getLocalizedTextString("attemptingToSuspend")); //$NON-NLS-1$
		m_session.suspend();
		if (m_session.isSuspended())
			out(getLocalizationManager().getLocalizedTextString("playerStopped")); //$NON-NLS-1$
		else
			out(getLocalizationManager().getLocalizedTextString("playerRunning")); //$NON-NLS-1$
	}

	public void appendReason(StringBuilder sb, int reason)
	{
		switch(reason)
		{
			case SuspendReason.Unknown:
				sb.append(getLocalizationManager().getLocalizedTextString("suspendReason_Unknown")); //$NON-NLS-1$
				break;

			case SuspendReason.Breakpoint:
				sb.append(getLocalizationManager().getLocalizedTextString("suspendReason_HitBreakpoint")); //$NON-NLS-1$
				break;

			case SuspendReason.Watch:
				sb.append(getLocalizationManager().getLocalizedTextString("suspendReason_HitWatchpoint")); //$NON-NLS-1$
				break;

			case SuspendReason.Fault:
				sb.append(getLocalizationManager().getLocalizedTextString("suspendReason_ProgramThrewException")); //$NON-NLS-1$
				break;

			case SuspendReason.StopRequest:
				sb.append(getLocalizationManager().getLocalizedTextString("suspendReason_StopRequest")); //$NON-NLS-1$
				break;

			case SuspendReason.Step:
				sb.append(getLocalizationManager().getLocalizedTextString("suspendReason_ProgramFinishedStepping")); //$NON-NLS-1$
				break;

			case SuspendReason.HaltOpcode:
				sb.append(getLocalizationManager().getLocalizedTextString("suspendReason_HaltOpcode")); //$NON-NLS-1$
				break;

			case SuspendReason.ScriptLoaded:
				sb.append(getLocalizationManager().getLocalizedTextString("suspendReason_ScriptHasLoadedIntoFlashPlayer")); //$NON-NLS-1$
				break;
		}
	}

	/**
	 * The big ticket item, where all your questions are answered.
	 *
	 */
	void doInfo() throws AmbiguousException, PlayerDebugException
	{
		/* info without any args brings up help */
		if (!hasMoreTokens())
			out( getHelpTopic("info") ); //$NON-NLS-1$
		else
		{
			/* otherwise we have a boatload of options */
			String subCmdString = nextToken();
			int subCmd = infoCommandFor(subCmdString);
			switch(subCmd)
			{
				case INFO_ARGS_CMD:
					doInfoArgs();
					break;

				case INFO_BREAK_CMD:
					doInfoBreak();
					break;

				case INFO_FILES_CMD:
					doInfoFiles();
					break;

				case INFO_FUNCTIONS_CMD:
					doInfoFuncs();
					break;

				case INFO_HANDLE_CMD:
					doInfoHandle();
					break;

				case INFO_LOCALS_CMD:
					doInfoLocals();
					break;

				case INFO_SCOPECHAIN_CMD:
					doInfoScopeChain();
					break;
					
				case INFO_SOURCES_CMD:
					doInfoSources();
					break;

				case INFO_STACK_CMD:
					doInfoStack();
					break;

				case INFO_VARIABLES_CMD:
					doInfoVariables();
					break;

				case INFO_DISPLAY_CMD:
					doInfoDisplay();
					break;

                case INFO_TARGETS_CMD:
                    doInfoTargets();
                    break;

                case INFO_SWFS_CMD:
                    doInfoSwfs();
                    break;

				default:
					doUnknown("info", subCmdString); //$NON-NLS-1$
					break;
			}
		}
	}

	void doInfoStack() throws PlayerDebugException
	{
		waitTilHalted();

		StringBuilder sb = new StringBuilder();
		Frame[] stack = m_session.getFrames();
		if (stack == null || stack.length == 0)
			sb.append(getLocalizationManager().getLocalizedTextString("noStackAvailable")); //$NON-NLS-1$
		else
		{
			boolean showThis = propertyGet(INFO_STACK_SHOW_THIS) == 1;
			for(int i=0; i<stack.length; i++)
			{
				// keep spitting out frames until we can't
				Frame frame = stack[i];
				boolean valid = appendFrameInfo(sb, frame, i, showThis, true);
				sb.append(m_newline);
                if (!valid)
                    break;
			}
		}

		/* dump it out */
		out(sb.toString());
	}
	
	/**
	 * Spit out frame information for a given frame number 
	 */
	boolean appendFrameInfo(StringBuilder sb, Frame ctx, int frameNumber, boolean showThis, boolean showFileId) throws PlayerDebugException
	{
		boolean validFrame = true;

		// some formatting properties
		int i = frameNumber;

		Location loc = ctx.getLocation();
		SourceFile file = loc.getFile();
		int line = loc.getLine();
		String name = (file == null) ? "<null>" : file.getName(); //$NON-NLS-1$
		String sig = ctx.getCallSignature();
		String func = extractFunctionName(sig);

		// file == null or line < 0 appears to be a terminator for stack info
		if (file == null && line < 0)
        {
            validFrame = false;
        }
        else
		{
			Variable[] var = ctx.getArguments(m_session);
			Variable dis = ctx.getThis(m_session);
			boolean displayArgs = (func != null) || (var != null);

			sb.append('#');
			FieldFormat.formatLong(sb, i, 3);
			sb.append(' ');

			if (showThis && dis != null)
			{
				ExpressionCache.appendVariable(sb, dis);
				sb.append("."); //$NON-NLS-1$
			}

			if (func != null)
				sb.append(func);

			if (displayArgs)
			{
				sb.append('(');
				for (int j=0; j<var.length; j++)
				{
					Variable v = var[j];
					sb.append(v.getName());
					sb.append('=');
					ExpressionCache.appendVariableValue(sb, v.getValue());
					if ((j+1)<var.length)
						sb.append(", "); //$NON-NLS-1$
				}
				sb.append(")"); //$NON-NLS-1$
				sb.append(getLocalizationManager().getLocalizedTextString("atFilename")); //$NON-NLS-1$
			}

			sb.append(name);

			// if this file is currently being filtered put the source file id after it
			if (file != null && (showFileId || !m_fileInfo.inFileList(file)))
			{
				sb.append('#');
				sb.append( file.getId() );

			}
			sb.append(':');
			sb.append(line);
		}
        return validFrame;
	}

	/** extract the function name from a signature */
	public static String extractFunctionName(String sig)
	{ 
		// strip everything after the leading ( 
		int at = sig.indexOf('(');
		if (at > -1)
			sig = sig.substring(0, at);

		// trim the leading [object_name::] since it doesn't seem to add much
		if (sig != null && (at = sig.indexOf("::")) > -1) //$NON-NLS-1$
			sig = sig.substring(at+2);

		return sig;
	}

	void doInfoVariables() throws PlayerDebugException
	{
		waitTilHalted();

		// dump a set of locals
		StringBuilder sb = new StringBuilder();

		// use our expression cache formatting routine
		try
		{
			Variable[] vars = m_session.getVariableList();
			for(int i=0; i<vars.length; i++)
			{
				Variable v = vars[i];

				// all non-local and non-arg variables
				if ( !v.isAttributeSet(VariableAttribute.IS_LOCAL) &&
					 !v.isAttributeSet(VariableAttribute.IS_ARGUMENT) )
				{
					ExpressionCache.appendVariable(sb, vars[i]);
					sb.append(m_newline);
				}
			}
		}
		catch(NullPointerException npe)
		{
			sb.append(getLocalizationManager().getLocalizedTextString("noVariables")); //$NON-NLS-1$
		}

		out(sb.toString());
	}

	void doInfoDisplay()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Num Enb Expression"+m_newline); //$NON-NLS-1$

		// our list of displays
		int count = displayCount();
		for(int i=0; i<count; i++)
		{
			DisplayAction b = displayAt(i);
			int num = b.getId();
			String exp = b.getContent();

			sb.append(':');
			FieldFormat.formatLong(sb, num, 3);

			if (b.isEnabled())
				sb.append(" y  "); //$NON-NLS-1$
			else
				sb.append(" n  "); //$NON-NLS-1$

			sb.append(exp);
			sb.append(m_newline);
		}

		out(sb.toString());
	}

	void doInfoArgs() throws PlayerDebugException
	{
		waitTilHalted();

		// dump a set of locals
		StringBuilder sb = new StringBuilder();

		// use our expression cache formatting routine
		try
		{
			int num = propertyGet(DISPLAY_FRAME_NUMBER);
			Frame[] frames = m_session.getFrames();
			Variable[] vars = frames[num].getArguments(m_session);
			for(int i=0; i<vars.length; i++)
			{
				ExpressionCache.appendVariable(sb, vars[i]);
				sb.append(m_newline);
			}
		}
		catch(NullPointerException npe)
		{
			sb.append(getLocalizationManager().getLocalizedTextString("noArguments")); //$NON-NLS-1$
		}
        catch(ArrayIndexOutOfBoundsException aix)
        {
            sb.append(getLocalizationManager().getLocalizedTextString("notInValidFrame")); //$NON-NLS-1$
        }

		out(sb.toString());
	}

	void doInfoLocals() throws PlayerDebugException
	{
		waitTilHalted();

		// dump a set of locals
		StringBuilder sb = new StringBuilder();

		// use our expression cache formatting routine
		try
		{
			// get the variables from the requested frame
			int num = propertyGet(DISPLAY_FRAME_NUMBER);
			Frame[] ar = m_session.getFrames();
			Frame ctx = ar[num];
			Variable[] vars = ctx.getLocals(m_session);

			for(int i=0; i<vars.length; i++)
			{
				Variable v = vars[i];

				// see if variable is local
				if ( v.isAttributeSet(VariableAttribute.IS_LOCAL) )
				{
					ExpressionCache.appendVariable(sb, v);
					sb.append(m_newline);
				}
			}
		}
		catch(NullPointerException npe)
		{
			sb.append(getLocalizationManager().getLocalizedTextString("noLocals")); //$NON-NLS-1$
		}
        catch(ArrayIndexOutOfBoundsException aix)
        {
            sb.append(getLocalizationManager().getLocalizedTextString("notInValidFrame")); //$NON-NLS-1$
        }

		out(sb.toString());
	}

	void doInfoScopeChain() throws PlayerDebugException
	{
		waitTilHalted();

		// dump the scope chain
		StringBuilder sb = new StringBuilder();

		// use our expression cache formatting routine
		try
		{
			// get the scope chainfrom the requested frame
			int num = propertyGet(DISPLAY_FRAME_NUMBER);
			Frame[] ar = m_session.getFrames();
			Frame ctx = ar[num];
			Variable[] scopes = ctx.getScopeChain(m_session);

			for(int i=0; i<scopes.length; i++)
			{
				Variable scope = scopes[i];
				ExpressionCache.appendVariable(sb, scope);
				sb.append(m_newline);
			}
		}
		catch(NullPointerException npe)
		{
			sb.append(getLocalizationManager().getLocalizedTextString("noScopeChain")); //$NON-NLS-1$
		}
        catch(ArrayIndexOutOfBoundsException aix)
        {
            sb.append(getLocalizationManager().getLocalizedTextString("notInValidFrame")); //$NON-NLS-1$
        }

		out(sb.toString());
	}
	
	void doInfoTargets()
    {
        if (!haveConnection())
		{
			out(getLocalizationManager().getLocalizedTextString("noActiveSession")); //$NON-NLS-1$
			if (m_launchURI != null)
			{
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("uri", m_launchURI); //$NON-NLS-1$
				out(getLocalizationManager().getLocalizedTextString("runWillLaunchUri", args)); //$NON-NLS-1$
			}
		}
        else
		{
			String uri = m_session.getURI();
			if (uri == null || uri.length() < 1)
				err(getLocalizationManager().getLocalizedTextString("targetUnknown")); //$NON-NLS-1$
			else
				out(uri);
		}
    }

	/**
	 * Dump some stats about our currently loaded swfs.
	 */
    void doInfoSwfs()
    {
		try
		{
			StringBuilder sb = new StringBuilder();
			SwfInfo[] swfs = m_fileInfo.getSwfs();
			for(int i=0; i<swfs.length; i++)
			{
				SwfInfo e = swfs[i];
				if (e == null || e.isUnloaded())
					continue;

				Map<String, Object> args = new HashMap<String, Object>();
				args.put("swfName", FileInfoCache.nameOfSwf(e)); //$NON-NLS-1$
				args.put("size", NumberFormat.getInstance().format(e.getSwfSize())); //$NON-NLS-1$

				try
				{
					int size = e.getSwdSize(m_session);

					// our swd is loaded so let's comb through our
					// list of scripts and locate the range of ids.
					SourceFile[] files = e.getSourceList(m_session);
					int max = Integer.MIN_VALUE;
					int min = Integer.MAX_VALUE;
					for(int j=0; j<files.length; j++)
					{
						SourceFile f = files[j];
						int id = f.getId();
						max = (id > max) ? id : max;
						min = (id < min) ? id : min;
					}

					args.put("scriptCount", Integer.toString(e.getSourceCount(m_session))); //$NON-NLS-1$
					args.put("min", Integer.toString(min)); //$NON-NLS-1$
					args.put("max", Integer.toString(max)); //$NON-NLS-1$
					args.put("plus", (e.isProcessingComplete()) ? "+" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					args.put("moreInfo", (size==0) ? getLocalizationManager().getLocalizedTextString("remainingSourceBeingLoaded") : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				catch(InProgressException ipe)
				{
					sb.append(getLocalizationManager().getLocalizedTextString("debugInfoBeingLoaded")); //$NON-NLS-1$
				}
				args.put("url", e.getUrl()); //$NON-NLS-1$
				sb.append(getLocalizationManager().getLocalizedTextString("swfInfo", args)); //$NON-NLS-1$
				sb.append(m_newline);
			}
			out( sb.toString() );
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("noSWFs")); //$NON-NLS-1$
		}
    }

	private static final int AUTHORED_FILE = 1;		// a file that was created by the end user, e.g. MyApp.mxml
	private static final int FRAMEWORK_FILE = 2;	// a file from the Flex framework, e.g. mx.controls.Button.as, see FRAMEWORK_FILE_PACKAGES
	private static final int SYNTHETIC_FILE = 3;	// e.g. "<set up XML utilities.1>"
	private static final int ACTIONS_FILE = 4;		// e.g. "Actions for UIComponent: Frame 1 of Layer Name Layer 1"

    private static final String[] FRAMEWORK_FILE_PACKAGES // package prefixes that we consider FRAMEWORK_FILEs
        = new String[] {"mx","flex","text"}; // 'text' is Vellum (temporary) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * Given a file, guesses what type it is -- e.g. a file created by the end user,
	 * or a file from the Flex framework, etc.
	 */
	private int getFileType(SourceFile sourceFile)
	{
		String name = sourceFile.getName();
		String pkg = sourceFile.getPackageName();

		if (name.startsWith("<") && name.endsWith(">") || name.equals("GeneratedLocale")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return SYNTHETIC_FILE;

        for (final String frameworkPkg : FRAMEWORK_FILE_PACKAGES )
        {
            // look for packages starting with pkgName
            if (pkg.startsWith(frameworkPkg + '\\') || //$NON-NLS-1$
                pkg.startsWith(frameworkPkg + '/')  || //$NON-NLS-1$
                pkg.equals(frameworkPkg))              //$NON-NLS-1$
            {
                return FRAMEWORK_FILE;
            }
        }

        if (name.startsWith("Actions for")) //$NON-NLS-1$
            return ACTIONS_FILE;

        return AUTHORED_FILE;
}

	void buildFileList(StringBuilder sb, boolean authoredFilesOnly)
	{
		SourceFile[] ar = m_fileInfo.getFileList();
		if (ar == null)
		{
			err(getLocalizationManager().getLocalizedTextString("noSourceFilesFound")); //$NON-NLS-1$
			return;
		}

		Vector<String> authoredFiles = new Vector<String>();
		Vector<String> frameworkFiles = new Vector<String>();
		Vector<String> syntheticFiles = new Vector<String>();
		Vector<String> actionsFiles = new Vector<String>();

		for (int i = 0; i < ar.length; i++)
		{
 			SourceFile m = ar[i];
			int fileType = getFileType(m);
			int id = m.getId();
			String entry = m.getName() + "#" + id; //$NON-NLS-1$

			switch (fileType)
			{
			case SYNTHETIC_FILE:
				syntheticFiles.add(entry);
				break;
			case FRAMEWORK_FILE:
				frameworkFiles.add(entry);
				break;
			case ACTIONS_FILE:
				actionsFiles.add(entry);
				break;
			case AUTHORED_FILE:
				authoredFiles.add(entry);
				break;
			}
		}

		int wrapAt = propertyGet(FILE_LIST_WRAP);

		if (!authoredFilesOnly)
		{
			if (actionsFiles.size() > 0)
			{
				appendStrings(sb, actionsFiles, (actionsFiles.size() > wrapAt) );
			}

			if (frameworkFiles.size() > 0)
			{
				sb.append("---"+m_newline); //$NON-NLS-1$
				appendStrings(sb, frameworkFiles, (frameworkFiles.size() > wrapAt) );
			}

			if (syntheticFiles.size() > 0)
			{
				sb.append("---"+m_newline); //$NON-NLS-1$
				appendStrings(sb, syntheticFiles, (syntheticFiles.size() > wrapAt) );
			}

			sb.append("---"+m_newline); //$NON-NLS-1$
		}

		appendStrings(sb, authoredFiles, (authoredFiles.size() > wrapAt) );
	}

	/**
	 * Dump a list of strings contained a vector
	 * If flow is set then the strings are placed
	 * on a single line and wrapped at $columnwidth
	 */
	void appendStrings(StringBuilder sb, Vector<String> v, boolean flow)
	{
		int count = v.size();
		int width = 0;
		int maxCol = propertyGet(COLUMN_WIDTH);

		for (int i = 0; i < count; i++)
		{
			String s = v.get(i);
			sb.append(s);

			// too many of them, then wrap according to columnwidth
			if (flow)
			{
				width += (s.length() + 2);
				if (width >= maxCol)
				{
					sb.append(m_newline);
					width = 0;
				}
				else
					sb.append(", "); //$NON-NLS-1$
			}
			else
				sb.append(m_newline);
		}

		// add a line feed for flow based
		if (flow && width > 0)
			sb.append(m_newline);
	}

	void doInfoFiles()
	{
		try
		{
			StringBuilder sb = new StringBuilder();
            if (hasMoreTokens())
            {
                String arg = nextToken();
                listFilesMatching(sb, arg);
            }
            else
            {
			    buildFileList(sb, false);
            }
			out(sb.toString());
		}
		catch(NullPointerException npe)
		{
			throw new IllegalStateException();
		}
	}

    public void waitForMetaData() throws InProgressException
    {
        // perform a query to see if our metadata has loaded
        int metadatatries = propertyGet(METADATA_ATTEMPTS);
        int maxPerCall = 8;   // cap on how many attempt we make per call

        int tries = Math.min(maxPerCall, metadatatries);
        if (tries > 0)
        {
            int remain = metadatatries - tries; // assume all get used up

            // perform the call and then update our remaining number of attempts
            try
            {
                tries = waitForMetaData(tries);
                remain = metadatatries - tries; // update our used count
            }
            catch(InProgressException ipe)
            {
                propertyPut(METADATA_ATTEMPTS, remain);
				throw ipe;
            }
        }
    }

	/**
	 * Wait for the API to load function names, which
	 * exist in the form of external meta-data.
	 *
	 * Only do this tries times, then give up
	 *
	 * We wait period * attempts
	 */
	public int waitForMetaData(int attempts) throws InProgressException
	{
        int start = attempts;
        int period = propertyGet(METADATA_ATTEMPTS_PERIOD);
		while(attempts > 0)
		{
			// are we done yet?
			if (isMetaDataAvailable())
				break;
			else
				try { attempts--; Thread.sleep(period); } catch(InterruptedException ie) {}
		}

		// throw exception if still not ready
		if (!isMetaDataAvailable())
			throw new InProgressException();

        return start-attempts;  // remaining number of tries
	}

	/**
	 * Ask each swf if metadata processing is complete
	 */
	public boolean isMetaDataAvailable()
	{
		boolean allLoaded = true;
		try 
		{
			// we need to ask the session since our fileinfocache will hide the exception
			SwfInfo[] swfs = m_session.getSwfs();
			for(int i=0; i<swfs.length; i++)
			{
				// check if our processing is finished.
				SwfInfo swf = swfs[i];
				if (swf != null && !swf.isProcessingComplete())
				{
					allLoaded = false;
					break;
				}
			}
		}
		catch(NoResponseException nre)
		{
			// ok we still need to wait for player to read the swd in
			allLoaded = false;
		}

		// count the number of times we checked and it wasn't there
		if (!allLoaded)
		{
			int count = propertyGet(METADATA_NOT_AVAILABLE);
			count++;
			propertyPut(METADATA_NOT_AVAILABLE, count);
		}
		else
		{
			// success so we reset our attempt counter
			propertyPut(METADATA_ATTEMPTS, METADATA_RETRIES);
		}
		return allLoaded;
	}

	void doInfoHandle()
	{
		if (hasMoreTokens())
		{
			// user specified a fault
			String faultName = nextToken();

			// make sure we know about this one
		    if (!m_faultTable.exists(faultName))
				err(getLocalizationManager().getLocalizedTextString("unrecognizedFault")); //$NON-NLS-1$
			else
				listFault(faultName);
		}
		else
		{
			// dump them all
			StringBuilder sb = new StringBuilder();

			appendFaultTitles(sb);

			Object names[]  = m_faultTable.names();
			Arrays.sort(names);

			for(int i=0; i<names.length; i++)
				appendFault(sb, (String)names[i]);

			out ( sb.toString() );
		}
	}

	void doInfoFuncs()
	{
		StringBuilder sb = new StringBuilder();

		String arg = null;

		// we take an optional single arg which specifies a module
		try
		{
			// let's wait a bit for the background load to complete
			waitForMetaData();

			if (hasMoreTokens())
			{
				arg = nextToken();
                int id = arg.equals(".") ? propertyGet(LIST_MODULE) : parseFileArg(-1, arg); //$NON-NLS-1$

				SourceFile m = m_fileInfo.getFile(id);
				listFunctionsFor(sb, m);
			}
			else
			{
				SourceFile[] ar = m_fileInfo.getFileList();
				if (ar == null)
					err(getLocalizationManager().getLocalizedTextString("noSourceFilesFound")); //$NON-NLS-1$
				else
                {
                    for(int i = 0; ar != null && i < ar.length; i++)
                    {
                        SourceFile m = ar[i];
                        listFunctionsFor(sb, m);
                    }
                }
			}

			out(sb.toString());
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("noFunctionsFound")); //$NON-NLS-1$
		}
		catch(ParseException pe)
		{
			err(pe.getMessage());
		}
		catch(NoMatchException nme)
		{
			err(nme.getMessage());
		}
		catch(AmbiguousException ae)
		{
			err(ae.getMessage());
		}
		catch(InProgressException ipe)
		{
		    err(getLocalizationManager().getLocalizedTextString("functionListBeingPrepared")); //$NON-NLS-1$
		}
	}

	void listFunctionsFor(StringBuilder sb, SourceFile m)
	{
		String[] names = m.getFunctionNames(m_session);
		if (names == null)
			return;

		Arrays.sort(names);

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("sourceFile", m.getName() + "#" + m.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(getLocalizationManager().getLocalizedTextString("functionsInSourceFile", args)); //$NON-NLS-1$
		sb.append(m_newline);

        for (int j = 0; j < names.length; j++)
		{
			String fname = names[j];
			sb.append(' ');
			sb.append(fname);
			sb.append(' ');
			sb.append(m.getLineForFunctionName(m_session, fname));
			sb.append(m_newline);
		}
	}

    void listFilesMatching(StringBuilder sb, String match)
    {
        SourceFile[] sourceFiles = m_fileInfo.getFiles(match);

        for (int j = 0; j < sourceFiles.length; j++)
        {
            SourceFile sourceFile = sourceFiles[j];
            sb.append(sourceFile.getName());
			sb.append('#');
			sb.append(sourceFile.getId());
			sb.append(m_newline);
        }
    }

    void doInfoSources()
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			buildFileList(sb, true);
			out(sb.toString());
		}
		catch(NullPointerException npe)
		{
			throw new IllegalStateException();
		}
	}

	void doInfoBreak() throws NotConnectedException
	{
//		waitTilHalted();

		StringBuilder sb = new StringBuilder();
		sb.append("Num Type           Disp Enb Address    What"+m_newline);

		// our list of breakpoints
		int count = breakpointCount();
		for(int i=0; i<count; i++)
		{
			BreakAction b = breakpointAt(i);
			int status = b.getStatus();
			boolean isResolved = (status == BreakAction.RESOLVED);
			Location l = b.getLocation();
			SourceFile file = (l != null) ? l.getFile() : null;
			String funcName = (file == null) ? null : file.getFunctionNameForLine(m_session, l.getLine()) ;
			boolean singleSwf = b.isSingleSwf();
			int cmdCount = b.getCommandCount();
			int hits = b.getHits();
			String cond = b.getConditionString();
			boolean silent = b.isSilent();
			int offset = adjustOffsetForUnitTests((file == null) ? 0 : file.getOffsetForLine(l.getLine()));

			int num = b.getId();
			FieldFormat.formatLong(sb, num, 3);
			sb.append(" breakpoint     ");

			if (b.isAutoDisable())
				sb.append("dis  ");
			else if (b.isAutoDelete())
				sb.append("del  ");
			else
				sb.append("keep ");

			if (b.isEnabled())
				sb.append("y   ");
			else
				sb.append("n   ");

			sb.append("0x"); //$NON-NLS-1$
			FieldFormat.formatLongToHex(sb, offset, 8);
			sb.append(' ');

			if (funcName != null)
			{
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("functionName", funcName); //$NON-NLS-1$
				sb.append(getLocalizationManager().getLocalizedTextString("inFunctionAt", args)); //$NON-NLS-1$
			}

			if (file != null)
			{
				sb.append(file.getName());
				if (isResolved && singleSwf)
				{
					sb.append("#"); //$NON-NLS-1$
					sb.append(file.getId());
				}
				sb.append(':');
				sb.append(l.getLine());
			}
			else
			{
				String expr = b.getBreakpointExpression();
				if (expr != null)
					sb.append(expr);
			}

			switch (status)
			{
			case BreakAction.UNRESOLVED:
				sb.append(getLocalizationManager().getLocalizedTextString("breakpointNotYetResolved")); //$NON-NLS-1$
				break;
			case BreakAction.AMBIGUOUS:
				sb.append(getLocalizationManager().getLocalizedTextString("breakpointAmbiguous")); //$NON-NLS-1$
				break;
			case BreakAction.NOCODE:
				sb.append(getLocalizationManager().getLocalizedTextString("breakpointNoCode")); //$NON-NLS-1$
				break;
			}

			// if a single swf break action then append more info
			if (singleSwf && isResolved)
			{
				try
				{
					SwfInfo info = m_fileInfo.swfForFile(file);
					Map<String, Object> swfArgs = new HashMap<String, Object>();
					swfArgs.put("swf", FileInfoCache.nameOfSwf(info)); //$NON-NLS-1$
					sb.append(getLocalizationManager().getLocalizedTextString("inSwf", swfArgs)); //$NON-NLS-1$
				}
				catch(NullPointerException npe)
				{
					// can't find the swf
					sb.append(getLocalizationManager().getLocalizedTextString("nonRestorable")); //$NON-NLS-1$
				}
			}
			sb.append(m_newline);

			final String INDENT = "        "; //$NON-NLS-1$

			// state our condition if we have one
			if (cond != null && cond.length() > 0)
			{
				sb.append(INDENT);
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("breakpointCondition", cond ); //$NON-NLS-1$
				sb.append(getLocalizationManager().getLocalizedTextString(getLocalizationManager().getLocalizedTextString("stopOnlyIfConditionMet", args))); //$NON-NLS-1$
				sb.append(m_newline);
			}

			// now if its been hit, lets state the fact
			if (hits > 0)
			{
				sb.append(INDENT);
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("count", Integer.toString(hits)); //$NON-NLS-1$
				sb.append(getLocalizationManager().getLocalizedTextString("breakpointAlreadyHit", args)); //$NON-NLS-1$
				sb.append(m_newline);
			}

			// silent?
			if (silent)
			{
				sb.append(INDENT);
				sb.append(getLocalizationManager().getLocalizedTextString("silentBreakpoint")+m_newline); //$NON-NLS-1$
			}

			// now if any commands are trailing then we pump them out
			for(int j=0; j<cmdCount; j++)
			{
				sb.append(INDENT);
				sb.append(b.commandAt(j));
				sb.append(m_newline);
			}
		}

		int wcount = watchpointCount();
		for(int k = 0; k < wcount; k++)
		{
			WatchAction b = watchpointAt(k);
			int id = b.getId();
			FieldFormat.formatLong(sb, id, 4);

			int flags = b.getKind();
			switch(flags)
			{
				case WatchKind.READ:
					sb.append("rd watchpoint  ");
					break;
				case WatchKind.WRITE:
					sb.append("wr watchpoint  ");
					break;
				case WatchKind.READWRITE:
				default:
					sb.append("watchpoint     ");
					break;
			}

			sb.append("keep ");
			sb.append("y   ");
			sb.append("           "); //$NON-NLS-1$
			sb.append(b.getExpr());
			sb.append(m_newline);
		}

		int ccount = catchpointCount();
		for (int k = 0; k < ccount; k++)
		{
			CatchAction c = catchpointAt(k);
			int id = c.getId();
			FieldFormat.formatLong(sb, id, 4);

			String typeToCatch = c.getTypeToCatch();
			if (typeToCatch == null)
				typeToCatch = "*"; //$NON-NLS-1$

			sb.append("catch          ");
			sb.append("keep ");
			sb.append("y   ");
			sb.append("           "); //$NON-NLS-1$
			sb.append(typeToCatch);
			sb.append(m_newline);
		}

		out(sb.toString());
	}

	/**
	 * Dump out the state of the execution, either the fact we are running
	 * or the breakpoint we hit.
	 */
	void dumpHaltState(boolean postStep) throws NotConnectedException
	{
		// spit out any event output, if we are to resume after a fault and we're not stepping then we're done.
		processEvents();
//		System.out.println("processEvents = "+m_requestResume);

		if (m_requestResume && !postStep)
			return;

		if (!m_session.isConnected())
		{
			// session is kaput
			out(getLocalizationManager().getLocalizedTextString("sessionTerminated")); //$NON-NLS-1$
			exitSession();
		}
		else
		{
			if (m_session.isSuspended())
			{
				// capture our break location / information
				StringBuilder sbLine = new StringBuilder();
				dumpBreakLine(postStep, sbLine);

				// Process our breakpoints.
				// Since we can have conditional breakpoints, which the
				// player always breaks for, but we may not want to, the variable
				// m_requestResume may be set after this call.  Additionally,
				// silent may be set for one of two reasons; 1) m_requestResume
				// was set to true in the call or one or more breakpoints that
				// hit contained the keyword silent in their command list.
				//
				StringBuilder sbBreak = new StringBuilder();
				boolean silent = processBreak(postStep, sbBreak);

				StringBuilder sb = new StringBuilder();
				if (silent)
				{
					// silent means we only spit out our current location
					dumpBreakLine(postStep, sb);
				}
				else
				{
					// not silent means we append things like normal
					sb.append(sbLine);
					if (sbLine.length() > 0 && sbLine.charAt(sbLine.length()-1) != '\n')
						sb.append(m_newline);
					sb.append(sbBreak);
				}

				// output whatever was generated 
				if (sb.length() > 0)
					out( sb.toString() );

//				System.out.println("processbreak = "+m_requestResume+",silent="+silent+",reason="+m_session.suspendReason());
			}
			else
			{
				// very bad, set stepping so that we don't trigger a continue on a breakpoint or fault
				out(getLocalizationManager().getLocalizedTextString("playerDidNotStop")); //$NON-NLS-1$
			}
		}
	}

	Location getCurrentLocation()
	{
		Location where = null;
		try
		{
			Frame[] ar = m_session.getFrames();
			propertyPut(CURRENT_FRAME_DEPTH, (ar.length > 0) ? ar.length : 0);
			where = ( (ar.length > 0) ? ar[0].getLocation() : null);
		}
		catch(PlayerDebugException pde)
		{
			// where == null
		}
		return where;
	}

	void dumpBreakLine(boolean postStep, StringBuilder sb) throws NotConnectedException
	{
		int bp = -1;
		String name = getLocalizationManager().getLocalizedTextString("unknownFilename"); //$NON-NLS-1$
		int line = -1;

		// clear our current frame display
		propertyPut(DISPLAY_FRAME_NUMBER, 0);

		/* dump a context line to the console */
		Location l = getCurrentLocation();

		// figure out why we stopped
		int reason = SuspendReason.Unknown;
		try { reason = m_session.suspendReason(); } catch(PlayerDebugException pde) {}

		// then see if it because of a swfloaded event
		if( reason == SuspendReason.ScriptLoaded)
		{
            // since the player takes a long time to provide swf/swd, try 80 * 250ms = ~20s
            if (propertyGet(METADATA_ATTEMPTS) > 0)
			    try { waitForMetaData(80); } catch(InProgressException ipe) { }

            m_fileInfo.setDirty();
			processEvents();
            propagateBreakpoints();
            
            sb.append(getLocalizationManager().getLocalizedTextString("additionalCodeLoaded")); //$NON-NLS-1$
            sb.append(m_newline);

			if (resolveBreakpoints(sb))
				sb.append(getLocalizationManager().getLocalizedTextString("setAdditionalBreakpoints")+m_newline); //$NON-NLS-1$
			else
				sb.append(getLocalizationManager().getLocalizedTextString("fixBreakpoints")+m_newline); //$NON-NLS-1$
		}
		else if ( l == null || l.getFile() == null )
		{
			// no idea where we are ?!?
			propertyPut(LAST_FRAME_DEPTH, 0);
			sb.append(getLocalizationManager().getLocalizedTextString("executionHalted")); //$NON-NLS-1$
			sb.append(' ');

			/** disable this line (and enable the one after) if implementation Extensions are not provided */
			appendBreakInfo(sb);
			//sb.append("unknown location");
		}
		else
		{
			SourceFile file = l.getFile();
			name = file.getName();
			line = l.getLine();
			String funcName = file.getFunctionNameForLine(m_session, line) ;

			// where were we last time
			int lastModule = propertyGet(LIST_MODULE);
			int lastDepth = propertyGet(LAST_FRAME_DEPTH);

			int thisModule = file.getId();
			int thisDepth = propertyGet(CURRENT_FRAME_DEPTH);  // triggered via getCurrentLocation()

			// mark where we stopped
			propertyPut(LAST_FRAME_DEPTH, thisDepth);

			// if we have changed our context or we are not spitting out source then dump our location
			if (!postStep || lastModule != thisModule || lastDepth != thisDepth )
			{
				// is it a fault?
				String reasonForHalting;
				if ( reason == SuspendReason.Fault || reason == SuspendReason.StopRequest)
				{
					StringBuilder s = new StringBuilder();
					appendReason(s, reason);
					reasonForHalting = s.toString();
				}
				// if its a breakpoint add that information
				else if ( (bp = enabledBreakpointIndexOf(l)) > -1 )
				{
					Map<String, Object> args = new HashMap<String, Object>();
					args.put("breakpointNumber", Integer.toString(breakpointAt(bp).getId())); //$NON-NLS-1$
					reasonForHalting = getLocalizationManager().getLocalizedTextString("hitBreakpoint", args); //$NON-NLS-1$
				}
				else
				{
					reasonForHalting = getLocalizationManager().getLocalizedTextString("executionHalted"); //$NON-NLS-1$
				}

				Map<String, Object> args = new HashMap<String, Object>();
				args.put("reasonForHalting", reasonForHalting); //$NON-NLS-1$
				args.put("fileAndLine", name + ':' + line); //$NON-NLS-1$
				String formatString;
				if (funcName != null)
				{
					args.put("functionName", funcName); //$NON-NLS-1$
					formatString = "haltedInFunction"; //$NON-NLS-1$
				}
				else
				{
					formatString = "haltedInFile"; //$NON-NLS-1$
				}
				sb.append(getLocalizationManager().getLocalizedTextString(formatString, args));

				if (!m_fullnameOption) 
					sb.append(m_newline);				
			}

            // set current listing poistion and emit emacs trigger
            setListingPosition(thisModule, line);

			// dump our source line if not in emacs mode
			if (!m_fullnameOption)
				appendSource(sb, file.getId(), line, file.getLine(line), false);
		}
	}

	void appendFullnamePosition(StringBuilder sb, SourceFile file, int lineNbr)
	{
        // fullname option means we dump 'path:line:col?:offset', which is used for emacs !
		String name = file.getFullPath();
		if (name.startsWith("file:/")) //$NON-NLS-1$
			name = name.substring(6);

		// Ctrl-Z Ctrl-Z
		sb.append('\u001a');
		sb.append('\u001a');

		sb.append(name);
		sb.append(':');
		sb.append(lineNbr);
		sb.append(':');
		sb.append('0');
		sb.append(':');
		sb.append("beg"); //$NON-NLS-1$
		sb.append(':');
		sb.append('0');
	}

	// pretty print a trace statement to the console
	void dumpTraceLine(String s)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[trace] "); //$NON-NLS-1$
		sb.append(s);
		out(sb.toString());
	}

	// pretty print a fault statement to the console
	void dumpFaultLine(FaultEvent e)
	{
		StringBuilder sb = new StringBuilder();

		// use a slightly different format for ConsoleErrorFaults
		if (e instanceof ConsoleErrorFault)
		{
			sb.append(getLocalizationManager().getLocalizedTextString("linePrefixWhenDisplayingConsoleError")); //$NON-NLS-1$
			sb.append(' ');
			sb.append(e.information);

            final String stackTrace = e.stackTrace();
            if (stackTrace != null && stackTrace.length() > 0) {
                sb.append("\n").append(stackTrace);
            }
		}
		else
		{
			String name = e.name();
			sb.append(getLocalizationManager().getLocalizedTextString("linePrefixWhenDisplayingFault")); //$NON-NLS-1$
			sb.append(' ');
			sb.append(name);
			if (e.information != null && e.information.length() > 0)
			{
				sb.append(getLocalizationManager().getLocalizedTextString("informationAboutFault")); //$NON-NLS-1$
				sb.append(e.information);
			}

            final String stackTrace = e.stackTrace();
            if (stackTrace != null && stackTrace.length() > 0) {
                sb.append("\n").append(stackTrace);
            }
		}
		out( sb.toString() );
	}

    /**
     * Called when a swf has been loaded by the player
     * @param e event documenting the load
     */
    void handleSwfLoadedEvent(SwfLoadedEvent e)
    {
        // first we dump out a message that displays we have loaded a swf
        dumpSwfLoadedLine(e);
    }

	// pretty print a SwfLoaded statement to the console
	void dumpSwfLoadedLine(SwfLoadedEvent e)
	{
		// now rip off any trailing ? options
		int at = e.path.lastIndexOf('?');
		String name = (at > -1) ? e.path.substring(0, at) : e.path;

		StringBuilder sb = new StringBuilder();
		sb.append(getLocalizationManager().getLocalizedTextString("linePrefixWhenSwfLoaded")); //$NON-NLS-1$
		sb.append(' ');
		sb.append(name);
		sb.append(" - "); //$NON-NLS-1$

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("size", NumberFormat.getInstance().format(e.swfSize)); //$NON-NLS-1$
		sb.append(getLocalizationManager().getLocalizedTextString("sizeAfterDecompression", args)); //$NON-NLS-1$
		out(sb.toString());
	}

    /**
     * Propagate current breakpoints to the newly loaded swf.
     */
    void propagateBreakpoints() throws NotConnectedException
    {
		// get the newly added swf, which lands at the end list
		SwfInfo[] swfs = m_fileInfo.getSwfs();
		SwfInfo swf = (swfs.length > 1) ? swfs[swfs.length-1] : null;

		// now walk through all breakpoints propagating the 
		// the break for each source and line number we
		// find in the new swf
		int size = m_breakpoints.size();
		for (int i = 0; (swf != null) && i < size; i++)
		{
			// dont do this for single swf breakpoints
			BreakAction bp = breakpointAt(i);
			if (bp.isSingleSwf())
				continue;
			if (bp.getStatus() != BreakAction.RESOLVED)
				continue;

			try
			{
				Location l = bp.getLocation();
				int line = l.getLine();
				SourceFile f = l.getFile();
				Location newLoc = findAndEnableBreak(swf, f, line);
				if (newLoc != null)
					bp.addLocation(newLoc);
			}
			catch(InProgressException ipe)
			{
				if (breakpointCount() > 0)
				{
					Map<String, Object> args = new HashMap<String, Object>();
					args.put("breakpointNumber", Integer.toString(bp.getId())); //$NON-NLS-1$
					out(getLocalizationManager().getLocalizedTextString("breakpointNotPropagated", args)); //$NON-NLS-1$
				}
			}
		}
    }

	/**
	 * Perform the tasks need for when a swf is unloaded
	 * the player
	 */
	void handleSwfUnloadedEvent(SwfUnloadedEvent e)
	{
		// print out the notification
		dumpSwfUnloadedLine(e);
	}

	// pretty print a SwfUnloaded statement to the console
	void dumpSwfUnloadedLine(SwfUnloadedEvent e)
	{
		// now rip off any trailing ? options
		int at = e.path.lastIndexOf('?');
		String name = (at > -1) ? e.path.substring(0, at) : e.path;

		StringBuilder sb = new StringBuilder();
		sb.append(getLocalizationManager().getLocalizedTextString("linePrefixWhenSwfUnloaded")); //$NON-NLS-1$
		sb.append(' ');
		sb.append(name);
		out(sb.toString());
	}

	void doContinue() throws NotConnectedException
	{
		waitTilHalted();

		// this will trigger a resume when we get back to the main loop
		m_requestResume = true;
		m_repeatLine = m_currentLine;
	}

	/**
	 * Our main loop when the player is off running
	 */
	void runningLoop() throws NoResponseException, NotConnectedException
	{
		int update = propertyGet(UPDATE_DELAY);
		boolean nowait = (propertyGet(NO_WAITING) == 1) ? true : false;  // DEBUG ONLY; do not document
		boolean stop = false;

		// not there, not connected or already halted and no pending resume requests => we are done
		if (!haveConnection() || (m_session.isSuspended() && !m_requestResume) )
		{
			processEvents();
			stop = true;
		}

	    while(!stop)
		{
			// allow keyboard input
			if (!nowait)
				m_keyboardReadRequest = true;

			if (m_requestResume)
			{
				// resume execution (request fulfilled) and look for keyboard input
				try
				{
					if (m_stepResume)
						m_session.stepContinue();
					else
						m_session.resume();
				}
				catch(NotSuspendedException nse)
				{
					err(getLocalizationManager().getLocalizedTextString("playerAlreadyRunning")); //$NON-NLS-1$
				}

				m_requestResume = false;
				m_requestHalt = false;
				m_stepResume = false;
			}

			// sleep for a bit, then process our events.
			try { Thread.sleep(update); } catch(InterruptedException ie) {}
			processEvents();

			// lost connection?
			if (!haveConnection())
			{
				stop = true;
				dumpHaltState(false);
			}
			else if (m_session.isSuspended())
			{
				/**
				 * We have stopped for some reason.  Now for all cases, but conditional
				 * breakpoints, we should be done.  For conditional breakpoints it
				 * may be that the condition has turned out to be false and thus
				 * we need to continue
				 */

				/**
				 * Now before we do this see, if we have a valid break reason, since
				 * we could be still receiving incoming messages, even though we have halted.
				 * This is definately the case with loading of multiple SWFs.  After the load
				 * we get info on the swf.
				 */
				int tries = 3;
				while (tries-- > 0 && m_session.suspendReason() == SuspendReason.Unknown)
					try { Thread.sleep(100); processEvents(); } catch(InterruptedException ie) {}

				dumpHaltState(false);
				if (!m_requestResume)
					stop = true;
			}
			else if (nowait)
			{
				stop = true;  // for DEBUG only
			}
			else
			{
				/**
				 * We are still running which is fine.  But let's see if the user has
				 * tried to enter something on the keyboard.  If so, then we need to
				 * stop
				 */
				if (!m_keyboardInput.isEmpty() && System.getProperty("fdbunit")==null) //$NON-NLS-1$
				{
					// flush the queue and prompt the user if they want us to halt
					m_keyboardInput.clear();
					try
					{
						if (yesNoQuery(getLocalizationManager().getLocalizedTextString("doYouWantToHalt"))) //$NON-NLS-1$
						{
							out(getLocalizationManager().getLocalizedTextString("attemptingToHalt")); //$NON-NLS-1$
							m_session.suspend();
							m_requestHalt = true;

							// no connection => dump state and end
							if (!haveConnection())
							{
								dumpHaltState(false);
								stop = true;
							}
							else if (!m_session.isSuspended())
								err(getLocalizationManager().getLocalizedTextString("couldNotHalt")); //$NON-NLS-1$
						}
					}
					catch(IllegalArgumentException iae)
					{
						out(getLocalizationManager().getLocalizedTextString("escapingFromDebuggerPendingLoop")); //$NON-NLS-1$
						propertyPut(NO_WAITING, 1);
						stop = true;
					}
					catch(IOException io)
					{
						Map<String, Object> args = new HashMap<String, Object>();
						args.put("error", io.getMessage()); //$NON-NLS-1$
						err(getLocalizationManager().getLocalizedTextString("continuingDueToError", args)); //$NON-NLS-1$
					}
					catch(SuspendedException se)
					{
						// lucky us, already stopped
					}
				}
			}
//		System.out.println("doContinue resume="+m_requestResume+",isSuspended="+m_session.isSuspended());
		}

		// DEBUG ONLY: if we are not waiting then process some events
		if (nowait)
			processEvents();
	}

	/**
	 * Bring the listing location back to the current frame
	 */
	void doHome()
	{
		try
		{
			Location l = getCurrentLocation();
			SourceFile file = l.getFile();
			int module = file.getId();
			int line = l.getLine();

			// now set it
            setListingPosition(module, line);
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("currentLocationUnknown")); //$NON-NLS-1$
		}
	}

	// Dump a source line of text to the display
	void dumpStep() throws NotConnectedException
	{
		dumpHaltState(true);
	}

	/**
	 * Simple interface used with stepWithTimeout().  Implementors of this interface
	 * are expected to call one of these function: Session.stepInto(), Session.stepOver(),
	 * Session.stepOut(), or Session.stepContinue().
	 */
	private interface AnyKindOfStep
	{
		public void step() throws PlayerDebugException;
	}

	/**
	 * Helper function to do a stepInto, stepOver, stepOut, or stepContinue,
	 * and then to block (processing events) until either the step has completed
	 * or it has timed out.
	 */
	private void stepWithTimeout(AnyKindOfStep step) throws PlayerDebugException
	{
		int timeout = m_session.getPreference(SessionManager.PREF_RESPONSE_TIMEOUT);
		long timeoutTime = System.currentTimeMillis() + timeout;

		step.step();
		
		while (System.currentTimeMillis() < timeoutTime && !m_session.isSuspended())
		{
			processEvents();
			if (!m_session.isSuspended())
			{
				try { Thread.sleep(1); } catch (InterruptedException e) { }
			}
		}
		if (System.currentTimeMillis() >= timeoutTime && !m_session.isSuspended())
			throw new NoResponseException(timeout);
	}

	private boolean allowedToStep() throws NotConnectedException
	{
		int suspendReason = m_session.suspendReason();
		if (suspendReason == SuspendReason.ScriptLoaded)
		{
			err(getLocalizationManager().getLocalizedTextString("cannotStep")); //$NON-NLS-1$
			return false;
		}

		return true;
	}

	/**
	 * Perform step into, optional COUNT parameter
	 */
	void doStep() throws PlayerDebugException
	{
		waitTilHalted();

		if (!allowedToStep())
			return;
		
		int count = 1;
		if (hasMoreTokens())
			count = nextIntToken();

		while(count-- > 0)
		{
			stepWithTimeout(new AnyKindOfStep() {
				public void step() throws PlayerDebugException
				{
					m_session.stepInto();
				}
			});

			for (;;)
			{
				dumpStep();

				if (m_requestResume) // perhaps we hit a conditional breakpoint
				{
					m_requestResume = false;
					stepWithTimeout(new AnyKindOfStep() {
						public void step() throws PlayerDebugException
						{
							m_session.stepContinue();
						}
					});
				}
				else
				{
					break;
				}
			}
		}

		m_repeatLine = m_currentLine;
	}

	/**
	 * Perform step over, optional COUNT parameter
	 */
	void doNext() throws PlayerDebugException
	{
		waitTilHalted();

		if (!allowedToStep())
			return;
		
		int count = 1;
		if (hasMoreTokens())
			count = nextIntToken();

		try
		{
			while(count-- > 0)
			{
				stepWithTimeout(new AnyKindOfStep() {
					public void step() throws PlayerDebugException
					{
						m_session.stepOver();
					}
				});

				for (;;)
				{
					dumpStep();

					if (m_requestResume) // perhaps we hit a conditional breakpoint
					{
						m_requestResume = false;
						stepWithTimeout(new AnyKindOfStep() {
							public void step() throws PlayerDebugException
							{
								m_session.stepContinue();
							}
						});
					}
					else
					{
						break;
					}
				}
			}
		}
		catch(NoResponseException nre)
		{
			if (count > 0)
			{
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("count", Integer.toString(count)); //$NON-NLS-1$
				err(getLocalizationManager().getLocalizedTextString("abortingStep", args)); //$NON-NLS-1$
			}
		}

		m_repeatLine = m_currentLine;
	}

	/**
	 * Perform step out
	 */
	void doFinish() throws PlayerDebugException
	{
		waitTilHalted();

		if (!allowedToStep())
			return;

		try
		{
			// make sure we have another frame?
			int depth = propertyGet(CURRENT_FRAME_DEPTH);
			if (depth < 2)
				err(getLocalizationManager().getLocalizedTextString("finishCommandNotMeaningfulOnOutermostFrame")); //$NON-NLS-1$
			else
			{
				stepWithTimeout(new AnyKindOfStep() {
					public void step() throws PlayerDebugException
					{
						m_session.stepOut();
					}
				});

				for (;;)
				{
					dumpStep();

					if (m_requestResume) // perhaps we hit a conditional breakpoint
					{
						m_requestResume = false;
						stepWithTimeout(new AnyKindOfStep() {
							public void step() throws PlayerDebugException
							{
								m_session.stepContinue();
							}
						});
					}
					else
					{
						break;
					}
				}

				m_repeatLine = m_currentLine;
			}
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("finishCommandNotMeaningfulWithoutStack")); //$NON-NLS-1$
		}
	}

	/**
	 * Delete a breakpoint, very similar logic to disable.
	 */
	void doDelete() throws IOException, AmbiguousException, NotConnectedException
	{
		waitTilHalted();

		try
		{
			if (!hasMoreTokens())
			{
				// no args means delete all breakpoints, last chance...
				if (yesNoQuery(getLocalizationManager().getLocalizedTextString("askDeleteAllBreakpoints"))) //$NON-NLS-1$
				{
					int count = breakpointCount();
					for(int i=count-1; i>-1; i--)
						removeBreakpointAt(i);

					removeAllWatchpoints();
					removeAllCatchpoints();
				}
			}
			else
			{
				// optionally specify  'display' or 'breakpoint'
				String arg = nextToken();
				int cmd = disableCommandFor(arg);
				int id = -1;
				if (cmd == CMD_DISPLAY)
					doUnDisplay();
				else
				{
					if (cmd == CMD_BREAK)
						id = nextIntToken();  // ignore and get next number token
					else
						id = Integer.parseInt(arg);

					do
					{
						try
						{
							int at = breakpointIndexOf(id);
							if(at > -1)
							{
								removeBreakpointAt(at);
							}
							else
							{
								at = watchpointIndexOf(id);
								if (at > -1)
								{
									removeWatchpointAt(at);
								}
								else
								{
									at = catchpointIndexOf(id);
									removeCatchpointAt(at);
								}
							}
						}
						catch(IndexOutOfBoundsException iob)
						{
							Map<String, Object> args = new HashMap<String, Object>();
							args.put("breakpointNumber", m_currentToken); //$NON-NLS-1$
							err(getLocalizationManager().getLocalizedTextString("noBreakpointNumber", args)); //$NON-NLS-1$
						}

						if (hasMoreTokens())
							id = nextIntToken();
						else
							id = -1;

						// keep going till we're blue in the face; also note that we cache'd a copy of locations
						// so that breakpoint numbers are consistent.
					}
					while( id > -1 );
				}
			}
		}
		catch(NumberFormatException nfe)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("token", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("badBreakpointNumber", args)); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("commandFailed")); //$NON-NLS-1$
		}
	}

	/**
	 * Set a breakpoint
	 */
	void doBreak() throws NotConnectedException
	{
		/* wait a bit if we are not halted */
		waitTilHalted();

        int module = propertyGet(LIST_MODULE);
		int line = propertyGet(LIST_LINE);
        String arg = null;

		/* currentXXX may NOT be invalid! */
		try
		{
			if (hasMoreTokens())
			{
                arg = nextToken();
                int[] result = parseLocationArg(module, line, arg);
                module = result[0];
                line = result[1];
			}
			else
			{
				// no parameter mean use current location;  null pointer if nothing works
				Location l = getCurrentLocation();
				SourceFile file = l.getFile();
				module = file.getId();
				line = l.getLine();
			}

//			// check to see if there are any existing breakpoints at this file/line
//			LinkedList existingBreakpoints = new LinkedList();
//			int start = 0;
//			for (;;)
//			{
//				int bp = breakpointIndexOf(module, line, start, true);
//				if (bp == -1)
//					break; // no more matches
//				boolean isEnabled = breakpointAt(bp).isEnabled();
//				existingBreakpoints.add("" + bp + (isEnabled ? "" : " (disabled)"));
//			}
//			if (existingBreakpoints.size() > 0)
//			{
//				String
//			}

			// go off; create it and set it
            BreakAction b = addBreakpoint(module, line);  // throws npe if not able to set
            Location l = b.getLocation();

			int which = b.getId();
			String name = l.getFile().getName();
			int offset  = adjustOffsetForUnitTests(l.getFile().getOffsetForLine(line));

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("breakpointNumber", Integer.toString(which)); //$NON-NLS-1$
			args.put("file", name); //$NON-NLS-1$
			args.put("line", Integer.toString(line)); //$NON-NLS-1$
			String formatString;
			if (offset != 0)
			{
				args.put("offset", "0x" + Integer.toHexString(offset)); //$NON-NLS-1$ //$NON-NLS-2$
				formatString = "createdBreakpointWithOffset"; //$NON-NLS-1$
			}
			else
			{
				formatString = "createdBreakpoint"; //$NON-NLS-1$
			}
			out(getLocalizationManager().getLocalizedTextString(formatString, args));

			// worked so add it to our tracking state
			propertyPut(BPNUM, which);
		}
		catch(ParseException pe)
		{
			err(pe.getMessage());
		}
		catch(AmbiguousException ae)
		{
			err(ae.getMessage());
		}
		catch(NoMatchException nme)
		{
			// We couldn't find a function name or filename which matched what
			// the user entered.  Do *not* fail; instead, just save this breakpoint
			// away, and later, as more ABCs get loaded from the SWF, we may be
			// able to resolve this breakpoint.
			BreakAction b = addUnresolvedBreakpoint(arg);

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("breakpointNumber", Integer.toString(b.getId())); //$NON-NLS-1$
			out(getLocalizationManager().getLocalizedTextString("breakpointCreatedButNotYetResolved", args)); //$NON-NLS-1$

			// add it to our tracking state
			propertyPut(BPNUM, b.getId());
		}
		catch(NullPointerException npe)
		{
			String filename;
			try
			{
				filename = m_fileInfo.getFile(module).getName() + "#" + module; //$NON-NLS-1$
			}
			catch (Exception e)
			{
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("fileNumber", Integer.toString(module)); //$NON-NLS-1$
				filename = getLocalizationManager().getLocalizedTextString("fileNumber", args); //$NON-NLS-1$
			}

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("filename", filename); //$NON-NLS-1$
			args.put("line", Integer.toString(line)); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("breakpointNotSetNoCode", args)); //$NON-NLS-1$
		}
	}

	/**
	 * Clear a breakpoint
	 */
	void doClear() throws NotConnectedException
	{
        int module = propertyGet(LIST_MODULE);
		int line = propertyGet(LIST_LINE);
        String arg = null;

		/* wait a bit if we are not halted */
		waitTilHalted();

		/* currentXXX may NOT be invalid! */
		try
		{
			if (hasMoreTokens())
			{
                arg = nextToken();
                int[] result = parseLocationArg(module, line, arg);
                module = result[0];
                line = result[1];
			}

			// map the breakpoint to location and then delete it
			removeBreakpoint(module, line);
		}
		catch(ParseException pe)
		{
			err(pe.getMessage());
		}
		catch(NoMatchException nme)
		{
			if (removeUnresolvedBreakpoint(arg) == null)
				err(getLocalizationManager().getLocalizedTextString("breakpointLocationUnknown")); //$NON-NLS-1$
		}
		catch(AmbiguousException ae)
		{
			err(ae.getMessage());
		}
		catch(ArrayIndexOutOfBoundsException aio)
		{
			// means no breakpoint at this location
			err(getLocalizationManager().getLocalizedTextString("breakpointLocationUnknown")); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("breakpointNotCleared")); //$NON-NLS-1$
		}
	}

	/**
	 * Remove the breakpoint from our table and then determine
	 */
	BreakAction removeBreakpoint(int fileId, int line) throws ArrayIndexOutOfBoundsException, NotConnectedException
	{
		int at = breakpointIndexOf(fileId, line);
		return removeBreakpointAt(at);
	}

	BreakAction removeUnresolvedBreakpoint(String unresolvedLocation) throws NotConnectedException
	{
		int size = breakpointCount();
		for(int i=0; i<size; i++)
		{
			BreakAction b = breakpointAt(i);
			String s = b.getBreakpointExpression();
			if (s != null && s.equals(unresolvedLocation))
				return removeBreakpointAt(i);
		}
		return null;
	}

	BreakAction removeBreakpointAt(int at) throws ArrayIndexOutOfBoundsException, NotConnectedException
	{
		BreakAction a = breakpointAt(at);
		m_breakpoints.removeElementAt(at);
		if (a.getStatus() == BreakAction.RESOLVED)
			breakDisableRequest(a.getLocations());
		return a;
	}

    /**
     * Attempt to create new breakpoint at the given file and line. It will be set
     * @param fileId source file identifier
     * @param line line number
     * @return object associated with breakpoint
     */
    BreakAction addBreakpoint(int fileId, int line) throws NotConnectedException, NullPointerException
    {
		// use fileId SourceFile to denote the name of file in which we wish to set a breakpoint
        SourceFile f = m_fileInfo.getFile(fileId);
		LocationCollection col = enableBreak(f, line);

        BreakAction b = new BreakAction(col);  //  throws NullPointerException if collection is null
        b.setEnabled(true);
		b.setSingleSwf(m_fileInfo.isSwfFilterOn());
        breakpointAdd(b);
        return b;
    }
	
	/**
	 * Create a new, *unresolved* breakpoint.  Unresolved means we weren't able to
	 * parse the location string, presumably because the filename to which it refers
	 * has not yet been loaded.
	 * @param unresolvedLocation the breakpoint location, exactly as typed by the user
	 * @return object associated with breakpoint
	 */
	private BreakAction addUnresolvedBreakpoint(String unresolvedLocation)
	{
		BreakAction b = new BreakAction(unresolvedLocation);
		b.setEnabled(true);
		b.setSingleSwf(m_fileInfo.isSwfFilterOn());
		breakpointAdd(b);
		return b;
	}

	/**
	 * Try to resolve any breakpoints which have not yet been resolved.  We
	 * do this every time a new ABC or SWF is loaded.  NOTE: The return
	 * value does NOT indicate whether any breakpoints were resolved!  Rather,
	 * it indicates whether the operation was considered "successful."
	 * If a previously-unresolved breakpoint is now ambiguous, then that is
	 * an error, and the return value is 'false' (indicating that the
	 * debugger should halt).
	 */
	private boolean resolveBreakpoints(StringBuilder sb)
	{
		int count = breakpointCount();
		boolean success = true;
		for (int i=0; i<count; ++i)
		{
			BreakAction b = breakpointAt(i);
			try
			{
				tryResolveBreakpoint(b, sb);
			}
			catch (Exception e) // AmbiguousException or NullPointerException
			{
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("breakpointNumber", Integer.toString(b.getId())); //$NON-NLS-1$
				args.put("expression", b.getBreakpointExpression()); //$NON-NLS-1$
				sb.append(getLocalizationManager().getLocalizedTextString("attemptingToResolve", args)); //$NON-NLS-1$
				sb.append(m_newline);
				sb.append(e.getMessage());
				sb.append(m_newline);
				success = false;
			}
		}
		return success;
	}

	/**
	 * Try to resolve one breakpoint.  We do this every time a new ABC or
	 * SWF is loaded.
	 * @param b the breakpoint to resolve (it's okay if it's already resolved)
	 * @param sb a StringBuilder to which any messages for are appended;
	 * 			to the user.
	 * @return true if the breakpoint is resolved
	 * @throws AmbiguousException
	 * @throws NullPointerException 
	 */
	private boolean tryResolveBreakpoint(BreakAction b, StringBuilder sb) throws AmbiguousException
	{
		int status = b.getStatus();
		boolean resolved = (status == BreakAction.RESOLVED);
		if (status == BreakAction.UNRESOLVED) // we don't do anything for RESOLVED or AMBIGUOUS
		{
			/* wait a bit if we are not halted */
			try
			{
				waitTilHalted();

				// First we check for the case where this breakpoint already has a
				// filename and line number, because those were determined during a
				// previous session, but then the user did a "kill".
				//
				// If this fails, then the "else" clause deals with the case where
				// the user typed in an expression for which we have not yet found
				// a filename and line number.
		    	if (enableBreakpoint(b, b.isAutoDisable(), b.isAutoDelete()))
		    	{
		    		resolved = true;
		    	}
		    	else
		    	{
			        int module = propertyGet(LIST_MODULE);
					int line = propertyGet(LIST_LINE);

			        String arg = b.getBreakpointExpression();

			        if (arg != null)
			        {
			            int[] result = parseLocationArg(module, line, arg);
		
						// whoo-hoo, it resolved!
		                module = result[0];
		                line = result[1];
		
						// use module SourceFile to denote the name of file in which we wish to set a breakpoint
				        SourceFile f = m_fileInfo.getFile(module);
						LocationCollection col = enableBreak(f, line);
						if (col.isEmpty())
							throw new NullPointerException(getLocalizationManager().getLocalizedTextString("noExecutableCode")); //$NON-NLS-1$
						b.setLocations(col);
		
						Location l = col.first();
						SourceFile file = (l != null) ? l.getFile() : null;
						String funcName = (file == null) ? null : file.getFunctionNameForLine(m_session, l.getLine()) ;

						Map<String, Object> args = new HashMap<String, Object>();
						String formatString;
						args.put("breakpointNumber", Integer.toString(b.getId())); //$NON-NLS-1$
						String filename = file.getName();
						if (b.isSingleSwf() && file != null)
						{
							filename = filename + "#" + file.getId(); //$NON-NLS-1$
						}
						args.put("file", filename); //$NON-NLS-1$
						args.put("line", new Integer(l.getLine())); //$NON-NLS-1$

						if (funcName != null)
						{
							args.put("functionName", funcName); //$NON-NLS-1$
							formatString = "resolvedBreakpointToFunction"; //$NON-NLS-1$
						}
						else
						{
							formatString = "resolvedBreakpointToFile"; //$NON-NLS-1$
						}

						sb.append(getLocalizationManager().getLocalizedTextString(formatString, args));
						sb.append(m_newline);
						sb.append(m_newline);
		
						resolved = true;
			        }
		    	}
			}
			catch (NotConnectedException e)
			{
				// Ignore
			}
			catch (NoMatchException e)
			{
				// Okay, it's still not resolved; do nothing
			}
			catch (ParseException e)
			{
				// this shouldn't happen
				if (Trace.error)
					Trace.trace(e.toString());
			}
			catch (AmbiguousException e)
			{
				b.setStatus(BreakAction.AMBIGUOUS);
				throw e; // rethrow
			}
			catch (NullPointerException e)
			{
				b.setStatus(BreakAction.NOCODE);
				throw e; // rethrow
			}
		}
		return resolved;
	}
	
	/**
	 * Enable a breakpoint using the SourceFile as a template
	 * for the source file in which the breakpoint should be 
	 * set.
	 */
	LocationCollection enableBreak(SourceFile f, int line) throws NotConnectedException
	{
        LocationCollection col = new LocationCollection();
		boolean singleSwfBreakpoint = m_fileInfo.isSwfFilterOn();
		SwfInfo swf = m_fileInfo.getSwfFilter();

        // If we have a swf filter enabled then we only want to
        // set a breakpoint in a specific swf not all of them
		try
		{
			if (singleSwfBreakpoint)
			{
				Location l = findAndEnableBreak(swf, f, line);
				col.add(l);
			}
			else
			{
				// walk all swfs looking to add this breakpoint
				SwfInfo[] swfs = m_fileInfo.getSwfs();
				for(int i=0; i<swfs.length; i++)
				{
					swf = swfs[i];
					if (swf != null)
					{
						Location l = findAndEnableBreak(swf, f, line);
						if (l != null)
							col.add(l);
					}
				}
			}
		}
		catch(InProgressException ipe)
		{
			if (Trace.error)
				Trace.trace( ( (swf==null)?"SWF ":swf.getUrl() )+" still loading, breakpoint at "+f.getName()+":"+line+" not set"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		return col;
	}

	/**
	 * Enable a breakpoint for a particular swf if the sourceFile 
	 * is available in that swf.
	 * @param swf if null, then set the breakpoint in the given source file
	 *			otherwise try to locate a matching source file in given swf.
	 * @return null if the swf does not contain this source file
	 */
	Location findAndEnableBreak(SwfInfo swf, SourceFile f, int line) throws NotConnectedException, InProgressException
	{
		int fileId = f.getId();
		if (swf != null)
		{
			SourceFile sameFile = m_fileInfo.similarFileInSwf(swf, f);
			if (sameFile != null)
				fileId = sameFile.getId();
			else 
				fileId = -1;
		}

		Location l = (fileId > -1) ? breakEnableRequest(fileId, line) : null;
		return l;
	}

	/**
	 * Received when a breakpoint has been removed (or disabled)
	 */
	Location breakEnableRequest(int fileId, int line) throws NotConnectedException
	{
		Location l = null;
		try
		{
			l = m_session.setBreakpoint(fileId, line);
		}
		catch(NoResponseException nre)
		{
			/**
			 * This could be that we have an old player which does not
			 * respond to this request, or that we have a new player and
			 * the location was not set.
			 */
		}
		return l;
	}

	/**
	 * Notification that a breakpoint has been removed (or disabled)
	 * at the CLI level and we may need to remove it at the session level
	 */
	void breakDisableRequest(LocationCollection col) throws NotConnectedException
	{
		// now let's comb the table looking to see if this breakpoint should
		// be removed at the session level.  Use the first entry as a template
		// for which location we are talking about.
		int at = 0;
		boolean hit = false;
		Location l = col.first();
		do
		{
		    at = breakpointIndexOf(l, at);
			if (at > -1)
			{
				if (breakpointAt(at).isEnabled())
					hit = true;
				else
					at++; // our location match is not enabled but let's continue after the hit
			}
		}
		while(at > -1 && !hit);

		// no one matches, so let's remove it at the session level
		if (!hit)
		{
			Iterator<Location> itr = col.iterator();
			while(itr.hasNext())
			{
				l = itr.next();
				try { m_session.clearBreakpoint(l); } catch(NoResponseException nre) {}
			}
		}
	}

	BreakAction breakpointAt(int at)			{ return m_breakpoints.elementAt(at); 	}
	boolean		breakpointAdd(BreakAction a)	{ return m_breakpoints.add(a); 	}
	int			breakpointCount()				{ return m_breakpoints.size(); 	}

	/**
	 * Probe the table looking for the first breakpoint that
	 * matches our criteria.  Various permutations of the call are supported.
	 */
	int breakpointIndexOf(int fileId, int line)			{ return breakpointIndexOf(fileId, line, 0, true); }
	int breakpointIndexOf(Location l, int start)		{ return breakpointIndexOf(l.getFile().getId(), l.getLine(), start, true); }
	int enabledBreakpointIndexOf(Location l)			{ return breakpointIndexOf(l.getFile().getId(), l.getLine(), 0, false); }

	int breakpointIndexOf(int fileId, int line, int start, boolean includeDisabled)
	{
		int size = breakpointCount();
		int hit = -1;
		for(int i=start; (hit<0) && (i<size) ; i++)
		{
			BreakAction b = breakpointAt(i);
			if (b.locationMatches(fileId, line) && (includeDisabled || b.isEnabled()) )
				hit = i;
		}
		return hit;
	}

	// probe by identifier
	int breakpointIndexOf(int id)
	{
		int size = breakpointCount();
		int hit = -1;

		for(int i=0; (hit<0) && (i<size) ; i++)
		{
			BreakAction b = breakpointAt(i);
			if (b.getId() == id)
				hit = i;
		}
		return hit;
	}

	// access to display
	DisplayAction		displayAt(int at)				{ return m_displays.get(at); 	}
	boolean				displayAdd(DisplayAction a)		{ return m_displays.add(a); 	}
	void				displayRemoveAt(int at)			{ m_displays.remove(at); 	}
	int					displayCount()					{ return m_displays.size(); 	}

	// probe by id
	int displayIndexOf(int id)
	{
		int size = displayCount();
		int hit = -1;

		for(int i=0; (hit<0) && (i<size) ; i++)
		{
			DisplayAction b = displayAt(i);
			if (b.getId() == id)
				hit = i;
		}
		return hit;
	}

	void doSet() throws NotConnectedException
	{
		/* wait a bit if we are not halted */
//		waitTilHalted();
		try
		{
			ValueExp exp = null;

			if (!hasMoreTokens())
				err(getLocalizationManager().getLocalizedTextString("setCommand")); //$NON-NLS-1$
			else
			{
				// pull the expression
				String s = restOfLine();

				// parse and eval which causes the assignment to occur...
				if ( (exp = parseExpression(s)) == null )
					;  // failed parse

				// make sure contains assignment

				else if ( !exp.containsAssignment() )
					throw new IllegalAccessException("="); //$NON-NLS-1$

				else
					evalExpression(exp);
			}
		}
		catch(IllegalAccessException iae)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("operator", iae.getMessage()); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("missingOperator", args)); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("couldNotEvaluate")); //$NON-NLS-1$
		}
	}

	void doPrint() throws NotConnectedException
	{
		/* wait a bit if we are not halted */
//		waitTilHalted();
		try
		{
			Object result = null;
			boolean isLookupMembers = false;

			if (!hasMoreTokens())
			{
				try
				{
					// attempt to get the last result
					result = m_exprCache.get("$"); //$NON-NLS-1$
				}
				catch(ArrayIndexOutOfBoundsException aib)
				{
					err(getLocalizationManager().getLocalizedTextString("commandHistoryIsEmpty")); //$NON-NLS-1$
					throw new NullPointerException();
				}
			}
			else
			{
				// pull the rest of the line
				String s = restOfLine();

				// first parse it, then attempt to evaluate the expression
				ValueExp expr = parseExpression(s);

				// make sure no assignment
				if ( expr.containsAssignment() )
					throw new IllegalAccessException();

				result = evalExpression(expr).value;
				isLookupMembers = expr.isLookupMembers();
			}

			/* it worked, add it to the list */
			int which = m_exprCache.add(result);

			/* dump the output */
			StringBuilder sb = new StringBuilder();
			sb.append('$');
			sb.append(which);
			sb.append(" = "); //$NON-NLS-1$

			if (result instanceof Variable)
				result = ((Variable)result).getValue();

			if (result instanceof InternalProperty)
				sb.append( ((InternalProperty)result).valueOf() );
			else if (isLookupMembers)
				sb.append(result);
			else
				ExpressionCache.appendVariableValue(sb, result);

			out( sb.toString() );

			m_repeatLine = m_currentLine;
		}
		catch(ArrayIndexOutOfBoundsException aio)
		{
			// $n not in range 0..size
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("number", aio.getMessage()); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("historyHasNotReached", args)); //$NON-NLS-1$
		}
		catch(IllegalAccessException iae)
		{
			err(getLocalizationManager().getLocalizedTextString("noSideEffectsAllowed")); //$NON-NLS-1$
		}
		catch(NoSuchVariableException nsv)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("variable", nsv.getMessage()); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("variableUnknown", args)); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("couldNotEvaluate")); //$NON-NLS-1$
		}
	}

	/* parse the given string and produce an error message as appropriate */
	ValueExp parseExpression(String s)
	{
		ValueExp expr = null;
		try
		{
			expr = m_exprCache.parse(s);
		}
		catch(ParseException pe)
		{
			// bad operation code
			err(getLocalizationManager().getLocalizedTextString("expressionCouldNotBeParsed") + " " + pe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch(IOException io)
		{
			// thrown from parser
			err(getLocalizationManager().getLocalizedTextString("expressionCouldNotBeParsed") + " " + s); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return expr;
	}

	/*
	 * Evaluate the given expression
	 */
	EvaluationResult evalExpression(ValueExp expr) { return evalExpression(expr, true); }

	EvaluationResult evalExpression(ValueExp expr, boolean displayExceptions)
	{
		/* now we go off and evaluate the expression */
		EvaluationResult result = null;
		try
		{
			result = m_exprCache.evaluate(expr);
		}
		catch(NoSuchVariableException nsv)
		{
			if (displayExceptions)
			{
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("variable", nsv.getMessage()); //$NON-NLS-1$
				err(getLocalizationManager().getLocalizedTextString("variableUnknown", args)); //$NON-NLS-1$
			}
		}
		catch(NumberFormatException nfe)
		{
			if (displayExceptions)
			{
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("value", nfe.getMessage()); //$NON-NLS-1$
				err(getLocalizationManager().getLocalizedTextString("couldNotConvertToNumber", args)); //$NON-NLS-1$
			}
		}
		catch(PlayerFaultException pfe)
		{
			if (displayExceptions)
				err(pfe.getMessage());
		}
		catch (PlayerDebugException e)
		{
			if (displayExceptions)
				err(e.getMessage());
		}

		return result;
	}

	/**
	 * Specialized dump of the contents of a movie clip tree, dumping
	 * all the _target properties of all MCs
	 * @throws NoResponseException 
	 * @throws NotSuspendedException 
	 */
	void doMcTree() throws NotConnectedException, NotSuspendedException, NoResponseException
	{
		/* wait a bit if we are not halted */
		waitTilHalted();
		try
		{
			String var = nextToken();  // our variable reference
			String member = "_target"; //$NON-NLS-1$
			boolean printPath = false;
			Object result = null;
			String name = null;

			// did the user specify a member name
			if (hasMoreTokens())
			{
				member = nextToken();

				// did they specify some other options
				while(hasMoreTokens())
				{
					String option = nextToken();
					if (option.equalsIgnoreCase("fullpath")) //$NON-NLS-1$
						printPath = true;
				}
			}

			// first parse it, then attempt to evaluate the expression
			ValueExp expr = parseExpression(var);
			result = evalExpression(expr).value;

			StringBuilder sb = new StringBuilder();

			if (result instanceof Variable)
			{
				name = ((Variable)result).getName();
				result = ((Variable)result).getValue();
			}

			// It worked an should now be a value that we can traverse looking for member properties

			if (result instanceof Value)
			{
				ArrayList<Object> e = new ArrayList<Object>();
				dumpTree(new HashMap<Object, String>(), e, name, (Value)result, member);

				// now sort according to our criteria
				treeResults(sb, e, member, printPath);
			}
			else
				throw new NoSuchVariableException(result);

			out( sb.toString() );
		}
		catch(NoSuchVariableException nsv)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("variable", nsv.getMessage()); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("variableUnknown", args)); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("couldNotEvaluate")); //$NON-NLS-1$
		}
	}

	/**
	 * Set the context from which info files
	 * and all other file releated commands
	 * will operate from.
	 *
	 * It no swf is given then we use the
	 * default mode which is to display all
	 * files from all swfs.  Files with identical
	 * names are only displayed once.
	 */
	void doViewSwf()
	{
		try
		{
			if (hasMoreTokens())
			{
				String swfName = nextToken();
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("swf", swfName); //$NON-NLS-1$
				if (m_fileInfo.setSwfFilter(swfName))
				{
					out(getLocalizationManager().getLocalizedTextString("commandsLimitedToSpecifiedSwf", args)); //$NON-NLS-1$
				}
				else
				{
					err(getLocalizationManager().getLocalizedTextString("notValidSwf", args)); //$NON-NLS-1$
				}
			}
			else
			{
				m_fileInfo.setSwfFilter(null);
				out(getLocalizationManager().getLocalizedTextString("commandsApplyToAllSwfs")); //$NON-NLS-1$
			}
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("noActiveSession")); //$NON-NLS-1$
		}
	}

	/**
	 * Increment the frame context by 1 and display the new current frame.
	 */
	void doUp() throws PlayerDebugException
	{
		int num = propertyGet(DISPLAY_FRAME_NUMBER) + 1;
		try
		{
			propertyPut(DISPLAY_FRAME_NUMBER, num);

			dumpFrame(num);
			setListingToFrame(num);
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("noActiveSession")); //$NON-NLS-1$
		}
        catch(ArrayIndexOutOfBoundsException aie)
        {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("frameNumber", Integer.toString(num)); //$NON-NLS-1$
            err(getLocalizationManager().getLocalizedTextString("frameDoesNotExist", args)); //$NON-NLS-1$
        }
		m_repeatLine = m_currentLine;
	}

	/**
	 * Decrement the frame context by 1 and display the new current frame.
	 */
	void doDown() throws PlayerDebugException
	{
		int num = propertyGet(DISPLAY_FRAME_NUMBER) - 1;
		try
		{
			propertyPut(DISPLAY_FRAME_NUMBER, num);

			dumpFrame(num);
			setListingToFrame(num);
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("noActiveSession")); //$NON-NLS-1$
		}
        catch(ArrayIndexOutOfBoundsException aie)
        {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("frameNumber", Integer.toString(num)); //$NON-NLS-1$
            err(getLocalizationManager().getLocalizedTextString("frameDoesNotExist", args)); //$NON-NLS-1$
        }
		m_repeatLine = m_currentLine;
	}

	/**
	 * Set the frame context to the given number and display the new current frame.
	 */
	void doFrame() throws PlayerDebugException
	{
		int num = 0;  // frame 0 by default 
		try
		{
			if (hasMoreTokens())
				num = nextIntToken();

			propertyPut(DISPLAY_FRAME_NUMBER, num);

			dumpFrame(num);
			setListingToFrame(num);
		}
		catch(NumberFormatException nfe)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("token", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("notANumber", args)); //$NON-NLS-1$
		}
        catch(ArrayIndexOutOfBoundsException aie)
        {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("frameNumber", Integer.toString(num)); //$NON-NLS-1$
            err(getLocalizationManager().getLocalizedTextString("frameDoesNotExist", args)); //$NON-NLS-1$
        }
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("noActiveSession")); //$NON-NLS-1$
		}
	}

	// Displays information on the current frame
    // @throws ArrayIndexOutOfBoundsException if frame 'frm' doesn't exist
	void dumpFrame(int frm) throws PlayerDebugException, ArrayIndexOutOfBoundsException
	{
		StringBuilder sb = new StringBuilder();
		Frame[] ar = m_session.getFrames();
		appendFrameInfo(sb, ar[frm], frm, false, true);

		sb.append(m_newline);
		out(sb.toString());
	}

	// set the listing command to point to the file/line of the given frame
	void setListingToFrame(int frameNum) throws PlayerDebugException
	{
		// set the module and line
		Frame[] frames = m_session.getFrames();
		Frame ctx = frames[frameNum];

		Location l = ctx.getLocation();
		SourceFile f = l.getFile();
		int id = f.getId();
		int line = l.getLine();

        setListingPosition(id, line);
    }

    // Set teh listing position to change to the given module and line number
    // also triggers emacs to move to this position if enabled
    void setListingPosition(int module, int line)
    {
		propertyPut(LIST_MODULE, module);
		propertyPut(LIST_LINE, line);

        // if we are running under emacs then dump out our new location
        if (m_fullnameOption)
        {
            SourceFile f = m_fileInfo.getFile(module);
            if (f != null)
            {
                StringBuilder sb = new StringBuilder();
                appendFullnamePosition(sb, f, line);
                sb.append('\n'); // not sure why this is needed but it seems to address some emacs bugs
                out(sb.toString());
            }
        }
	}

	/**
	 * Traverse the given variables dumping any Movieclips we find that
	 * contain a member called 'member'
	 * @throws NotConnectedException 
	 * @throws NoResponseException 
	 * @throws NotSuspendedException 
	 */
	void dumpTree(Map<Object, String> tree, List<Object> e, String name, Value result, String member) throws NotSuspendedException, NoResponseException, NotConnectedException
	{
		// name for this variable
		if (name == null)
			name = ""; //$NON-NLS-1$

		// have we seen it already
		if (tree.containsKey(result))
			return;

		tree.put(result, name);  // place it

		// first iterate over our members looking for 'member'
		Value proto = result;
		boolean done = false;
		while(!done && proto != null)
		{
			Variable[] members = proto.getMembers(m_session);
			proto = null;

			// see if we find one called 'member'
			for(int i=0; i<members.length; i++)
			{
				Variable m = members[i];
				String memName = m.getName();
				if (memName.equals(member) && !tree.containsKey(m))
				{
					e.add(name);
					e.add(result);
					e.add(m);
					tree.put(m, name+"."+memName); //$NON-NLS-1$
					done = true;
				}
				else if (memName.equals("__proto__")) //$NON-NLS-1$
					proto = members[i].getValue();
			}
		}

		// now traverse other mcs recursively
		done = false;
		proto = result;
		while(!done && proto != null)
		{
			Variable[] members = proto.getMembers(m_session);
			proto = null;

			// see if we find an mc
			for(int i=0; i<members.length; i++)
			{
				Variable m = members[i];
				String memName = m.getName();

				// if our type is NOT object or movieclip then we are done
				if (m.getValue().getType() != VariableType.OBJECT && m.getValue().getType() != VariableType.MOVIECLIP)
					;
				else if (m.getValue().getId() != Value.UNKNOWN_ID)
					dumpTree(tree, e, name, m.getValue(), member);
				else if (memName.equals("__proto__")) //$NON-NLS-1$
				{
					proto = m.getValue();
//					name = name + ".__proto__";
				}
			}
		}
	}

	StringBuilder treeResults(StringBuilder sb, List<Object> e, String memName, boolean fullName)
	{
		// walk the list
		Iterator<Object> i = e.iterator();
		while(i.hasNext())
		{
			String name = (String) i.next();
			Variable key = (Variable) i.next();
			Variable val = (Variable)i.next();

//			sb.append(key);
//			sb.append(".");
//			sb.append(val.getName());
			if (fullName)
				sb.append(name);
			ExpressionCache.appendVariableValue(sb, key.getValue(), key.getName());
			sb.append("."); //$NON-NLS-1$
			sb.append(memName);
			sb.append(" = "); //$NON-NLS-1$
			ExpressionCache.appendVariableValue(sb, val.getValue(), val.getName());
			sb.append(m_newline);
		}
		return sb;
	}

	/**
	 * Output a source line of code to the output channel formatting nicely
	 */
	public void outputSource(int module, int line, String s)
	{
		StringBuilder sb = new StringBuilder();
		appendSource(sb, module, line, s, true);
		out( sb.toString() );
	}

	void appendSource(StringBuilder sb, int module, int line, String s, boolean markCurrent)
	{
		String lineS = String.valueOf(line);
		int padding = 6 - lineS.length();

		// if we are the current location then mark it
		if (markCurrent && isCurrentLocation(module, line))
			sb.append('=');
		else
			sb.append(' ');
		sb.append(lineS);
		repeat(sb, ' ', padding);
		sb.append(s);
	}

	// see if this module, line combo is the current location
	boolean isCurrentLocation(int module, int line)
	{
		boolean yes = false;
		Location l = getCurrentLocation();
		if (l != null)
		{
			SourceFile file = l.getFile();
			if (file != null && file.getId() == module && l.getLine() == line)
				yes = true;
		}
		return yes;
	}

    private int parseLineNumber(String lineNumber) throws ParseException
    {
        try
        {
            return Integer.parseInt(lineNumber);
        }
        catch(NumberFormatException nfe)
        {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("token", lineNumber); //$NON-NLS-1$
        	throw new ParseException(getLocalizationManager().getLocalizedTextString("expectedLineNumber", args), 0); //$NON-NLS-1$
        }
    }

    private int parseFileNumber(String fileNumber) throws ParseException
    {
        try
        {
            return Integer.parseInt(fileNumber);
        }
        catch(NumberFormatException nfe)
        {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("token", fileNumber); //$NON-NLS-1$
        	throw new ParseException(getLocalizationManager().getLocalizedTextString("expectedFileNumber", args), 0); //$NON-NLS-1$
        }
    }

    private int parseFileName(String partialFileName) throws NoMatchException, AmbiguousException
    {
        SourceFile[] sourceFiles = m_fileInfo.getFiles(partialFileName);
        int nSourceFiles = sourceFiles.length;

        if (nSourceFiles == 0)
        {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("name", partialFileName); //$NON-NLS-1$
            throw new NoMatchException(getLocalizationManager().getLocalizedTextString("noSourceFileWithSpecifiedName", args)); //$NON-NLS-1$
        }

        else if (nSourceFiles > 1)
        {
            String s = getLocalizationManager().getLocalizedTextString("ambiguousMatchingFilenames") + m_newline; //$NON-NLS-1$
            for (int i = 0; i < nSourceFiles; i++)
            {
                SourceFile sourceFile = sourceFiles[i];
                s += " " + sourceFile.getName() + "#" + sourceFile.getId(); //$NON-NLS-1$ //$NON-NLS-2$
                if (i < nSourceFiles - 1)
                    s += m_newline;
            }
            throw new AmbiguousException(s);
        }
        return sourceFiles[0].getId();
    }

	/* used by parseFunctionName */
	private static class ModuleFunctionPair implements Comparable<ModuleFunctionPair>
	{
		public ModuleFunctionPair(int moduleId, String functionName) {
			this.moduleId = moduleId;
			this.functionName = functionName;
		}
		public int moduleId;
		public String functionName;

		public int compareTo(ModuleFunctionPair other) {
			return functionName.compareTo(other.functionName);
		}
	}

	/**
	 * Parse a partial function name
	 * @param module the FIRST module to search; but we also search all the others if 'onlyThisModule' is false
	 * @return two ints: first is the module, and second is the line
	 */
    private int[] parseFunctionName(int module, String partialFunctionName, boolean onlyThisModule) throws NoMatchException, AmbiguousException
    {
        try { waitForMetaData(); } catch(InProgressException ipe) {}  // wait a bit before we try this to give the background thread time to complete

        SourceFile m = m_fileInfo.getFile(module);
		ArrayList<ModuleFunctionPair> functionNames = new ArrayList<ModuleFunctionPair>(); // each member is a ModuleFunctionPair

        appendFunctionNamesMatching(functionNames, m, partialFunctionName);

        if (functionNames.size() == 0)
        {
			if (!onlyThisModule)
			{
				// not found in the specified module; search all the other modules
				Iterator fileIter = m_fileInfo.getAllFiles();
				while (fileIter.hasNext())
				{
					SourceFile nextFile = (SourceFile) ((Map.Entry)fileIter.next()).getValue();
					if (nextFile != m) // skip the one file we searched at the beginning
					{
						appendFunctionNamesMatching(functionNames, nextFile, partialFunctionName);
					}
				}
			}

			if (functionNames.size() == 0)
			{
	        	Map<String, Object> args = new HashMap<String, Object>();
	        	args.put("name", partialFunctionName); //$NON-NLS-1$
	            throw new NoMatchException(getLocalizationManager().getLocalizedTextString("noFunctionWithSpecifiedName", args)); //$NON-NLS-1$
			}
        }

		if (functionNames.size() > 1)
        {
			ModuleFunctionPair[] functionNameArray = functionNames.toArray( new ModuleFunctionPair[functionNames.size()] );
			Arrays.sort(functionNameArray);

            String s = getLocalizationManager().getLocalizedTextString("ambiguousMatchingFunctionNames") + m_newline; //$NON-NLS-1$
            Map<String, Object> args = new HashMap<String, Object>();
            for (int i = 0; i < functionNameArray.length; i++)
            {
				String moduleName = m_fileInfo.getFile(functionNameArray[i].moduleId).getName();
                String functionName = functionNameArray[i].functionName;
                args.put("functionName", functionName); //$NON-NLS-1$
                args.put("filename", moduleName + "#" + functionNameArray[i].moduleId); //$NON-NLS-1$ //$NON-NLS-2$
                s += " " + getLocalizationManager().getLocalizedTextString("functionInFile", args); //$NON-NLS-1$ //$NON-NLS-2$
                if (i < functionNameArray.length - 1)
                    s += m_newline;
            }
            throw new AmbiguousException(s);
        }

		ModuleFunctionPair pair = functionNames.get(0);
		module = pair.moduleId;
		m = m_fileInfo.getFile(module);
		int line = m.getLineForFunctionName(m_session, pair.functionName);
        return new int[] { module, line };
     }

    /**
     * Find function names in this module that start with
     * the specified string, and append them to the specified List.
	 *
	 * If partialName contains parenthesis then we look for an exact match
     */
	private void appendFunctionNamesMatching(List<ModuleFunctionPair> functionNameList, SourceFile m, String partialName)
	{
		int exactHitAt = -1;

		// trim off the trailing parenthesis, if any
		int parenAt = partialName.lastIndexOf('(');
		if (parenAt > -1)
			partialName = partialName.substring(0, parenAt);

		String[] names = m.getFunctionNames(m_session);
		for(int i=0; i<names.length; i++)
        {
            String functionName = names[i];
            if (functionName.equals(partialName))
			{
				exactHitAt = i;
				break;
			}
			else if (functionName.startsWith(partialName))
                functionNameList.add(new ModuleFunctionPair(m.getId(), functionName));
        }

		// exact match?
		if (exactHitAt > -1)
		{
			functionNameList.clear();
			functionNameList.add(new ModuleFunctionPair(m.getId(), names[exactHitAt]));
		}
	}


    /**
      * Parse arg to determine which file it specifies.
      * Allowed formats: #29, MyApp.mxml, MyA
      * A variety of exceptions are thrown for other formats.
      */
    public int parseFileArg(int module, String arg) throws ParseException, AmbiguousException, NoMatchException
    {
        /* Special case: a location arg like :15 produces a file arg
           which is an empty string. */
        if (arg.length() == 0)
            return module;

        char firstChar = arg.charAt(0);

        /* The first character can't be 0-9 or '-'. */
        if (Character.isDigit(firstChar) || firstChar == '-')
        {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("token", arg); //$NON-NLS-1$
        	throw new ParseException(getLocalizationManager().getLocalizedTextString("expectedFile", args), 0); //$NON-NLS-1$
        }
        /* If the first character is '#', the rest must be a file number. */
        else if (firstChar == '#')
        {
            return parseFileNumber(arg.substring(1));
        }
        /* Otherwise, assume beforeColon is a full or partial file name. */
        else
        {
            return parseFileName(arg);
        }
    }

    /**
      * Parse arg to determine which line it specifies.
      * Allowed formats: 17, MyFunction, MyF
      * A variety of exceptions are thrown for other formats.
      */
    public int parseLineArg(int module, String arg) throws ParseException, AmbiguousException, NoMatchException
    {
        /* Special case: a location arg like #29: produces a line arg
           which is an empty string. */
         if (arg.length() == 0)
            return 1;

        char firstChar = arg.charAt(0);

        /* If the first character is 0-9 or '-', arg is assumed to be a line number. */
        if (Character.isDigit(firstChar) || firstChar == '-')
        {
            return parseLineNumber(arg);
        }
        /* The first character can't be '#'. */
        else if (firstChar == '#')
        {
        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("token", arg); //$NON-NLS-1$
        	throw new ParseException(getLocalizationManager().getLocalizedTextString("expectedLineNumber", args), 0); //$NON-NLS-1$
        }
        /* Otherwise, assume arg is a full or partial function name. */
        else
        {
            int[] moduleAndLine = parseFunctionName(module, arg, true);
			return moduleAndLine[1];
        }
    }

    /**
     * Parse arg to figure out what module and line it specifies.
     *
     * Allowed formats (assuming Button.as is file #29
     * and the first line of MyFunction is line 17):
     *
     *  arg                     module      line
     *  17                      no change   17
     *  MyFunction              no change   17
     *  MyF                     no change   17
     *  #29                     29          1
     *  Button.as               29          1
     *  Bu                      29          1
     *  #29:17                  29          17
     *  #29:MyFunction          29          17
     *  #29:MyF                 29          17
     *  Button.as:17            29          17
     *  Button.as:MyFunction    29          17
     *  Button.as:MyF           29          17
     *  Bu:17                   29          17
     *  Bu:MyFunction           29          17
     *  Bu:MyF                  29          17
     *
     * A variety of exceptions are thrown for other formats.
     */
    public int[] parseLocationArg(int module, int line, String arg) throws ParseException, AmbiguousException, NoMatchException
    {
        int colonAt = arg.indexOf(':');
		int wasFunc = 0;  // set to 1 if a function was named

        /* First deal with the case where arg doesn't contain a ':'
           and therefore might be specifying either a file or a line. */
        if (colonAt < 0)
        {
            char firstChar = arg.charAt(0);

            /* If the first character is 0-9 or '-', arg is assumed to be a line number. */
            if (Character.isDigit(firstChar) || firstChar == '-')
            {
                line = parseLineNumber(arg);
            }
            /* If the first character is a '#', what follows
               is assumed to be a file number. */
            else if (firstChar == '#')
            {
                module = parseFileNumber(arg.substring(1));
                line = 1;
            }
            /* Otherwise, assume arg is a full or partial function name or file name. */
            else
            {
                /* Assume arg is a full or partial function in the specified module. */
                try
                {
                    int[] moduleAndLine = parseFunctionName(module, arg, false);
					module = moduleAndLine[0];
					line = moduleAndLine[1];
					wasFunc = 1;
                }
                /* If it isn't, assume arg is a full or partial file name. */
                catch(NoMatchException pe)
                {
					try
					{
						module = parseFileName(arg);
						line = 1;
					}
					catch(NoMatchException pee)
					{
						// catch the 'file name' string
						Map<String, Object> args = new HashMap<String, Object>();
						args.put("token", arg); //$NON-NLS-1$
						throw new NoMatchException(getLocalizationManager().getLocalizedTextString("noSuchFileOrFunction", args)); //$NON-NLS-1$
					}
                }
            }
        }

        /* Now deal with the case where arg contains a ':',
           and is therefore specifying both a file and a line. */
        else
        {
            module = parseFileArg(module, arg.substring(0, colonAt));
            line = parseLineArg(module, arg.substring(colonAt + 1));
			wasFunc = (arg.substring(colonAt+1).length() > 1 && Character.isDigit(arg.substring(colonAt+1).charAt(0)) ) ? 0 : 1;
        }

        return new int[] { module, line, wasFunc };
    }

	/**
	 * Print the context of a Variable
	 */
	void doWhat() throws NotConnectedException
	{
		/* wait a bit if we are not halted */
		waitTilHalted();
		try
		{
			Object result = null;

			/* pull the rest of the line */
			String s = restOfLine();

			// first parse it, then attempt to evaluate the expression
			ValueExp expr = parseExpression(s);

			// make sure no assignment
			if ( expr.containsAssignment() )
				throw new IllegalAccessException();

			result = evalExpression(expr).value;

			/* dump the output */
			StringBuilder sb = new StringBuilder();

			if (result instanceof Variable)
			{
				Variable v = (Variable) result;

				// if it has a path then display it!
				if (v.isAttributeSet(VariableAttribute.IS_LOCAL))
					s = getLocalizationManager().getLocalizedTextString("localVariable"); //$NON-NLS-1$
				else if (v.isAttributeSet(VariableAttribute.IS_ARGUMENT))
					s = getLocalizationManager().getLocalizedTextString("functionArgumentVariable"); //$NON-NLS-1$
				else if ( (v instanceof VariableFacade) && (s = ((VariableFacade)v).getPath()) != null && s.length() > 0 )
					;
				else
					s = "_global"; //$NON-NLS-1$

				sb.append(s);
			}
			else
				sb.append(getLocalizationManager().getLocalizedTextString("mustBeOnlyOneVariable")); //$NON-NLS-1$

			out( sb.toString() );
		}
		catch(IllegalAccessException iae)
		{
			err(getLocalizationManager().getLocalizedTextString("noSideEffectsAllowed")); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("couldNotEvaluate")); //$NON-NLS-1$
		}
	}

    /*
     * We accept zero, one or two args for this command. Zero args
     * means list the next 10 line around the previous listing.  One argument
     * specifies a line and 10 lines are listed around that line.  Two arguments
     * with a command between specifies starting and ending lines.
     */
	void doList()
	{
        /* currentXXX may NOT be invalid! */

		int currentModule = propertyGet(LIST_MODULE);
        int currentLine = propertyGet(LIST_LINE);
        int listsize = propertyGet(LIST_SIZE);

        String arg1 = null;
        int module1 = currentModule;
        int line1 = currentLine;

        String arg2 = null;
        int line2 = currentLine;

        int numLines = 0;

		try
		{
            if (hasMoreTokens())
			{
                arg1 = nextToken();

                if (arg1.equals("-")) //$NON-NLS-1$
                {
					// move back two times the listing size and if listsize is odd then move forward one
                    line1 = line2 = line1 - (2 * listsize);
                }
                else
                {
                    int[] result = parseLocationArg(currentModule, currentLine, arg1);
                    module1 = result[0];
                    line2 = line1 = result[1];

                    if (hasMoreTokens())
                    {
                        arg2 = nextToken();
                        line2 = parseLineArg(module1, arg2);
                    }
                }
			}

//			System.out.println("1="+module1+":"+line1+",2=:"+line2);

			/**
			 * Check for a few error conditions, otherwise we'll write a listing!
			 */
			if (hasMoreTokens())
			{
				err(getLocalizationManager().getLocalizedTextString("lineJunk")); //$NON-NLS-1$
			}
			else
			{
				int half = listsize/2;
				SourceFile file = m_fileInfo.getFile(module1);
				numLines = file.getLineCount();

				int newLine;
				if (numLines == 1 && file.getLine(1).equals("")) //$NON-NLS-1$
				{
					// there's no source in the file at all!
					// this presumably means that the source file isn't in the current directory
					err(getLocalizationManager().getLocalizedTextString("sourceFileNotFound")); //$NON-NLS-1$
					newLine = currentLine;
				}
				else
				{
					// pressing return is ok, otherwise throw the exception
					if (line1 > numLines && arg1 != null)
						throw new IndexOutOfBoundsException();
	
					/* if no arg2 then user requested the next N lines around something */
					if (arg2 == null)
					{
						line2 = line1 + (half) - 1;
						line1 = line1 - (listsize-half);
					}

					/* adjust our range of lines to ensure we conform */
					if (line1 < 1)
					{
						/* shrink line 1, grow line2 */
						line2 += -(line1 - 1);
						line1 = 1;
					}
	
					if (line2 > numLines)
						line2 = numLines;
	
//				    System.out.println("1="+module1+":"+line1+",2="+module2+":"+line2+",num="+numLines+",half="+half);
	
					/* nothing to display */
					if (line1 > line2)
						throw new IndexOutOfBoundsException();
	
					/* now do it! */
					SourceFile source = m_fileInfo.getFile(module1);
					for(int i=line1; i<=line2; i++)
						outputSource(module1, i, source.getLine(i));
					
					newLine = line2 + half + (((listsize % 2) == 0) ? 1 : 2);  // add one if even, 2 for odd;
				}
				
				/* save away valid context */
				propertyPut(LIST_MODULE, module1);
				propertyPut(LIST_LINE, newLine);
				m_repeatLine = "list";   /* allow repeated listing by typing CR */ //$NON-NLS-1$
			}
		}
        catch(IndexOutOfBoundsException iob)
		{
			String name = "#"+module1; //$NON-NLS-1$
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("line", Integer.toString(line1)); //$NON-NLS-1$
			args.put("filename", name); //$NON-NLS-1$
			args.put("total", Integer.toString(numLines)); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("lineNumberOutOfRange", args)); //$NON-NLS-1$
		}
		catch(AmbiguousException ae)
		{
			err(ae.getMessage());
		}
		catch(NoMatchException nme)
		{
			// TODO [mmorearty]: try to find a matching source file
			err(nme.getMessage());
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("noFilesFound")); //$NON-NLS-1$
		}
		catch(ParseException pe)
		{
			err(pe.getMessage());
		}
	}

	/**
	 * Fire up a session or await a connection from the socket if no
	 * URI was specified.
	 */
	void doRun() throws IOException
	{
		if (m_session != null)
		{
			err(getLocalizationManager().getLocalizedTextString("sessionInProgress")); //$NON-NLS-1$
			return;
		}

		SessionManager mgr = Bootstrap.sessionManager();

		if (hasMoreTokens())
		{
			if (!setLaunchURI(restOfLine()))
				return;
		}

		mgr.startListening();

		try
		{
			if (m_launchURI == null)
			{
				out(getLocalizationManager().getLocalizedTextString("waitingForPlayerToConnect")); //$NON-NLS-1$
				m_session = mgr.accept(null);
			}
			else
			{
				out(getLocalizationManager().getLocalizedTextString("launchingWithUrl") + m_newline + m_launchURI); //$NON-NLS-1$
				m_session = mgr.launch(m_launchURI, null, true, null, null);
			}

			// now see what happened
			if (m_session == null)
			{
				// shouldn't have gotten here
				throw new SocketTimeoutException();
			}
			else
			{
				out(getLocalizationManager().getLocalizedTextString("playerConnectedSessionStarting")); //$NON-NLS-1$
				initSession(m_session);

				// pause for a while during startup, don't let exceptions ripple outwards
				try { waitTilHalted(); } catch(Exception e) {}

				// pause for a while during startup, don't let exceptions ripple outwards
				try { waitForMetaData(); } catch(Exception e) {}

				setInitialSourceFile();

				out(getLocalizationManager().getLocalizedTextString("setBreakpointsThenResume")); //$NON-NLS-1$

				// now poke to see if the player is good enough
				try
				{
					if (m_session.getPreference(SessionManager.PLAYER_SUPPORTS_GET) == 0 )
						err(m_newline + getLocalizationManager().getLocalizedTextString("warningNotAllCommandsSupported")); //$NON-NLS-1$
				}
				catch(Exception npe) {}
			}
		}
		catch (FileNotFoundException fnf)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("uri", fnf.getLocalizedMessage()); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("fileDoesNotExist", args)); //$NON-NLS-1$
		}
		catch (SocketTimeoutException ste)
		{
			err(getLocalizationManager().getLocalizedTextString("failedToConnect")); //$NON-NLS-1$
		}
		catch (IOException io)
		{
			err(io.getLocalizedMessage());
		}
		finally
		{
			// turn off listening, to allow other session to connect
			mgr.stopListening();
		}
	}

	/**
	 * When we begin a debugging session, it would be nice if the default file
	 * for the "list" command etc. was the user's main MXML application file.
	 * There is no good way to really figure out what that file is, but we can
	 * certainly take a guess.
	 */
	private void setInitialSourceFile()
	{
		int largestAuthoredId = -1;
		SourceFile[] files = m_fileInfo.getFileList();
		for (int i=0; i<files.length; ++i)
		{
			SourceFile sf = files[i];
			if (sf.getId() > largestAuthoredId && getFileType(sf) == AUTHORED_FILE)
				largestAuthoredId = sf.getId();
		}
		if (largestAuthoredId != -1)
			setListingPosition(largestAuthoredId, 1);
	}

	private boolean setLaunchURI(String launchURI)
	{
		if (launchURI != null)
		{
			SessionManager mgr = Bootstrap.sessionManager();

			// If doing fdbunit, we always try to do launch(), even on platforms
			// that say they don't support it
			if (!mgr.supportsLaunch() && System.getProperty("fdbunit") == null) //$NON-NLS-1$
			{
				err(getLocalizationManager().getLocalizedTextString("manuallyLaunchPlayer")); //$NON-NLS-1$
				return false;
			}

			// check for special form of URI when in fullname mode, since we can't pass http: in this mode?!?
			if (m_fullnameOption)
			{
				if (launchURI.startsWith("//")) //$NON-NLS-1$
					launchURI = "http:"+launchURI; //$NON-NLS-1$
			}
		}

		m_launchURI = launchURI;
		return true;
	}

	// set the URI
	void doFile()
	{
		if (!hasMoreTokens())
			setLaunchURI(null);
		else
			setLaunchURI(restOfLine());
	}

	void doSource()
	{
		String name = ""; //$NON-NLS-1$
		try
		{
			name = nextToken();
			FileReader f = new FileReader(name);

			// push our current source onto the stack and open the new one
			pushStream(m_in);
			m_in = new LineNumberReader(f);
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("sourceCommandRequiresPath")); //$NON-NLS-1$
		}
		catch(NoSuchElementException nse)
		{
			err(getLocalizationManager().getLocalizedTextString("sourceCommandRequiresPath")); //$NON-NLS-1$
		}
		catch(FileNotFoundException fnf)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("filename", name); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("fileNotFound", args)); //$NON-NLS-1$
		}
	}

	void listFault(String f)
	{
		StringBuilder sb = new StringBuilder();
		appendFaultTitles(sb);
		appendFault(sb, f);

		out( sb.toString() );
	}

	void appendFault(StringBuilder sb, String f)
	{
		sb.append(f);

		int space = 30 - f.length();
		repeat(sb, ' ', space);

		boolean stop = m_faultTable.is(f, "stop"); //$NON-NLS-1$
		boolean print = m_faultTable.is(f, "print"); //$NON-NLS-1$

		sb.append( stop ? "Yes" : "No" );
		repeat(sb, ' ', stop ? 0 : 1);

		repeat(sb, ' ', 5);

		sb.append( print ? "Yes" : "No" );
		repeat(sb, ' ', print ? 0 : 1);

		// description
		repeat(sb, ' ', 7);

		String desc = m_faultTable.getDescription(f);
		sb.append(desc);
		sb.append(m_newline);
	}

	void appendFaultTitles(StringBuilder sb)
	{
		sb.append("Fault                         Stop    Print     Description"+m_newline);
		sb.append("-----                         ----    -----     -----------"+m_newline);
	}

	/**
	 * Controls the configuration of what occurs when a
	 * fault is encountered
	 */
	void doHandle()
	{
		// should be at least on arg
		if (!hasMoreTokens())
			err(getLocalizationManager().getLocalizedTextString("argumentRequired")); //$NON-NLS-1$
		else
		{
			// poor man's fix for supporting 'all' option
			String faultName = nextToken();
			Object[] names = new Object[] { faultName };

			// replace the single name with all of them
			if (faultName.equalsIgnoreCase("all")) //$NON-NLS-1$
				names = m_faultTable.names();

			// make sure we know about at least one
		    if (!m_faultTable.exists((String)names[0]))
				err(getLocalizationManager().getLocalizedTextString("unrecognizedFault")); //$NON-NLS-1$
			else
			{
				if (!hasMoreTokens())
					listFault((String)names[0]);
				else
				{
					String action = null;
					try
					{
						while(hasMoreTokens())
						{
							action = nextToken();
							for(int i=0; i<names.length; i++)
								m_faultTable.action((String)names[i], action);
						}
					}
					catch(IllegalArgumentException iae)
					{
						Map<String, Object> args = new HashMap<String, Object>();
						args.put("action", action); //$NON-NLS-1$
						err(getLocalizationManager().getLocalizedTextString("unrecognizedAction", args)); //$NON-NLS-1$
					}
				}
			}
		}
	}

	/**
	 * Do the commands command!  This attaches a series of lines of text input by the user
	 * to a particular breakpoint, with the intention of exeuting these lines when the
	 * breakpoint is hit.
	 */
	void doCommands() throws IOException
	{
		try
		{
			int id = -1;
			if (hasMoreTokens())
				id = nextIntToken();
			else
				id = propertyGet(BPNUM);

			// get the breakpoint
			int at = breakpointIndexOf(id);
			BreakAction a = breakpointAt(at);

			// ready it
			a.clearCommands();
			a.setSilent(false);

			// now just pull the commands as they come while not end
			String line = null;
			boolean first = true;
			boolean isEnd = false;

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("breakpointNumber", Integer.toString(id)); //$NON-NLS-1$
			out(getLocalizationManager().getLocalizedTextString("typeCommandsForBreakpoint", args)); //$NON-NLS-1$

			do
			{
				displayCommandPrompt();
				line = readLine().trim();
			    isEnd = line.equalsIgnoreCase("end"); //$NON-NLS-1$

				if (!isEnd)
				{
					if (first && line.equalsIgnoreCase("silent")) //$NON-NLS-1$
						a.setSilent(true);
					else
						a.addCommand(line);
				}
				first = false;
			}
			while(!isEnd);
		}
		catch(IndexOutOfBoundsException iob)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("breakpointNumber", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("noBreakpointNumber", args)); //$NON-NLS-1$
		}
		catch(NumberFormatException nfe)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("token", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("badBreakpointNumber", args)); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("commandFailed")); //$NON-NLS-1$
		}
	}

	/**
	 * Apply or remove conditions to a breakpoint.
	 */
	void doCondition() throws IOException
	{
		try
		{
			// must have a breakpoint number
			int id = nextIntToken();

			// get the breakpoint
			int at = breakpointIndexOf(id);
			BreakAction a = breakpointAt(at);

			// no more parms means to clear it
			if (hasMoreTokens())
			{
				// now just pull the commands as they come while not end
				String line = restOfLine();

				// build an expression and attach it to the breakpoint
				ValueExp exp = parseExpression(line);

				// warn about the assignment!
				if ( exp.containsAssignment() && !yesNoQuery(getLocalizationManager().getLocalizedTextString("askExpressionContainsAssignment")) ) //$NON-NLS-1$
					throw new IllegalAccessException("="); //$NON-NLS-1$

				a.setCondition(exp, line);
			}
			else
			{
				a.clearCondition();   // clear it
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("breakpointNumber", Integer.toString(id)); //$NON-NLS-1$
				out(getLocalizationManager().getLocalizedTextString("breakpointNowUnconditional", args)); //$NON-NLS-1$
			}
		}
		catch(IllegalAccessException iae)
		{
			err(getLocalizationManager().getLocalizedTextString("breakpointNotChanged")); //$NON-NLS-1$
		}
		catch(IndexOutOfBoundsException iob)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("breakpointNumber", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("noBreakpointNumber", args)); //$NON-NLS-1$
		}
		catch(NumberFormatException nfe)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("token", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("badBreakpointNumber", args)); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("commandFailed")); //$NON-NLS-1$
		}
	}

	/**
	 * Request to add a new watchpoint
	 * This may result in one of two things happening
	 * (1) a new watchpoint could be added or
	 * (2) an existing watchpoint may be modified.
	 *
	 * The watch, awatch, and rwatch commands will set a watchpoint on the
	 * given expression. The different commands control the read/write aspect
	 * of the watchpoint.
	 *
	 * awatch will trigger a break if the expression is read or written.
	 * rwatch will trigger a break if the expression is read.
	 * watch will trigger a break if the expression is written.
	 */
	void doWatch(boolean read, boolean write) throws PlayerDebugException
	{
		try
		{
			if (read)
			{
				err("Only break-on-write watchpoints are supported.");
				return;
			}

			StringBuilder sb = new StringBuilder();

			/* pull the rest of the line */
			String s = restOfLine();

			int flags = 3;
			if(read && write) flags = WatchKind.READWRITE;
			else if(read) flags = WatchKind.READ;
			else if(write) flags = WatchKind.WRITE;

			// snapshot of our existing list
			Watch[] list = m_session.getWatchList();

			// We need to separate the front part the 'a.b' in 'a.b.c' 
			// of the expression to resolve it into a variable 
			// We usually get back a VariableFacade which contains
			// the context id (i.e the variable id) and the member name.
			ValueExp expr = parseExpression(s);
			VariableFacade result = (VariableFacade)(evalExpression(expr).value);

			// extract the 2 pieces and get the raw variable.
			int varId = result.getContext(); // TODO fix this???  -mike
			String memberName = result.getName();
			Value v = m_session.getValue(varId);

			// attempt to set.
			Watch w = m_session.setWatch(v, memberName, flags);
			if (w == null)
			{
				// failed
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("expression", s); //$NON-NLS-1$
				err(getLocalizationManager().getLocalizedTextString("watchpointCouldNotBeSet", args)); //$NON-NLS-1$
			}
			else
			{
				// if modified then lists are same length
				// otherwise 1 will be added
				Watch[] newList = m_session.getWatchList();
				if (newList.length == list.length)
				{
					// modified, lets locate the one that changed
					// and reset it
					int at = missingWatchpointIndexOf(newList);
					WatchAction a = null;
					try	
					{ 
						a = watchpointAt(at); 
					} 
					catch(ArrayIndexOutOfBoundsException aio)
					{
						// this is pretty bad it means the player thinks we have a watchpoint
						// but we don't have a record of it.  So let's create a new one
						// and hope that we are now in sync with the player.
						a = new WatchAction(w);
					}

					// modify our view of the watchpoint
					int id = a.getId();
					a.resetWatch(w);

					Map<String, Object> args = new HashMap<String, Object>();
					args.put("watchpointNumber", Integer.toString(id)); //$NON-NLS-1$
					args.put("expression", s); //$NON-NLS-1$
					args.put("watchpointMode", getWatchpointModeString(a.getKind())); //$NON-NLS-1$
					sb.append(getLocalizationManager().getLocalizedTextString("changedWatchpointMode", args)); //$NON-NLS-1$
				}
				else
				{
					// newly added
					WatchAction a = new WatchAction(w);
					watchpointAdd(a);

					int which = a.getId();
					Map<String, Object> args = new HashMap<String, Object>();
					args.put("watchpointNumber", Integer.toString(which)); //$NON-NLS-1$
					args.put("expression", s); //$NON-NLS-1$
					sb.append(getLocalizationManager().getLocalizedTextString("createdWatchpoint", args)); //$NON-NLS-1$
				}
				out(sb.toString());
			}
		}
		catch(ArrayIndexOutOfBoundsException aio)
		{
			// We should really do some cleanup after this exception
			// since it most likely means we can't find the watchpoint
			// that was just modified, therefore our watchlists are
			// out of sync with those of the API.
			err(getLocalizationManager().getLocalizedTextString("badWatchpointNumber")); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("couldNotEvaluate")); //$NON-NLS-1$
		}
		catch(ClassCastException cce)
		{
			err(getLocalizationManager().getLocalizedTextString("couldNotResolveExpression")); //$NON-NLS-1$
		}
	}

	WatchAction watchpointAt(int at)			{ return m_watchpoints.get(at); }
	boolean		watchpointAdd(WatchAction a)	{ return m_watchpoints.add(a); 	}
	int			watchpointCount()				{ return m_watchpoints.size(); 	}

	int watchpointIndexOf(int id)
	{
		int size = watchpointCount();
		for(int i = 0; i < size; i++)
		{
			WatchAction b = watchpointAt(i);
			if(b.getId() == id)
				return i;
		}

		return -1;
	}

	void removeAllWatchpoints() throws NotConnectedException
	{
		while(watchpointCount() > 0)
			removeWatchpointAt(0);
	}

	void removeWatchpointAt(int at) throws NotConnectedException
	{
		WatchAction b = watchpointAt(at);
		boolean worked = false;

		try { worked = (m_session.clearWatch(b.getWatch()) == null) ? false : true; } catch(NoResponseException nre) {}

		if(!worked)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("variable", b.getExpr() ); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("couldNotFindWatchpoint", args)); //$NON-NLS-1$
		}

		// remove in any event
		m_watchpoints.remove(at);
	}

	String getWatchpointModeString(int flags) 
	{
		switch(flags) 
		{
			case 1:
				return getLocalizationManager().getLocalizedTextString("watchpointMode_read"); //$NON-NLS-1$
			case 2:
				return getLocalizationManager().getLocalizedTextString("watchpointMode_write"); //$NON-NLS-1$
			case 3:
				return getLocalizationManager().getLocalizedTextString("watchpointMode_readWrite"); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Locate the index of a WatchAction that does not
	 * have a corresponding Watch in the given list. 
	 * 
	 * WARNING: this call can be very expensive but
	 * it is assumed that a.list and watchpointCount()
	 * are both small. 
	 */
	int missingWatchpointIndexOf(Watch[] a)
	{
		int size = watchpointCount();
		int at = -1;
		for(int i=0; i<size && at<0; i++)
		{
			WatchAction action = watchpointAt(i);
			Watch w = action.getWatch();
			
			// now scan the list of watches looking for a hit
			int hit = -1;
			for(int j=0; j<a.length && hit<0; j++)
			{
				if (w == a[j])
					hit = j;
			}

			// can't find the watch object corresponding to our
			// watchpoint in list of session watches.
			if (hit < 0)
				at = i;
		}

		return at;
	}

	/**
	 * Display command
	 */
	void doDisplay()
	{
		try
		{
			if (!hasMoreTokens())
				doInfoDisplay();
			else
			{
				// followed by an expression (i.e. a line we just pull in)
				String s = restOfLine();

				// first parse it, then attempt to evaluate the expression
				ValueExp expr = parseExpression(s);

				// make sure no assignment
				if ( expr.containsAssignment() )
					throw new IllegalAccessException();

				// it worked so create a new DisplayAction and then add it in

				DisplayAction b = new DisplayAction(expr, s);
				b.setEnabled(true);
				displayAdd(b);
			}
		}
		catch(IllegalAccessException iae)
		{
			err(getLocalizationManager().getLocalizedTextString("noSideEffectsAllowed")); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			// already handled by parseExpression
		}
	}

	/**
	 * Remove auto-display expressions
	 */
	void doUnDisplay() throws IOException
	{
		try
		{
			if (!hasMoreTokens())
			{
				// no args means delete all displays, last chance...
				if (yesNoQuery(getLocalizationManager().getLocalizedTextString("askDeleteAllAutoDisplay"))) //$NON-NLS-1$
				{
					int count = displayCount();
					for(int i=count-1; i>-1; i--)
					{
						displayRemoveAt(i);
					}
				}
			}
			else
			{
				while(hasMoreTokens())
				{
					int id = nextIntToken();
					int at = displayIndexOf(id);
					displayRemoveAt(at);
				}
			}
		}
		catch(IndexOutOfBoundsException iob)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("displayNumber", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("noDisplayNumber", args)); //$NON-NLS-1$
		}
		catch(NumberFormatException nfe)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("token", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("badDisplayNumber", args)); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("commandFailed")); //$NON-NLS-1$
		}
	}

	/**
	 * Enabled breakpoints and disaplays
	 */
	void doDisable() throws AmbiguousException, NotConnectedException
	{
		waitTilHalted();

		try
		{
			if (!hasMoreTokens())
			{
				// disables all breakpoints,
				int count = breakpointCount();
				for(int i=0; i<count; i++)
					disableBreakpointAt(i);
			}
			else
			{
				// optionally specify  'display' or 'breakpoint'
				String arg = nextToken();
				int cmd = disableCommandFor(arg);
				int id = -1;
				if (cmd == CMD_DISPLAY)
					doDisableDisplay();
				else
				{
					if (cmd == CMD_BREAK)
						id = nextIntToken();  // ignore and get next number token
					else
						id = Integer.parseInt(arg);

					do
					{
						int at = breakpointIndexOf(id);
						disableBreakpointAt(at);

						if (hasMoreTokens())
							id = nextIntToken();
						else
							id = -1;

						// keep going till we're blue in the face; also note that we cache'd a copy of locations
						// so that breakpoint numbers are consistent.
					}
					while( id > -1 );
				}
			}
		}
		catch(IndexOutOfBoundsException iob)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("breakpointNumber", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("noBreakpointNumber", args)); //$NON-NLS-1$
		}
		catch(NumberFormatException nfe)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("token", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("badBreakpointNumber", args)); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("commandFailed")); //$NON-NLS-1$
		}
	}

	// disable a breakpoint
	void disableBreakpointAt(int at) throws NotConnectedException
	{
		BreakAction a = breakpointAt(at);
		a.setEnabled(false);
		breakDisableRequest(a.getLocations());
	}

	void doDisableDisplay() { doEnableDisableDisplay(false); }

	void doEnableDisableDisplay(boolean enable)
	{
		try
		{
			if (!hasMoreTokens())
			{
				// means do all!
				int size = displayCount();
				for(int i=0; i<size; i++)
					displayAt(i).setEnabled(enable);
			}
			else
			{
				// read ids until no more
				while( (hasMoreTokens()) )
				{
					int id = nextIntToken();
					int at = displayIndexOf(id);
					DisplayAction a = displayAt(at);
					a.setEnabled(enable);
				}
			}
		}
		catch(IndexOutOfBoundsException iob)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("displayNumber", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("noDisplayNumber", args)); //$NON-NLS-1$
		}
		catch(NumberFormatException nfe)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("token", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("badDisplayNumber", args)); //$NON-NLS-1$
		}
	}

	/**
	 * Enables breakpoints (forever, one-shot hit or auto delete) and displays
	 */
	void doEnable() throws AmbiguousException, NotConnectedException
	{
		waitTilHalted();

		try
		{
			if (!hasMoreTokens())
			{
				// enables all breakpoints
				int count = breakpointCount();
				int tally = 0;
				for(int i=0; i<count; i++)
					tally += enableBreakpointAt(i) ? 1 : 0;
				
				// mention that not all was good
				if (tally != count)
					err(getLocalizationManager().getLocalizedTextString("notAllBreakpointsEnabled")); //$NON-NLS-1$
			}
			else
			{
				// optionally specify  'display' or 'breakpoint'
				String arg = nextToken();
				int cmd = enableCommandFor(arg);
				int id = -1;
				boolean autoDelete = false;
				boolean autoDisable = false;
				if (cmd == CMD_DISPLAY)
					doEnableDisplay();
				else
				{
					if (cmd == CMD_BREAK)
						id = nextIntToken();  // ignore and get next number token
					else if (cmd == CMD_DELETE)
					{
						autoDelete = true;
						id = nextIntToken();  // set and get next number token
					}
					else if (cmd == ENABLE_ONCE_CMD)
					{
						autoDisable = true;
						id = nextIntToken();  // set and get next number token
					}
					else
						id = Integer.parseInt(arg);

					boolean worked = true;
					do
					{
						int at = breakpointIndexOf(id);
						worked = enableBreakpointAt(at, autoDisable, autoDelete);

						if (hasMoreTokens())
							id = nextIntToken();
						else
							id = -1;

						// keep going till we're blue in the face; also note that we cache'd a copy of locations
						// so that breakpoint numbers are consistent.
					}
					while( worked && id > -1 );

					if (!worked)
					{
						Map<String, Object> args = new HashMap<String, Object>();
						args.put("breakpointNumber", Integer.toString(id)); //$NON-NLS-1$
						err(getLocalizationManager().getLocalizedTextString("breakpointLocationNoLongerExists", args)); //$NON-NLS-1$
					}
				}
			}
		}
		catch(IndexOutOfBoundsException iob)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("breakpointNumber", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("noBreakpointNumber", args)); //$NON-NLS-1$
		}
		catch(NumberFormatException nfe)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("token", m_currentToken); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("badBreakpointNumber", args)); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("commandFailed")); //$NON-NLS-1$
		}
	}

	// request to enable a breakpoint
	// @return false if we couldn't enable it.
	boolean enableBreakpointAt(int at) throws NotConnectedException { return enableBreakpointAt(at, false, false); }

	boolean enableBreakpointAt(int at, boolean autoDisable, boolean autoDelete) throws NotConnectedException
	{
		return enableBreakpoint(breakpointAt(at), autoDisable, autoDelete);
	}
	
	boolean enableBreakpoint(BreakAction a, boolean autoDisable, boolean autoDelete) throws NotConnectedException
	{
		boolean retval = false;
		Location l = a.getLocation();   // use the first location as a source file / line number template 
		if (l != null)
		{
			LocationCollection col = enableBreak(l.getFile(), l.getLine());
			if (!col.isEmpty())
			{
				a.setEnabled(true);
				a.setLocations(col);
				a.setAutoDisable(autoDisable);
				a.setAutoDelete(autoDelete);
				a.setSingleSwf(false);
				a.setStatus(BreakAction.RESOLVED);
				retval = true;
			}
		}
		return retval;
	}

	void doEnableDisplay() { doEnableDisableDisplay(true); }

    /* Print working directory */
    void doPWD()
    {
        out(System.getProperty("user.dir")); //$NON-NLS-1$
    }

    /* Display or change current file */
    void doCF()
    {
        try
        {
            int module = propertyGet(LIST_MODULE);
			int currentLine = propertyGet(LIST_LINE);

            if (hasMoreTokens())
            {
                String arg = nextToken();
                module = parseFileArg(module, arg);
				currentLine = 1;
                setListingPosition(module, currentLine);
             }

            SourceFile sourceFile = m_fileInfo.getFile(module);
			StringBuilder sb = new StringBuilder();
			sb.append(sourceFile.getName());
			sb.append('#');
			sb.append(sourceFile.getId());
			sb.append(':');
			sb.append(currentLine);
			out( sb.toString() );
        }
		catch(NullPointerException npe)
		{
			err(getLocalizationManager().getLocalizedTextString("noFilesFound")); //$NON-NLS-1$
		}
		catch(ParseException pe)
		{
			err(pe.getMessage());
		}
		catch(AmbiguousException ae)
		{
			err(ae.getMessage());
		}
		catch(NoMatchException nme)
		{
			err(nme.getMessage());
		}
    }

 	/* Terminates current debugging sesssion */
	void doKill() throws IOException
	{
		if (m_session == null)
			err(getLocalizationManager().getLocalizedTextString("programNotBeingRun")); //$NON-NLS-1$
		else
		{
			if (yesNoQuery(getLocalizationManager().getLocalizedTextString("askKillProgram"))) //$NON-NLS-1$
				exitSession();
		}
	}

	/* Terminates fdb */
	boolean doQuit() throws IOException
	{
		boolean quit = false;

		// no session, no questions
		if (m_session == null)
			quit = true;
		else
		{
			quit = yesNoQuery(getLocalizationManager().getLocalizedTextString("askProgramIsRunningExitAnyway")); //$NON-NLS-1$
			if (quit)
				exitSession();
		}
		return quit;
	}

	/* (non-Javadoc)
	 * @see flash.tools.debugger.SourceLocator#locateSource(java.lang.String, java.lang.String, java.lang.String)
	 */
	public InputStream locateSource(String path, String pkg, String name)
	{
		File f = null;
		boolean exists = false;
		String pkgPlusName;

		if ((pkg != null && pkg.length() > 0))
			pkgPlusName = pkg + File.separator + name;
		else
			pkgPlusName = null;

		Iterator<String> iter = m_sourceDirectories.iterator();
		while (iter.hasNext())
		{
			String dir = iter.next();

			// new File("", filename) searches the root dir -- that's not what we want!
			if (dir.equals("")) //$NON-NLS-1$
				dir = "."; //$NON-NLS-1$

			// look for sourcedir\package\filename
			if (pkgPlusName != null)
			{
				f = new File(dir, pkgPlusName);
				exists = f.exists();
			}

			// look for sourcedir\filename
			if (!exists)
			{
				f = new File(dir, name);
				exists = f.exists();
			}

			if (exists)
			{
				try
				{
					return new FileInputStream(f);
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace(); // shouldn't happen
				}
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see flash.tools.debugger.SourceLocator#getChangeCount()
	 */
	public int getChangeCount()
	{
		return m_sourceDirectoriesChangeCount;
	}

	private void doDirectory() throws IOException
	{
		if (hasMoreTokens())
		{
			// File.separator is ";" on Windows or ":" on Mac
			StringTokenizer dirs = new StringTokenizer(restOfLine(), File.pathSeparator);
			int insertPos = 0;

			while (dirs.hasMoreTokens())
			{
				String dir = dirs.nextToken();
				if (dir.length() > 2 && dir.charAt(0) == '"' && dir.charAt(dir.length()-1) == '"')
					dir = dir.substring(1, dir.length() - 1);
				dir = dir.trim();
				if (dir.length() > 0)
				{
					// For Unix and Mac, we want to escape "~" and "$HOME"
					if (!System.getProperty("os.name").toLowerCase().startsWith("windows")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						// If the string starts with "~", or contains any environment variables
						// such as "$HOME", we need to escape those
						if (dir.matches("^.*[~$].*$")) //$NON-NLS-1$
						{
							try
							{
								Process p = Runtime.getRuntime().exec(
										new String[] { "/bin/sh", "-c", "echo " + dir} ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
								String line = r.readLine();
								if (line != null)
								{
									line = line.trim();
									if (line.length() > 0)
										dir = line;
								}
							}
							catch (IOException e)
							{
								// ignore
							}
						}
					}

					try
					{
						dir = new File(dir).getCanonicalPath();
						m_sourceDirectories.add(insertPos++, dir);
					}
					catch (IOException e)
					{
						err(e.getMessage());
					}
				}
			}
			++m_sourceDirectoriesChangeCount;
		}
		else
		{
			if (yesNoQuery(getLocalizationManager().getLocalizedTextString("askReinitSourcePath"))) //$NON-NLS-1$
			{
				initSourceDirectoriesList();
			}
		}

		doShowDirectories();
	}

	protected void initSourceDirectoriesList()
	{
		m_sourceDirectories.clear();
		File flexHome = getFlexHomeDirectory();
		if (flexHome != null)
		{
			try
			{
				File projectsDir = new File(flexHome, "frameworks/projects"); //$NON-NLS-1$
				File[] files = projectsDir.listFiles();
				if (files != null)
				{
					for (int i=0; i<files.length; ++i)
					{
						if (files[i].isDirectory())
						{
							File srcDir = new File(files[i], "src"); //$NON-NLS-1$
							if (srcDir.isDirectory())
							{
								m_sourceDirectories.add(srcDir.getCanonicalPath());
							}
						}
					}
				}
			}
			catch (IOException e)
			{
				// ignore
			}
		}
		++m_sourceDirectoriesChangeCount;
	}

	/**
	 * Returns the Flex home directory.  This is based on the <code>application.home</code>
	 * Java system property if present, or the current directory otherwise.
	 * This directory is one up from the "bin" and "lib" directories.  For example,
	 * <code>&lt;flexhome&gt;/lib/fdb.jar</code> can be used to refer to the fdb jar.
	 */
	protected File getFlexHomeDirectory()
	{
		if (!m_initializedFlexHomeDirectory)
		{
			m_initializedFlexHomeDirectory = true;
			m_flexHomeDirectory = new File("."); // default in case the following logic fails //$NON-NLS-1$
			String flexHome = System.getProperty("application.home"); //$NON-NLS-1$
			if (flexHome != null && flexHome.length() > 0)
			{
				try
				{
					m_flexHomeDirectory = new File(flexHome).getCanonicalFile();
				}
				catch (IOException e)
				{
					// ignore
				}
			}
		}

		return m_flexHomeDirectory;
	}

	protected String getenv(String var)
	{
		String[] cmd;
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			cmd = new String[] { "cmd.exe", "/c", "echo", "%" + var + "%" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		else
		{
			cmd = new String[] { "/bin/sh", "-c", "echo $" + var }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		try
		{
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = r.readLine();
			if (line != null && line.length() > 0)
			{
				return line;
			}
		}
		catch (IOException e)
		{
			// ignore
		}
		
		return null;
	}

	private void doCatch() throws NotConnectedException, NotSuspendedException, NoResponseException
	{
		/* wait a bit if we are not halted */
		waitTilHalted();

        String typeToCatch = null;

		/* currentXXX may NOT be invalid! */
		if (!hasMoreTokens())
		{
			err("Catch requires an exception name.");
			return;
		}

        typeToCatch = nextToken();
        if (typeToCatch == null || typeToCatch.length() == 0)
        {
        	err("Illegal argument");
        	return;
        }

        Value type = null;
        if (typeToCatch.equals("*")) //$NON-NLS-1$
        {
        	typeToCatch = null;
        }
        else
        {
	        type = getSession().getGlobal(typeToCatch);
	        if (type == null)
	        {
	        	err("Type not found.");
	        	return;
	        }

	        String typeName = type.getTypeName();
	        int at = typeName.indexOf('@');
	        if (at != -1)
	        	typeName = typeName.substring(0, at);
	        if (!typeName.endsWith("$"))
	        {
	        	err("Not a type: " + type);
	        	return;
	        }
        }

        CatchAction c;
		try {
			c = addCatch(typeToCatch);
		} catch (NotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

    	Map<String, Object> args = new HashMap<String, Object>();
    	args.put("id", c.getId()); //$NON-NLS-1$
    	c.getId();
	}

	private CatchAction addCatch(String typeToCatch) throws NotSupportedException, NoResponseException {
		CatchAction c = new CatchAction(typeToCatch);
		catchpointAdd(c);
		return c;
	}

	CatchAction catchpointAt(int at)			{ return m_catchpoints.get(at); }
	int			catchpointCount()				{ return m_catchpoints.size(); 	}

	boolean catchpointAdd(CatchAction a) throws NotSupportedException, NoResponseException
	{
		if (catchpointCount() == 0)
			getSession().breakOnCaughtExceptions(true);

		return m_catchpoints.add(a);
	}

	int catchpointIndexOf(int id)
	{
		int size = catchpointCount();
		for(int i = 0; i < size; i++)
		{
			CatchAction c = catchpointAt(i);
			if(c.getId() == id)
				return i;
		}

		return -1;
	}

	void removeAllCatchpoints() throws NotConnectedException
	{
		while(catchpointCount() > 0)
			removeCatchpointAt(0);
	}

	void removeCatchpointAt(int at) throws NotConnectedException
	{
		// remove in any event
		m_catchpoints.remove(at);

		if (catchpointCount() == 0) {
			try {
				getSession().breakOnCaughtExceptions(false);
			} catch (NotSupportedException e) {
			} catch (NoResponseException e) {
			}
		}
	}

	void doUnknown(String s) { doUnknown("", s); } //$NON-NLS-1$

	void doUnknown(String what, String s)
	{
		Map<String, Object> args = new HashMap<String, Object>();
		String formatString;
		args.put("command", s); //$NON-NLS-1$
		if (what == null || what.equals("")) //$NON-NLS-1$
		{
			formatString = "unknownCommand"; //$NON-NLS-1$
			args.put("commandCategory", what); //$NON-NLS-1$
		}
		else
		{
			formatString = "unknownSubcommand"; //$NON-NLS-1$
		}
		err(getLocalizationManager().getLocalizedTextString(formatString, args));
	}

	/**
	 * Process the incoming debug event queue
	 */
	void processEvents() throws NotConnectedException
	{
		boolean requestResume = false;
		boolean requestHalt = m_requestHalt;

		while(m_session != null && m_session.getEventCount() > 0)
		{
			DebugEvent e = m_session.nextEvent();

			if (e instanceof TraceEvent)
			{
				dumpTraceLine(e.information);
			}
			else if (e instanceof SwfLoadedEvent)
			{
				handleSwfLoadedEvent((SwfLoadedEvent)e);
			}
			else if (e instanceof SwfUnloadedEvent)
			{
				handleSwfUnloadedEvent((SwfUnloadedEvent)e);
			}
			else if (e instanceof BreakEvent)
			{
				// we ignore these for now
			}
			else if (e instanceof FileListModifiedEvent)
			{
				// we ignore this
			}
			else if (e instanceof FunctionMetaDataAvailableEvent)
			{
				// we ignore this
			}
			else if (e instanceof FaultEvent)
			{
				if ( handleFault((FaultEvent)e) )
					requestResume = true;
				else
					requestHalt = true;
			}
			else
			{
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("type", e); //$NON-NLS-1$
				args.put("info", e.information); //$NON-NLS-1$
				err(getLocalizationManager().getLocalizedTextString("unknownEvent", args)); //$NON-NLS-1$
			}
		}

		// only if we have processed a fault which requested a resume and no other fault asked for a break
		// and we are suspended and it was due to us that the stop occurred!
		if (requestResume && !requestHalt && m_session.isSuspended() && m_session.suspendReason() == SuspendReason.Fault)
			m_requestResume = true;
	}

	/**
	 * Our logic for handling a break condition.
	 *
	 * @return some hit breakpoint requested silence, shhhh!
	 */
	boolean processBreak(boolean postStep, StringBuilder sb) throws NotConnectedException
	{
		Location l = getCurrentLocation();
		if (l == null || l.getFile() == null)
			return false;

		int fileId = l.getFile().getId();
		int line = l.getLine();
		boolean isSilent = false;
		boolean bpHit = false;
		boolean stoppedDueToBp = false;

		int count = breakpointCount();
		boolean[] markedForRemoval = new boolean[count];
		boolean previousResume = m_requestResume;
		for(int i=0; i<count; i++)
		{
			BreakAction a = breakpointAt(i);
			if (a.locationMatches(fileId, line))
			{
				/**
				 * Note that it appears that we stopped due to hitting a hard breakpoint
				 * Now if the breakpoint is conditional it may eval to false, meaning we
				 * won't stop here, otherwise we will process the breakpoint.
				 */
				stoppedDueToBp = (m_session.suspendReason() == SuspendReason.Breakpoint);
				if (shouldBreak(a, fileId, line))
				{
					// its a hit
					bpHit = true;
					a.hit();
					isSilent = (isSilent) ? true : a.isSilent();

					// autodelete, autodisable
					if (a.isAutoDisable())
						disableBreakpointAt(i);

					if (a.isAutoDelete())
						markedForRemoval[i] = true;

					// now issue any commands that are attached to the breakpoint
					int n = a.getCommandCount();
					for(int j=0 ;j<n; j++)
						issueCommand(a.commandAt(j), sb);
				}
			}
		}

		// kill them backwards so our i is acurate
		for(int i=markedForRemoval.length-1; i>-1; i--)
			if (markedForRemoval[i])
				removeBreakpointAt(i);

		/**
		 * Now we should request to resume only if it was due to
		 * breakpoints that were hit.
		 *
		 * For the first case, we hit a conditional breakpoint that
		 * eval'd to false, resulting in bpHit == false.  Thus we
		 * want to resume and additionally if we were stepping, we'd
		 * like to do so 'softly' that is without loosing the stepping
		 * information on the Player.
		 *
		 * For the 2nd case, we hit a breakpoint and we executed
		 * commands that resulted in a m_requestResume.
		 */
		if (stoppedDueToBp && !bpHit)
		{
			m_requestResume = true;
			m_stepResume = postStep;    // resume without losing our stepping
			isSilent = true;			// do so quietly
		}
		else if (stoppedDueToBp && bpHit && m_requestResume && !previousResume)
		{
			m_requestResume = true;
			m_stepResume = postStep;    // resume as we would
		    processDisplay(sb);
		}

		// If we aren't continuing, then show display variables
		if (!m_requestResume)
			processDisplay(sb);

//		System.out.println("processBreak stopDueToBp="+stoppedDueToBp+",bpHit="+bpHit+",postStep="+postStep+",reason="+suspendReason());

		return isSilent;
	}

	// iterate through our display list entries
	void processDisplay(StringBuilder sb)
	{
		int count = displayCount();
		for(int i=0;i<count; i++)
		{
			DisplayAction a = displayAt(i);
			if (a.isEnabled())
			{
				try
				{
					sb.append(a.getId());
					sb.append(": "); //$NON-NLS-1$
					sb.append(a.getContent());
					sb.append(" = "); //$NON-NLS-1$

					// command[0] contains our expression, so first we parse it, evalulate it then print it
					Object result = m_exprCache.evaluate(a.getExpression()).value;

					if (result instanceof Variable)
						ExpressionCache.appendVariableValue(sb, ((Variable)result).getValue());

					else if (result instanceof Value)
						ExpressionCache.appendVariableValue(sb, (Value) result);

					else if (result instanceof InternalProperty)
						sb.append( ((InternalProperty)result).valueOf() );

					else
						sb.append(result);

					sb.append(m_newline);
				}
				catch(NoSuchVariableException nsv)
				{
					Map<String, Object> args = new HashMap<String, Object>();
					args.put("variable", nsv.getMessage() ); //$NON-NLS-1$
					sb.append(getLocalizationManager().getLocalizedTextString("variableUnknown", args)); //$NON-NLS-1$
					sb.append(m_newline);
				}
				catch(NumberFormatException nfe)
				{
					Map<String, Object> args = new HashMap<String, Object>();
					args.put("value", nfe.getMessage() ); //$NON-NLS-1$
					sb.append(getLocalizationManager().getLocalizedTextString("couldNotConvertToNumber", args)); //$NON-NLS-1$
					sb.append(m_newline);
				}
				catch(PlayerFaultException pfe)
				{
					sb.append(pfe.getMessage() + m_newline);
				}
				catch (PlayerDebugException e)
				{
					sb.append(e.getMessage() + m_newline);
				}
				catch(NullPointerException npe)
				{
					sb.append(getLocalizationManager().getLocalizedTextString("couldNotEvaluate")); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Determines if the given BreakAction requests a halt given the file
	 * line and optionally a conditional to evaluate.'
	 */
	boolean shouldBreak(BreakAction a, int fileId, int line)
	{
		boolean should = a.isEnabled();
		ValueExp exp = a.getCondition();
		if (should && exp != null && !m_requestHalt)  // halt request fires true
		{
			// evaluate it then update our boolean
			try
			{
				EvaluationResult result = evalExpression(exp, false);
				if (result != null)
					should = ECMA.toBoolean(result.context.toValue(result.value));
			}
			catch(NullPointerException npe) {}
			catch(NumberFormatException nfe) {}
		}
		return should;
	}

	/**
	 * Sets the command interpreter up to execute the
	 * given string a  command
	 *
	 * This io redirection crap is really UGLY!!!
	 */
	void issueCommand(String cmd, StringBuilder output)
	{
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(ba);

		// temporarily re-wire i/o to catch all output
		PrintStream oldOut = m_out;
		PrintStream oldErr = m_err;

		m_out = ps;
		m_err = ps;
		try
		{
			setCurrentLine(cmd);
			processLine();
		}
		catch(AmbiguousException ae)
		{
			// we already put up a warning for the user
		}
		catch(IllegalStateException ise)
		{
			err(getLocalizationManager().getLocalizedTextString("illegalStateException")); //$NON-NLS-1$
		}
		catch(IllegalMonitorStateException ime)
		{
			err(getLocalizationManager().getLocalizedTextString("commandNotValidUntilPlayerSuspended")); //$NON-NLS-1$
		}
		catch(NoSuchElementException nse)
		{
			err(getLocalizationManager().getLocalizedTextString("noSuchElementException")); //$NON-NLS-1$
		}
		catch(SocketException se)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("socketErrorMessage", se.getMessage()); //$NON-NLS-1$
			err(getLocalizationManager().getLocalizedTextString("problemWithConnection", args)); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			err(getLocalizationManager().getLocalizedTextString("unexpectedErrorWithStackTrace")); //$NON-NLS-1$
			if (Trace.error)
				e.printStackTrace();
		}

		// flush the stream and then send its contents to our string buffer
		ps.flush();
		output.append( ba.toString() );

		m_err = oldErr;
		m_out = oldOut;
	}

	/**
	 * We have received a fault and are possibly suspended at this point.
	 * We need to look at our fault table and determine what do.
	 * @return true if we resumed execution
	 */
	boolean handleFault(FaultEvent e)
	{
		// lookup what we need to do
		boolean requestResume = false;
		String name = e.name();
		boolean stop = true;
		boolean print = true;
		try
		{
			print = m_faultTable.is(name, "print"); //$NON-NLS-1$
			stop = m_faultTable.is(name, "stop"); //$NON-NLS-1$
		}
		catch(NullPointerException npe)
		{
			if (Trace.error)
			{
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("faultName", name); //$NON-NLS-1$
				Trace.trace(getLocalizationManager().getLocalizedTextString("faultHasNoTableEntry", args)); //$NON-NLS-1$
				npe.printStackTrace();
			}
		}

		if (e instanceof ExceptionFault)
		{
			ExceptionFault ef = (ExceptionFault) e;
			Value thrownValue = ef.getThrownValue();
			if (thrownValue != null)
			{
				if (!ef.willExceptionBeCaught())
				{
					stop = true;
				}
				else
				{
					stop = false;

					String typeName = thrownValue.getTypeName();
					int at = typeName.indexOf('@');
					if (at != -1)
						typeName = typeName.substring(0, at);

					for (int i=0; i<catchpointCount(); ++i)
					{
						CatchAction c = catchpointAt(i);
						String typeToCatch = c.getTypeToCatch();
						try {
							if (typeToCatch == null || getSession().evalIs(thrownValue, typeToCatch))
							{
								stop = true;
								break;
							}
						} catch (PlayerDebugException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							stop = true;
						} catch (PlayerFaultException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							stop = true;
						}
					}

					if (!stop)
						print = false;
				}
			}
		}

		// should we stop?
		if (!stop)
			requestResume = true;

		if (print)
			dumpFaultLine(e);

		return requestResume;
	}

	// wait a little bit of time until the player halts, if not throw an exception!
	void waitTilHalted() throws NotConnectedException
	{
		if (!haveConnection())
			throw new IllegalStateException();

		int timeout = propertyGet(HALT_TIMEOUT);
		int update = propertyGet(UPDATE_DELAY);
		boolean wait = (propertyGet(NO_WAITING) == 1) ? false : true;

		if (wait)
		{
			// spin for a while waiting for a halt; updating trace messages as we get them
			waitForSuspend(timeout, update);

			if (!m_session.isSuspended())
				throw new IllegalMonitorStateException();
		}
	}

	/**
	 * We spin in this spot until the player reaches the
	 * requested suspend state, either true or false.
	 *
	 * During this time we wake up every period milliseconds
	 * and update the display and our state with information
	 * received from the debug event queue.
	 */
	void waitForSuspend(int timeout, int period) throws NotConnectedException
	{
		while(timeout > 0)
		{
			// dump our events to the console while we are waiting.
			processEvents();
			if (m_session.isSuspended())
				break;

			try { Thread.sleep(period); } catch(InterruptedException ie) {}
			timeout -= period;
		}
	}

	/**
	 * If we still have a socket try to send an exit message
	 * Doesn't seem to work ?!?
	 */
	void exitSession()
	{
		// clear out our watchpoint list and displays
		// keep breakpoints around so that we can try to reapply them if we reconnect
		m_displays.clear();
		m_watchpoints.clear();

		if (m_fileInfo != null)
			m_fileInfo.unbind();

		if (m_session != null)
			m_session.terminate();

		m_session = null;
		m_fileInfo = null;
	}

	void initSession(Session s)
	{
		s.setSourceLocator(this);

		m_fileInfo = new FileInfoCache();
		m_exprCache.clear();

		m_fileInfo.bind(s);
		m_exprCache.bind(s);

		// bind catching a version problem
		boolean correctVersion = true;
		try { s.bind(); } catch(VersionException ve) { correctVersion = false; }

		// reset session properties
		propertyPut(LIST_LINE, 1);
		propertyPut(LIST_MODULE, 1);  // default to module #1
		propertyPut(BPNUM, 0); // set current breakpoint number as something bad
		propertyPut(LAST_FRAME_DEPTH, 0);
		propertyPut(CURRENT_FRAME_DEPTH, 0);
		propertyPut(DISPLAY_FRAME_NUMBER, 0);
		propertyPut(METADATA_ATTEMPTS_PERIOD, 250); // 1/4s per attempt
		propertyPut(METADATA_NOT_AVAILABLE, 0);  // counter for failures
		propertyPut(METADATA_ATTEMPTS, METADATA_RETRIES);
		propertyPut(PLAYER_FULL_SUPPORT, correctVersion ? 1 : 0);

		String previousURI = m_mruURI;
		m_mruURI = m_session.getURI();

		// try to reapply the breakpoint's
		if (previousURI != null && m_mruURI != null && previousURI.equalsIgnoreCase(m_mruURI))
			reapplyBreakpoints();
		else
		{
			while(m_breakpoints.size() > 0)
				m_breakpoints.removeElementAt(0);
		}

		m_requestResume = false;
		m_stepResume = false;
	}

	/**
	 * Walk through the list of breakpoints and try to apply them to our session
	 * We aren't that smart in that we ignore the singleSwf property of the breakpoint
	 * meaning that if you have a breakpoint set on a single swf, it will be restored
	 * across all swfs.
	 */
	void reapplyBreakpoints()
	{
		// give us a bit of time to process the newly loaded swf
		if (propertyGet(METADATA_ATTEMPTS) > 0)
			try { waitForMetaData(80); } catch(InProgressException ipe) { }

		int count = breakpointCount();
		for(int i=0; i<count; i++)
		{
			BreakAction a = breakpointAt(i);
			a.clearHits();
			a.setStatus(BreakAction.UNRESOLVED);
		}

		StringBuilder sb = new StringBuilder();
		resolveBreakpoints(sb);
		out(sb.toString());
	}

	/**
	 * Process a single line of input and return true if the quit command was encountered
	 */
	boolean processLine() throws IOException, AmbiguousException, PlayerDebugException
	{
		if (!hasMoreTokens())
			return false;

		String command = nextToken();
		boolean quit = false;
		int cmdID = commandFor(command);

		/* assume line will not be repeated. (i.e. user hits CR nothing happens) */
		m_repeatLine = null;

		switch(cmdID)
		{
			case CMD_QUIT:
				quit = doQuit();
				break;

			case CMD_CONTINUE:
				doContinue();
				break;

			case CMD_HOME:
				doHome();
				break;

			case CMD_HELP:
				doHelp();
				break;

			case CMD_SHOW:
				doShow();
				break;

			case CMD_STEP:
				doStep();
				break;

			case CMD_NEXT:
				doNext();
				break;

			case CMD_FINISH:
				doFinish();
				break;

			case CMD_BREAK:
				doBreak();
				break;

			case CMD_CLEAR:
				doClear();
				break;

			case CMD_SET:
				doSet();
				break;

			case CMD_LIST:
				doList();
				break;

			case CMD_PRINT:
				doPrint();
				break;

			case CMD_TUTORIAL:
				doTutorial();
				break;

			case CMD_INFO:
				doInfo();
				break;

			case CMD_FILE:
				doFile();
				break;

			case CMD_DELETE:
				doDelete();
				break;

			case CMD_RUN:
				doRun();
				break;

			case CMD_SOURCE:
				doSource();
				break;

			case CMD_KILL:
				doKill();
				break;

			case CMD_HANDLE:
				doHandle();
				break;

			case CMD_ENABLE:
				doEnable();
				break;

			case CMD_DISABLE:
				doDisable();
				break;

			case CMD_DISPLAY:
				doDisplay();
				break;

			case CMD_UNDISPLAY:
				doUnDisplay();
				break;

			case CMD_COMMANDS:
				doCommands();
				break;

			case CMD_PWD:
				doPWD();
				break;

			case CMD_CF:
				doCF();
				break;

//			case CMD_AWATCH:
//				doWatch(true, true);
//				break;

			case CMD_WATCH:
				doWatch(false, true);
				break;

//			case CMD_RWATCH:
//				doWatch(true, false);
//				break;

            case CMD_CONDITION:
                doCondition();
                break;

            case CMD_WHAT:
                doWhat();
                break;

            case CMD_DISASSEMBLE:
                doDisassemble();
                break;

            case CMD_HALT:
                doHalt();
                break;

            case CMD_MCTREE:
                doMcTree();
                break;

            case CMD_VIEW_SWF:
                doViewSwf();
                break;

            case CMD_DOWN:
                doDown();
                break;

            case CMD_UP:
                doUp();
                break;

            case CMD_FRAME:
                doFrame();
                break;

			case CMD_COMMENT:
				; // nop
				break;

			case INFO_STACK_CMD:
				; // from bt
				doInfoStack();
				break;

			case CMD_DIRECTORY:
				doDirectory();
				break;

			case CMD_CATCH:
				doCatch();
				break;

			default:
				doUnknown(command);
				break;
		}
		return quit;
	}

    /**
     * Read help text from fdbhelp*.txt.
     */
    String getHelpTopic(String topic)
    {
        // Open the file fdbhelp*.txt that is a sibling of this class file.
        // (Note: build.xml copies it into the classes directory.)
        InputStream helpStream = Help.getResourceAsStream();
        if (helpStream == null)
            return getLocalizationManager().getLocalizedTextString("noHelpFileFound"); //$NON-NLS-1$

        // Read the help file line-by-line, looking for topic lines like [Break].
        // Build an array of the lines within the section for the specified topic.
        topic = "[" + topic + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        Vector<String> lines = new Vector<String>();
        BufferedReader r = null;
        try
        {
            r = new BufferedReader(new InputStreamReader(helpStream, "UTF-8")); //$NON-NLS-1$
            String line;
            // Read lines until we find the specified topic line.
            while ((line = r.readLine()) != null)
            {
                if (line.startsWith(topic))
                    break;
            }
            // Read lines until we find the next topic line.
            while ((line = r.readLine()) != null)
            {
                 if (line.startsWith("[")) //$NON-NLS-1$
                     break;
                 lines.add(line);
            }
        }
        catch(FileNotFoundException fnf)
		{
			err(fnf.getLocalizedMessage());
		}
        catch(IOException e)
		{
			err(e.getLocalizedMessage());
		}
        finally
        {
        	if (r != null)
				try { r.close(); } catch (IOException e) { e.printStackTrace(); }
        }

        // Concatenate the lines, leaving out the first and last ones
        // which are supposed to be blank. They're only there to make
        // fdbhelp*.txt more readable.
        StringBuilder helpText = new StringBuilder();
        int n = lines.size();
        for (int i = 1; i < n - 1; i++)
        {
            String line = lines.get(i);
            helpText.append(line);
            if (i != n - 2)
                helpText.append(m_newline);
        }

        return helpText.toString();
    }

	/**
	 * Provide contenxt sensistive help
	 */
	void doHelp() throws AmbiguousException
	{
        // someone entered a help command so let's help them!
		String topic = "help"; //$NON-NLS-1$

        int cmd;
        String commandName;

        // they might have entered something like "help br"
        if (hasMoreTokens())
		{
			// map "br" to CMD_BREAK
            cmd = commandFor(nextToken());
             // and then back to "break"
            commandName = commandNumberToCommandName(g_commandArray, cmd);
            // so we'll look up the topic named "break" in fdbhelp*.txt
            topic = commandName;

            // they might have entered something like "help inf fil"
            if (cmd == CMD_INFO && hasMoreTokens())
            {
                // map "fil" to CMD_INFO_FILE
                cmd = infoCommandFor(nextToken());
                // and then back to "file"
                commandName = commandNumberToCommandName(g_infoCommandArray, cmd);
                // so we'll look up the topic named "info file" in fdbhelp*.txt
                 topic += " " +  commandName; //$NON-NLS-1$
            }

            // or like "help sho n"
            else if (cmd == CMD_SHOW && hasMoreTokens())
            {
                // map "n" to CMD_SHOW_NET
                cmd = showCommandFor(nextToken());
                // and then back to "net"
                commandName = commandNumberToCommandName(g_showCommandArray, cmd);
                // so we'll look up the topic named "show net" in fdbhelp*.txt
                topic += " " + commandName; //$NON-NLS-1$
            }
		}

		out( getHelpTopic(topic) );
	}

    void doTutorial()
	{
		out( getHelpTopic("Tutorial") ); //$NON-NLS-1$
	}

	// process strings to command ids
	int commandFor(String s) throws AmbiguousException			{ return determineCommand(g_commandArray, s, CMD_UNKNOWN);	}
	int showCommandFor(String s) throws AmbiguousException		{ return determineCommand(g_showCommandArray, s, SHOW_UNKNOWN_CMD);	}
	int infoCommandFor(String s) throws AmbiguousException		{ return determineCommand(g_infoCommandArray, s, INFO_UNKNOWN_CMD);	}
	int enableCommandFor(String s) throws AmbiguousException	{ return determineCommand(g_enableCommandArray, s, CMD_UNKNOWN);	}
	int disableCommandFor(String s) throws AmbiguousException	{ return determineCommand(g_disableCommandArray, s, CMD_UNKNOWN);	}

	/**
	 * Attempt to match given the given string against our set of commands
	 * @return the command code that was hit.
	 */
	int determineCommand(StringIntArray cmdList, String input, int defCmd) throws AmbiguousException
	{
		int cmd = defCmd;

		// first check for a comment
		if (input.charAt(0) == '#')
			cmd = CMD_COMMENT;
		else
		{
//			long start = System.currentTimeMillis();
			ArrayList ar = cmdList.elementsStartingWith(input);
//			long end = System.currentTimeMillis();

			int size = ar.size();

			/**
			 * 3 cases:
			 *  - No hits, return unknown and let our caller
			 *    dump the error.
			 *  - We match unambiguously or we have 1 or more matches
			 *    and the input is a single character. We then take the
			 *    first hit as our command.
			 *  - If we have multiple hits then we dump a 'ambiguous' message
			 *    and puke quietly.
			 */
			if (size == 0)
				; // no command match return unknown

			// only 1 match or our input is 1 character or first match is exact
			else if (size == 1 ||
					 input.length() == 1 ||
					 cmdList.getString( ((Integer)ar.get(0)).intValue() ).compareTo(input) == 0)
			{
				cmd = (cmdList.getInteger( ((Integer)ar.get(0)).intValue() )).intValue();
			}
			else
			{
				// matches more than one command dump message and go
				StringBuilder sb = new StringBuilder();
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("input", input); //$NON-NLS-1$
				sb.append(getLocalizationManager().getLocalizedTextString("ambiguousCommand", args)); //$NON-NLS-1$
				sb.append(' ');
				sb.append(input);
				for(int i=0; i<size; i++)
				{
					String s = cmdList.getString( ((Integer)ar.get(i)).intValue() );
					sb.append(s);
					if (i+1 < size)
						sb.append(", "); //$NON-NLS-1$
				}
				sb.append('.');
				err( sb.toString() );
				throw new AmbiguousException();
			}
		}
		return cmd;
	}

    String commandNumberToCommandName(StringIntArray cmdList, int cmdNumber)
    {
        for (int i = 0; i < cmdList.size(); i++)
        {
            if (cmdList.getInt(i) == cmdNumber)
                return cmdList.getString(i);
        }

        return "?"; //$NON-NLS-1$
    }

	/**
	 * The array of top level commands that we support.
	 * They are placed into a Nx2 array, whereby the first component
	 * is a String which is the command and the 2nd component is the
	 * integer identifier for the command.
	 *
	 * The StringIntArray object provides a convenient wrapper class
	 * that implements the List interface.
	 *
	 * NOTE: order matters!  For the case of a single character
	 *       match, we let the first hit act like an unambiguous match.
	 */
	static StringIntArray g_commandArray = new StringIntArray( new Object[][]
	{
		{ "awatch", new Integer(CMD_AWATCH) }, //$NON-NLS-1$
		{ "break", new Integer(CMD_BREAK) }, //$NON-NLS-1$
		{ "bt", new Integer(INFO_STACK_CMD) }, //$NON-NLS-1$
        { "continue", new Integer(CMD_CONTINUE) }, //$NON-NLS-1$
        { "catch", new Integer(CMD_CATCH) }, //$NON-NLS-1$
        { "cf", new Integer(CMD_CF) }, //$NON-NLS-1$
		{ "clear", new Integer(CMD_CLEAR) }, //$NON-NLS-1$
		{ "commands", new Integer(CMD_COMMANDS) }, //$NON-NLS-1$
		{ "condition", new Integer(CMD_CONDITION) }, //$NON-NLS-1$
		{ "delete", new Integer(CMD_DELETE) }, //$NON-NLS-1$
		{ "disable", new Integer(CMD_DISABLE) }, //$NON-NLS-1$
		{ "disassemble", new Integer(CMD_DISASSEMBLE) }, //$NON-NLS-1$
		{ "display", new Integer(CMD_DISPLAY) }, //$NON-NLS-1$
		{ "directory", new Integer(CMD_DIRECTORY) }, //$NON-NLS-1$
        { "down", new Integer(CMD_DOWN) }, //$NON-NLS-1$
		{ "enable", new Integer(CMD_ENABLE) }, //$NON-NLS-1$
		{ "finish", new Integer(CMD_FINISH) }, //$NON-NLS-1$
		{ "file", new Integer(CMD_FILE) }, //$NON-NLS-1$
        { "frame", new Integer(CMD_FRAME) }, //$NON-NLS-1$
		{ "help", new Integer(CMD_HELP) }, //$NON-NLS-1$
		{ "halt", new Integer(CMD_HALT) }, //$NON-NLS-1$
		{ "handle", new Integer(CMD_HANDLE) }, //$NON-NLS-1$
		{ "home", new Integer(CMD_HOME) }, //$NON-NLS-1$
		{ "info", new Integer(CMD_INFO) }, //$NON-NLS-1$
		{ "kill", new Integer(CMD_KILL) }, //$NON-NLS-1$
		{ "list", new Integer(CMD_LIST) }, //$NON-NLS-1$
		{ "next", new Integer(CMD_NEXT) }, //$NON-NLS-1$
		{ "nexti", new Integer(CMD_NEXT) }, //$NON-NLS-1$
		{ "mctree", new Integer(CMD_MCTREE) }, //$NON-NLS-1$
        { "print", new Integer(CMD_PRINT) }, //$NON-NLS-1$
        { "pwd", new Integer(CMD_PWD) }, //$NON-NLS-1$
		{ "quit", new Integer(CMD_QUIT) }, //$NON-NLS-1$
		{ "run", new Integer(CMD_RUN) }, //$NON-NLS-1$
		{ "rwatch", new Integer(CMD_RWATCH) }, //$NON-NLS-1$
		{ "step", new Integer(CMD_STEP) }, //$NON-NLS-1$
		{ "stepi", new Integer(CMD_STEP) }, //$NON-NLS-1$
		{ "set", new Integer(CMD_SET) }, //$NON-NLS-1$
		{ "show", new Integer(CMD_SHOW) }, //$NON-NLS-1$
		{ "source", new Integer(CMD_SOURCE) }, //$NON-NLS-1$
		{ "tutorial", new Integer(CMD_TUTORIAL) }, //$NON-NLS-1$
		{ "undisplay", new Integer(CMD_UNDISPLAY) }, //$NON-NLS-1$
        { "up", new Integer(CMD_UP) }, //$NON-NLS-1$
		{ "where", new Integer(INFO_STACK_CMD) }, //$NON-NLS-1$
		{ "watch", new Integer(CMD_WATCH) }, //$NON-NLS-1$
		{ "what", new Integer(CMD_WHAT) }, //$NON-NLS-1$
		{ "viewswf", new Integer(CMD_VIEW_SWF) }, //$NON-NLS-1$

	} );

	/**
	 * Info sub-commands
	 */
	static StringIntArray g_infoCommandArray = new StringIntArray( new Object[][]
	{
		{ "arguments", new Integer(INFO_ARGS_CMD) }, //$NON-NLS-1$
		{ "breakpoints", new Integer(INFO_BREAK_CMD) }, //$NON-NLS-1$
		{ "display", new Integer(INFO_DISPLAY_CMD) }, //$NON-NLS-1$
		{ "files", new Integer(INFO_FILES_CMD) }, //$NON-NLS-1$
		{ "functions", new Integer(INFO_FUNCTIONS_CMD) }, //$NON-NLS-1$
		{ "handle", new Integer(INFO_HANDLE_CMD) }, //$NON-NLS-1$
		{ "locals", new Integer(INFO_LOCALS_CMD) }, //$NON-NLS-1$
		{ "stack", new Integer(INFO_STACK_CMD) }, //$NON-NLS-1$
		{ "scopechain", new Integer(INFO_SCOPECHAIN_CMD) }, //$NON-NLS-1$
        { "sources", new Integer(INFO_SOURCES_CMD) }, //$NON-NLS-1$
        { "swfs", new Integer(INFO_SWFS_CMD) }, //$NON-NLS-1$
        { "targets", new Integer(INFO_TARGETS_CMD) }, //$NON-NLS-1$
		{ "variables", new Integer(INFO_VARIABLES_CMD) }, //$NON-NLS-1$
	} );

	/**
	 * Show sub-commands
	 */
	static StringIntArray g_showCommandArray = new StringIntArray( new Object[][]
	{
		{ "break", new Integer(SHOW_BREAK_CMD) }, //$NON-NLS-1$
		{ "directories", new Integer(SHOW_DIRS_CMD) }, //$NON-NLS-1$
		{ "files", new Integer(SHOW_FILES_CMD) }, //$NON-NLS-1$
		{ "functions", new Integer(SHOW_FUNC_CMD) }, //$NON-NLS-1$
		{ "locations", new Integer(SHOW_LOC_CMD) }, //$NON-NLS-1$
		{ "memory", new Integer(SHOW_MEM_CMD) }, //$NON-NLS-1$
		{ "net", new Integer(SHOW_NET_CMD) }, //$NON-NLS-1$
		{ "properties", new Integer(SHOW_PROPERTIES_CMD) }, //$NON-NLS-1$
		{ "uri", new Integer(SHOW_URI_CMD) }, //$NON-NLS-1$
		{ "variable", new Integer(SHOW_VAR_CMD) }, //$NON-NLS-1$
	} );

	/**
	 * enable sub-commands
	 */
	static StringIntArray g_enableCommandArray = new StringIntArray( new Object[][]
	{
		{ "breakpoints", new Integer(CMD_BREAK) }, //$NON-NLS-1$
		{ "display", new Integer(CMD_DISPLAY) }, //$NON-NLS-1$
		{ "delete", new Integer(CMD_DELETE) }, //$NON-NLS-1$
		{ "once", new Integer(ENABLE_ONCE_CMD) }, //$NON-NLS-1$
	} );

	/**
	 * disable sub-commands
	 */
	static StringIntArray g_disableCommandArray = new StringIntArray( new Object[][]
	{
		{ "display", new Integer(CMD_DISPLAY) }, //$NON-NLS-1$
		{ "breakpoints", new Integer(CMD_BREAK) }, //$NON-NLS-1$
	} );

	void populateFaultTable()
	{
		// possible actions for our fault table
		m_faultTable.addAction("stop"); //$NON-NLS-1$
		m_faultTable.addAction("print"); //$NON-NLS-1$

		// the faults we support
		m_faultTable.add(InvalidTargetFault.name);
		m_faultTable.add(RecursionLimitFault.name);
		m_faultTable.add(InvalidWithFault.name);
		m_faultTable.add(ProtoLimitFault.name);
		m_faultTable.add(InvalidURLFault.name);
		m_faultTable.add(ExceptionFault.name);
		m_faultTable.add(StackUnderFlowFault.name);
		m_faultTable.add(DivideByZeroFault.name);
		m_faultTable.add(ScriptTimeoutFault.name);
//		m_faultTable.add(ConsoleErrorFault.name);

		// nice description of the faults
		m_faultTable.putDescription(InvalidTargetFault.name, getLocalizationManager().getLocalizedTextString("invalidTargetFault")); //$NON-NLS-1$
		m_faultTable.putDescription(RecursionLimitFault.name, getLocalizationManager().getLocalizedTextString("recursionLimitFault")); //$NON-NLS-1$
		m_faultTable.putDescription(InvalidWithFault.name, getLocalizationManager().getLocalizedTextString("invalidWithFault")); //$NON-NLS-1$
		m_faultTable.putDescription(ProtoLimitFault.name, getLocalizationManager().getLocalizedTextString("protoLimitFault")); //$NON-NLS-1$
		m_faultTable.putDescription(InvalidURLFault.name, getLocalizationManager().getLocalizedTextString("invalidUrlFault")); //$NON-NLS-1$
		m_faultTable.putDescription(ExceptionFault.name, getLocalizationManager().getLocalizedTextString("exceptionFault")); //$NON-NLS-1$
		m_faultTable.putDescription(StackUnderFlowFault.name, getLocalizationManager().getLocalizedTextString("stackUnderflowFault")); //$NON-NLS-1$
		m_faultTable.putDescription(DivideByZeroFault.name, getLocalizationManager().getLocalizedTextString("divideByZeroFault")); //$NON-NLS-1$
		m_faultTable.putDescription(ScriptTimeoutFault.name, getLocalizationManager().getLocalizedTextString("scriptTimeoutFault")); //$NON-NLS-1$
//		m_faultTable.putDescription(ConsoleErrorFault.name, "ActionScript recoverable error");

		// default values for the faults
		m_faultTable.action(InvalidTargetFault.name, "stop"); //$NON-NLS-1$
		m_faultTable.action(InvalidTargetFault.name, "print"); //$NON-NLS-1$
		m_faultTable.action(RecursionLimitFault.name, "stop"); //$NON-NLS-1$
		m_faultTable.action(RecursionLimitFault.name, "print"); //$NON-NLS-1$
		m_faultTable.action(InvalidWithFault.name, "stop"); //$NON-NLS-1$
		m_faultTable.action(InvalidWithFault.name, "print"); //$NON-NLS-1$
		m_faultTable.action(ProtoLimitFault.name, "stop"); //$NON-NLS-1$
		m_faultTable.action(ProtoLimitFault.name, "print"); //$NON-NLS-1$
		m_faultTable.action(InvalidURLFault.name, "stop"); //$NON-NLS-1$
		m_faultTable.action(InvalidURLFault.name, "print"); //$NON-NLS-1$
		m_faultTable.action(ExceptionFault.name, "stop"); //$NON-NLS-1$
		m_faultTable.action(ExceptionFault.name, "print"); //$NON-NLS-1$
		m_faultTable.action(StackUnderFlowFault.name, "stop"); //$NON-NLS-1$
		m_faultTable.action(StackUnderFlowFault.name, "print"); //$NON-NLS-1$
		m_faultTable.action(DivideByZeroFault.name, "stop"); //$NON-NLS-1$
		m_faultTable.action(DivideByZeroFault.name, "print"); //$NON-NLS-1$
		m_faultTable.action(ScriptTimeoutFault.name, "stop"); //$NON-NLS-1$
		m_faultTable.action(ScriptTimeoutFault.name, "print"); //$NON-NLS-1$
//		m_faultTable.action(ConsoleErrorFault.name, "print"); //$NON-NLS-1$
//		m_faultTable.action(ConsoleErrorFault.name, "stop"); //$NON-NLS-1$
	}

	/**
	 * -------------------------------------------------------------------------
	 * Any code that accesses the implementation of the API is wrapped 
	 * in Extensions.  This way one can easily factor this stuff out
	 * and build an fdb that is completely compliant to the API.
	 *
	 * I'm pretty sure there's a better way of doing this like
	 * making Extensions a final static variable and then 
	 * toggling it between two classes Extensions and something
	 * like ExtensionsDisabled (methods with only out("not supported")
	 * in them).
	 * -------------------------------------------------------------------------
	 */
	void appendBreakInfo(StringBuilder sb) throws NotConnectedException	{ Extensions.appendBreakInfo(this, sb, false);	}
	void doShowStats()													{ Extensions.doShowStats(this);					}
	void doShowFuncs()													{ Extensions.doShowFuncs(this);					}
	void doShowProperties()												{ Extensions.doShowProperties(this);			}
	void doShowVariable() throws PlayerDebugException					{ Extensions.doShowVariable(this);				}
	void doShowBreak() throws NotConnectedException						{ Extensions.doShowBreak(this);					}
 	void doDisassemble() throws PlayerDebugException					{ Extensions.doDisassemble(this);				}
}
