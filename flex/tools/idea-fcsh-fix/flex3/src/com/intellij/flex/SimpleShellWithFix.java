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
// svn://opensource.adobe.com/svn/opensource/flex/sdk/tags/3.2.0.3958/modules/compiler/src/java/flex2/tools/SimpleShell.java (revision 4153)

package com.intellij.flex;

import flash.localization.LocalizationManager;
import flash.localization.ResourceBundleLocalizer;
import flex2.compiler.*;
import flex2.compiler.common.CompilerConfiguration;
import flex2.compiler.common.Configuration;
import flex2.compiler.common.DefaultsConfigurator;
import flex2.compiler.config.ConfigurationBuffer;
import flex2.compiler.config.ConfigurationException;
import flex2.compiler.i18n.I18nUtils;
import flex2.compiler.io.FileUtil;
import flex2.compiler.io.VirtualFile;
import flex2.compiler.swc.API;
import flex2.compiler.swc.SwcCache;
import flex2.compiler.swc.SwcException;
import flex2.compiler.util.Benchmark;
import flex2.compiler.util.NameMappings;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.linker.LinkerException;
import flex2.tools.*;
import flex2.tools.Compiler;
import flex2.tools.PreLink;
import flex2.tools.Compiler.InitialSetup;
import flex2.tools.Compiler.LoadedSWCs;
import flex2.tools.Compiler.OutputMessage;
import static flex2.tools.SimpleShell.*;

import java.io.*;
import java.util.*;

/**
 * @author Clement Wong
 */
public class SimpleShellWithFix extends Tool
{
	public static void main(String[] args) throws IOException
	{
		exit = false;
		counter = 1;
		targets = new HashMap();
		processes = new HashMap();

		String s;
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

		flex2.compiler.API.useAS3();

		LocalizationManager localizationManager = new LocalizationManager();
		localizationManager.addLocalizer( new ResourceBundleLocalizer() );
		ThreadLocalToolkit.setLocalizationManager( localizationManager );

		intro();
		prompt();
		while ((s = r.readLine()) != null)
		{
			flex2.compiler.API.useConsoleLogger();

			if (s.trim().length() == 0)
			{
				prompt();
				continue;
			}

//			try
//			{
				process(s);
//			}
//			catch (Throwable t)
//			{
//				t.printStackTrace();
//			}

			if (exit)
			{
				break;
			}
			else
			{
				prompt();
			}
		}
	}

	private static int counter;
	private static boolean exit;
	private static Map targets;
	private static Map processes;

	private static void process(String s)
	{
		LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();

		if (s.startsWith("mxmlc"))
		{
			StringTokenizer t = new CommandLineArgumentsTokenizer(s.substring("mxmlc".length()).trim(), " ");
			String[] args = new String[t.countTokens()];
			for (int i = 0; t.hasMoreTokens(); i++)
			{
				args[i] = t.nextToken();
			}

			if (args.length == 1)
			{
				try
				{
					int id = Integer.parseInt(args[0]);
					Target target = (Target) targets.get("" + id);
					if (target == null)
					{
						ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new TargetNotFound("" + id)));
					}
					else
					{
						mxmlc(target.args, id);
					}
				}
				catch (NumberFormatException ex)
				{
					ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new AssignTargetID(counter)));
					mxmlc(args, counter++);
				}
			}
			else
			{
				ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new AssignTargetID(counter)));
				mxmlc(args, counter++);
			}
		}
		else if (s.startsWith("compc"))
		{
			StringTokenizer t = new CommandLineArgumentsTokenizer(s.substring("compc".length()).trim(), " ");
			String[] args = new String[t.countTokens()];
			for (int i = 0; t.hasMoreTokens(); i++)
			{
				args[i] = t.nextToken();
			}

			if (args.length == 1)
			{
				try
				{
					int id = Integer.parseInt(args[0]);
					Target target = (Target) targets.get("" + id);
					if (target == null)
					{
						ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new TargetNotFound("" + id)));
					}
					else
					{
						compc(target.args, id);
					}
				}
				catch (NumberFormatException ex)
				{
					ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new AssignTargetID(counter)));
					compc(args, counter++);
				}
			}
			else
			{
				ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new AssignTargetID(counter)));
				compc(args, counter++);
			}
		}
		else if (s.startsWith("compile"))
		{
			String id = s.substring("compile".length()).trim();
			if (targets.containsKey(id))
			{
				compile(id);
				if (ThreadLocalToolkit.errorCount() == 0)
				{
					link(id, true);
				}
			}
			else
			{
				ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new TargetNotFound(id)));
			}
		}
		else if (s.startsWith("clear"))
		{
			String id = s.substring("clear".length()).trim();
			if (id.length() == 0)
			{
				HashSet keys = new HashSet(targets.keySet());
				for (Iterator i = keys.iterator(); i.hasNext();)
				{
					clear((String) i.next());
				}
			}
			else if (targets.containsKey(id))
			{
				clear(id);
			}
			else
			{
				ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new TargetNotFound(id)));
			}
		}
		else if (s.startsWith("info"))
		{
			String id = s.substring("info".length()).trim();
			if (id.length() == 0)
			{
				HashSet keys = new HashSet(targets.keySet());
				for (Iterator i = keys.iterator(); i.hasNext();)
				{
					info((String) i.next());
				}
			}
			else if (targets.containsKey(id))
			{
				info(id);
			}
			else
			{
				ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new TargetNotFound(id)));
			}
		}
		else if (s.startsWith("touch"))
		{
			String args = s.substring("touch".length()).trim();
			StringTokenizer stok = new CommandLineArgumentsTokenizer(args);
			while (stok.hasMoreTokens())
			{
				String f = stok.nextToken();

				if (!new File(f).canWrite())
				{
					ThreadLocalToolkit.logInfo("touch: cannot write " + f);
				}
				else
				{
					new File(f).setLastModified(System.currentTimeMillis());
				}
			}
		}
        else if (s.startsWith("cp"))
        {
            String args = s.substring("cp".length()).trim();
			StringTokenizer stok = new CommandLineArgumentsTokenizer(args);
            if (stok.countTokens() != 2)
			{
                ThreadLocalToolkit.logInfo("cp fileFrom fileTo");
            }
			else
			{
                String copyFrom = stok.nextToken();
                String copyTo = stok.nextToken();
                try
				{
                    FileUtil.writeBinaryFile(new File(copyTo), FileUtil.openStream(copyFrom));
                }
				catch (IOException e)
				{
                    ThreadLocalToolkit.logInfo(e.getMessage());
                }
            }
        }
        else if (s.startsWith("mv"))
        {
            String args = s.substring("mv".length()).trim();
            StringTokenizer stok = new CommandLineArgumentsTokenizer(args);
            if (stok.countTokens() != 2)
			{
                ThreadLocalToolkit.logInfo("mv fileFrom fileTo");
            }
			else
			{
                String moveFrom = stok.nextToken();
                String moveTo = stok.nextToken();
                new File(moveFrom).renameTo(new File(moveTo));
            }
        }
        else if (s.startsWith("rm"))
        {
            String args = s.substring("rm".length()).trim();
            StringTokenizer stok = new CommandLineArgumentsTokenizer(args);
            if (stok.countTokens() != 1)
			{
                ThreadLocalToolkit.logInfo("rm file");
            }
			else
			{
                String rmFile=stok.nextToken();
                new File(rmFile).delete();
            }
        }
        else if (s.equals("quit"))
		{
			Set names = new HashSet(targets.keySet());
			for (Iterator i = names.iterator(); i.hasNext();)
			{
				process("clear " + (String) i.next());
			}

			exit = true;
		}
		else
		{
			cmdList();
		}
    }

	private static void clear(String target)
	{
		Process p = (Process) processes.remove(target);

		if (p != null)
		{
			p.destroy();
		}

		targets.remove(target);
	}

	private static void info(String target)
	{
		Target s = (Target) targets.get(target);
		ThreadLocalToolkit.logInfo("id: " + s.id);
		StringBuffer b = new StringBuffer();
		for (int i = 0, size = s.args.length; i < size; i++)
		{
			b.append(s.args[i]);
			b.append(' ');
		}
		ThreadLocalToolkit.logInfo((s instanceof SwcTarget ? "compc: " : "mxmlc: ") + b);
	}

	private static void compile(String id)
	{
		Target s = (Target) targets.get(id);
		if (s instanceof SwcTarget)
		{
			compile_compc((SwcTarget) s);
		}
		else
		{
			compile_mxmlc(s);
		}
	}

	private static void compile_compc(SwcTarget s)
	{
		LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();

		try
		{
            // setup the path resolver
			flex2.compiler.API.usePathResolver();

			// process configuration
			ConfigurationBuffer cfgbuf = new ConfigurationBuffer(CompcConfiguration.class, CompcConfiguration.getAliases());
			cfgbuf.setDefaultVar("include-classes");
	        DefaultsConfigurator.loadCompcDefaults( cfgbuf );
	        CompcConfiguration configuration = (CompcConfiguration) Compiler.processConfiguration(
                ThreadLocalToolkit.getLocalizationManager(), "compc", s.args, cfgbuf, CompcConfiguration.class, "include-classes");

			flex2.compiler.API.setupHeadless(configuration);

			if (configuration.benchmark())
			{
				flex2.compiler.API.runBenchmark();
			}
			else
			{
				flex2.compiler.API.disableBenchmark();
			}

			s.sourcePath.clearCache();
			s.bundlePath.clearCache();
			s.resources.refresh();

			// C: We don't really need to parse the manifest files again.
			CompilerConfiguration compilerConfig = configuration.getCompilerConfiguration();
			// note: if Configuration is ever shared with other parts of the system, then this part will need
			// to change, since we're setting a compc-specific setting below
			compilerConfig.setMetadataExport(true);

			NameMappings mappings = flex2.compiler.API.getNameMappings(configuration);

			flex2.compiler.Transcoder[] transcoders = flex2.tools.API.getTranscoders( configuration );
			flex2.compiler.Compiler[] compilers = flex2.tools.API.getCompilers(compilerConfig, mappings, transcoders);

			if (ThreadLocalToolkit.getBenchmark() != null)
			{
				ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new InitialSetup()));
			}

            // load SWCs
			CompilerSwcContext swcContext = new CompilerSwcContext(false, true,
																   configuration.getCompatibilityVersionString());
	        // for compc the theme and include-libraries values have been purposely not passed in below.
	        // This is done because the theme attribute doesn't make sense and the include-libraries value
	        // actually causes issues when the default value is used with external libraries.
	        // FIXME: why don't we just get rid of these values from the configurator for compc?  That's a problem
	        // for include-libraries at least, since this value appears in flex-config.xml
            swcContext.load( compilerConfig.getLibraryPath(),
                             compilerConfig.getExternalLibraryPath(),
                             null,
                             null,
							 mappings,
							 I18nUtils.getTranslationFormat(compilerConfig),
							 s.swcCache );
            configuration.addExterns( swcContext.getExterns() );

			if (ThreadLocalToolkit.getBenchmark() != null)
			{
				ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new LoadedSWCs(swcContext.getNumberLoaded())));
			}

			// recompile or incrementally compile...
			boolean recompile = false;
			int newChecksum = cfgbuf.checksum_ts() + swcContext.checksum();

			if (newChecksum != s.checksum)
			{
				ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new DetectConfigurationChange()));
				s.checksum = newChecksum;
				s.resources = new ResourceContainer();
				recompile = true;
			}

			// validate CompilationUnits in FileSpec and SourcePath
			if (flex2.compiler.API.validateCompilationUnits(s.fileSpec, s.sourceList, s.sourcePath, s.bundlePath, s.resources,
															swcContext, s.perCompileData,
															recompile, configuration) > 0)
			{
				Map licenseMap = configuration.getLicensesConfiguration().getLicenseMap();

				// create a symbol table
				SymbolTable symbolTable = new SymbolTable(s.perCompileData);
				s.configuration = configuration;

		        Map classes = new HashMap();
				s.nsComponents = API.setupNamespaceComponents(configuration, mappings, s.sourcePath, classes);
				API.setupClasses(configuration, s.sourcePath, classes);

	            // compile
		        Map rbFiles = new HashMap();

                ThreadLocalToolkit.getPathResolver().addSinglePathResolver(new SourcePathResolver(s.sourcePath));
		        List units = flex2.compiler.API.compile(s.fileSpec, s.sourceList, classes.values(), s.sourcePath, s.resources, s.bundlePath, swcContext,
		                                                   symbolTable, configuration, compilers, new CompcPreLink(rbFiles, configuration.getIncludeResourceBundles()), licenseMap, new ArrayList()); // List<CompilationUnit>

		        s.units = units;
		        s.rbFiles = rbFiles;
	    		s.sourcePath.clearCache();
	    		s.bundlePath.clearCache();
	    		s.resources.refresh();

	    		s.classes = classes.keySet();
			}
			else
			{
				ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new NoChange()));
			}
		}
        catch (ConfigurationException ex)
        {
            Compc.displayStartMessage();
            Compiler.processConfigurationException(ex, "compc");
        }
		catch (LicenseException ex)
		{
            ThreadLocalToolkit.logError(ex.getMessage());
		}
		catch (CompilerException ex)
		{
			assert ThreadLocalToolkit.errorCount() > 0;
		}
		catch (SwcException ex)
		{
		    assert ThreadLocalToolkit.errorCount() > 0;
		}
		catch (IOException t) // IOException, Throwable
		{
			ThreadLocalToolkit.logError(t.getMessage());
			t.printStackTrace();
		}
		finally
		{
			Benchmark benchmark = ThreadLocalToolkit.getBenchmark();
			if (benchmark != null)
			{
			    benchmark.totalTime();
			    benchmark.peakMemoryUsage(true);
			}

			flex2.compiler.API.removePathResolver();
		}
	}

	private static void compile_mxmlc(Target s)
	{
		LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();

		try
		{
            // setup the path resolver
			flex2.compiler.API.usePathResolver();

			// process configuration
			ConfigurationBuffer cfgbuf = new ConfigurationBuffer(CommandLineConfiguration.class, Configuration.getAliases());
			cfgbuf.setDefaultVar(Compiler.FILE_SPECS);
			DefaultsConfigurator.loadDefaults( cfgbuf );
			CommandLineConfiguration configuration = (CommandLineConfiguration) Compiler.processConfiguration(
				ThreadLocalToolkit.getLocalizationManager(), "mxmlc", s.args, cfgbuf, CommandLineConfiguration.class, Compiler.FILE_SPECS);

			flex2.compiler.API.setupHeadless(configuration);

			if (configuration.benchmark())
			{
				flex2.compiler.API.runBenchmark();
			}
			else
			{
				flex2.compiler.API.disableBenchmark();
			}

			s.sourcePath.clearCache();
			s.bundlePath.clearCache();
			s.resources.refresh();

			// C: We don't really need to parse the manifest files again.
			CompilerConfiguration compilerConfig = configuration.getCompilerConfiguration();
			NameMappings mappings = flex2.compiler.API.getNameMappings(configuration);

			flex2.compiler.Transcoder[] transcoders = flex2.tools.API.getTranscoders( configuration );
			flex2.compiler.Compiler[] compilers = flex2.tools.API.getCompilers(compilerConfig, mappings, transcoders);

			if (ThreadLocalToolkit.getBenchmark() != null)
			{
				ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new InitialSetup()));
			}

			CompilerSwcContext swcContext = new CompilerSwcContext(false, true,
																   configuration.getCompatibilityVersionString());
			swcContext.load( compilerConfig.getLibraryPath(),
							 Configuration.getAllExcludedLibraries(compilerConfig, configuration),
                             compilerConfig.getThemeFiles(),
                             compilerConfig.getIncludeLibraries(),
							 mappings,
							 I18nUtils.getTranslationFormat(compilerConfig),
							 s.swcCache );
            configuration.addExterns( swcContext.getExterns() );
			configuration.addIncludes( swcContext.getIncludes() );
			configuration.getCompilerConfiguration().addDefaultsCssFiles( swcContext.getDefaultsStyleSheets() );
            configuration.getCompilerConfiguration().addThemeCssFiles( swcContext.getThemeStyleSheets() );

			if (ThreadLocalToolkit.getBenchmark() != null)
			{
				ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new LoadedSWCs(swcContext.getNumberLoaded())));
			}

			// recompile or incrementally compile...
			boolean recompile = false;
			int newChecksum = cfgbuf.checksum_ts() + swcContext.checksum();

			if (newChecksum != s.checksum)
			{
				ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new DetectConfigurationChange()));
				s.checksum = newChecksum;
				s.resources = new ResourceContainer();
				recompile = true;
			}

			// validate CompilationUnits in FileSpec and SourcePath
			if (flex2.compiler.API.validateCompilationUnits(s.fileSpec, s.sourceList, s.sourcePath, s.bundlePath, s.resources,
															swcContext, s.perCompileData,
															recompile, configuration) > 0)
			{
				Map licenseMap = configuration.getLicensesConfiguration().getLicenseMap();

				// create a symbol table
				SymbolTable symbolTable = new SymbolTable(s.perCompileData);
				s.configuration = configuration;

	            VirtualFile projector = configuration.getProjector();

	            if (projector != null && projector.getName().endsWith("avmplus.exe"))
	            {
	                // compile
                    ThreadLocalToolkit.getPathResolver().addSinglePathResolver(new SourcePathResolver(s.sourcePath));
	                List units = flex2.compiler.API.compile(s.fileSpec, s.sourceList, null, s.sourcePath, s.resources, s.bundlePath,
	                										swcContext, symbolTable, configuration, compilers,
	                										null, licenseMap, new ArrayList());
	    			s.units = units;
	            }
	            else
	            {
	                // compile
                    ThreadLocalToolkit.getPathResolver().addSinglePathResolver(new SourcePathResolver(s.sourcePath));
	    			List units = flex2.compiler.API.compile(s.fileSpec, s.sourceList, null, s.sourcePath, s.resources, s.bundlePath,
	    			                                        swcContext, symbolTable, configuration, compilers,
	    			                                        new PreLink(), licenseMap, new ArrayList());
	    			s.units = units;
	            }

	    		s.sourcePath.clearCache();
	    		s.bundlePath.clearCache();
	    		s.resources.refresh();
			}
			else
			{
				ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new NoChange()));
			}
		}
        catch (flex2.compiler.config.ConfigurationException ex)
        {
            Compiler.processConfigurationException(ex, "mxmlc");
        }
		catch (LicenseException ex)
		{
            ThreadLocalToolkit.logError(ex.getMessage());
		}
		catch (CompilerException ex)
		{
			assert ThreadLocalToolkit.errorCount() > 0;
		}
		catch (SwcException ex)
		{
		    assert ThreadLocalToolkit.errorCount() > 0;
		}
		catch (IOException t) // IOException, Throwable
		{
			ThreadLocalToolkit.logError(t.getMessage());
			t.printStackTrace();
		}
		finally
		{
			Benchmark benchmark = ThreadLocalToolkit.getBenchmark();
			if (benchmark != null)
			{
			    benchmark.totalTime();
			    benchmark.peakMemoryUsage(true);
			}

			flex2.compiler.API.removePathResolver();
		}
	}

	private static void link(String target, boolean optimize)
	{
		Target s = (Target) targets.get(target);
		if (s instanceof SwcTarget)
		{
			link_compc((SwcTarget) s);
		}
		else
		{
			link_mxmlc(s, optimize);
		}
	}

	private static void link_compc(SwcTarget s)
	{
		try
		{
			ThreadLocalToolkit.resetBenchmark();

			// setup the path resolver
			flex2.compiler.API.usePathResolver();

			if (s.units != null)
			{
		        // export SWC
	            flex2.compiler.swc.API.exportSwc( (CompcConfiguration) s.configuration, s.units, s.nsComponents, s.swcCache, s.rbFiles );

	            if (s.outputName != null && ThreadLocalToolkit.errorCount() == 0)
	            {
		            File file = new File(s.outputName);
		            s.outputName = FileUtil.getCanonicalPath(file);
		            ThreadLocalToolkit.log(new OutputMessage(s.outputName, Long.toString(file.length())));
	            }
			}
		}
        catch (LinkerException ex)
        {
            assert ThreadLocalToolkit.errorCount() > 0;
        }
        catch (Exception t) // IOException, Throwable
        {
            ThreadLocalToolkit.logError(t.getMessage());
            t.printStackTrace();
        }
	    finally
        {
			Benchmark benchmark = ThreadLocalToolkit.getBenchmark();
			if (benchmark != null)
			{
			    benchmark.totalTime();
			    benchmark.peakMemoryUsage(true);
			}

			flex2.compiler.API.removePathResolver();
        }
	}

	private static void link_mxmlc(Target s, boolean optimize)
	{
		OutputStream swfOut = null;

		try
		{
			ThreadLocalToolkit.resetBenchmark();

			// link
			if (s.units != null)
			{
	            VirtualFile projector = ((CommandLineConfiguration) s.configuration).getProjector();

	            if (projector != null && projector.getName().endsWith("avmplus.exe"))
	            {
	                // link
	                s.app = flex2.linker.API.linkConsole(s.units, optimize ? new PostLink(s.configuration) : null, s.configuration);

	                // output .exe
	                File file = FileUtil.openFile(s.outputName, true);
	                swfOut = new BufferedOutputStream(new FileOutputStream(file));

	                flex2.tools.Compiler.createProjector(projector, s.app, swfOut);

	                swfOut.flush();
	                swfOut.close();

	                ThreadLocalToolkit.log(new OutputMessage(s.outputName, Long.toString(file.length())));
	            }
	            else
	            {
	    			// link
	    			s.movie = flex2.linker.API.link(s.units, optimize ? new PostLink(s.configuration) : null, s.configuration);

	                // output SWF
	    			File file = FileUtil.openFile(s.outputName, true);
	    			swfOut = new BufferedOutputStream(new FileOutputStream(file));

	                if (projector != null)
	                {
	                	flex2.tools.Compiler.createProjector(projector, s.movie, swfOut);
	                }
	                else
	                {
	                    flex2.compiler.API.encode(s.movie, swfOut);
	                }

	    			swfOut.flush();
	    			swfOut.close();

	    			ThreadLocalToolkit.log(new OutputMessage(s.outputName, Long.toString(file.length())));
	            }
			}
		}
		catch (LinkerException ex)
		{
			assert ThreadLocalToolkit.errorCount() > 0;
		}
		catch (IOException t) // IOException, Throwable
		{
			ThreadLocalToolkit.logError(t.getMessage());
			t.printStackTrace();
		}
		finally
		{
			Benchmark benchmark = ThreadLocalToolkit.getBenchmark();
			if (benchmark != null)
			{
			    benchmark.totalTime();
			    benchmark.peakMemoryUsage(true);
			}

			if (swfOut != null) { try { swfOut.close(); } catch (IOException ioe) {} }
		}
	}

	private static void mxmlc(String[] args, int id)
	{
		LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();

        OutputStream swfOut = null;
		Target s = new Target();
		s.id = id;

		try
		{
            // setup the path resolver
			flex2.compiler.API.usePathResolver();

			// process configuration
			ConfigurationBuffer cfgbuf = new ConfigurationBuffer(CommandLineConfiguration.class, Configuration.getAliases());
			cfgbuf.setDefaultVar(Compiler.FILE_SPECS);
			DefaultsConfigurator.loadDefaults( cfgbuf );
			CommandLineConfiguration configuration = (CommandLineConfiguration) Compiler.processConfiguration(
				ThreadLocalToolkit.getLocalizationManager(), "mxmlc", args, cfgbuf, CommandLineConfiguration.class, Compiler.FILE_SPECS);
			s.configuration = configuration;

			flex2.compiler.API.setupHeadless(configuration);

			if (configuration.benchmark())
			{
				flex2.compiler.API.runBenchmark();
			}
			else
			{
				flex2.compiler.API.disableBenchmark();
			}

			String target = configuration.getTargetFile();
			targets.put("" + id, s);
			s.args = args;

			// make sure targetFile abstract pathname is an absolute path...
			VirtualFile targetFile = flex2.compiler.API.getVirtualFile(target);
			flex2.tools.API.checkSupportedTargetMimeType(targetFile);
			List virtualFileList = flex2.compiler.API.getVirtualFileList(configuration.getFileList()); // List<VirtualFile>

			CompilerConfiguration compilerConfig = configuration.getCompilerConfiguration();
			NameMappings mappings = flex2.compiler.API.getNameMappings(configuration);

            flex2.compiler.Transcoder[] transcoders = flex2.tools.API.getTranscoders( configuration );
            flex2.compiler.Compiler[] compilers = flex2.tools.API.getCompilers(compilerConfig, mappings, transcoders);

			// construct the SWF file name...
            VirtualFile projector = configuration.getProjector();

            if (projector != null && projector.getName().endsWith("avmplus.exe"))
            {
                // output .exe
                s.outputName = configuration.getOutput();
    	        if (s.outputName == null)
    	        {
    		        s.outputName = targetFile.getName();
    		        s.outputName = s.outputName.substring(0, s.outputName.lastIndexOf('.')) + ".exe";
    	        }
            }
            else
            {
                // output SWF
                s.outputName = configuration.getOutput();
    	        if (s.outputName == null)
    	        {
    		        s.outputName = targetFile.getName();
    		        if (projector != null)
    		        {
    		        	s.outputName = s.outputName.substring(0, s.outputName.lastIndexOf('.')) + ".exe";
    		        }
    		        else
    		        {
    		        	s.outputName = s.outputName.substring(0, s.outputName.lastIndexOf('.')) + ".swf";
    		        }
    	        }
            }

			VirtualFile[] asClasspath = compilerConfig.getSourcePath();

			// create a FileSpec...
			s.fileSpec = new FileSpec(Collections.EMPTY_LIST, flex2.tools.API.getFileSpecMimeTypes());

			// create a SourceList
			s.sourceList = new SourceList(virtualFileList,
			                              asClasspath, targetFile, flex2.tools.API.getSourcePathMimeTypes());

			// create a SourcePath...
			s.sourcePath = new SourcePath(asClasspath, targetFile, flex2.tools.API.getSourcePathMimeTypes(),
			                              compilerConfig.allowSourcePathOverlap());

			// create a ResourceContainer...
			s.resources = new ResourceContainer();

			// create a ResourceBundlePath...
			s.bundlePath = new ResourceBundlePath(configuration.getCompilerConfiguration(), targetFile);

			if (ThreadLocalToolkit.getBenchmark() != null)
			{
				ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new InitialSetup()));
			}

			// load SWCs
			s.swcCache = new SwcCache();
			CompilerSwcContext swcContext = new CompilerSwcContext(true, true,
																   configuration.getCompatibilityVersionString());
			swcContext.load( compilerConfig.getLibraryPath(),
							 Configuration.getAllExcludedLibraries(compilerConfig, configuration),
                             compilerConfig.getThemeFiles(),
                             compilerConfig.getIncludeLibraries(),
							 mappings,
							 I18nUtils.getTranslationFormat(compilerConfig),
							 s.swcCache );
			configuration.addExterns( swcContext.getExterns() );
			configuration.addIncludes( swcContext.getIncludes() );
			configuration.getCompilerConfiguration().addDefaultsCssFiles( swcContext.getDefaultsStyleSheets() );
            configuration.getCompilerConfiguration().addThemeCssFiles( swcContext.getThemeStyleSheets() );

			if (ThreadLocalToolkit.getBenchmark() != null)
			{
				ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new LoadedSWCs(swcContext.getNumberLoaded())));
			}

           	s.checksum = cfgbuf.checksum_ts() + swcContext.checksum();

			final SymbolTable symbolTable = SymbolTable.newSymbolTable(configuration);
			s.perCompileData = symbolTable.perCompileData;

			Map licenseMap = configuration.getLicensesConfiguration().getLicenseMap();

            if (projector != null && projector.getName().endsWith("avmplus.exe"))
            {
                // compile
                ThreadLocalToolkit.getPathResolver().addSinglePathResolver(new SourcePathResolver(s.sourcePath));
                List units = flex2.compiler.API.compile(s.fileSpec, s.sourceList, null, s.sourcePath, s.resources, s.bundlePath,
                										swcContext, symbolTable, configuration, compilers,
                										null, licenseMap, new ArrayList());

    			s.units = units;
    			s.sourcePath.clearCache();
    			s.bundlePath.clearCache();
    			s.resources.refresh();

                // link
                s.app = flex2.linker.API.linkConsole(units, new PostLink(configuration), configuration);

                // output .exe
                File file = FileUtil.openFile(s.outputName, true);
                swfOut = new BufferedOutputStream(new FileOutputStream(file));

                flex2.tools.Compiler.createProjector(projector, s.app, swfOut);

                swfOut.flush();
                swfOut.close();

                ThreadLocalToolkit.log(new OutputMessage(s.outputName, Long.toString(file.length())));
            }
            else
            {
                // compile
                ThreadLocalToolkit.getPathResolver().addSinglePathResolver(new SourcePathResolver(s.sourcePath));
    			List units = flex2.compiler.API.compile(s.fileSpec, s.sourceList, null, s.sourcePath, s.resources, s.bundlePath,
    			                                        swcContext, symbolTable, configuration, compilers,
    			                                        new PreLink(), licenseMap, new ArrayList());

    			s.units = units;
    			s.sourcePath.clearCache();
    			s.bundlePath.clearCache();
    			s.resources.refresh();

    			// link
    			s.movie = flex2.linker.API.link(units, new PostLink(configuration), configuration);

                // output SWF
    			File file = FileUtil.openFile(s.outputName, true);
    			swfOut = new BufferedOutputStream(new FileOutputStream(file));

                if (projector != null)
                {
                	flex2.tools.Compiler.createProjector(projector, s.movie, swfOut);
                }
                else
                {
                    flex2.compiler.API.encode(s.movie, swfOut);
                }

    			swfOut.flush();
    			swfOut.close();

    			ThreadLocalToolkit.log(new OutputMessage(s.outputName, Long.toString(file.length())));
            }
		}
		catch (ConfigurationException ex)
		{
			Compiler.processConfigurationException(ex, "mxmlc");
		}
		catch (LicenseException ex)
		{
            ThreadLocalToolkit.logError(ex.getMessage());
		}
		catch (CompilerException ex)
		{
			assert ThreadLocalToolkit.errorCount() > 0;
		}
		catch (LinkerException ex)
		{
			assert ThreadLocalToolkit.errorCount() > 0;
		}
		catch (SwcException ex)
		{
		    assert ThreadLocalToolkit.errorCount() > 0;
		}
		catch (IOException thr) // IOException, Throwable
		{
			ThreadLocalToolkit.logError(thr.getMessage());
			thr.printStackTrace();
		}
		finally
		{
			Benchmark benchmark = ThreadLocalToolkit.getBenchmark();
			if (benchmark != null)
			{
			    benchmark.totalTime();
			    benchmark.peakMemoryUsage(true);
			}

			flex2.compiler.API.removePathResolver();

			if (swfOut != null) try { swfOut.close(); } catch (IOException ioe) {}
		}
	}

	private static void compc(String[] args, int id)
	{
		LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();

		SwcTarget s = new SwcTarget();
		s.id = id;

		try
		{
            // setup the path resolver
			flex2.compiler.API.usePathResolver();

			// process configuration
			ConfigurationBuffer cfgbuf = new ConfigurationBuffer(CompcConfiguration.class, CompcConfiguration.getAliases());
			cfgbuf.setDefaultVar("include-classes");
	        DefaultsConfigurator.loadCompcDefaults( cfgbuf );
	        CompcConfiguration configuration = (CompcConfiguration) Compiler.processConfiguration(
                ThreadLocalToolkit.getLocalizationManager(), "compc", args, cfgbuf, CompcConfiguration.class, "include-classes");
			s.configuration = configuration;

			flex2.compiler.API.setupHeadless(configuration);

			if (configuration.benchmark())
			{
				flex2.compiler.API.runBenchmark();
			}
			else
			{
				flex2.compiler.API.disableBenchmark();
			}

			targets.put("" + id, s);
			s.args = args;

			String[] sourceMimeTypes = flex2.tools.API.getSourcePathMimeTypes();

			CompilerConfiguration compilerConfig = configuration.getCompilerConfiguration();

			// create a SourcePath...
			s.sourcePath = new SourcePath(sourceMimeTypes, compilerConfig.allowSourcePathOverlap());
			s.sourcePath.addPathElements( compilerConfig.getSourcePath() );

			List[] array = flex2.compiler.API.getVirtualFileList(configuration.getIncludeSources(), configuration.getStylesheets().values(),
																 new HashSet(Arrays.asList(sourceMimeTypes)),
																 s.sourcePath.getPaths());

			NameMappings mappings = flex2.compiler.API.getNameMappings(configuration);

			// note: if Configuration is ever shared with other parts of the system, then this part will need
			// to change, since we're setting a compc-specific setting below
			compilerConfig.setMetadataExport(true);

			//	get standard bundle of compilers, transcoders
            flex2.compiler.Transcoder[] transcoders = flex2.tools.API.getTranscoders( configuration );
            flex2.compiler.Compiler[] compilers = flex2.tools.API.getCompilers(compilerConfig, mappings, transcoders);

			// construct the SWC file name...
			s.outputName = FileUtil.getCanonicalPath(FileUtil.openFile(configuration.getOutput()));

			// create a FileSpec...
			s.fileSpec = new FileSpec(array[0], flex2.tools.API.getFileSpecMimeTypes(), false);

            // create a SourceList...
            s.sourceList = new SourceList(array[1], compilerConfig.getSourcePath(), null,
            									   flex2.tools.API.getSourceListMimeTypes(), false);

			// create a ResourceContainer...
			s.resources = new ResourceContainer();

			// create a ResourceBundlePath...
			s.bundlePath = new ResourceBundlePath(configuration.getCompilerConfiguration(), null);

			if (ThreadLocalToolkit.getBenchmark() != null)
			{
				ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new InitialSetup()));
			}

            // load SWCs
			s.swcCache = new SwcCache();
            CompilerSwcContext swcContext = new CompilerSwcContext(true, true,
																   configuration.getCompatibilityVersionString());
	        // for compc the theme and include-libraries values have been purposely not passed in below.
	        // This is done because the theme attribute doesn't make sense and the include-libraries value
	        // actually causes issues when the default value is used with external libraries.
	        // FIXME: why don't we just get rid of these values from the configurator for compc?  That's a problem
	        // for include-libraries at least, since this value appears in flex-config.xml
            swcContext.load( compilerConfig.getLibraryPath(),
                             compilerConfig.getExternalLibraryPath(),
                             null,
                             null,
							 mappings,
							 I18nUtils.getTranslationFormat(compilerConfig),
							 s.swcCache );
            configuration.addExterns( swcContext.getExterns() );

			if (ThreadLocalToolkit.getBenchmark() != null)
			{
				ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new LoadedSWCs(swcContext.getNumberLoaded())));
			}

           	s.checksum = cfgbuf.checksum_ts() + swcContext.checksum();

            final SymbolTable symbolTable = SymbolTable.newSymbolTable(configuration);
			s.perCompileData = symbolTable.perCompileData;

			Map licenseMap = configuration.getLicensesConfiguration().getLicenseMap();

	        Map classes = new HashMap();
			s.nsComponents = API.setupNamespaceComponents(configuration, mappings, s.sourcePath, classes);
			API.setupClasses(configuration, s.sourcePath, classes);

            // compile
	        Map rbFiles = new HashMap();

            ThreadLocalToolkit.getPathResolver().addSinglePathResolver(new SourcePathResolver(s.sourcePath));
	        List units = flex2.compiler.API.compile(s.fileSpec, s.sourceList, classes.values(), s.sourcePath, s.resources, s.bundlePath, swcContext,
	                                                   symbolTable, configuration, compilers, new CompcPreLink(rbFiles, configuration.getIncludeResourceBundles()), licenseMap, new ArrayList()); // List<CompilationUnit>

	        s.units = units;
	        s.rbFiles = rbFiles;
    		s.sourcePath.clearCache();
    		s.bundlePath.clearCache();
    		s.resources.refresh();

    		s.classes = classes.keySet();

	        // export SWC
            flex2.compiler.swc.API.exportSwc( configuration, units, s.nsComponents, s.swcCache, s.rbFiles );

            if (s.outputName != null && ThreadLocalToolkit.errorCount() == 0)
            {
	            File file = FileUtil.openFile(s.outputName);
	            if (file != null && file.exists() && file.isFile())
	            {
		            s.outputName = FileUtil.getCanonicalPath(file);
		            ThreadLocalToolkit.log(new OutputMessage(s.outputName, Long.toString(file.length())));
	            }
            }
		}
        catch (ConfigurationException ex)
        {
            Compc.displayStartMessage();
            Compiler.processConfigurationException(ex, "compc");
        }
        catch (LicenseException ex)
        {
            ThreadLocalToolkit.logError(ex.getMessage());
        }
        catch (CompilerException ex)
        {
            assert ThreadLocalToolkit.errorCount() > 0;
        }
        catch (LinkerException ex)
        {
            assert ThreadLocalToolkit.errorCount() > 0;
        }
        catch (SwcException ex)
        {
            assert ThreadLocalToolkit.errorCount() > 0;
        }
        catch (Exception t) // IOException, Throwable
        {
            ThreadLocalToolkit.logError(t.getMessage());
            t.printStackTrace();
        }
	    finally
        {
			Benchmark benchmark = ThreadLocalToolkit.getBenchmark();
			if (benchmark != null)
			{
			    benchmark.totalTime();
			    benchmark.peakMemoryUsage(true);
			}

	        flex2.compiler.API.removePathResolver();
        }
	}

	private static String getPlayer()
	{
		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.startsWith("windows"))
		{
			return "SAFlashPlayer";
		}
		else if (osName.startsWith("mac os x"))
		{
			return "SAFlashPlayer";
		}
		else
		{
			return "flashplayer";
		}
	}

	private static void intro()
	{
	    LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();
	    System.out.println(l10n.getLocalizedTextString(new ShellMessage("fcsh", VersionInfo.buildMessage())));
	}

	private static void prompt()
	{
		System.out.print("(fcsh) ");
	}

	private static void cmdList()
	{
	    LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();
	    System.out.println(l10n.getLocalizedTextString(new CommandList()));
	}

}
