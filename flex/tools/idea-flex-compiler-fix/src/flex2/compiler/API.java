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

// AdobePatentID="B576"

// Original file can be found at:
// svn://opensource.adobe.com/svn/opensource/flex/sdk/tags/3.2.0.3958/modules/compiler/src/java/flex2/compiler/API.java (revision 2721)

package flex2.compiler;

import flash.fonts.FontManager;
import flash.localization.LocalizationManager;
import flash.swf.Movie;
import flash.swf.MovieEncoder;
import flash.swf.TagEncoder;
import flash.util.FileUtils;

import flex.messaging.config.ServicesDependencies;

import flex2.compiler.as3.SignatureExtension;
import flex2.compiler.common.CompilerConfiguration;
import flex2.compiler.common.Configuration;
import flex2.compiler.config.ConfigurationException;
import flex2.compiler.common.FramesConfiguration;
import flex2.compiler.common.LocalFilePathResolver;
import flex2.compiler.common.PathResolver;
import flex2.compiler.common.SinglePathResolver;
import flex2.compiler.i18n.I18nUtils;
import flex2.compiler.io.FileUtil;
import flex2.compiler.io.InMemoryFile;
import flex2.compiler.io.LocalFile;
import flex2.compiler.io.ResourceFile;
import flex2.compiler.io.VirtualFile;
import flex2.compiler.util.*;
import flex2.linker.ConsoleApplication;
import flex2.tools.oem.ProgressMeter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import macromedia.asc.util.ContextStatics;

/**
 * compile()
 * encode()
 * useAS3()
 * useConsoleLogger()
 * usePathResolver()
 * loadCompilationUnits()
 * persistCompilationUnits()
 * getVirtualFile()
 * getVirtualFileList()
 * getNameMappings()
 * getSupportedMimeTypes()
 * <p/>
 * resolveMultiName()
 * retrieveTypeInfo()
 * setupHeadless
 * validateCompilationUnits()
 *
 * @author Clement Wong
 */
public final class API
{
	public static void useAS3()
	{
		// do this so there is no need to start java with -DAS3 and -DAVMPLUS...
		// this will likely not work in server environment.
		System.setProperty("AS3", "");
		System.setProperty("AVMPLUS", "");
	}

	public static void useConsoleLogger()
	{
		useConsoleLogger(true, true, true, true);
	}

	public static void useConsoleLogger(boolean isInfoEnabled, boolean isDebugEnabled, boolean isWarningEnabled, boolean isErrorEnabled)
	{
		ThreadLocalToolkit.setLogger(new Console(isInfoEnabled, isDebugEnabled, isWarningEnabled, isErrorEnabled));
	}

	public static void runBenchmark()
	{
		Benchmark b;
		try
		{
			Class benchmarkClass = Class.forName(System.getProperty("flex2.compiler.benchmark"),
												 true,
												 Thread.currentThread().getContextClassLoader());
			b = (Benchmark) benchmarkClass.newInstance();
		}
		catch (Exception e)
		{
			b = new Benchmark();
		}
		ThreadLocalToolkit.setBenchmark(b);
		ThreadLocalToolkit.resetBenchmark();
	}

	public static void disableBenchmark()
	{
		ThreadLocalToolkit.setBenchmark(null);
	}

	public static void usePathResolver()
	{
		usePathResolver(null);
	}
	
	public static void usePathResolver(SinglePathResolver resolver)
	{
		PathResolver pathResolver = new PathResolver();
		if (resolver != null)
		{
			pathResolver.addSinglePathResolver(resolver);			
		}		
		pathResolver.addSinglePathResolver( LocalFilePathResolver.getSingleton() );
		pathResolver.addSinglePathResolver( URLPathResolver.getSingleton() );
		ThreadLocalToolkit.setPathResolver(pathResolver);
	}

	public static void removePathResolver()
	{
		ThreadLocalToolkit.setPathResolver(null);
		ThreadLocalToolkit.resetResolvedPaths();
	}

	public static void setupHeadless(Configuration configuration)
	{
		if (configuration.getCompilerConfiguration().headlessServer())
		{
			try
			{
				// needed for J#, which does not support setProperty method on System
				java.util.Properties systemProps = java.lang.System.getProperties();
				systemProps.put("java.awt.headless", "true");
				java.lang.System.setProperties(systemProps);
			}
			catch (SecurityException securityException)
			{
				// log warning for users who need to set property via command line due to policy settings
				ThreadLocalToolkit.log(new UnableToSetHeadless());
			}
		}
	}

	public static NameMappings getNameMappings(Configuration configuration)
	{
		NameMappings mappings = new NameMappings();
		Map manifests = configuration.getCompilerConfiguration().getNamespacesConfiguration().getManifestMappings();
		if (manifests != null)
		{
			for (Iterator i = manifests.keySet().iterator(); i.hasNext();)
			{
				String ns = (String) i.next();
				VirtualFile path = (VirtualFile) manifests.get(ns);
				ManifestParser.parse(ns, path, mappings);
			}
		}
		return mappings;
	}

	private static final int preprocess                 = (1 << 1);
	private static final int parse1                     = (1 << 2);
	private static final int parse2                     = (1 << 3);
	private static final int analyze1                   = (1 << 4);
	private static final int analyze2                   = (1 << 5);
	private static final int analyze3                   = (1 << 6);
	private static final int analyze4                   = (1 << 7);
//	private static final int resolveInheritance         = (1 << 8);
//	private static final int sortInheritance            = (1 << 9);
	private static final int resolveType                = (1 << 10);
//	private static final int importType                 = (1 << 11);
//	private static final int resolveExpression          = (1 << 12);
	private static final int generate                   = (1 << 13);
	private static final int resolveImportStatements    = (1 << 14);
	private static final int adjustQNames               = (1 << 15);
	private static final int extraSources               = (1 << 16);

	private static void batch1(List sources, List units, DependencyGraph igraph, DependencyGraph dgraph,
	                           SymbolTable symbolTable, flex2.compiler.Compiler[] compilers, SourceList sourceList,
	                           SourcePath sourcePath, ResourceContainer resources, CompilerSwcContext swcContext,
	                           Configuration configuration)
	{
		int start = 0, end = sources.size();

		while (start < end)
		{
			if (!preprocess(sources, compilers, start, end, symbolTable.getSuppressWarningsIncremental()))
			{
				break;
			}

			if (tooManyErrors() || forcedToStop()) break;
			
			if (!parse1(sources, units, igraph, dgraph, compilers, symbolTable, start, end))
			{
				break;
			}

			if (tooManyErrors() || forcedToStop()) break;

			// C: context-free above this line...

			resolveInheritance(sources, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext, start, end);
			addGeneratedSources(sources, igraph, dgraph, resources, symbolTable, configuration, start, end);

			start = end;
			end = sources.size();

			if (start < end)
			{
				continue;
			}

			if (!sortInheritance(sources, units, igraph))
			{
				break;
			}

			if (!parse2(sources, compilers, symbolTable))
			{
				break;
			}

			if (tooManyErrors() || forcedToStop()) break;

			if (!analyze(sources, compilers, symbolTable, 1))
			{
				break;
			}

			if (tooManyErrors() || forcedToStop()) break;

			resolveNamespace(sources, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext, 0, end);
			addGeneratedSources(sources, igraph, dgraph, resources, symbolTable, configuration, 0, end);

			start = end;
			end = sources.size();

			if (start < end)
			{
				continue;
			}

			if (!analyze(sources, compilers, symbolTable, 2))
			{
				break;
			}

			if (tooManyErrors() || forcedToStop()) break;

			resolveType(sources, units, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext);

			final CompilerConfiguration config = (configuration != null) ? configuration.getCompilerConfiguration() : null;
			if (config != null && config.strict())
			{
				resolveImportStatements(sources, units, sourcePath, swcContext);
			}

			// C: If --coach is turned on, do resolveExpression() here...
			if (config != null && (config.strict() || config.warnings()))
			{
				resolveExpression(sources, units, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext, configuration);
			}

			start = end;
			end = sources.size();

			if (start < end)
			{
				continue;
			}

			if (!analyze(sources, compilers, symbolTable, 3))
			{
				break;
			}

			if (tooManyErrors() || forcedToStop()) break;

			if (!analyze(sources, compilers, symbolTable, 4))
			{
				break;
			}

			if (tooManyErrors() || forcedToStop()) break;

			if (!generate(sources, units, compilers, symbolTable))
			{
				break;
			}

			if (tooManyErrors() || forcedToStop()) break;

			markDone(sources, units);

			if (!postprocess(sources, units, compilers, symbolTable))
			{
				break;
			}

			if (tooManyErrors() || forcedToStop()) break;

			resolveExpression(sources, units, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext, configuration);
			addGeneratedSources(sources, igraph, dgraph, resources, symbolTable, configuration, 0, end);

			start = end;
			end = sources.size();
		}

		adjustQNames(units, igraph, symbolTable);
	}

	private static void batch2(List sources, List units, DependencyGraph igraph, DependencyGraph dgraph,
	                           SymbolTable symbolTable, flex2.compiler.Compiler[] compilers, SourceList sourceList,
	                           SourcePath sourcePath, ResourceContainer resources, CompilerSwcContext swcContext,
	                           Configuration configuration)
	{
		CompilerConfiguration config = (configuration != null) ? configuration.getCompilerConfiguration() : null;
		List targets = new ArrayList(sources.size());

		units.clear();
        for (int i = 0, size = sources.size(); i < size; i++)
        {
            Source s = (Source) sources.get(i);
            if (s != null && s.isCompiled())
            {
                units.add(s.getCompilationUnit());
            }
            else
            {
                units.add(null);
            }
        }
		
		while (nextSource(sources, units, igraph, dgraph, targets, symbolTable, configuration) > 0)
		{
			int postprocessCount = 0;

			// 1. targets.size() == sources.size()
			// 2. targets.get(i) == sources.get(i) or targets.get(i) == null
			for (int i = 0, size = targets.size(); i < size; i++)
			{
				Source s = (Source) targets.get(i);
				if (s == null) continue;

				int w = getCompilationUnitWorkflow(s);

				if ((w & preprocess) == 0)
				{
					// C: it returns false if it errors. There is no need to catch that. It's okay to
					//    keep going because findSources() takes into account of errors.
					preprocess(sources, compilers, i, i + 1, symbolTable.getSuppressWarningsIncremental());
				}
				else if ((w & parse1) == 0)
				{
					parse1(sources, units, igraph, dgraph, compilers, symbolTable, i, i + 1);
					resolveInheritance(sources, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext, i, i + 1);
					addGeneratedSources(sources, igraph, dgraph, resources, symbolTable, configuration, i, i + 1);
				}
				else if ((w & parse2) == 0)
				{
					parse2(sources, compilers, symbolTable, i, i + 1);
					addGeneratedSources(sources, igraph, dgraph, resources, symbolTable, configuration, i, i + 1);
				}
				else if ((w & analyze1) == 0)
				{
					// analyze1
					analyze(sources, compilers, symbolTable, i, i + 1, 1);
					resolveNamespace(sources, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext, i, i + 1);
					addGeneratedSources(sources, igraph, dgraph, resources, symbolTable, configuration, i, i + 1);
				}
				else if ((w & analyze2) == 0)
				{
					// analyze2
					analyze(sources, compilers, symbolTable, i, i + 1, 2);
					resolveType(sources, units, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext, i, i + 1);

					if (config.strict())
					{
						resolveImportStatements(sources, units, sourcePath, swcContext, i, i + 1);
					}

					// C: we don't need this batch1-based memory optimization.
					// if (config.strict() || config.coach())
					{
						resolveExpression(sources, units, igraph, dgraph, symbolTable, sourceList, sourcePath, resources,
						                  swcContext, configuration, i, i + 1);
					}
				}
				else if ((w & analyze3) == 0)
				{
					// analyze3
					analyze(sources, compilers, symbolTable, i, i + 1, 3);
				}
				else if ((w & analyze4) == 0)
				{
					// analyze4
					analyze(sources, compilers, symbolTable, i, i + 1, 4);
				}
				else if ((w & generate) == 0)
				{
					// generate
					generate(sources, units, compilers, symbolTable, i, i + 1);
					addGeneratedSources(sources, igraph, dgraph, resources, symbolTable, configuration, i, i + 1);
					resolveExpression(sources, units, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext, configuration, i, i + 1);
					markDone(sources, units, i, i + 1);
				}

				if (tooManyErrors() || forcedToStop()) break;

				if ((w & generate) != 0)
				{
					// postprocess
					postprocess(sources, units, compilers, symbolTable, i, i + 1);
					resolveExpression(sources, units, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext, configuration, i, i + 1);

					postprocessCount++;
				}

				if (tooManyErrors() || forcedToStop()) break;
			}

			// If all of them are doing postprocessing and they're not resolving and bringing in more source files,
			// we can call the compilation done and it should exit the loop.
			if ((postprocessCount == targets.size() && sources.size() == targets.size()) || tooManyErrors() || forcedToStop())
			{
				break;
			}
		}

		adjustQNames(units, igraph, symbolTable);
	}

	private static int nextSource(List sources, List units, DependencyGraph igraph, DependencyGraph dgraph,
								  List targets, SymbolTable symbolTable, Configuration configuration)
	{
		int count = 0, isDone = 0;
		boolean strict = configuration.getCompilerConfiguration().strict();
		boolean warnings = configuration.getCompilerConfiguration().warnings();
		int factor = configuration.getCompilerConfiguration().factor();

		targets.clear();
        
		// if 'targets' is smaller than 'sources', fill it up.
		for (int i = targets.size(), size = sources.size(); i < size; i++)
		{
			targets.add(null);
		}

		// Map notOkay = new HashMap();
		Set processed = new HashSet();

		for (int i = sources.size() - 1; i >= 0; i--)
		{
			Source s = (Source) sources.get(i);
			CompilationUnit u = s != null ? s.getCompilationUnit() : null;
			int w = getCompilationUnitWorkflow(s);

			if (w == 0 || (w & preprocess) == 0 || (w & parse1) == 0)
			{
				// anything before 'parse2' requires no dependencies
				boolean okay = s.getLogger() == null || s.getLogger().errorCount() == 0;

				if (okay)
				{
					targets.set(i, s);
					count++;
				}
				/*
				else
				{
					notOkay.put(s, "1");
				}
				*/
			}
			else if ((w & parse2) == 0)
			{
				boolean okay = ((s.getLogger() == null || s.getLogger().errorCount() == 0) &&
								check(u, u.inheritance, symbolTable, parse2));

				if (okay)
				{
					targets.set(i, s);
					count++;
				}
            }
			else if ((w & analyze1) == 0)
			{
				boolean okay = (s.getLogger() == null || s.getLogger().errorCount() == 0);

				if (okay)
				{
					targets.set(i, s);
					count++;
				}
            }
			else if ((w & analyze2) == 0)
			{
				// analyze1 --> analyze2? focus on inheritance and namespaces
				//
				// 1. get their workflow values... must be greater than or equal to analyze2.
				// 2. CompilationUnit.typeinfo must be present.
				// 3. error count must be zero.

				boolean okay =  (s.getLogger() == null || s.getLogger().errorCount() == 0) &&
								checkInheritance(u, u.inheritance, symbolTable, analyze2, processed) &&
								check(u, u.namespaces, symbolTable, analyze2);
				processed.clear();

				if (okay)
				{
					targets.set(i, s);
					count++;
				}
				/*
				else
				{
					notOkay.put(s, "2");
				}
				*/
			}
			else if ((w & analyze3) == 0)
			{
				// analyze2 --> analyze3? focus on types, expressions and namespaces
				//
				// 1. get their workflow values... must be greater than or equal to analyze3.
				// 2. CompilationUnit.typeinfo must be present.
				// 3. error count must be zero.

				boolean okay =  (s.getLogger() == null || s.getLogger().errorCount() == 0) &&
								checkInheritance(u, u.inheritance, symbolTable, analyze3, processed) &&
								check(u, u.types, symbolTable, analyze2) &&
								check(u, u.namespaces, symbolTable, analyze3) &&
								((!strict && !warnings) || check(u, u.expressions, symbolTable, analyze2));
				processed.clear();

				if (okay)
				{
					targets.set(i, s);
					count++;
				}
				/*
				else
				{
					notOkay.put(s, "3");
				}
				*/
			}
			else if ((w & analyze4) == 0)
			{
				// analyze3 --> analyze4?
				//
				// 1. get their workflow values... must be greater than or equal to analyze4.
				// 3. error count must be zero.

				boolean okay =  (s.getLogger() == null || s.getLogger().errorCount() == 0) &&
								checkInheritance(u, u.inheritance, symbolTable, analyze4, processed) &&
								check(u, u.namespaces, symbolTable, analyze4) &&
								checkDeep(u, u.types, symbolTable, processed) &&
								((!strict && !warnings) || checkDeep(u, u.expressions, symbolTable, processed));
				processed.clear();

				if (okay)
				{
					targets.set(i, s);
					count++;
				}
				/*
				else
				{
					notOkay.put(s, "4");
				}
				*/
			}
			else if ((w & generate) == 0)
			{
				// analyze4 --> generate
				//
				// 1. error count must be zero.

				if ((s.getLogger() == null) || (s.getLogger().errorCount() == 0))
				{
					targets.set(i, s);
					count++;
				}
				/*
				else
				{
					notOkay.put(s, "5");
				}
				*/
			}
			else
			{
				isDone = (s.getLogger() == null || s.getLogger().errorCount() == 0) ? isDone + 1 : isDone;
			}
		}

		if (count > 0)
		{
			boolean[] bits = new boolean[targets.size()];
			double maxBudget = 100, budget = 0;

			// Preferences
			//
			// 1. Compiler.generate()
			// 2. Compiler.analyze3()
			// 3. Compiler.analyze4()
			// 4. Compiler.analyze1()
			// 5. Compiler.preprocess()
			// 6. Compiler.analyze2() for .abc
			// 7. Compiler.parse2() for .abc
			// 8. Compiler.parse1() for .abc
			// 9. Compiler.analyze2() for .as and .mxml
			// 10. Compiler.parse2() for .as and .mxml
			// 11. Compiler.parse1() for .as and .mxml

			
			// 1. Compiler.generate()
			for (int i = targets.size() - 1; i >= 0 && budget < maxBudget; i--)
			{
				Source s = (Source) targets.get(i);
				if (s == null) continue;

				int w = getCompilationUnitWorkflow(s);
				if (w != 0 &&
					(w & preprocess) != 0 &&
					(w & parse1) != 0 &&
					(w & parse2) != 0 &&
					(w & analyze1) != 0 &&
					(w & analyze2) != 0 &&
					(w & analyze3) != 0 &&
					(w & analyze4) != 0 &&
					(w & generate) == 0)
				{
					bits[i] = true;
				}
			}

			// 2. Compiler.analyze3()
			for (int i = targets.size() - 1; i >= 0 && budget < maxBudget; i--)
			{
				Source s = (Source) targets.get(i);
				if (s == null) continue;

				int w = getCompilationUnitWorkflow(s);
				if (w != 0 &&
					(w & preprocess) != 0 &&
					(w & parse1) != 0 &&
					(w & parse2) != 0 &&
					(w & analyze1) != 0 &&
					(w & analyze2) != 0 &&
					(w & analyze3) == 0)
				{
					bits[i] = true;
				}
			}

			// 3. Compiler.analyze4()
			for (int i = targets.size() - 1; i >= 0 && budget < maxBudget; i--)
			{
				Source s = (Source) targets.get(i);
				if (s == null) continue;

				int w = getCompilationUnitWorkflow(s);
				if (w != 0 &&
					(w & preprocess) != 0 &&
					(w & parse1) != 0 &&
					(w & parse2) != 0 &&
					(w & analyze1) != 0 &&
					(w & analyze2) != 0 &&
					(w & analyze3) != 0 &&
					(w & analyze4) == 0)
				{
					bits[i] = true;
				}
			}

			// 4. Compiler.analyze1()
			for (int i = targets.size() - 1; i >= 0 && budget < maxBudget; i--)
			{
				Source s = (Source) targets.get(i);
				if (s == null) continue;

				int w = getCompilationUnitWorkflow(s);
				if (w != 0 &&
					(w & preprocess) != 0 &&
					(w & parse1) != 0 &&
					(w & parse2) != 0 &&
					(w & analyze1) == 0)
				{
					bits[i] = true;
				}
			}

			// 5. Compiler.preprocess()
			for (int i = targets.size() - 1; i >= 0 && budget < maxBudget; i--)
			{
				Source s = (Source) targets.get(i);
				if (s == null) continue;

				int w = getCompilationUnitWorkflow(s);
				if (w == 0)
				{
					bits[i] = true;
				}
			}

			// 6. Compiler.analyze2() for .abc
			for (int i = targets.size() - 1; i >= 0 && budget < maxBudget; i--)
			{
				Source s = (Source) targets.get(i);
				if (s == null) continue;

				int w = getCompilationUnitWorkflow(s);
				if (w != 0 &&
					(w & preprocess) != 0 &&
					(w & parse1) != 0 &&
					(w & parse2) != 0 &&
					(w & analyze1) != 0 &&
					(w & analyze2) == 0)
				{
					if (MimeMappings.ABC.equals(s.getMimeType()))
					{
						bits[i] = true;
					}
				}
			}

			// 7. Compiler.parse2() for .abc
			for (int i = targets.size() - 1; i >= 0 && budget < maxBudget; i--)
			{
				Source s = (Source) targets.get(i);
				if (s == null) continue;

				int w = getCompilationUnitWorkflow(s);
				if (w != 0 &&
					(w & preprocess) != 0 &&
					(w & parse1) != 0 &&
					(w & parse2) == 0)
				{
					if (MimeMappings.ABC.equals(s.getMimeType()))
					{
						bits[i] = true;
					}
				}
			}

			// 8. Compiler.parse1() for .abc
			for (int i = targets.size() - 1; i >= 0 && budget < maxBudget; i--)
			{
				Source s = (Source) targets.get(i);
				if (s == null) continue;

				int w = getCompilationUnitWorkflow(s);
				if (w != 0 &&
					(w & preprocess) != 0 &&
					(w & parse1) == 0)
				{
					if (MimeMappings.ABC.equals(s.getMimeType()))
					{
						bits[i] = true;
					}
				}
			}

			// 9. Compiler.analyze2() for .as and .mxml
			for (int i = targets.size() - 1; i >= 0 && budget < maxBudget; i--)
			{
				Source s = (Source) targets.get(i);
				if (s == null) continue;

				int w = getCompilationUnitWorkflow(s);
				if (w != 0 &&
					(w & preprocess) != 0 &&
					(w & parse1) != 0 &&
					(w & parse2) != 0 &&
					(w & analyze1) != 0 &&
					(w & analyze2) == 0)
				{
					if (!MimeMappings.ABC.equals(s.getMimeType()))
					{
						budget += calculateBudget(s, factor);
						bits[i] = true;
					}
				}
			}

			// 10. Compiler.parse2() for .as and .mxml
			for (int i = targets.size() - 1; i >= 0 && budget < maxBudget; i--)
			{
				Source s = (Source) targets.get(i);
				if (s == null) continue;

				int w = getCompilationUnitWorkflow(s);
				if (w != 0 &&
					(w & preprocess) != 0 &&
					(w & parse1) != 0 &&
					(w & parse2) == 0)
				{
					if (!MimeMappings.ABC.equals(s.getMimeType()))
					{
						budget += calculateBudget(s, factor);
						bits[i] = true;
					}
				}
			}

			// 11. Compiler.parse1() for .as and .mxml
			for (int i = targets.size() - 1; i >= 0 && budget < maxBudget; i--)
			{
				Source s = (Source) targets.get(i);
				if (s == null) continue;

				int w = getCompilationUnitWorkflow(s);
				if (w != 0 &&
					(w & preprocess) != 0 &&
					(w & parse1) == 0)
				{
					if (!MimeMappings.ABC.equals(s.getMimeType()))
					{
						budget += calculateBudget(s, factor);
						bits[i] = true;
					}
				}
			}

			count = 0;
			for (int i = 0, size = bits.length; i < size; i++)
			{
				if (!bits[i])
				{
					targets.set(i, null);
				}
				else
				{
					count++;
				}
			}
		}
		else if (count == 0 && isDone == sources.size())
		{
			// successful... start postprocessing. batch2() won't call nextSource() again if postprocess()
			// stops generating new Sources...
			targets.clear();
			targets.addAll(sources);
			count = targets.size();
		}
		else if (count == 0 && isDone != sources.size())
		{
			// problem...
			//
			// 1. detect circular inheritance
			// 2. what else?
			detectCycles(sources, igraph);
			assert ThreadLocalToolkit.errorCount() > 0 : "There is a problem in one of the compiler algorithms. Please use --conservative=true to compile. Also, please file a bug report.";
		}

		// C: sources.size() == targets.size() when this returns.
		return count;
	}

	private static double calculateBudget(Source s, int factor)
	{
		String mimeType = s.getMimeType();

		if (MimeMappings.MXML.equals(mimeType))
		{
			return s.size() * 4.5 / factor;
		}
		else if (MimeMappings.AS.equals(mimeType))
		{
			return s.size() / factor;
		}
		else
		{
			return 0;
		}
	}

	private static int calculateCheckBitsMask(int id, int workflow)
	{
		int j = id - 1;
		int k = 0;

		switch (workflow)
		{
			case parse2: k = 0; break;
			case analyze2: k = 1; break;
			case analyze3: k = 2; break;
			case analyze4: k = 3; break;
			default: assert false;
		}

		return 1 << (j * 4 + k);
	}

	private static boolean check(CompilationUnit unit, MultiNameSet types, SymbolTable symbolTable, int workflow)
	{
		// 1. inheritance, 2. namespaces, 3. types, 4. expressions
		int mask = calculateCheckBitsMask(types.getId(), workflow);

		if ((unit.checkBits & mask) > 0)
		{
			return true;
		}

		for (Iterator i = types.iterator(); i.hasNext();)
		{
			Object name = i.next();

			if (name instanceof flex2.compiler.util.QName)
			{
				flex2.compiler.util.QName qName = (flex2.compiler.util.QName) name;
				Source s = symbolTable.findSourceByQName(qName);
				CompilationUnit u = (s != null) ? s.getCompilationUnit() : null;

				// a compilation unit should not have itself as the dependency.
				// let's continue and let the compiler catch the problem later.
				if (unit == u)
				{
					continue;
				}

				// workflow
				if (u == null || (u.getWorkflow() & workflow) == 0)
				{
					return false;
				}

				// type info
				if (u == null || u.typeInfo == null)
				{
					return false;
				}

				// error count
				if (s.getLogger() != null && s.getLogger().errorCount() > 0)
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}

		unit.checkBits |= mask;
		return true;
	}

	// For CompilationUnit.inheritance
	private static boolean checkInheritance(CompilationUnit unit, MultiNameSet types, SymbolTable symbolTable, int workflow, Set processed)
	{
		// 1. inheritance

		// Don't short circuit if the CompilationUnit has already been
		// checked, because we still need to check the rest of the
		// inheritance tree.  This is due to possibility that there is
		// an mxml document, which has been reset, higher up the
		// chain.  We want to return false in that case, so we don't
		// let the CompilationUnit continue to the next phase until
		// the mxml document catches back up.

		processed.add(unit.getSource().getName());

		if (!check(unit, types, symbolTable, workflow))
		{
			return false;
		}

		for (Iterator i = types.iterator(); i.hasNext();)
		{
			Object name = i.next();

			if (name instanceof flex2.compiler.util.QName)
			{
				flex2.compiler.util.QName qName = (flex2.compiler.util.QName) name;
				Source s = symbolTable.findSourceByQName(qName);
				CompilationUnit u = (s != null) ? s.getCompilationUnit() : null;

				if (u == null)
				{
					return false;
				}
				
				// a compilation unit should not have itself as the dependency.
				// let's continue and let the compiler catch the problem later.
				if (unit == u || processed.contains(s.getName()))
				{
					continue;
				}

				if (!checkInheritance(u, u.inheritance, symbolTable, workflow, processed))
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}

		return true;
	}

	// For CompilationUnit.types and CompilationUnit.expressions
	private static boolean checkDeep(CompilationUnit unit, MultiNameSet types, SymbolTable symbolTable, Set processed)
	{
		// 3. types, 4. expressions
		int mask = calculateCheckBitsMask(types.getId(), analyze3);		
		
		if ((unit.checkBits & mask) > 0)
		{
			return true;
		}

		processed.add(unit.getSource().getName());

		if (!check(unit, types, symbolTable, analyze3))
		{
			return false;
		}

		for (Iterator i = types.iterator(); i.hasNext();)
		{
			Object name = i.next();

			if (name instanceof flex2.compiler.util.QName)
			{
				flex2.compiler.util.QName qName = (flex2.compiler.util.QName) name;
				Source s = symbolTable.findSourceByQName(qName);
				CompilationUnit u = (s != null) ? s.getCompilationUnit() : null;

				if (u == null)
				{
					return false;
				}
				
				// a compilation unit should not have itself as the dependency.
				// let's continue and let the compiler catch the problem later.
				if (unit == u || processed.contains(s.getName()))
				{
					continue;
				}

				if (!checkDeep(u, u.inheritance, symbolTable, processed))
				{
					return false;
				}

				if (!checkDeep(u, u.types, symbolTable, processed))
				{
					return false;
				}

				if (!checkDeep(u, u.expressions, symbolTable, processed))
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}

		unit.checkBits |= mask;
		return true;
	}

	private static int getCompilationUnitWorkflow(Source s)
	{
		if (!s.isPreprocessed())
		{
			return 0;
		}
		else if (s.getCompilationUnit() == null || (s.getCompilationUnit().getWorkflow() & parse1) == 0)
		{
			return preprocess;
		}
		else
		{
			return s.getCompilationUnit().getWorkflow();
		}
	}

	private static void batch(List sources, List units, DependencyGraph igraph, DependencyGraph dgraph, SymbolTable symbolTable,
							  Compiler[] compilers, SourceList sourceList, SourcePath sourcePath, ResourceContainer resources,
							  CompilerSwcContext swcContext, Configuration configuration, boolean useFileSpec)
		throws CompilerException
	{
		do
		{
			units.clear();
			if (useFileSpec || configuration.getCompilerConfiguration().useConservativeAlgorithm())
			{
				batch1(sources, units, igraph, dgraph, symbolTable, compilers, sourceList, sourcePath, resources, swcContext, configuration);
			}
			else
			{
				batch2(sources, units, igraph, dgraph, symbolTable, compilers, sourceList, sourcePath, resources, swcContext, configuration);
			}
			symbolTable.perCompileData.reuse();

			if (swcContext.errorLocations().size() > 0)
			{
				for (Iterator it = swcContext.errorLocations().iterator(); it.hasNext();)
				{
					ThreadLocalToolkit.log(new IncompatibleSWCArchive((String) it.next()));
				}
			}

			if (ThreadLocalToolkit.errorCount() > 0)
			{
				throw new CompilerException();
			}
			
			if (forcedToStop()) break;
		}
		while (unitsReset(units) > 0);
	}

	public static List compileSwc(FileSpec fileSpec,
								  Collection classes,
								  SourcePath sourcePath,
								  ResourceContainer resources,
								  ResourceBundlePath bundlePath,
								  CompilerSwcContext swcContext,
								  SymbolTable symbolTable,
								  Configuration configuration,
								  flex2.compiler.Compiler[] compilers,
								  PreLink preLink,
								  Map licenseMap)
		throws LicenseException, CompilerException
	{
		return compile(fileSpec,
					   null,
					   classes,
					   sourcePath,
					   resources,
					   bundlePath,
					   swcContext,
					   symbolTable,
					   configuration,
					   compilers,
					   preLink,
					   licenseMap,
					   new ArrayList());
	}

	public static List compile(FileSpec fileSpec,
							   SourceList sourceList,
							   SourcePath sourcePath,
							   ResourceContainer resources,
							   ResourceBundlePath bundlePath,
							   CompilerSwcContext swcContext,
							   SymbolTable symbolTable,
							   Configuration configuration,
							   flex2.compiler.Compiler[] compilers,
							   PreLink preLink,
							   Map licenseMap)
		throws LicenseException, CompilerException
	{
		return compile(fileSpec,
				   sourceList,
				   null,
				   sourcePath,
				   resources,
				   bundlePath,
				   swcContext,
				   symbolTable,
				   configuration,
				   compilers,
				   preLink,
				   licenseMap,
				   new ArrayList());		
	}
	
	// full compilation
	public static List compile(FileSpec fileSpec,
							   SourceList sourceList,
							   Collection classes,
							   SourcePath sourcePath,
							   ResourceContainer resources,
							   ResourceBundlePath bundlePath,
							   CompilerSwcContext swcContext,
							   Configuration configuration,
							   flex2.compiler.Compiler[] compilers,
							   PreLink preLink,
							   Map licenseMap,
							   List sources)
		throws LicenseException, CompilerException
	{
		return compile(fileSpec,
					   sourceList,
					   classes,
					   sourcePath,
					   resources,
					   bundlePath,
					   swcContext,
					   SymbolTable.newSymbolTable(configuration),
					   configuration,
					   compilers,
					   preLink,
					   licenseMap,
					   sources);
	}

	// incremental compilation
	public static List compile(FileSpec fileSpec,
							   SourceList sourceList,
							   Collection classes,
							   final SourcePath sourcePath,
							   ResourceContainer resources,
							   ResourceBundlePath bundlePath,
							   CompilerSwcContext swcContext,
							   SymbolTable symbolTable,
							   Configuration configuration,
							   flex2.compiler.Compiler[] compilers,
							   PreLink preLink,
							   Map licenseMap,
							   List sources)
		throws LicenseException, CompilerException
	{	    
		// C: display any SourcePath-related warnings before starting to compile.
		if (sourcePath != null)
		{
            ThreadLocalToolkit.getPathResolver().addSinglePathResolver(new SinglePathResolver() {
                // Implementation of resolve() method is the same as in svn://opensource.adobe.com/svn/opensource/flex/sdk/trunk/modules/compiler/src/java/flex2/compiler/SourcePath.java (revision 4208)
                public VirtualFile resolve(String path) {
                    if (path.charAt(0) == '/') {
                        String relativePath = path.substring(1);

                        for (File directory : (Iterable<File>)sourcePath.getPaths()) {
                            File file = FileUtil.openFile(directory, relativePath);

                            if ((file != null) && file.exists()) {
                                return new LocalFile(file);
                            }
                        }
                    }

                    return null;
                }
            });
			sourcePath.displayWarnings();
		}
		
		LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();

		ProgressMeter meter = ThreadLocalToolkit.getProgressMeter();

		if (meter != null)
		{
			meter.start();
		}

		// List sources = new ArrayList();
		List units = new ArrayList(); // List<CompilationUnit>
		DependencyGraph igraph = new DependencyGraph();
		DependencyGraph dgraph = null; // new DependencyGraph();

		boolean useFileSpec = false;

		// based on the starting source file, retrieve a list of dependent files.
		if (fileSpec != null)
		{
			sources.addAll(fileSpec.retrieveSources()); // List<Source>
			useFileSpec = sources.size() > 0;
		}

		if (sourceList != null)
		{
			sources.addAll(sourceList.retrieveSources());
		}

		// C: This is here for SWC compilation.
		if (classes != null)
		{
			sources.addAll(classes);
			useFileSpec = useFileSpec || classes.size() > 0;
		}

		// add the sources to the dependency graphs as vertices.
		addVerticesToGraphs(sources, igraph, dgraph);

		try
		{
			getCommonBuiltinClasses(sources, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext);

			//	build unit list
			batch(sources, units, igraph, dgraph, symbolTable, compilers, sourceList, sourcePath, resources, swcContext, configuration, useFileSpec);

			// enterprise messaging classes referenced by the messaging config file
			getMessagingClasses(sources, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext, configuration);

			// unconditionally includes classes specified by --includes.
			getIncludeClasses(sources, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext, configuration);

			// backward compatibility
			getIncludeResources(sources, igraph, dgraph, bundlePath, symbolTable, swcContext, configuration);

            // getMessagingClasses, and getIncludeClasses may produce errors. check them...
            if (ThreadLocalToolkit.errorCount() > 0)
            {
                throw new CompilerException();
            }
            
            if (forcedToStop()) return units;

            // compile additional sources before running prelink so that all metadata-fed lists
            // contributing to codegen (i.e. mixins) are complete
            batch(sources, units, igraph, dgraph, symbolTable, compilers, sourceList, sourcePath, resources, swcContext, configuration, useFileSpec);

            if (forcedToStop()) return units;

            if (preLink != null)
			{
				// run the prelink step...
				preLink.run(sources, units, fileSpec, sourceList, sourcePath, bundlePath, resources, symbolTable, swcContext, configuration);

				// prelink also may produce errors
				if (ThreadLocalToolkit.errorCount() > 0)
				{
					throw new CompilerException();
				}

				// prelink introduces more sources, so we compile again
				batch(sources, units, igraph, dgraph, symbolTable, compilers, sourceList, sourcePath, resources, swcContext, configuration, useFileSpec);
			}

			// loader classes, licensing classes, extra classes
			//
            int count = 0;         // just in case something impossibly odd happens,
            while (++count < 1000) // don't wedge the compiler forever.
            {
                int numSources = sources.size();
                getExtraSources(sources, igraph, dgraph, sourceList, sourcePath, resources, bundlePath, symbolTable, swcContext,
                				configuration, licenseMap);

    			// getExtraSources may produce errors. check them...
    			if (ThreadLocalToolkit.errorCount() > 0)
    			{
    				throw new CompilerException();
    			}

    			// getExtraSources pulls in more classes, compile again
    			batch(sources, units, igraph, dgraph, symbolTable, compilers, sourceList, sourcePath, resources, swcContext, configuration, useFileSpec);

                if (sources.size() == numSources)
                {
                    break;
                }

                if (forcedToStop()) return units;
            }
            
			checkResourceBundles(sources, symbolTable);
            assert count < 1000;
        }
		finally
		{
			if (ThreadLocalToolkit.getBenchmark() != null)
			{
				ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new OutputTime(sources.size())));
			}

			// must close swc file handles...
			swcContext.close();
			symbolTable.cleanClassTable();
			symbolTable.adjustProgress();

			if (meter != null)
			{
				meter.end();
			}
		}

        return units;
	}

	private static final MultiName[] multiNames = new MultiName[]
	{
		new MultiName(SymbolTable.OBJECT),
		new MultiName(SymbolTable.CLASS),
		new MultiName(SymbolTable.FUNCTION),
		new MultiName(SymbolTable.BOOLEAN),
		new MultiName(SymbolTable.NUMBER),
		new MultiName(SymbolTable.STRING),
		new MultiName(SymbolTable.ARRAY),
		new MultiName(SymbolTable.INT),
		new MultiName(SymbolTable.UINT),
		new MultiName(SymbolTable.NAMESPACE),
		new MultiName(SymbolTable.REGEXP),
		new MultiName(SymbolTable.XML),
		new MultiName(SymbolTable.XML_LIST),
	};

	private static void getCommonBuiltinClasses(List sources, DependencyGraph igraph, DependencyGraph dgraph,
	                                            SymbolTable symbolTable, SourceList sourceList, SourcePath sourcePath,
	                                            ResourceContainer resources, CompilerSwcContext swcContext)
	{
		for (int i = 0, size = multiNames.length; i < size; i++)
		{
			QName qName = resolveMultiName("builtin", multiNames[i], sources, sourceList, sourcePath, resources, swcContext, symbolTable);
			if (qName != null)
			{
				Source tailSource = symbolTable.findSourceByQName(qName);
				addVertexToGraphs(tailSource, tailSource.getCompilationUnit(), igraph, dgraph);
			}
		}
	}

	private static void getMessagingClasses(List sources, DependencyGraph igraph, DependencyGraph dgraph,
	                                        SymbolTable symbolTable, SourceList sourceList, SourcePath sourcePath,
	                                        ResourceContainer resources, CompilerSwcContext swcContext,
	                                        Configuration configuration)
	{
		// The enterprise messaging config file may refer to some classes. We want to load them up-front.
		ServicesDependencies services = configuration.getCompilerConfiguration().getServicesDependencies();
		if (services != null)
		{
			for (Iterator i = services.getChannelClasses().iterator(); i.hasNext();)
			{
				String clientType = (String) i.next();

				if (clientType != null)
				{
					QName qName = resolveMultiName("messaging", new MultiName(NameFormatter.toColon(clientType)), sources,
					                               sourceList, sourcePath, resources, swcContext, symbolTable);

					if (qName == null)
					{
						ThreadLocalToolkit.log(new ChannelDefinitionNotFound(clientType),
											   configuration.getCompilerConfiguration().getServices().getNameForReporting());
					}
					else
					{
						Source s = symbolTable.findSourceByQName(qName);
						addVertexToGraphs(s, s.getCompilationUnit(), igraph, dgraph);
					}
				}
			}
		}
	}

	/**
	 * reset non-internal units that have failed to produce bytecode.
	 * NOTE: contract is that all units from non-internal sources will eventually produce either bytecode or an error,
	 * after a finite number of resets.
	 */
	private static int unitsReset(List units)
	{
		int resetCount = 0;
		for (int i = 0, n = units.size(); i < n; i++)
		{
			CompilationUnit unit = (CompilationUnit) units.get(i);
            Source source = unit.getSource();

            if (!source.isInternal() && !source.isCompiled())
            {
                unit.reset();
                resetCount++;
            }
            else if (!source.isInternal())
            {
            	unit.checkBits = 0;
            }
		}
		return resetCount;
	}

	public static int validateCompilationUnits(FileSpec fileSpec, SourceList sourceList, SourcePath sourcePath,
											   ResourceBundlePath bundlePath, ResourceContainer resources,
											   CompilerSwcContext swcContext, ContextStatics perCompileData,
											   boolean recompile, Configuration configuration)
	{
		final LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();

		final boolean strict = configuration.getCompilerConfiguration().strict();
        
        final Map updated                     = new HashMap(), // VirtualFile.getName(), Source
                  updatedWithStableSignature  = new HashMap(), // VirtualFile.getName(), Source
                  affected                    = new HashMap(), // VirtualFile.getName(), Source
                  deleted                     = new HashMap(), // VirtualFile.getName(), Source
                  reasons                     = new HashMap(), // VirtualFile.getName(), String
                  qnames                      = new HashMap(); // QName, VirtualFile.getName()
        
        final Set includeUpdated              = new HashSet(), // VirtualFile.getName()
                  resourceDelegates           = new HashSet(), // VirtualFile.getNameForReporting()
                  namespaces                  = new HashSet();

        // put all the Source objects together
        final List sources = new ArrayList();
        {
            if (fileSpec != null)
                sources.addAll(fileSpec.sources());
            if (sourceList != null)
                sources.addAll(sourceList.sources());
            if (sourcePath != null)
                sources.addAll(sourcePath.sources().values());
            if (bundlePath != null)
                sources.addAll(bundlePath.sources().values());
        }

		// if any of the Source objects in ResourceContainer is bad, obsolete the originating Source.
		for (Iterator i = resources.sources().iterator(); i.hasNext(); )
		{
			Source s = (Source) i.next();

			if (s != null)
			{
			    CompilationUnit u = (s != null) ? s.getCompilationUnit() : null;
				if (s.hasError()
                        || (u != null && !u.isDone())
                        || s.isUpdated()
                        || (u != null && u.getAssets().isUpdated()))
				{
					resourceDelegates.add(s.getNameForReporting());
					s.removeCompilationUnit();
				}
			}
		}

		// collect the names of all the update file includes...
		for (int i = 0, size = sources.size(); i < size; i++)
		{
			Source s = (Source) sources.get(i);
			for (Iterator j = s.getUpdatedFileIncludes(); j != null && j.hasNext();)
			{
				VirtualFile f = (VirtualFile) j.next();
				includeUpdated.add(f.getNameForReporting());
			}
		}

		// if any one of the files in the filespec has changed, the entire app must be recompiled...
		// no need to null out ResourceContainer here because stuff in ResourceContainer will be replaced later.
		// - ForceRecompilation
        // - SourceFileChanged
        if (recompile || (fileSpec != null && fileSpec.isUpdated()))
        {
    		for (int i = 0, size = sources.size(); i < size; i++)
    		{
    			Source s = (Source) sources.get(i);
    			affected.put(s.getName(), s);
    			if (recompile)
    			{
    				reasons.put(s.getName(), l10n.getLocalizedTextString(new ForceRecompilation()));
    			}
    			else
    			{
    				reasons.put(s.getName(), l10n.getLocalizedTextString(new SourceFileChanged()));
    			}
    		}
            
    		// optimization -- if this block runs, all sources are invalidated anyways;
            // we get through the rest of the function a little faster if clear()ed
            sources.clear();
        }

		final DependencyGraph graph = new DependencyGraph();

		// build a dependency graph
		for (int i = 0, size = sources.size(); i < size; i++)
		{
			Source s = (Source) sources.get(i);
            if (s == null) continue;
            
			CompilationUnit u = s.getCompilationUnit();
            if (u == null) continue;

			graph.put(s.getName(), u);
			if (!graph.containsVertex(s.getName()))
			{
				graph.addVertex(new Vertex(s.getName()));
			}

			// register QName --> VirtualFile.getName()
			for (Iterator j = u.topLevelDefinitions.iterator(); j.hasNext();)
			{
				qnames.put(j.next(), s.getName());
			}
		}

		// setup inheritance-based dependencies...
		for (int i = 0, size = sources.size(); i < size; i++)
		{
            Source s = (Source) sources.get(i);
            if (s == null) continue;
            
            CompilationUnit u = s.getCompilationUnit();
            if (u == null) continue;

			String head = s.getName();
			for (Iterator k = u.inheritance.iterator(); k.hasNext();)
			{
				Object obj = k.next();

				if (obj instanceof QName)
				{
					QName qname = (QName) obj;
					String tail = (String) qnames.get(qname);

					if (tail != null && !head.equals(tail) && !graph.dependencyExists(head, tail))
					{
						graph.addDependency(head, tail);
					}
				}
			}
		}

        // identify obsolete CompilationUnit
        //   - NotFullyCompiled
        //   - SourceNoLongerExists
        //   - SourceFileUpdated
        //   - AssedUpdated
		for (int i = 0, size = sources.size(); i < size; i++)
		{
			Source s = (Source) sources.get(i);
            if (s == null) continue;
            
			CompilationUnit u = s.getCompilationUnit();

			if (s.hasError() || (u != null && !u.isDone()) || resourceDelegates.contains(s.getName()))
			{
				affected.put(s.getName(), s);
				reasons.put(s.getName(), l10n.getLocalizedTextString(new NotFullyCompiled()));
				sources.set(i, null);
			}
            else if (!s.exists())
            {
                updated.put(s.getName(), s);
                reasons.put(s.getName(), l10n.getLocalizedTextString(new SourceNoLongerExists()));
                deleted.put(s.getName(), s);

                if (u != null)
                {
                    for (Iterator j = u.topLevelDefinitions.iterator(); j.hasNext(); )
                    {
                        QName qName = (QName) j.next();
                        namespaces.add(qName.toString());
                    }
                }

                sources.set(i, null);
            }
            else if (s.isUpdated())
            {
                // signature optimization:
                //     read the old signature from the incremental cache
                //     generate a new signature from the current source
                //     compare -- if stable, we don't have to recompile dependencies
                boolean signatureIsStable = false;
                if ((u != null) && (!configuration.getCompilerConfiguration()
                                                  .getDisableIncrementalOptimizations())
                                // skip MXML sources:
                                //      MXML is too complicated to parse/codegen at this point in
                                //      order to generate and compare a new checksum
                                && (!s.getMimeType().equals(MimeMappings.MXML)))
                {
                    final Long persistedCRC = u.getSignatureChecksum();
                    if (persistedCRC != null)
                    {
                        assert (s.getMimeType().equals(MimeMappings.ABC) ||
                                s.getMimeType().equals(MimeMappings.AS));
                        
                        //TODO if we calculate a new checksum that does not match,
                        //     can we store this checksum and not recompute it later?
                        final Long currentCRC = computeSignatureChecksum(configuration, s);
                        signatureIsStable = (currentCRC != null) &&
                                            (persistedCRC.compareTo(currentCRC) == 0);
                        
                        // if (SignatureExtension.debug)
                        // {
                        //     final String name = u.getSource().getName();
                        //     SignatureExtension.debug("*** FILE UPDATED: Signature "
                        //                                    + (signatureIsStable ? "IS" : "IS NOT")
                        //                                    + " stable ***");
                        //     SignatureExtension.debug("PERSISTED CRC32: " + persistedCRC + "\t--> " + name);
                        //     SignatureExtension.debug("CURRENT   CRC32: " + currentCRC   + "\t--> " + name);
                        // }
                    }
                }
                
                // if the class signature is stable (it has not changed since the last compile)
                // then we can invalidate and recompile the updated unit alone
                // otherwise we default to a chain reaction, invalidating _all_ dependent units
                if (signatureIsStable)
                {
                    updatedWithStableSignature.put(s.getName(), s);
                }
                else
                {
                    updated.put(s.getName(), s);
                }
                
                reasons.put(s.getName(), l10n.getLocalizedTextString(new SourceFileUpdated()));
                sources.set(i, null);
            }
			else if (u != null && u.getAssets().isUpdated())
			{
				updated.put(s.getName(), s);
				reasons.put(s.getName(), l10n.getLocalizedTextString(new AssetUpdated()));
				sources.set(i, null);
			}
		}

		// permanently remove the deleted Source objects from SourcePath
		//
		// Note: this step is currently necessary because the location-updating loop that follows iterates over
		// 'sources', which has had deleted entries (among others) set to null in the loop above. So here we iterate
		// directly over the deleted entries. (Note also that 'reasons' already has an entry for this source.)
		//
		for (Iterator i = deleted.values().iterator(); i.hasNext(); )
		{
			Source s = (Source) i.next();
			if (s.isSourcePathOwner())
			{
				SourcePath sp = (SourcePath) s.getOwner();
				sp.removeSource(s);
			}
		}

		// Examine each Source object in SourcePath or ResourceBundlePath...
		// if a Source object in SourcePath or ResourceBundlePath is no longer the
		// first choice according to findFile, it should be removed... i.e. look for ambiguous sources
        // - NotSourcePathFirstPreference
		for (int i = 0, size = sources.size(); i < size; i++)
		{
			Source s = (Source) sources.get(i);

			if (s != null && (s.isSourcePathOwner() || s.isResourceBundlePathOwner()))
			{
				SourcePathBase sp = (SourcePathBase) s.getOwner();
				if (!sp.checkPreference(s))
				{
					affected.put(s.getName(), s);
					reasons.put(s.getName(), l10n.getLocalizedTextString(new NotSourcePathFirstPreference()));
					sources.set(i, null);
				}
			}
		}

		// invalidate the compilation unit if its dependencies are deleted
		// - DependentFileNoLongerExists
        // - InvalidImportStatement
		for (int i = 0, size = sources.size(); i < size; i++)
		{
            Source s = (Source) sources.get(i);
            if (s == null) continue;
            
            CompilationUnit u = s.getCompilationUnit();
            if (u == null) continue;

			MultiNameSet[] dependencies = new MultiNameSet[]{u.inheritance,
															 u.namespaces,
															 u.expressions,
															 u.types};
			boolean valid = true;

            // if deleted is an empty set, we don't even need to do this
            if (deleted.size() > 0)
            {
    			for (int j = 0; j < 4; j++)
    			{
    				for (Iterator k = dependencies[j].iterator(); k.hasNext();)
    				{
    					Object obj = k.next();
    					if (obj instanceof QName)
    					{
    						String location = (String) qnames.get(obj);
    						if (location != null && deleted.containsKey(location))
    						{
    							affected.put(s.getName(), s);
    							reasons.put(s.getName(), l10n.getLocalizedTextString(new DependentFileNoLongerExists(location)));
    							sources.set(i, null);
    							valid = false;
    							break;
    						}
    					}
    				}
    			}
            }

			// only check the following when strict is enabled.
			valid = valid && strict;

			for (Iterator k = u.importPackageStatements.iterator(); valid && k.hasNext();)
			{
				String packageName = (String) k.next();

				if (!hasPackage(sourcePath, swcContext, packageName))
				{
					affected.put(s.getName(), s);
					reasons.put(s.getName(), l10n.getLocalizedTextString(new InvalidImportStatement(packageName)));
					sources.set(i, null);
					namespaces.add(packageName);
					valid = false;
					break;
				}
			}

			for (Iterator k = u.importDefinitionStatements.iterator(); valid && k.hasNext();)
			{
				QName defName = (QName) k.next();

				if (!hasDefinition(sourcePath, swcContext, defName))
				{
					affected.put(s.getName(), s);
					reasons.put(s.getName(), l10n.getLocalizedTextString(new InvalidImportStatement(defName.toString())));
					sources.set(i, null);
					namespaces.add(defName.toString());
					valid = false;
					break;
				}
			}
		}

        // - SuperclassUpdated
        // - QNameSourceUpdated
		Algorithms.topologicalSort(graph, new Visitor()
		{
			public void visit(Object v)
			{
				String name = (String) ((Vertex) v).getWeight();
				CompilationUnit u = (CompilationUnit) graph.get(name);

				if (u != null)
				{
					checkInheritance(u);
					checkNamespaces(u);
				}
			}

			private void checkInheritance(CompilationUnit u)
			{
				for (Iterator j = u.inheritance.iterator(); j.hasNext();)
				{
					Object mn = j.next();
					if (mn instanceof QName)
					{
						QName qname = (QName) mn;
						String sourceName = (String) qnames.get(qname);
						Source s = u.getSource();

						if (sourceName != null && (updated.containsKey(sourceName) || affected.containsKey(sourceName)))
						{
							affected.put(s.getName(), s);
							reasons.put(s.getName(), l10n.getLocalizedTextString(new SuperclassUpdated(sourceName, qname)));
							int index = sources.indexOf(s);
							if (index != -1)
							{
								sources.set(index, null);
							}
						}
					}
				}
			}

			private void checkNamespaces(CompilationUnit u)
			{
				for (Iterator j = u.namespaces.iterator(); j.hasNext();)
				{
					Object mn = j.next();
					if (mn instanceof QName)
					{
						QName qname = (QName) mn;
						String sourceName = (String) qnames.get(qname);
						Source s = u.getSource();

						if (sourceName != null && updated.containsKey(sourceName))
						{
							affected.put(s.getName(), s);
							reasons.put(s.getName(), l10n.getLocalizedTextString(new QNameSourceUpdated(sourceName, qname)));
							int index = sources.indexOf(s);
							if (index != -1)
							{
								sources.set(index, null);
							}
						}
					}
				}
			}
		});

        // - DependentFileModified
        if (strict)
        {
    		for (int i = 0, size = sources.size(); i < size; i++)
    		{
    			Source s = (Source) sources.get(i);
                if (s == null) continue;
                
    			CompilationUnit u = s.getCompilationUnit();
                if (u == null) continue;
    
				MultiNameSet[] dependencies = new MultiNameSet[]{u.expressions, u.types};

				for (int j = 0; j < 2; j++)
				{
					for (Iterator k = dependencies[j].iterator(); k.hasNext();)
					{
						Object obj = k.next();
						if (obj instanceof QName)
						{
							String location = (String) qnames.get(obj);
							if (location != null && (updated.containsKey(location) || affected.containsKey(location)))
							{
								affected.put(s.getName(), s);
								reasons.put(s.getName(), l10n.getLocalizedTextString(new DependentFileModified(location)));
								sources.set(i, null);
								break;
							}
						}
    				}
    			}
    		}
        }

		for (Iterator i = includeUpdated.iterator(); i.hasNext();)
		{
			ThreadLocalToolkit.getLogger().includedFileUpdated((String) i.next());
		}

		int affectedCount = affected.size();
		logReasonAndRemoveCompilationUnit(affected,                   reasons, includeUpdated);
		logReasonAndRemoveCompilationUnit(updated,                    reasons, includeUpdated);
        logReasonAndRemoveCompilationUnit(updatedWithStableSignature, reasons, includeUpdated);

		// if a compilation unit becomes obsolete, its satellite compilation units in ResourceContainer
		// must go away too.
		for (Iterator i = resources.sources().iterator(); i.hasNext(); )
		{
			Source s = (Source) i.next();
			if (s != null)
			{
				String name = s.getNameForReporting();
				if (affected.containsKey(name) || updated.containsKey(name))
				{
					s.removeCompilationUnit();
				}
			}
		}

		affected.clear();

        // validate multinames
		// - MultiNameMeaningChanged
		for (int i = 0, size = sources.size(); i < size; i++)
		{
			Source s = (Source) sources.get(i);
            if (s == null) continue;

            CompilationUnit u = s.getCompilationUnit();
            if (u == null) continue;
            
			for (Iterator j = u.inheritanceHistory.keySet().iterator(); j.hasNext();)
			{
				MultiName multiName = (MultiName) j.next();
				QName qName = (QName) u.inheritanceHistory.get(multiName);

				try
				{
					if (!validateMultiName(multiName, qName, sourcePath))
					{
						affected.put(s.getName(), s);
						reasons.put(s.getName(), l10n.getLocalizedTextString(new MultiNameMeaningChanged(multiName, qName)));
						sources.set(i, null);
					}
				}
				catch (CompilerException ex)
				{
					affected.put(s.getName(), s);
					reasons.put(s.getName(), ex.getMessage());
					sources.set(i, null);
				}
			}
		}

		affectedCount += affected.size();

        // remove CompilationUnits from affected Map
        logReasonAndRemoveCompilationUnit(affected, reasons, includeUpdated);

		// if a compilation unit becomes obsolete, its satellite compilation units in ResourceContainer
		// must go away too.
		for (Iterator i = resources.sources().iterator(); i.hasNext(); )
		{
			Source s = (Source) i.next();
			if (s != null)
			{
				String name = s.getNameForReporting();
				if (affected.containsKey(name))
				{
					s.removeCompilationUnit();
				}
			}
		}

		// refresh the state of ResourceContainer
		resources.refresh();

		// finally, remove the deleted namespaces from SymbolTable...
		if (perCompileData != null)
		{
			for (Iterator i = namespaces.iterator(); i.hasNext(); )
			{
				String ns = (String) i.next();
				perCompileData.removeNamespace(ns);
			}
		}

        final int updateCount = updated.size() + updatedWithStableSignature.size();
        if (updateCount + affectedCount > 0)
        {
            ThreadLocalToolkit.log(new FilesChangedAffected(updateCount, affectedCount));
        }
        return (updateCount + affectedCount);
	}

    /**
     * Helper for validateCompilationUnits(). Removes the CompilationUnit for sources in map,
     * and logs the reasons. 
     */
    private static void logReasonAndRemoveCompilationUnit(final Map map, final Map reasons, final Set includeUpdated)
    {
        for (Iterator i = map.keySet().iterator(); i.hasNext();)
		{
			String name = (String) i.next();
			Source s = (Source) map.get(name);

			for (Iterator j = s.getFileIncludes(); j.hasNext();)
			{
				VirtualFile f = (VirtualFile) j.next();
				if (!includeUpdated.contains(f.getNameForReporting()))
				{
					ThreadLocalToolkit.getLogger().includedFileAffected(f.getNameForReporting());
				}
			}

			s.removeCompilationUnit();

			ThreadLocalToolkit.getLogger().needsCompilation(s.getName(), (String) reasons.get(s.getName()));
		}
    }
    
    /**
     * Runs the parser over a Source and returns the SignatureChecksum. The Source is copied.
     */
    private static Long computeSignatureChecksum(Configuration configuration, final Source source)
    {
        assert (configuration != null);
        
        //TODO It would be nice to cache this; it cannot (?) be static, however,
        //     as the Configuration changes. Solution would be a static Map or some fancy logic?
        // temporary compiler to get a syntax tree, for signature generation
        final flex2.compiler.as3.Compiler asc
            = new flex2.compiler.as3.Compiler(configuration.getCompilerConfiguration());
        asc.addCompilerExtension(SignatureExtension.getInstance());
        
        // create a new CompilationUnit if no error occur
        // then grab the signature if no signature error occur
        CompilationUnit u = null;
        
        // this swallows any parse errors -- they will get thrown when the file is
        // officially reparsed for compilation
        final Logger original = ThreadLocalToolkit.getLogger();
        ThreadLocalToolkit.setLogger(new LocalLogger(null));
        {
            final Source tmpSource = asc.preprocess(
                           Source.newSource(source.getBackingFile(),      source.getFileTime(),
                                            source.getPathRoot(),         source.getRelativePath(),
                                            source.getShortName(),        source.getOwner(),
                                            source.isInternal(),          source.isRoot(),
                                            source.isDebuggable(),        source.getFileIncludesSet(),
                                            source.getFileIncludeTimes(), source.getLogger()));
    
            // HACK: Forcefully disable any chance of signatures getting emitted to
            //       the filesystem -- since this code should be as fast as possible.
            //       Don't worry though, it WILL happen later during re-compilation.
            final String tmp = SignatureExtension.signatureDirectory;
            SignatureExtension.signatureDirectory = null;
            {
                u = asc.parse1(tmpSource, SymbolTable.newSymbolTable(configuration));
            }
            SignatureExtension.signatureDirectory = tmp;
        }
        ThreadLocalToolkit.setLogger(original);
        
        return ((u != null) ? u.getSignatureChecksum() : null);
    }


	private static boolean validateMultiName(MultiName multiName, QName qName, SourcePath sourcePath)
		throws CompilerException
	{
		for (int i = 0, length = multiName.namespaceURI.length; i < length; i++)
		{
			String ns = multiName.namespaceURI[i];
			String name = multiName.localPart;

			// C: findSource() may do a Source.copy()... we don't need Source.copy() in this case.
			Source s = (sourcePath != null) ? sourcePath.findSource(ns, name) : null;
			CompilationUnit u = (s != null) ? s.getCompilationUnit() : null;

			if (u != null)
			{
				ns = u.topLevelDefinitions.first().getNamespace();
			}

			if (s != null && !(qName.getNamespace().equals(ns) && qName.getLocalPart().equals(name)))
			{
				return false;
			}
		}

		return true;
	}

	private static void addVerticesToGraphs(List sources, DependencyGraph igraph, DependencyGraph dgraph)
	{
		for (int i = 0, size = sources.size(); i < size; i++)
		{
			Source s = (Source) sources.get(i);
			if (s != null)
			{
				addVertexToGraphs(s, s.getCompilationUnit(), igraph, dgraph);
			}
		}
	}

	private static void addVertexToGraphs(Source s, CompilationUnit u, DependencyGraph igraph, DependencyGraph dgraph)
	{
		String name = s.getName();

		if (u != null || igraph.get(name) == null)
		{
			igraph.put(name, u);
		}

		if (!igraph.containsVertex(name))
		{
			igraph.addVertex(new Vertex(name));
		}

		if (dgraph != null)
		{
			dgraph.put(name, s);
			if (!dgraph.containsVertex(name))
			{
				dgraph.addVertex(new Vertex(name));
			}
		}
	}

	private static boolean preprocess(List sources, flex2.compiler.Compiler[] compilers,
									  int start, int end, boolean suppressWarnings)
	{
		boolean result = true;

		for (int i = start; i < end; i++)
		{
			Source s = (Source) sources.get(i);
			if (s.isPreprocessed())
			{
				continue;
			}

			if ((s = preprocess(s, compilers, suppressWarnings)) == null)
			{
				result = false;
			}
			else
			{
				sources.set(i, s);
			}

			if (tooManyErrors())
			{
				ThreadLocalToolkit.log(new TooManyErrors());
				break;
			}

			if (forcedToStop())
			{
				ThreadLocalToolkit.log(new ForcedToStop());
				break;
			}
		}

		return result;
	}

	/**
	 * use this to display warnings from the compilation unit list when nothing needs to be recompiled.
	 */
	public static void displayWarnings(List units)
	{
		for (int i = 0, size = units == null ? 0 : units.size(); i < size; i++)
		{
			CompilationUnit u = (CompilationUnit) units.get(i);
			Source s = (u != null) ? u.getSource() : null;
			
			if (s != null && s.getLogger() != null && s.getLogger().warningCount() > 0 && !s.getLogger().isConnected())
			{
				s.getLogger().displayWarnings(ThreadLocalToolkit.getLogger());
			}
		}
	}
	
	static Source preprocess(Source s, flex2.compiler.Compiler[] compilers, boolean suppressWarnings)
	{
		if (!s.isCompiled())
		{
			// C: A fresh or healthy Source should not have a Logger.
			if (s.getLogger() != null && s.getLogger().warningCount() > 0 && !s.getLogger().isConnected() && !suppressWarnings)
			{
				s.getLogger().displayWarnings(ThreadLocalToolkit.getLogger());
			}

			flex2.compiler.Compiler c = getCompiler(s, compilers);
			if (c != null)
			{
				Logger original = ThreadLocalToolkit.getLogger();
				// assert !(original instanceof LocalLogger);

				LocalLogger local = new LocalLogger(original, s);
				local.setLocalizationManager(ThreadLocalToolkit.getLocalizationManager());
				s.setLogger(local);
				ThreadLocalToolkit.setLogger(local);

				s = c.preprocess(s);

				ThreadLocalToolkit.setLogger(original);

				if (local.errorCount() > 0)
				{
					if (s != null)
					{
						s.disconnectLogger();
					}

					s = null;
				}
				else
				{
					s.setPreprocessed();
				}
			}
			else
			{
				s = null;
			}
		}

		return s;
	}

	private static boolean parse1(List sources, List units, DependencyGraph igraph, DependencyGraph dgraph,
								 flex2.compiler.Compiler[] compilers, SymbolTable symbolTable,
								 int start, int end)
	{
		boolean result = true;

		for (int i = start; i < end; i++)
		{
			Source s = (Source) sources.get(i);
			CompilationUnit u;

			if ((u = parse1(s, compilers, symbolTable)) == null)
			{
				result = false;
				s.disconnectLogger();
			}

			for (int j = units.size(); j < i + 1; j++)
			{
				units.add(null);
			}

			units.set(i, u);

			addVertexToGraphs(s, u, igraph, dgraph);

			calculateProgress(sources, symbolTable);

			if (tooManyErrors())
			{
				ThreadLocalToolkit.log(new TooManyErrors());
				break;
			}

			if (forcedToStop())
			{
				ThreadLocalToolkit.log(new ForcedToStop());
				break;
			}
		}

		return result;
	}

	static CompilationUnit parse1(Source s, flex2.compiler.Compiler[] compilers, SymbolTable symbolTable)
	{
		if (s.isCompiled())
		{
			return s.getCompilationUnit();
		}

		CompilationUnit u = null;
		flex2.compiler.Compiler c = getCompiler(s, compilers);
		if (c != null)
		{
			Logger original = ThreadLocalToolkit.getLogger(), local = s.getLogger();
			ThreadLocalToolkit.setLogger(local);

			u = c.parse1(s, symbolTable);

			// reset the logger to the original one...
			ThreadLocalToolkit.setLogger(original);

			if (local.errorCount() == 0)
			{
				symbolTable.registerQNames(u.topLevelDefinitions, u.getSource());

				u.setState(CompilationUnit.SyntaxTree);
				u.setWorkflow(preprocess);
				u.setWorkflow(parse1);
			}
		}

		return u;
	}

	private static boolean parse2(List sources, flex2.compiler.Compiler[] compilers,
								  SymbolTable symbolTable, int start, int end)
	{
		boolean result = true;

		for (int i = start; i < end; i++)
		{
			Source source = (Source) sources.get(i);
			CompilationUnit u = source.getCompilationUnit();

			if ((u.getWorkflow() & parse2) != 0)
			{
				continue;
			}

			if (!parse2(u, compilers, symbolTable))
			{
				result = false;
				u.getSource().disconnectLogger();
			}

			calculateProgress(sources, symbolTable);

			if (tooManyErrors())
			{
				ThreadLocalToolkit.log(new TooManyErrors());
				break;
			}

			if (forcedToStop())
			{
				ThreadLocalToolkit.log(new ForcedToStop());
				break;
			}
		}

		return result;
	}

	private static boolean parse2(List sources, flex2.compiler.Compiler[] compilers, SymbolTable symbolTable)
	{
		return parse2(sources, compilers, symbolTable, 0, sources.size());
	}

	private static boolean parse2(CompilationUnit u, flex2.compiler.Compiler[] compilers, SymbolTable symbolTable)
	{
		Source s = u.getSource();
		if (!s.isCompiled())
		{
			flex2.compiler.Compiler c = getCompiler(s, compilers);
			if (c != null)
			{
				// C: may use CompilationUnit to reference the local logger so as to minimize
				//    the number of creations...
				Logger original = ThreadLocalToolkit.getLogger(), local = s.getLogger();
				ThreadLocalToolkit.setLogger(local);

				c.parse2(u, symbolTable);
				u.setWorkflow(parse2);
				ThreadLocalToolkit.setLogger(original);

				if (local.errorCount() > 0)
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}

		return true;		
	}
	
	private static boolean analyze(List sources, flex2.compiler.Compiler[] compilers,
								   SymbolTable symbolTable, int phase)
	{
		return analyze(sources, compilers, symbolTable, 0, sources.size(), phase);
	}

	private static boolean analyze(List sources, flex2.compiler.Compiler[] compilers,
								   SymbolTable symbolTable, int start, int end, int phase)
	{
		boolean result = true;

		for (int i = start; i < end; i++)
		{
			Source source = (Source) sources.get(i);
			CompilationUnit u = source.getCompilationUnit();

			if ((phase == 1 && (u.getWorkflow() & analyze1) != 0) ||
				(phase == 2 && (u.getWorkflow() & analyze2) != 0) ||
				(phase == 3 && (u.getWorkflow() & analyze3) != 0) ||
				(phase == 4 && (u.getWorkflow() & analyze4) != 0))
			{
				continue;
			}

			if (!analyze(u, compilers, symbolTable, phase))
			{
				result = false;
				u.getSource().disconnectLogger();
			}

			calculateProgress(sources, symbolTable);

			// C: make sure that Source and CompilationUnit always point to each other.
			assert u.getSource().getCompilationUnit() == u;

			if (tooManyErrors())
			{
				ThreadLocalToolkit.log(new TooManyErrors());
				break;
			}

			if (forcedToStop())
			{
				ThreadLocalToolkit.log(new ForcedToStop());
				break;
			}
		}

		return result;
	}

	private static boolean analyze(CompilationUnit u, flex2.compiler.Compiler[] compilers, SymbolTable symbolTable,
								   int phase)
	{
		Source s = u.getSource();
		if (!s.isCompiled())
		{
			flex2.compiler.Compiler c = getCompiler(s, compilers);
			if (c != null)
			{
				// C: may use CompilationUnit to reference the local logger so as to minimize
				//    the number of creations...
				Logger original = ThreadLocalToolkit.getLogger(), local = s.getLogger();
				ThreadLocalToolkit.setLogger(local);

				if (phase == 1)
				{
					c.analyze1(u, symbolTable);

					if (local.errorCount() == 0)
					{
						// C: check u.topLevelDefinitions...
						if (s.isSourcePathOwner() || s.isSourceListOwner())
						{
							int size = u.topLevelDefinitions.size();
							if (size > 1)
							{
								ThreadLocalToolkit.log(new MoreThanOneDefinition(u.topLevelDefinitions), s);
							}
							else if (size < 1)
							{
								ThreadLocalToolkit.log(new MustHaveOneDefinition(), s);
							}
							else if (s.isSourcePathOwner())
							{
								SourcePath owner = (SourcePath) s.getOwner();
								
								String[] packages = owner.checkPackageNameDirectoryName(s);
								if (packages != null)
								{
									ThreadLocalToolkit.log(new WrongPackageName(packages[0], packages[1]), s);
								}

								String[] classes = owner.checkClassNameFileName(s);
								if (classes != null)
								{
									ThreadLocalToolkit.log(new WrongDefinitionName(classes[0], classes[1]), s);
								}
							}
							else if (s.isSourceListOwner())
							{
								SourceList owner = (SourceList) s.getOwner();

								String[] packages = owner.checkPackageNameDirectoryName(s);
								if (packages != null)
								{
									ThreadLocalToolkit.log(new WrongPackageName(packages[0], packages[1]), s);
								}

								String[] classes = owner.checkClassNameFileName(s);
								if (classes != null)
								{
									ThreadLocalToolkit.log(new WrongDefinitionName(classes[0], classes[1]), s);
								}
							}
						}

						// symbolTable.registerQNames(u.topLevelDefinitions, u.getSource());
					}

					u.setWorkflow(analyze1);
				}
				else if (phase == 2)
				{
					c.analyze2(u, symbolTable);
					u.setWorkflow(analyze2);
				}
				else if (phase == 3)
				{
					c.analyze3(u, symbolTable);
					u.setWorkflow(analyze3);
				}
				else // phase == 4
				{
					c.analyze4(u, symbolTable);
					u.setWorkflow(analyze4);
				}

				ThreadLocalToolkit.setLogger(original);

				if (local.errorCount() > 0)
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}

		return true;
	}

	private static void resolveInheritance(List sources, DependencyGraph igraph, DependencyGraph dgraph,
	                                       SymbolTable symbolTable, SourceList sourceList, SourcePath sourcePath,
	                                       ResourceContainer resources, CompilerSwcContext swcContext, int start, int end)
	{
		Set qNames = new HashSet();

		for (int i = start; i < end; i++)
		{
			Source source = (Source) sources.get(i);
			CompilationUnit u = source.getCompilationUnit();

			if (u == null || u.inheritance.size() == 0)
			{
				continue;
			}

			qNames.clear();

			String head = source.getName();
			String name = source.getNameForReporting();

			for (Iterator k = u.inheritance.iterator(); k.hasNext();)
			{
				Object unresolved = k.next();

				if (unresolved instanceof MultiName)
				{
					MultiName mName = (MultiName) unresolved;
					QName qName = resolveMultiName(name, mName, sources, sourceList, sourcePath, resources, swcContext, symbolTable);

					if (qName != null)
					{
						qNames.add(qName);
						u.inheritanceHistory.put(mName, qName);

						Source tailSource = symbolTable.findSourceByQName(qName);
						String tail = tailSource.getName();
						addVertexToGraphs(tailSource, tailSource.getCompilationUnit(), igraph, dgraph);
						addEdgeToGraphs(igraph, dgraph, head, tail);
					}

					k.remove();
				}
			}

			if (qNames.size() > 0)
			{
				u.inheritance.addAll(qNames);
			}
		}
	}

	private static void resolveNamespace(List sources, DependencyGraph igraph, DependencyGraph dgraph,
										 SymbolTable symbolTable, SourceList sourceList, SourcePath sourcePath,
										 ResourceContainer resources, CompilerSwcContext swcContext, int start, int end)
	{
		Set qNames = new HashSet();

		for (int i = start; i < end; i++)
		{
			Source source = (Source) sources.get(i);
			CompilationUnit u = source.getCompilationUnit();

			if (u.namespaces.size() == 0)
			{
				continue;
			}

			qNames.clear();

			String head = source.getName();
			String name = u.getSource().getNameForReporting();

			for (Iterator k = u.namespaces.iterator(); k.hasNext();)
			{
				Object unresolved = k.next();

				if (unresolved instanceof MultiName)
				{
					MultiName mName = (MultiName) unresolved;
					QName qName = resolveMultiName(name, mName, sources, sourceList, sourcePath, resources, swcContext, symbolTable);

					if (qName != null)
					{
						qNames.add(qName);
						u.namespaceHistory.put(mName, qName);

						Source tailSource = symbolTable.findSourceByQName(qName);
						String tail = tailSource.getName();
						addVertexToGraphs(tailSource, tailSource.getCompilationUnit(), igraph, dgraph);
						addEdgeToGraphs(null, dgraph, head, tail);
					}

					k.remove();
				}
			}

			if (qNames.size() > 0)
			{
				u.namespaces.addAll(qNames);
			}
		}		
	}

	private static int findDefinition(List sources, SourceList sourceList, SourcePathBase sourcePath,
	                                  ResourceContainer resources, CompilerSwcContext swcContext,
									  String namespaceURI, String localPart)
		throws CompilerException
	{
		Source s = (sourceList != null) ? sourceList.findSource(namespaceURI, localPart) : null;

		if (s == null)
		{
			s = (sourcePath != null) ? sourcePath.findSource(namespaceURI, localPart) : null;
		}

		if (s == null)
		{
			s = (resources != null) ? resources.findSource(namespaceURI, localPart) : null;
		}

		if (s == null)
		{
			s = (swcContext != null) ? swcContext.getSource(namespaceURI, localPart) : null;
		}

		if (s != null)
		{
			int where = sources.indexOf(s);
			if (where == -1)
			{
				sources.add(s);
				return sources.size() - 1;
			}
			else
			{
				return where;
			}
		}
		else
		{
			return -1;
		}
	}

	private static int findResourceBundle(List sources, SourceList sourceList, SourcePathBase sourcePath, CompilerSwcContext swcContext,
										  String[] locales, String namespaceURI, String localPart)
		throws CompilerException
	{
		Source s1, s2, s3;
		VirtualFile o1, o2, o3;
		ResourceFile rf1, rf2, rf3;
		
		s1 = (sourceList != null) ? sourceList.findSource(namespaceURI, localPart) : null;
		o1 = (s1 != null) ? s1.getBackingFile() : null;
		
		// already compiled. return...
		if (o1 instanceof InMemoryFile)
		{
			return findResourceBundleHelper(sources, s1);
		}
		
		rf1 = (ResourceFile) o1;

		if (rf1 != null && rf1.complete())
		{
			return findResourceBundleHelper(sources, s1);
		}
		else
		{
			// rf1 == null || !rf1.complete(), must get rf2...
			s2 = (sourcePath != null) ? sourcePath.findSource(namespaceURI, localPart) : null;
			o2 = (s2 != null) ? s2.getBackingFile() : null;
			
			// already compiled. return...
			if (rf1 == null && o2 instanceof InMemoryFile)
			{
				return findResourceBundleHelper(sources, s2);				
			}
			else if (o2 instanceof InMemoryFile)
			{
				o2 = null;
			}
			
			rf2 = (ResourceFile) o2;
			
			if (rf1 != null)
			{
				rf1.merge(rf2);
			}
			else
			{
				rf1 = rf2;
				s1 = s2;
			}
		}
		
		if (rf1 != null && rf1.complete())
		{
			return findResourceBundleHelper(sources, s1);
		}
		else
		{
			// rf1 == null || !rf1.complete(), must get rf3...
			s3 = (swcContext != null) ? swcContext.getResourceBundle(locales, namespaceURI, localPart) : null;
			o3 = (s3 != null) ? s3.getBackingFile() : null;
			
			// already compiled. return...
			if (rf1 == null && o3 instanceof InMemoryFile)
			{
				return findResourceBundleHelper(sources, s3);				
			}
			else if (o3 instanceof InMemoryFile)
			{
				o3 = null;
			}
			
			rf3 = (ResourceFile) o3;
			
			if (rf1 != null)
			{
				rf1.merge(rf3);
			}
			else
			{
				rf1 = rf3;
				s1 = s3;
			}
		}
		
		return findResourceBundleHelper(sources, s1);
	}
	
	private static int findResourceBundleHelper(List sources, Source s)
	{
		if (s != null)
		{
			int where = sources.indexOf(s);
			if (where == -1)
			{
				sources.add(s);
				return sources.size() - 1;
			}
			else
			{
				return where;
			}
		}
		else
		{
			return -1;
		}
	}

	private static boolean hasPackage(SourcePath sourcePath, CompilerSwcContext swcContext, String packageName)
	{
		// C: This should check with "sources" before SourcePath and CompilerSwcContext... or check with
		//    FileSpec and SourceList, not "sources"... will fix it asap...
		boolean hasPackage = (sourcePath != null) && sourcePath.hasPackage(packageName);

		if (!hasPackage && swcContext != null)
		{
			hasPackage = swcContext.hasPackage(packageName);
		}

		return hasPackage;
	}

	private static boolean hasDefinition(SourcePath sourcePath, CompilerSwcContext swcContext, QName defName)
	{
		boolean hasDefinition = (sourcePath != null) && sourcePath.hasDefinition(defName);

		if (!hasDefinition && swcContext != null)
		{
			hasDefinition = swcContext.hasDefinition(defName);
		}

		return hasDefinition;
	}

	private static boolean sortInheritance(List sources, final List units, final DependencyGraph graph)
	{
		assert sources.size() == units.size();
		boolean success = true;
		final List tsort = new ArrayList(units.size());

		Algorithms.topologicalSort(graph, new Visitor()
		{
			public void visit(Object v)
			{
				String name = (String) ((Vertex) v).getWeight();
				CompilationUnit u = (CompilationUnit) graph.get(name);
				assert u != null : name;
				tsort.add(u);
			}
		});

		if (units.size() > tsort.size())
		{
			for (int i = 0, size = units.size(); i < size; i++)
			{
				CompilationUnit u = (CompilationUnit) units.get(i);
				if (!tsort.contains(u))
				{
					ThreadLocalToolkit.log(new CircularInheritance(), u.getSource());
					success = false;
				}
			}
			assert !success;
		}
		else
		{
			sources.clear();
			units.clear();
			for (int i = 0, size = tsort.size(); i < size; i++)
			{
				CompilationUnit u = (CompilationUnit) tsort.get(i);
				sources.add(u.getSource());
				units.add(u);
			}
		}

		return success;
	}

	private static boolean detectCycles(List sources, final DependencyGraph graph)
	{
		final Map tsort = new HashMap(sources.size());

		for (int i = 0, size = sources.size(); i < size; i++)
		{
			Source s = (Source) sources.get(i);
			tsort.put(s.getName(), s);
		}

		Algorithms.topologicalSort(graph, new Visitor()
		{
			public void visit(Object v)
			{
				String name = (String) ((Vertex) v).getWeight();
				tsort.remove(name);
			}
		});

		if (tsort.size() > 0)
		{
			for (Iterator i = tsort.keySet().iterator(); i.hasNext();)
			{
				String name = (String) i.next();
				Source s = (Source) tsort.get(name);
				if (!s.hasError())
				{
					ThreadLocalToolkit.log(new CircularInheritance(), name);
				}
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	private static void addGeneratedSources(List sources, DependencyGraph igraph, DependencyGraph dgraph,
											ResourceContainer resources, SymbolTable symbolTable,
											Configuration configuration, int start, int end)
	{
		for (int i = start; i < end; i++)
		{
			Source source = (Source) sources.get(i);
			CompilationUnit u = source.getCompilationUnit();
			if (u != null)
			{
				Map generatedSources = u.getGeneratedSources();
				if (generatedSources != null)
				{
					for (Iterator j = generatedSources.keySet().iterator(); j.hasNext();)
					{
						QName qN = (QName) j.next();
						MultiName mN = new MultiName(qN.getNamespace(), qN.getLocalPart());
						Source gSource = (Source) generatedSources.get(qN);
						String gName = gSource.getName();

						if (!igraph.containsVertex(gName))
						{
							gSource = resources.addResource(gSource);
							addVertexToGraphs(gSource, gSource.getCompilationUnit(), igraph, dgraph);
							sources.add(gSource);

							// C: This is similar to API.resolveMultiName(). The resolveMultiName() method does more
							//    than adding Source objects to "sources".
							symbolTable.registerMultiName(mN, qN);
							symbolTable.registerQName(qN, gSource);
						}

						// C: If we manually add to CompilationUnit.expressions, we must add MultiName instances,
						//    otherwise, incremental compilation will lose the dependency because the multiname
						//    qname mapping isn't in expressionHistory.
						u.expressions.add(mN);
					}

					u.clearGeneratedSources();
				}
				
				Map archiveFiles = (Map) u.getContext().getAttribute(Context.CSS_ARCHIVE_FILES);
				if (archiveFiles != null && archiveFiles.size() > 0)
				{
					configuration.addCSSArchiveFiles(archiveFiles);
					archiveFiles.clear();
				}
				
				archiveFiles = (Map) u.getContext().getAttribute(Context.L10N_ARCHIVE_FILES);
				if (archiveFiles != null && archiveFiles.size() > 0)
				{
					configuration.addL10nArchiveFiles(archiveFiles);
					archiveFiles.clear();
				}
			}
		}
	}

	private static void resolveType(List sources, List units, DependencyGraph igraph, DependencyGraph dgraph, SymbolTable symbolTable,
									SourceList sourceList, SourcePath sourcePath, ResourceContainer resources, CompilerSwcContext swcContext)
	{
		resolveType(sources, units, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext, 0, units.size());
	}

	private static void resolveType(List sources, List units, DependencyGraph igraph, DependencyGraph dgraph,
	                                SymbolTable symbolTable, SourceList sourceList, SourcePath sourcePath,
	                                ResourceContainer resources, CompilerSwcContext swcContext, int start, int end)
	{
		Set qNames = new HashSet();

		for (int i = start; i < end; i++)
		{
			Source s = (Source) sources.get(i);
			CompilationUnit u = (s != null) ? s.getCompilationUnit() : (CompilationUnit) units.get(i);

			if ((u.getWorkflow() & resolveType) != 0 || u.types.size() == 0)
			{
				continue;
			}

			qNames.clear();

			String head = u.getSource().getName();
			String name = u.getSource().getNameForReporting();

			for (Iterator k = u.types.iterator(); k.hasNext();)
			{
				Object unresolved = k.next();
				if (unresolved instanceof MultiName)
				{
					MultiName mName = (MultiName) unresolved;
					QName qName = resolveMultiName(name, mName, sources, sourceList, sourcePath, resources, swcContext, symbolTable);

					if (qName != null)
					{
						qNames.add(qName);
						u.typeHistory.put(mName, qName);

						Source tailSource = symbolTable.findSourceByQName(qName);
						String tail = tailSource.getName();
						addVertexToGraphs(tailSource, tailSource.getCompilationUnit(), igraph, dgraph);
						addEdgeToGraphs(null, dgraph, head, tail);
					}

					k.remove();
				}
			}

			if (qNames.size() > 0)
			{
				u.types.addAll(qNames);
			}
		}

		for (int i = start; i < end; i++)
		{
			Source s = (Source) sources.get(i);
			CompilationUnit u = (s != null) ? s.getCompilationUnit() : (CompilationUnit) units.get(i);

			if ((u.getWorkflow() & resolveType) != 0)
			{
				continue;
			}
			else
			{
				u.setWorkflow(resolveType);
				if (u.namespaces.size() == 0)
				{
					continue;
				}
			}

			qNames.clear();

			String head = u.getSource().getName();
			String name = u.getSource().getNameForReporting();

			for (Iterator k = u.namespaces.iterator(); k.hasNext();)
			{
				Object unresolved = k.next();
				if (unresolved instanceof MultiName)
				{
					MultiName mName = (MultiName) unresolved;
					QName qName = resolveMultiName(name, mName, sources, sourceList, sourcePath, resources, swcContext, symbolTable);

					if (qName != null)
					{
						qNames.add(qName);
						u.namespaceHistory.put(mName, qName);

						Source tailSource = symbolTable.findSourceByQName(qName);
						String tail = tailSource.getName();
						addVertexToGraphs(tailSource, tailSource.getCompilationUnit(), igraph, dgraph);
						addEdgeToGraphs(null, dgraph, head, tail);
					}

					k.remove();
				}
			}

			if (qNames.size() > 0)
			{
				u.namespaces.addAll(qNames);
			}

		}
	}

	private static void resolveImportStatements(List sources, List units,
												SourcePath sourcePath,
												CompilerSwcContext swcContext)
	{
		resolveImportStatements(sources, units, sourcePath, swcContext, 0, units.size());
	}

	private static void resolveImportStatements(List sources, List units,
												SourcePath sourcePath,
												CompilerSwcContext swcContext,
												int start, int end)
	{
		for (int i = start; i < end; i++)
		{
			Source s = (Source) sources.get(i);
			CompilationUnit u = (s != null) ? s.getCompilationUnit() : (CompilationUnit) units.get(i);

			if ((u.getWorkflow() & resolveImportStatements) != 0)
			{
				continue;
			}
			else
			{
				u.setWorkflow(resolveImportStatements);
			}

			for (Iterator k = u.importPackageStatements.iterator(); k.hasNext();)
			{
				String packageName = (String) k.next();

				if (!hasPackage(sourcePath, swcContext, packageName))
				{
					k.remove();
				}
			}

			for (Iterator k = u.importDefinitionStatements.iterator(); k.hasNext();)
			{
				QName defName = (QName) k.next();

				if (!hasDefinition(sourcePath, swcContext, defName))
				{
					k.remove();
				}
			}
		}
	}

	private static void resolveExpression(List sources, List units, DependencyGraph igraph, DependencyGraph dgraph, SymbolTable symbolTable,
										  SourceList sourceList, SourcePath sourcePath, ResourceContainer resources,
										  CompilerSwcContext swcContext, Configuration configuration)
	{
		resolveExpression(sources, units, igraph, dgraph, symbolTable, sourceList, sourcePath, resources, swcContext,
		                  configuration, 0, units.size());
	}

	private static void resolveExpression(List sources, List units, DependencyGraph igraph, DependencyGraph dgraph, SymbolTable symbolTable,
										  SourceList sourceList, SourcePath sourcePath, ResourceContainer resources,
										  CompilerSwcContext swcContext, Configuration configuration,
										  int start, int end)
	{
		Set qNames = new HashSet();

		for (int i = start; i < end; i++)
		{
			Source s = (Source) sources.get(i);
			CompilationUnit u = (s != null) ? s.getCompilationUnit() : (CompilationUnit) units.get(i);

			/*
			if ((u.getWorkflow() & resolveExpression) != 0)
			{
				continue;
			}
			else
			{
				u.setWorkflow(resolveExpression);
				if (u.expressions.size() == 0)
				{
					continue;
				}
			}
            */

			if (u.expressions.size() == 0)
			{
				continue;
			}

			qNames.clear();

			String head = u.getSource().getName();
			String name = u.getSource().getNameForReporting();

			for (Iterator k = u.expressions.iterator(); k.hasNext();)
			{
				Object unresolved = k.next();
				if (unresolved instanceof MultiName)
				{
					MultiName mName = (MultiName) unresolved;
					QName qName = resolveMultiName(name, mName, sources, sourceList, sourcePath, resources, swcContext, symbolTable);

					if (qName != null)
					{
						qNames.add(qName);
						u.expressionHistory.put(mName, qName);

						Source tailSource = symbolTable.findSourceByQName(qName);
						String tail = tailSource.getName();
						addVertexToGraphs(tailSource, tailSource.getCompilationUnit(), igraph, dgraph);
						addEdgeToGraphs(null, dgraph, head, tail);
					}
					else
					{
						// ASC doesn't seem to care about unresolved expression deps too much.
						// This list seems to have a lot of false positives (i.e. generated local methods?)
						// so the warning is contingent on an advanced config var.
						if (configuration.getCompilerConfiguration().showDependencyWarnings())
						{
							ThreadLocalToolkit.log(new UnableToResolveDependency(mName.getLocalPart()), u.getSource());
						}
					}


					k.remove();
				}
			}

			if (qNames.size() > 0)
			{
				u.expressions.addAll(qNames);
			}
		}
	}

	// e.g. head = mx.core.Application (subclass), tail = mx.containers.Container (superclass), 'head' needs 'tail'
	private static void addEdgeToGraphs(DependencyGraph igraph, DependencyGraph dgraph, String head, String tail)
	{
		if (igraph != null)
		{
			if (!head.equals(tail) && !igraph.dependencyExists(head, tail))
			{
				igraph.addDependency(head, tail);
			}
		}

		if (dgraph != null)
		{
			if (!head.equals(tail) && !dgraph.dependencyExists(head, tail))
			{
				dgraph.addDependency(head, tail);
			}
		}
	}

	private static void adjustQNames(List units, DependencyGraph igraph, SymbolTable symbolTable)
	{
		// C: A temporary fix for the issue when the true QName of the top level definition in a source file
		//    from the classpath is not known until the source file is parsed...
		for (int i = 0, size = units.size(); i < size; i++)
		{
			CompilationUnit u = (CompilationUnit) units.get(i);

			if (u != null && u.isDone() && (u.getWorkflow() & adjustQNames) == 0)
			{
				for (Iterator j = u.inheritance.iterator(); j.hasNext();)
				{
					Object obj = j.next();
					if (obj instanceof QName)
					{
						QName qName = (QName) obj;
						adjustQName(qName, igraph, symbolTable);
					}
				}

				for (Iterator j = u.namespaces.iterator(); j.hasNext();)
				{
					Object obj = j.next();
					if (obj instanceof QName)
					{
						QName qName = (QName) obj;
						adjustQName(qName, igraph, symbolTable);
					}
				}

				for (Iterator j = u.types.iterator(); j.hasNext();)
				{
					Object obj = j.next();
					if (obj instanceof QName)
					{
						QName qName = (QName) obj;
						adjustQName(qName, igraph, symbolTable);
					}
				}

				for (Iterator j = u.expressions.iterator(); j.hasNext();)
				{
					Object obj = j.next();
					if (obj instanceof QName)
					{
						QName qName = (QName) obj;
						adjustQName(qName, igraph, symbolTable);
					}
				}

				u.setWorkflow(adjustQNames);
			}
		}
	}

	private static void adjustQName(QName qName, DependencyGraph igraph, SymbolTable symbolTable)
	{
		Source s = symbolTable.findSourceByQName(qName);
		CompilationUnit u = (CompilationUnit) igraph.get(s == null ? null : s.getName());
		if (u != null && (u.getSource().isSourcePathOwner() || u.getSource().isSourceListOwner()) &&
			u.topLevelDefinitions.size() == 1)
		{
			QName def = u.topLevelDefinitions.last();
			if (qName.getLocalPart().equals(def.getLocalPart()) && !qName.getNamespace().equals(def.getNamespace()))
			{
				qName.setNamespace( def.getNamespace() );
			}
		}
	}

	// C: making this method public is only temporary...
	public static QName resolveMultiName(MultiName multiName, List sources, SourceList sourceList, SourcePath sourcePath,
	                                     ResourceContainer resources, CompilerSwcContext swcContext, SymbolTable symbolTable)
	{
		return resolveMultiName(null, multiName, sources, sourceList, sourcePath, resources, swcContext, symbolTable);
	}

	private static QName resolveMultiName(String nameForReporting, MultiName multiName, List sources, SourceList sourceList,
										  SourcePath sourcePath, ResourceContainer resources, CompilerSwcContext swcContext,
										  SymbolTable symbolTable)
	{
		QName qName = symbolTable.isMultiNameResolved(multiName), qName2 = null;
		Source source = null, source2 = null;
		boolean hasAmbiguity = false;

		if (qName != null)
		{
			return qName;
		}

		String[] namespaceURI = multiName.getNamespace();
		String localPart = multiName.getLocalPart();

		for (int j = 0, length = namespaceURI.length; j < length; j++)
		{
			Source s = symbolTable.findSourceByQName(namespaceURI[j], localPart);
			int where = -1;

			if (s == null)
			{
				try
				{
					where = findDefinition(sources, sourceList, sourcePath, resources, swcContext, namespaceURI[j], localPart);
				}
				catch (CompilerException ex)
				{
					ThreadLocalToolkit.logError(ex.getMessage());
				}

				if (where != -1)
				{
					s = (Source) sources.get(where);
				}
			}

			if (s != null)
			{
				if (qName == null)
				{
					qName = new QName(namespaceURI[j], localPart);
					source = s;

					// C: comment out the break statement to enforce ambiguity checks...
					// break;
				}
				else if (!qName.equals(namespaceURI[j], localPart))
				{
					hasAmbiguity = true;
					qName2 = new QName(namespaceURI[j], localPart);
					source2 = s;
					break;
				}
			}
		}

		if (hasAmbiguity)
		{
			CompilerMessage msg = new AmbiguousMultiname(qName, source.getName(), qName2, source2.getName());

			// C: The MultiName representation does not carry a line number. It looks like it'll improve
			//    error reporting if the AS compiler also tells this method where it found the reference.
			if (nameForReporting != null)
			{
				ThreadLocalToolkit.log(msg, nameForReporting);
			}
			else
			{
				ThreadLocalToolkit.log(msg);
			}

			return null;
		}
		else if (source != null)
		{
			symbolTable.registerMultiName(multiName, qName);
			symbolTable.registerQName(qName, source);
		}

		return qName;
	}

	public static QName[] resolveResourceBundleName(String rbName, List sources, SourceList sourceList,
													SourcePathBase sourcePath, ResourceContainer resources, CompilerSwcContext swcContext,
													SymbolTable symbolTable, String[] locales)
	{
		QName[] qNames = symbolTable.isResourceBundleResolved(rbName);
		if (qNames != null)
		{
			return qNames;
		}
		
		Source source = symbolTable.findSourceByResourceBundleName(rbName);
		if (source == null)
		{
			int where = -1;
			QName bundleName = new QName(rbName);
			String namespaceURI = bundleName.getNamespace();
			String localPart = bundleName.getLocalPart();
			
			try
			{
				where = findResourceBundle(sources, sourceList, sourcePath, swcContext, locales, namespaceURI, localPart);
			}
			catch (CompilerException ex)
			{
				ThreadLocalToolkit.logError(ex.getMessage());
			}

			if (where != -1)
			{
				source = (Source) sources.get(where);
				qNames = new QName[locales == null ? 0 : locales.length];
				
				for (int i = 0, length = qNames.length; i < length; i++)
				{
					qNames[i] = new QName(namespaceURI, locales[i] + "$" + localPart + I18nUtils.CLASS_SUFFIX);
				}
			}
		}

		symbolTable.register(rbName, qNames, source);

		return qNames;
	}
		
	private static boolean generate(List sources, List units, flex2.compiler.Compiler[] compilers, SymbolTable symbolTable)
	{
		return generate(sources, units, compilers, symbolTable, 0, units.size());
	}

	private static boolean generate(List sources, List units, flex2.compiler.Compiler[] compilers, SymbolTable symbolTable,
									int start, int end)
	{
		boolean result = true;

		for (int i = start; i < end; i++)
		{
			Source s = (Source) sources.get(i);
			CompilationUnit u = (s != null) ? s.getCompilationUnit() : (CompilationUnit) units.get(i);

			if ((u.getWorkflow() & generate) != 0)
			{
				continue;
			}
			else
			{
				u.setWorkflow(generate);
			}

			if (!u.isBytecodeAvailable() && !generate(u, compilers, symbolTable))
			{
				result = false;
				u.getSource().disconnectLogger();
			}

			calculateProgress(sources, symbolTable);

			if (tooManyErrors())
			{
				ThreadLocalToolkit.log(new TooManyErrors());
				break;
			}

			if (forcedToStop())
			{
				ThreadLocalToolkit.log(new ForcedToStop());
				break;
			}
		}

		return result;
	}

	private static boolean generate(CompilationUnit u, flex2.compiler.Compiler[] compilers, SymbolTable symbolTable)
	{
		Source s = u.getSource();
		if (!s.isCompiled())
		{
			flex2.compiler.Compiler c = getCompiler(s, compilers);
			if (c != null)
			{
				// C: may use CompilationUnit to reference the local logger so as to minimize
				//    the number of creations...
				Logger original = ThreadLocalToolkit.getLogger(), local = s.getLogger();
				ThreadLocalToolkit.setLogger(local);

				c.generate(u, symbolTable);
				if (u.bytes.size() > 0)
				{
					u.setState(CompilationUnit.abc);
				}

				ThreadLocalToolkit.setLogger(original);

				if (local.errorCount() > 0)
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}

		return true;
	}

	private static boolean postprocess(List sources, List units, flex2.compiler.Compiler[] compilers, SymbolTable symbolTable)
	{
		return postprocess(sources, units, compilers, symbolTable, 0, units.size());
	}

	private static boolean postprocess(List sources, List units, flex2.compiler.Compiler[] compilers, SymbolTable symbolTable,
									   int start, int end)
	{
		boolean result = true;

		for (int i = start; i < end; i++)
		{
			Source s = (Source) sources.get(i);
			CompilationUnit u = (s != null) ? s.getCompilationUnit() : (CompilationUnit) units.get(i);

			if (!postprocess(u, compilers, symbolTable))
			{
				result = false;
				u.getSource().disconnectLogger();
			}

			if (tooManyErrors())
			{
				ThreadLocalToolkit.log(new TooManyErrors());
				break;
			}

			if (forcedToStop())
			{
				ThreadLocalToolkit.log(new ForcedToStop());
				break;
			}
		}

		return result;
	}

	private static boolean postprocess(CompilationUnit u, flex2.compiler.Compiler[] compilers, SymbolTable symbolTable)
	{
		Source s = u.getSource();
		if (!s.isCompiled())
		{
			flex2.compiler.Compiler c = getCompiler(s, compilers);
			if (c != null)
			{
				Logger original = ThreadLocalToolkit.getLogger(), local = s.getLogger();
				ThreadLocalToolkit.setLogger(local);

				c.postprocess(u, symbolTable);

				ThreadLocalToolkit.setLogger(original);

				if (local.errorCount() > 0)
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}

		return true;
	}

	private static void getIncludeClasses(List sources, DependencyGraph igraph, DependencyGraph dgraph, SymbolTable symbolTable,
										  SourceList sourceList, SourcePath sourcePath, ResourceContainer resources,
										  CompilerSwcContext swcContext, Configuration configuration)
	{
        Set includes = new HashSet();
        includes.addAll( configuration.getIncludes() );
        for (Iterator it = configuration.getFrameList().iterator(); it.hasNext();)
        {
            FramesConfiguration.FrameInfo f = (FramesConfiguration.FrameInfo) it.next();
            includes.addAll( f.frameClasses );
        }
        for (Iterator it = includes.iterator(); it.hasNext();)
		{
			String className = (String) it.next();
			MultiName mName = new MultiName(className);
			QName qName = resolveMultiName("configuration", mName, sources, sourceList, sourcePath, resources, swcContext, symbolTable);

			if (qName != null)
			{
				Source tailSource = symbolTable.findSourceByQName(qName);
				addVertexToGraphs(tailSource, tailSource.getCompilationUnit(), igraph, dgraph);
			}
			else
			{
				ThreadLocalToolkit.log(new UnableToResolveClass("include", className));
			}
		}
	}

	private static void getIncludeResources(List sources, DependencyGraph igraph, DependencyGraph dgraph, ResourceBundlePath bundlePath,
											SymbolTable symbolTable, CompilerSwcContext swcContext, Configuration configuration)
	{
		Map resourceIncludes = swcContext.getResourceIncludes();
		String[] locales = configuration.getCompilerConfiguration().getLocales();

		for (Iterator it = resourceIncludes.keySet().iterator(); it.hasNext();)
		{
			String rbName = NameFormatter.toColon((String) it.next());
			QName[] qNames = resolveResourceBundleName(rbName, sources, null, bundlePath,
													   null, swcContext, symbolTable, locales);
			if (qNames != null)
			{
				Source source = symbolTable.findSourceByResourceBundleName(rbName);
				addVertexToGraphs(source, source.getCompilationUnit(), igraph, dgraph);

				for (int i = 0; i < qNames.length; i++)
				{
					configuration.getIncludes().add(qNames[i].toString());
				}
			}
		}
	}

	private static void getExtraSources(List sources, DependencyGraph igraph, DependencyGraph dgraph,
										SourceList sourceList, SourcePath sourcePath, ResourceContainer resources,
										ResourceBundlePath bundlePath, SymbolTable symbolTable, CompilerSwcContext swcContext,
										Configuration configuration, Map licenseMap)
	{
		getExtraSources(sources, igraph, dgraph, sourceList, sourcePath, resources, bundlePath, symbolTable, swcContext, 0,
		                sources.size(), configuration, licenseMap);
	}

	private static void getExtraSources(List sources, DependencyGraph igraph, DependencyGraph dgraph,
										SourceList sourceList, SourcePath sourcePath, ResourceContainer resources,
										ResourceBundlePath bundlePath, SymbolTable symbolTable,
										CompilerSwcContext swcContext, int start, int end, Configuration configuration,
										Map licenseMap)
	{
		for (int i = start; i < end; i++)
		{
			Source s = (Source) sources.get(i);
			CompilationUnit u = (s != null) ? s.getCompilationUnit() : null;

			if (u != null)
			{
				getExtraSources(u, sources, igraph, dgraph, sourceList, sourcePath, resources, bundlePath, symbolTable, swcContext,
				                configuration, licenseMap);
			}
		}
	}

	private static void getExtraSources(CompilationUnit u, List sources, DependencyGraph igraph, DependencyGraph dgraph,
										SourceList sourceList, SourcePath sourcePath, ResourceContainer resources,
										ResourceBundlePath bundlePath, SymbolTable symbolTable, CompilerSwcContext swcContext,
										Configuration configuration, Map licenseMap)
	{
		if ((u.getWorkflow() & extraSources) != 0) return;
		
		if (u.loaderClass != null)
		{
			String className = u.loaderClass;
			MultiName mName = new MultiName(className);
			QName qName = resolveMultiName(u.getSource().getNameForReporting(), mName, sources, sourceList, sourcePath,
			                               resources, swcContext, symbolTable);

			if (qName != null)
			{
				Source tailSource = symbolTable.findSourceByQName(qName);
				addVertexToGraphs(tailSource, tailSource.getCompilationUnit(), igraph, dgraph);
			}
			else
			{
				ThreadLocalToolkit.log(new UnableToResolveClass("factoryClass", className));
			}
		}

		configuration.getResourceBundles().addAll(u.resourceBundleHistory);
		boolean processResourceBundles = configuration.getCompilerConfiguration().useResourceBundleMetadata() && u.resourceBundleHistory.size() > 0;
		
		if (processResourceBundles)
		{
			String[] locales = configuration.getCompilerConfiguration().getLocales();
			
			for (Iterator it = u.resourceBundleHistory.iterator(); it.hasNext();)
			{
				String rbName = NameFormatter.toColon((String) it.next());
				Source source = null;
				QName[] qNames = resolveResourceBundleName(rbName, sources, null, bundlePath,
														   null, swcContext, symbolTable, locales);
				if (qNames != null)
				{
					source = symbolTable.findSourceByResourceBundleName(rbName);
					addVertexToGraphs(source, source.getCompilationUnit(), igraph, dgraph);
					continue;
				}
				
				MultiName mName = new MultiName(rbName);
				QName qName = resolveMultiName(u.getSource().getNameForReporting(), mName, sources, null, sourcePath,
											   null, null, symbolTable);
				if (qName != null)
				{
					source = symbolTable.findSourceByQName(qName);
					addVertexToGraphs(source, source.getCompilationUnit(), igraph, dgraph);
					
					symbolTable.register(rbName, qNames, source);
					continue;
				}

				mName = new MultiName(rbName + I18nUtils.CLASS_SUFFIX);
				qName = resolveMultiName(u.getSource().getNameForReporting(), mName, sources, null, null,
										 null, swcContext, symbolTable);
				if (qName != null)
				{
					source = symbolTable.findSourceByQName(qName);
					addVertexToGraphs(source, source.getCompilationUnit(), igraph, dgraph);

					symbolTable.register(rbName, qNames, source);
				}
				else if (locales.length == 1)
				{
					ThreadLocalToolkit.log(new UnableToResolveResourceBundleForLocale(rbName, locales[0]));
				}
				else if (locales.length > 1)
				{
					ThreadLocalToolkit.log(new UnableToResolveResourceBundle(rbName));
				}
			}
		}

		if ((u.licensedClassReqs != null) && (u.licensedClassReqs.size() > 0))
		{
			for (Iterator it = u.licensedClassReqs.entrySet().iterator(); it.hasNext();)
			{
				Map.Entry e = (Map.Entry) it.next();
				String id = (String) e.getKey();
				String handler = (String) e.getValue();

				if (!hasValidLicense(licenseMap, id))
				{
					MultiName mName = new MultiName(handler);
					QName qName = resolveMultiName(u.getSource().getNameForReporting(), mName, sources, sourceList,
					                               sourcePath, resources, swcContext, symbolTable);
					configuration.getIncludes().add(handler);
					configuration.getExterns().remove(handler);   // don't let them try to extern it

					if (qName != null)
					{
					    // if the license is missing, we still may be able to be in
                        // a "demo" mode under control of the license handler
						Source tailSource = symbolTable.findSourceByQName(qName);
						addVertexToGraphs(tailSource, tailSource.getCompilationUnit(), igraph, dgraph);
					}
					else
					{
					    // no license, no handler, no SWF
						ThreadLocalToolkit.log(new UnableToResolveClass("RequiresLicense handler", handler));
					}
				}
				else
				{
					// if there is a license and the license handler is unconditionally added, remove it.
					configuration.getIncludes().remove(handler);
					configuration.getExterns().add(handler);
				}
			}
		}

		if ((u.extraClasses != null) && (u.extraClasses.size() > 0))
		{
			for (Iterator it = u.extraClasses.iterator(); it.hasNext();)
			{
				String className = (String) it.next();
				MultiName mName = new MultiName(className);
				QName qName = resolveMultiName(u.getSource().getNameForReporting(), mName, sources, sourceList,
				                               sourcePath, resources, swcContext, symbolTable);

				if (qName != null)
				{
					Source tailSource = symbolTable.findSourceByQName(qName);
					addVertexToGraphs(tailSource, tailSource.getCompilationUnit(), igraph, dgraph);
				}
				else
				{
					ThreadLocalToolkit.log(new UnableToResolveNeededClass(className));
				}
			}
		}
		
		u.setWorkflow(extraSources);
	}

	private static void checkResourceBundles(List sources, SymbolTable symbolTable)
			throws CompilerException
	{
		for (Iterator iterator = sources.iterator(); iterator.hasNext();)
		{
			Source s = (Source)iterator.next();
			CompilationUnit u = (s != null) ? s.getCompilationUnit() : null;
			if (u != null && u.resourceBundleHistory.size() > 0)
			{
				for (Iterator it = u.resourceBundleHistory.iterator(); it.hasNext();)
				{
					String rbName = (String) it.next();
					Source rbSource = symbolTable.findSourceByResourceBundleName(rbName);
					if (rbSource != null)
					{
						CompilationUnit rbUnit = rbSource.getCompilationUnit();
						for (int j = 0, size = rbUnit == null ? 0 : rbUnit.topLevelDefinitions.size(); j < size; j++)
						{
							u.resourceBundles.add(rbUnit.topLevelDefinitions.get(j).toString());
						}
					}
				}
			}
		}
		
		if (ThreadLocalToolkit.errorCount() > 0)
		{
			throw new CompilerException();
		}
	}

	private static boolean hasValidLicense(Map licenseMap, String id)
	{
		boolean result = false;

		// If we have a way to associate a particular license with a particular product,
		// we can do something smarter here, but for this release, the decision was
		// to hard-code stuff for charts.  At the moment, the product id is "mx.fbpro",
		if (id.equals("mx.fbpro"))
		{
			if (licenseMap != null)
			{
				// If license.jar is present, create an instance of the
				// flex.license.License class, passing it the licenseMap,
				// and call its isFlexBuilderProValid() method.
				try
				{
					Class licenseClass = Class.forName("flex.license.License");
					Constructor ctor = licenseClass.getConstructor(new Class[] {Map.class});
					Object instance = ctor.newInstance(new Object[] {licenseMap});
					Method method = licenseClass.getMethod("isFlexBuilderProValid", null);
					result = ((Boolean)method.invoke(instance, null)).booleanValue();
				}
				catch (Exception e)
				{
				}
			}
		}

		return result;
	}

	private static void markDone(List sources, List units)
	{
		markDone(sources, units, 0, units.size());
	}

	private static void markDone(List sources, List units, int start, int end)
	{
		for (int i = start; i < end; i++)
		{
			Source s = (Source) sources.get(i);
			CompilationUnit u = (s != null) ? s.getCompilationUnit() : (CompilationUnit) units.get(i);
			// C: There are requirements a CompilationUnit must meet before this marks the unit as Done.
			//    1. is the bytecode available?
			//    2. how about assets??
			if (u.getSource().isCompiled())
			{
				u.setState(CompilationUnit.Done);
			}
		}
	}

	private static flex2.compiler.Compiler getCompiler(Source source, flex2.compiler.Compiler[] compilers)
	{
		for (int i = 0, length = source == null || compilers == null ? 0 : compilers.length; i < length; i++)
		{
			if (compilers[i].isSupported(source.getMimeType()))
			{
				return compilers[i];
			}
		}
		return null;
	}

	/**
	 * builds a list of VirtualFiles from list of path strings.
	 */
	public static List getVirtualFileList(List files) // List<VirtualFile> List<String>
		throws ConfigurationException
	{
		return new ArrayList(fileSetFromPaths(files, false, null, null));
	}

	/**
	 * builds a list of VirtualFiles from list of path strings. Directories are scanned recursively, using mimeTypes
	 * (if not null) as a filter.
	 */
	public static List getVirtualFileList(Collection paths, Set mimeTypes)
		throws ConfigurationException
	{
		return new ArrayList(fileSetFromPaths(paths, true, mimeTypes, null));
	}

	/**
	 * list[0] --> List for FileSpec
	 * list[1] --> List for SourceList 
	 */
	public static List[] getVirtualFileList(Collection paths, Collection stylesheets, Set mimeTypes, List directories)
		throws ConfigurationException
	{
		List[] array = new List[2];
		array[0] = new ArrayList();
		array[1] = new ArrayList();
		
		List list = new ArrayList(fileSetFromPaths(paths, true, mimeTypes, null));
		for (int i = 0, len = list == null ? 0 : list.size(); i < len; i++)
		{
			VirtualFile f = (VirtualFile) list.get(i);
			array[(SourceList.calculatePathRoot(f, directories) == null) ? 0 : 1].add(f);
		}
		for (Iterator j = stylesheets.iterator(); j.hasNext(); )
		{
			VirtualFile f = (VirtualFile) j.next();
			array[(SourceList.calculatePathRoot(f, directories) == null) ? 0 : 1].add(f);
		}
		
		return array;
	}
	
	public static List[] getVirtualFileList(Set fileSet, List directories)
	{
		List[] array = new List[2];
		array[0] = new ArrayList();
		array[1] = new ArrayList();

		for (Iterator i = fileSet.iterator(); i.hasNext(); )
		{
			VirtualFile f = (VirtualFile) i.next();
			array[(SourceList.calculatePathRoot(f, directories) == null) ? 0 : 1].add(f);
		}
		
		return array;
	}

	/**
	 * Build a set of virtual files by scanning a mix of file and directory paths.
	 * @param paths a list of path strings.
	 * @param recurse if true, directories are recursively scanned. If not, they're just added to the returned set
	 * @param mimeTypes if non-null, this filters the files found in scanned directories (but not top-level, i.e.
	 * explicitly given files)
	 * @param fileSet if non-null, files are added to this set and a reference ts returned. If null, a new Set is created.
	 */
	private static Set fileSetFromPaths(Collection paths, boolean recurse, Set mimeTypes, Set fileSet)
		throws ConfigurationException
	{
		boolean topLevel;
		if (topLevel = (fileSet == null))
		{
			fileSet = new HashSet(paths.size());
		}

		for (Iterator iter = paths.iterator(); iter.hasNext(); )
		{
			Object next = iter.next();
			VirtualFile file;
			if (next instanceof VirtualFile)
			{
				file = (VirtualFile) next;
			}
			else
			{
				String path = (next instanceof File) ? ((File)next).getAbsolutePath() : (String)next;
				file = getVirtualFile(path);
			}
			if (file != null)
			{
				if (recurse && file.isDirectory())
				{
					File dir = FileUtil.openFile(file.getName());
					if (dir == null)
					{
						throw new ConfigurationException.IOError(file.getName());
					}

					fileSetFromPaths(Arrays.asList(dir.listFiles()), true, mimeTypes, fileSet);
				}
				else if (topLevel || mimeTypes == null || mimeTypes.contains(file.getMimeType()))
				{
					fileSet.add(file);
				}
			}
		}
		return fileSet;
	}

	public static VirtualFile getVirtualFile(String path) throws ConfigurationException
	{
		return getVirtualFile(path, true);
	}

	/**
	 * Create virtual file for given file and throw configuration exception if not possible
	 */
	public static VirtualFile getVirtualFile(String path, boolean reportError) throws ConfigurationException
	{
		VirtualFile result;
		File file = FileUtil.openFile(path);

		if (file != null && FileUtils.exists(file))
		{
			result = new LocalFile(FileUtil.getCanonicalFile(file));
		}
		else
		{
			PathResolver resolver = ThreadLocalToolkit.getPathResolver();
			result = resolver.resolve(path);

			if (result == null && reportError)
			{
				throw new ConfigurationException.IOError(path);
			}
		}

		return result;
	}

	/**
	 * Encode movie; produce binary output
	 *
	 * @param movie
	 * @throws java.io.IOException
	 */
	public static void encode(Movie movie, OutputStream out) throws IOException
	{
        // TODO PERFORMANCE:
        // We create a TagEncoder, writes everything to the TagEncoder, and then copy the
        // result to the intended output stream. There is no reason for the copy. TagEncoder
        // contains a "protected SwfEncoder writer", and SwfEncoder extends RandomAccessBuffer,
        // which extends ByteArrayOutputStream -- this writer would need to be replaced with
        // some other object that can accept an OutputStream in its constructor. The point is
        // to eliminate the extra buffers, and just always write directly to the intended target.
        // - mikemo 
		TagEncoder encoder = new TagEncoder();
		new MovieEncoder(encoder).export(movie);
		encoder.writeTo(out);

		if (ThreadLocalToolkit.getBenchmark() != null)
		{
            LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();
			ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new SWFEncoding()));
		}
	}

	public static void encode(ConsoleApplication app, OutputStream out) throws IOException
	{
		List abcList = app.getABCs();
		for (int i = 0, size = abcList.size(); i < size; i++)
		{
			out.write((byte[]) abcList.get(i));
		}

		if (ThreadLocalToolkit.getBenchmark() != null)
		{
            LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();
			ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new SWFEncoding()));
		}
	}

	public static void persistCompilationUnits(Configuration configuration, FileSpec fileSpec, SourceList sourceList, SourcePath sourcePath,
											   ResourceContainer resources, ResourceBundlePath bundlePath,
											   int checksum, String description, RandomAccessFile f)
		throws IOException
	{
		persistCompilationUnits(configuration, fileSpec, sourceList, sourcePath, resources, bundlePath, null, null, checksum, checksum, checksum, checksum, null, null, description, f);
    }

    public static void persistCompilationUnits(Configuration configuration,
            FileSpec fileSpec, SourceList sourceList, SourcePath sourcePath,
            ResourceContainer resources, ResourceBundlePath bundlePath,
            List sources, List units, int checksums[],
            Map swcDefSignatureChecksums, Map swcFileChecksums,
            Map archiveFiles,
            String description, RandomAccessFile f) throws IOException
    {
        persistCompilationUnits(configuration, fileSpec, sourceList, sourcePath, resources, bundlePath, 
                sources, units, 
                checksums[0], checksums[1], checksums[2], checksums[3], 
                swcDefSignatureChecksums, swcFileChecksums, 
                archiveFiles, description, f);
    }

    public static void persistCompilationUnits(Configuration configuration, FileSpec fileSpec, SourceList sourceList, SourcePath sourcePath,
            ResourceContainer resources, ResourceBundlePath bundlePath,
            List sources, List units,
            int checksum, int cmd_checksum, int linker_checksum, int swc_checksum,
            Map swcDefSignatureChecksums, Map swcFileChecksums,
            String description, RandomAccessFile f)
        throws IOException
   {
        persistCompilationUnits(configuration, fileSpec, sourceList, sourcePath,
                resources, bundlePath,
                sources, units,
                checksum, cmd_checksum, linker_checksum, swc_checksum,
                swcDefSignatureChecksums, swcFileChecksums, null,
                description, f);
   }

    public static void persistCompilationUnits(Configuration configuration, FileSpec fileSpec, SourceList sourceList, SourcePath sourcePath,
											   ResourceContainer resources, ResourceBundlePath bundlePath,
											   List sources, List units,
											   int checksum, int cmd_checksum, int linker_checksum, int swc_checksum,
											   Map swcDefSignatureChecksums, Map swcFileChecksums,
											   Map archiveFiles,
											   String description, RandomAccessFile f)
		throws IOException
	{
		PersistenceStore store = new PersistenceStore(configuration, f);
		int count = -1;
		try
		{
			count = store.write(fileSpec, sourceList, sourcePath, resources, bundlePath, sources, units,
								checksum, cmd_checksum, linker_checksum, swc_checksum,
								swcDefSignatureChecksums, swcFileChecksums, description, archiveFiles);
		}
		finally
		{
			if (count != -1 && ThreadLocalToolkit.getBenchmark() != null)
			{
				LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();
				ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new PersistingCompilationUnits(count)));
			}
		}
	}

    public static void loadCompilationUnits(Configuration configuration, FileSpec fileSpec, SourceList sourceList, SourcePath sourcePath,
                ResourceContainer resources, ResourceBundlePath bundlePath,
                List sources, List units,
                int[] checksums, Map swcDefSignatureChecksums, Map swcFileChecksums,
                RandomAccessFile f, String cacheName) throws IOException
    {
        loadCompilationUnits(configuration, fileSpec, sourceList, sourcePath,
                resources, bundlePath, sources, units, checksums, 
                swcDefSignatureChecksums, swcFileChecksums, null, f, cacheName, null);
    }

	public static void loadCompilationUnits(Configuration configuration, FileSpec fileSpec, SourceList sourceList, SourcePath sourcePath,
											ResourceContainer resources, ResourceBundlePath bundlePath,
											int checksum, RandomAccessFile f, String cacheName)
		throws IOException
	{
		loadCompilationUnits(configuration, fileSpec, sourceList, sourcePath, resources, bundlePath, null, null,
							 new int[] {checksum, checksum, checksum, checksum}, null, null, null, f, cacheName, null);
	}

	public static void loadCompilationUnits(Configuration configuration, FileSpec fileSpec, SourceList sourceList, SourcePath sourcePath,
											ResourceContainer resources, ResourceBundlePath bundlePath,
											List sources, List units,
											int[] checksums, Map swcDefSignatureChecksums, Map swcFileChecksums,
											RandomAccessFile f, String cacheName, FontManager fontManager)
		throws IOException
	{
	        loadCompilationUnits(configuration, fileSpec, sourceList, sourcePath, resources, bundlePath, null, null,
                    checksums, null, null, null, f, cacheName, null);
	}

	public static void loadCompilationUnits(Configuration configuration, FileSpec fileSpec, SourceList sourceList, SourcePath sourcePath,
											ResourceContainer resources, ResourceBundlePath bundlePath,
											List sources, List units,
											int[] checksums, Map swcDefSignatureChecksums, Map swcFileChecksums,
											Map archiveFiles,
											RandomAccessFile f, String cacheName, FontManager fontManager)
		throws IOException
	{
		LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();
		PersistenceStore store = new PersistenceStore(configuration, f, fontManager);		
		int count = -1;
		try
		{
			if ((count = store.read(fileSpec, sourceList, sourcePath, resources, bundlePath, sources, units,
									checksums, swcDefSignatureChecksums, swcFileChecksums, archiveFiles)) < 0)
			{
				throw new IOException(l10n.getLocalizedTextString(new FailedToMatchCacheFile(cacheName)));
			}
		}
		finally
		{
			if (count >= 0 && ThreadLocalToolkit.getBenchmark() != null)
			{
				ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new LoadingCompilationUnits(count)));
			}
		}
	}

	private static boolean tooManyErrors()
	{
		return ThreadLocalToolkit.errorCount() > 100;
	}

	public static boolean forcedToStop()
	{
		CompilerControl cc = ThreadLocalToolkit.getCompilerControl();
		return (cc != null && cc.getStatus() == CompilerControl.STOP);
	}

	private static void calculateProgress(List sources, SymbolTable symbolTable)
	{
		symbolTable.tick++;
		int total = sources.size() * 12;
		double p = (double) symbolTable.tick / (double) total;
		int percent = (int) (p * 100);

		if (percent > 100)
		{
			percent = 100;
		}

		if (percent > symbolTable.currentPercentage)
		{
			symbolTable.currentPercentage = percent;
			ProgressMeter meter = ThreadLocalToolkit.getProgressMeter();

			if (meter != null)
			{
				meter.percentDone(percent);
			}
		}
	}

    // Useful when debugging batch issues.
	private static String workflowToString(int workflow)
	{
		String result;

		if ((workflow & extraSources) >= 1)
		{
			result = "extraSources";
		}
		else if ((workflow & adjustQNames) >= 1)
		{
			result = "adjustQNames";
		}
		else if ((workflow & resolveImportStatements) >= 1)
		{
			result = "resolveImportStatements";
		}
		else if ((workflow & generate) >= 1)
		{
			result = "generate";
		}
		else if ((workflow & resolveType) >= 1)
		{
			result = "resolveType";
		}
		else if ((workflow & analyze4) >= 1)
		{
			result = "analyze4";
		}
		else if ((workflow & analyze3) >= 1)
		{
			result = "analyze3";
		}
		else if ((workflow & analyze2) >= 1)
		{
			result = "analyze2";
		}
		else if ((workflow & analyze1) >= 1)
		{
			result = "analyze1";
		}
		else if ((workflow & parse2) >= 1)
		{
			result = "parse2";
		}
		else if ((workflow & parse1) >= 1)
		{
			result = "parse1";
		}
		else if ((workflow & preprocess) >= 1)
		{
			result = "preprocess";
		}
		else
		{
			result = "before preprocessed";
		}

		return result;
	}

	// error messages

	public static class BetaExpired extends CompilerMessage.CompilerError
	{
		public BetaExpired()
		{
			super();
		}
	}

	public static class UnableToSetHeadless extends CompilerMessage.CompilerWarning
	{
		public UnableToSetHeadless()
		{
			super();
		}
	}

	public static class IncompatibleSWCArchive extends CompilerMessage.CompilerError
	{
		public IncompatibleSWCArchive(String swc)
		{
			super();
			this.swc = swc;
		}

		public final String swc;
	}

	public static class InfoCompiling extends CompilerMessage.CompilerInfo
	{
		public InfoCompiling()
		{
			super();
		}
	}

	public static class OutputTime extends CompilerMessage.CompilerInfo
	{
		public OutputTime(int size)
		{
			super();
			this.size = size;
		}

		public final int size;
	}

	public static class ForceRecompilation extends CompilerMessage.CompilerInfo
	{
		public ForceRecompilation()
		{
			super();
		}
	}

	public static class SourceFileChanged extends CompilerMessage.CompilerInfo
	{
		public SourceFileChanged()
		{
			super();
		}
	}

	public static class NotFullyCompiled extends CompilerMessage.CompilerInfo
	{
		public NotFullyCompiled()
		{
			super();
		}
	}

	public static class SourceNoLongerExists extends CompilerMessage.CompilerInfo
	{
		public SourceNoLongerExists()
		{
			super();
		}
	}

	public static class SourceFileUpdated extends CompilerMessage.CompilerInfo
	{
		public SourceFileUpdated()
		{
			super();
		}
	}

	public static class AssetUpdated extends CompilerMessage.CompilerInfo
	{
		public AssetUpdated()
		{
			super();
		}
	}

	public static class NotSourcePathFirstPreference extends CompilerMessage.CompilerInfo
	{
		public NotSourcePathFirstPreference()
		{
			super();
		}
	}

	public static class DependentFileNoLongerExists extends CompilerMessage.CompilerInfo
	{
		public DependentFileNoLongerExists(String location)
		{
			super();
			this.location = location;
		}

		public final String location;
	}

	public static class InvalidImportStatement extends CompilerMessage.CompilerInfo
	{
		public InvalidImportStatement(String defName)
		{
			super();
			this.defName = defName;
		}

		public final String defName;
	}

	public static class DependentFileModified extends CompilerMessage.CompilerInfo
	{
		public DependentFileModified(String location)
		{
			super();
			this.location = location;
		}

		public final String location;
	}

	public static class SuperclassUpdated extends CompilerMessage.CompilerInfo
	{
		public SuperclassUpdated(String sourceName, QName qname)
		{
			super();
			this.sourceName = sourceName;
			this.qname = qname;
		}

		public final String sourceName;
		public final QName qname;
	}

	public static class QNameSourceUpdated extends CompilerMessage.CompilerInfo
	{
		public QNameSourceUpdated(String sourceName, QName qname)
		{
			super();
			this.sourceName = sourceName;
			this.qname = qname;
		}

		public final String sourceName;
		public final QName qname;
	}

	public static class MultiNameMeaningChanged extends CompilerMessage.CompilerInfo
	{
		public MultiNameMeaningChanged(MultiName multiName, QName qName)
		{
			super();
			this.multiName = multiName;
			this.qName = qName;
		}

		public final MultiName multiName;
		public final QName qName;
	}

	public static class FilesChangedAffected extends CompilerMessage.CompilerInfo
	{
		public FilesChangedAffected(int updateCount, int count)
		{
			super();
			this.updateCount = updateCount;
			this.count = count;
		}

		public final int updateCount, count;
	}

	public static class MoreThanOneDefinition extends CompilerMessage.CompilerError
	{
		public MoreThanOneDefinition(List topLevelDefinitions)
		{
			super();
			this.topLevelDefinitions = topLevelDefinitions;
		}

		public final List topLevelDefinitions;
	}

	public static class MustHaveOneDefinition extends CompilerMessage.CompilerError
	{
		public MustHaveOneDefinition()
		{
			super();
		}
	}

	public static class WrongPackageName extends CompilerMessage.CompilerError
	{
		public WrongPackageName(String pathPackage, String defPackage)
		{
			super();
			this.pathPackage = pathPackage;
			this.defPackage = defPackage;
		}

		public final String pathPackage, defPackage;
	}

	public static class WrongDefinitionName extends CompilerMessage.CompilerError
	{
		public WrongDefinitionName(String pathName, String defName)
		{
			super();
			this.pathName = pathName;
			this.defName = defName;
		}

		public final String pathName, defName;
	}

	public static class DefinitionNameFileNameMismatch extends CompilerMessage.CompilerError
	{
		public DefinitionNameFileNameMismatch(String defName, String pathName)
		{
			super();
			this.pathName = pathName;
			this.defName = defName;
		}

		public final String pathName, defName;
	}

	public static class CircularInheritance extends CompilerMessage.CompilerError
	{
		public CircularInheritance()
		{
			super();
		}
	}

	public static class UnableToResolveDependency extends CompilerMessage.CompilerWarning
	{
		public UnableToResolveDependency(String localPart)
		{
			super();
			this.localPart = localPart;
		}

		public final String localPart;
	}

	public static class AmbiguousMultiname extends CompilerMessage.CompilerError
	{
		public AmbiguousMultiname(QName qName1, String source1, QName qName2, String source2)
		{
			super();
			this.qName1 = qName1;
			this.source1 = source1;
			this.qName2 = qName2;
			this.source2 = source2;
		}

		public final QName qName1, qName2;
		public final String source1, source2;
	}

	public static class SWFEncoding extends CompilerMessage.CompilerInfo
	{
		public SWFEncoding()
		{
			super();
		}
	}

	public static class PersistingCompilationUnits extends CompilerMessage.CompilerInfo
	{
		public PersistingCompilationUnits(int count)
		{
			super();
			this.count = count;
		}

		public final int count;
	}

	public static class FailedToMatchCacheFile extends CompilerMessage.CompilerInfo
	{
		public FailedToMatchCacheFile(String cacheName)
		{
			super();
			this.cacheName = cacheName;
		}

		public final String cacheName;
	}

	public static class LoadingCompilationUnits extends CompilerMessage.CompilerInfo
	{
		public LoadingCompilationUnits(int count)
		{
			super();
			this.count = count;
		}

		public final int count;
	}

	public static class ChannelDefinitionNotFound extends CompilerMessage.CompilerError
	{
		public ChannelDefinitionNotFound(String clientType)
		{
			super();
			this.clientType = clientType;
		}

		public final String clientType;
	}

	public static class TooManyErrors extends CompilerMessage.CompilerInfo
	{
		public TooManyErrors()
		{
			super();
		}
	}

	public static class ForcedToStop extends CompilerMessage.CompilerInfo
	{
		public ForcedToStop()
		{
			super();
		}
	}

	public static class UnableToResolveClass extends CompilerMessage.CompilerError
	{
		public UnableToResolveClass(String type, String className)
		{
			super();
			this.type = type;
			this.className = className;
		}

		public final String type;
		public final String className;
	}

	public static class UnableToResolveNeededClass extends CompilerMessage.CompilerError
	{
		public UnableToResolveNeededClass(String className)
		{
			super();
			this.className = className;
		}

		public final String className;
	}

	public static class UnableToResolveResourceBundle extends CompilerMessage.CompilerError
	{
		public UnableToResolveResourceBundle(String bundleName)
		{
			super();
			this.bundleName = bundleName;
		}

		public final String bundleName;
	}

	public static class UnableToResolveResourceBundleForLocale extends CompilerMessage.CompilerError
	{
		public UnableToResolveResourceBundleForLocale(String bundleName, String locale)
		{
			super();
			this.bundleName = bundleName;
			this.locale = locale;
		}

		public final String bundleName;
		public final String locale;
	}

	public static class NotResourceBundleSubclass extends CompilerMessage.CompilerError
	{
		public NotResourceBundleSubclass(String className, String sourceName, String resourceBundle)
		{
			super();
			this.className = className;
			this.sourceName = sourceName;
			this.resourceBundle = resourceBundle;
		}

		public final String className;
		public final String sourceName;
		public final String resourceBundle;
	}

 	public static class ShowBetaExpiration extends CompilerMessage.CompilerInfo
 	{
 		public ShowBetaExpiration(String date)
 		{
 			super();
 			this.date = date;
 		}

 		public final String date;
 	}
}
