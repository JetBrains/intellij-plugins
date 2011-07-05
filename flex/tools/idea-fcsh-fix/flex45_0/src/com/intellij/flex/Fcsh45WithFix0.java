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
// svn://opensource.adobe.com/svn/opensource/flex/sdk/trunk/modules/compiler/src/java/flex2/tools/Fcsh.java (revision 21499)

package com.intellij.flex;

import flash.localization.LocalizationManager;
import flash.localization.ResourceBundleLocalizer;
import flash.util.Trace;
import flex2.compiler.*;
import flex2.compiler.common.CompilerConfiguration;
import flex2.compiler.common.Configuration;
import flex2.compiler.common.DefaultsConfigurator;
import flex2.compiler.config.ConfigurationBuffer;
import flex2.compiler.config.ConfigurationException;
import flex2.compiler.i18n.I18nUtils;
import flex2.compiler.io.FileUtil;
import flex2.compiler.io.VirtualFile;
import flex2.compiler.swc.SwcAPI;
import flex2.compiler.swc.SwcCache;
import flex2.compiler.swc.SwcException;
import flex2.compiler.util.Benchmark;
import flex2.compiler.util.NameMappings;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.linker.LinkerAPI;
import flex2.linker.LinkerException;
import flex2.tools.*;
import static flex2.tools.Fcsh.*;
import flex2.tools.Mxmlc.InitialSetup;
import flex2.tools.Mxmlc.OutputMessage;
import flex2.tools.PreLink;

import java.io.*;
import java.util.*;

/**
 * fcsh (Flex Compiler SHell)
 *
 * @author Clement Wong
 */
public class Fcsh45WithFix0 extends Tool
{
    public static void main(String[] args) throws IOException
    {
        exit = false;
        counter = 1;
        targets = new HashMap<String, Target>();
        processes = new HashMap<String, Process>();

        String s;
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

        CompilerAPI.useAS3();

        LocalizationManager localizationManager = new LocalizationManager();
        localizationManager.addLocalizer( new ResourceBundleLocalizer() );
        ThreadLocalToolkit.setLocalizationManager( localizationManager );

        intro();
        prompt();
        while ((s = r.readLine()) != null)
        {
            CompilerAPI.useConsoleLogger();

            if (s.trim().length() == 0)
            {
                prompt();
                continue;
            }

//            try
//            {
                process(s);
//            }
//            catch (Throwable t)
//            {
//                if (Trace.error)
//                {
//                    t.printStackTrace();
//                }
//            }

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
    private static Map<String, Target> targets;
    private static Map<String, Process> processes;

    private static void process(String s)
    {
        macromedia.asc.util.ContextStatics.omitTrace = false; // reset to initial state

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
                    Target target = targets.get("" + id);
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
                    Target target = targets.get("" + id);
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
                    link(id);
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
                HashSet<String> keys = new HashSet<String>(targets.keySet());
                for (Iterator<String> i = keys.iterator(); i.hasNext();)
                {
                    clear(i.next());
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
                HashSet<String> keys = new HashSet<String>(targets.keySet());
                for (Iterator<String> i = keys.iterator(); i.hasNext();)
                {
                    info(i.next());
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
            Set<String> names = new HashSet<String>(targets.keySet());
            for (Iterator<String> i = names.iterator(); i.hasNext();)
            {
                process("clear " + i.next());
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
        Process p = processes.remove(target);

        if (p != null)
        {
            p.destroy();
        }

        targets.remove(target);
    }

    private static void info(String target)
    {
        Target s = targets.get(target);
        ThreadLocalToolkit.logInfo("id: " + s.id);
        StringBuilder b = new StringBuilder();
        for (int i = 0, size = s.args.length; i < size; i++)
        {
            b.append(s.args[i]);
            b.append(' ');
        }
        ThreadLocalToolkit.logInfo((s instanceof SwcTarget ? "compc: " : "mxmlc: ") + b);
    }

    private static void compile(String id)
    {
        Target s = targets.get(id);
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
        Benchmark benchmark = null;

        try
        {
            // setup the path resolver
            CompilerAPI.usePathResolver();

            // process configuration
            ConfigurationBuffer cfgbuf = new ConfigurationBuffer(CompcConfiguration.class, CompcConfiguration.getAliases());
            cfgbuf.setDefaultVar("include-classes");
            DefaultsConfigurator.loadCompcDefaults( cfgbuf );
            CompcConfiguration configuration = (CompcConfiguration) Mxmlc.processConfiguration(
                ThreadLocalToolkit.getLocalizationManager(), "compc", s.args, cfgbuf, CompcConfiguration.class, "include-classes");

            CompilerAPI.setupHeadless(configuration);

            if (configuration.benchmark())
            {
                benchmark = CompilerAPI.runBenchmark();
                benchmark.startTime(Benchmark.PRECOMPILE);
            }
            else
            {
                CompilerAPI.disableBenchmark();
            }

            s.sourcePath.clearCache();
            s.bundlePath.clearCache();
            s.resources.refresh();

            // C: We don't really need to parse the manifest files again.
            CompilerConfiguration compilerConfig = configuration.getCompilerConfiguration();
            // note: if Configuration is ever shared with other parts of the system, then this part will need
            // to change, since we're setting a compc-specific setting below
            compilerConfig.setMetadataExport(true);

            NameMappings mappings = CompilerAPI.getNameMappings(configuration);

            Transcoder[] transcoders = WebTierAPI.getTranscoders( configuration );
            SubCompiler[] compilers = WebTierAPI.getCompilers(compilerConfig, mappings, transcoders);

            if (benchmark != null)
            {
                benchmark.benchmark(l10n.getLocalizedTextString(new InitialSetup()));
            }

            // load SWCs
            CompilerSwcContext swcContext = new CompilerSwcContext(true);
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
            if (recompile || CompilerAPI.validateCompilationUnits(s.fileSpec, s.sourceList, s.sourcePath, s.bundlePath, s.resources,
                                                                  swcContext, s.classes, s.perCompileData, configuration) > 0)
            {
                Map licenseMap = configuration.getLicensesConfiguration().getLicenseMap();

                // create a symbol table
                SymbolTable symbolTable = new SymbolTable(configuration, s.perCompileData);
                s.configuration = configuration;

                Map<String, Source> classes = new HashMap<String, Source>();
                s.nsComponents = SwcAPI.setupNamespaceComponents(configuration, mappings, s.sourcePath, s.sourceList, classes);
                SwcAPI.setupClasses(configuration, s.sourcePath, s.sourceList, classes);
                // Only updated the SwcTarget's classes if
                // setupNamespaceComponents() and setupClasses() are
                // successful.
                s.classes = classes;

                Map<String, VirtualFile> rbFiles = new HashMap<String, VirtualFile>();

                if (benchmark != null)
                {
                    benchmark.stopTime(Benchmark.PRECOMPILE, false);
                }

                List<CompilationUnit> units = CompilerAPI.compile(s.fileSpec, s.sourceList, s.classes.values(),
                                                                  s.sourcePath, s.resources, s.bundlePath, swcContext,
                                                                  symbolTable, mappings, configuration, compilers,
                                                                  new CompcPreLink(rbFiles, configuration.getIncludeResourceBundles(),
                                                                		  false),
                                                                  licenseMap, new ArrayList<Source>());

                if (benchmark != null)
                {
                    benchmark.startTime(Benchmark.POSTCOMPILE);
                }

                s.units = units;
                s.rbFiles = rbFiles;
                s.sourcePath.clearCache();
                s.bundlePath.clearCache();
                s.resources.refresh();
            }
            else
            {
                ThreadLocalToolkit.logInfo(l10n.getLocalizedTextString(new NoChange()));
            }
        }
        catch (ConfigurationException ex)
        {
            Compc.displayStartMessage();
            Mxmlc.processConfigurationException(ex, "compc");
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
            if (Trace.error)
            {
                t.printStackTrace();
            }
        }
        finally
        {
            if (benchmark != null)
            {
                if ((ThreadLocalToolkit.errorCount() == 0) &&
                    benchmark.hasStarted(Benchmark.POSTCOMPILE))
                {
                    benchmark.stopTime(Benchmark.POSTCOMPILE, false);
                }
                benchmark.totalTime();
                benchmark.peakMemoryUsage(true);
            }

            CompilerAPI.removePathResolver();
        }
    }

    private static void compile_mxmlc(Target s)
    {
        LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();
        Benchmark benchmark = null;

        try
        {
            // setup the path resolver
            CompilerAPI.usePathResolver();

            // process configuration
            ConfigurationBuffer cfgbuf = new ConfigurationBuffer(CommandLineConfiguration.class, Configuration.getAliases());
            cfgbuf.setDefaultVar(Mxmlc.FILE_SPECS);
            DefaultsConfigurator.loadDefaults( cfgbuf );
            CommandLineConfiguration configuration = (CommandLineConfiguration) Mxmlc.processConfiguration(
                ThreadLocalToolkit.getLocalizationManager(), "mxmlc", s.args, cfgbuf, CommandLineConfiguration.class, Mxmlc.FILE_SPECS);

            CompilerAPI.setupHeadless(configuration);

            if (configuration.benchmark())
            {
                benchmark = CompilerAPI.runBenchmark();
                benchmark.startTime(Benchmark.PRECOMPILE);
            }
            else
            {
                CompilerAPI.disableBenchmark();
            }

            s.sourcePath.clearCache();
            s.bundlePath.clearCache();
            s.resources.refresh();

            // C: We don't really need to parse the manifest files again.
            CompilerConfiguration compilerConfig = configuration.getCompilerConfiguration();
            NameMappings mappings = CompilerAPI.getNameMappings(configuration);

            Transcoder[] transcoders = WebTierAPI.getTranscoders( configuration );
            SubCompiler[] compilers = WebTierAPI.getCompilers(compilerConfig, mappings, transcoders);

            if (benchmark != null)
            {
                benchmark.benchmark(l10n.getLocalizedTextString(new InitialSetup()));
            }

            CompilerSwcContext swcContext = new CompilerSwcContext(true);
            swcContext.load( compilerConfig.getLibraryPath(),
                             Configuration.getAllExcludedLibraries(compilerConfig, configuration),
                             compilerConfig.getThemeFiles(),
                             compilerConfig.getIncludeLibraries(),
                             mappings,
                             I18nUtils.getTranslationFormat(compilerConfig),
                             s.swcCache );
            configuration.addExterns( swcContext.getExterns() );
            configuration.addIncludes( swcContext.getIncludes() );
            configuration.getCompilerConfiguration().addThemeCssFiles( swcContext.getThemeStyleSheets() );

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
            if (recompile || CompilerAPI.validateCompilationUnits(s.fileSpec, s.sourceList, s.sourcePath, s.bundlePath, s.resources,
                                                                  swcContext, s.perCompileData,
                                                                  configuration) > 0)
            {
                Map licenseMap = configuration.getLicensesConfiguration().getLicenseMap();

                // create a symbol table
                SymbolTable symbolTable = new SymbolTable(configuration, s.perCompileData);
                s.configuration = configuration;

                VirtualFile projector = configuration.getProjector();

                if (benchmark != null)
                {
                    benchmark.stopTime(Benchmark.PRECOMPILE, false);
                }

                if (projector != null && projector.getName().endsWith("avmplus.exe"))
                {
                    s.units = CompilerAPI.compile(s.fileSpec, s.sourceList, null, s.sourcePath, s.resources, s.bundlePath,
                                                  swcContext, symbolTable, mappings, configuration, compilers,
                                                  null, licenseMap, new ArrayList<Source>());
                }
                else
                {
                    s.units = CompilerAPI.compile(s.fileSpec, s.sourceList, null, s.sourcePath, s.resources, s.bundlePath,
                                                  swcContext, symbolTable, mappings, configuration, compilers,
                                                  new PreLink(), licenseMap, new ArrayList<Source>());
                }

                if (benchmark != null)
                {
                    benchmark.startTime(Benchmark.POSTCOMPILE);
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
        catch (ConfigurationException ex)
        {
            Mxmlc.processConfigurationException(ex, "mxmlc");
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
            if (Trace.error)
            {
                t.printStackTrace();
            }
        }
        finally
        {
            if (benchmark != null)
            {
                if ((ThreadLocalToolkit.errorCount() == 0) &&
                    benchmark.hasStarted(Benchmark.POSTCOMPILE))
                {
                    benchmark.stopTime(Benchmark.POSTCOMPILE, false);
                }
                benchmark.totalTime();
                benchmark.peakMemoryUsage(true);
            }

            CompilerAPI.removePathResolver();
        }
    }

    private static void link(String target)
    {
        Target s = targets.get(target);
        if (s instanceof SwcTarget)
        {
            link_compc((SwcTarget) s);
        }
        else
        {
            link_mxmlc(s);
        }
    }

    private static void link_compc(SwcTarget s)
    {
        try
        {
            ThreadLocalToolkit.resetBenchmark();

            // setup the path resolver
            CompilerAPI.usePathResolver();

            if (s.units != null)
            {
                // export SWC
                SwcAPI.exportSwc( (CompcConfiguration) s.configuration, s.units, s.nsComponents, s.swcCache, s.rbFiles );

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
            if (Trace.error)
            {
                t.printStackTrace();
            }
        }
        finally
        {
            Benchmark benchmark = ThreadLocalToolkit.getBenchmark();
            if (benchmark != null)
            {
                benchmark.totalTime();
                benchmark.peakMemoryUsage(true);
            }

            CompilerAPI.removePathResolver();
        }
    }

    private static void link_mxmlc(Target s)
    {
        OutputStream swfOut = null;

        try
        {
            ThreadLocalToolkit.resetBenchmark();

            // link
            if (s.units != null)
            {
                VirtualFile projector = ((CommandLineConfiguration) s.configuration).getProjector();
                PostLink postLink = null;

                if (s.configuration.optimize() && !s.configuration.debug())
                {
                    postLink = new PostLink(s.configuration);
                }

                if (projector != null && projector.getName().endsWith("avmplus.exe"))
                {
                    // link
                    s.app = LinkerAPI.linkConsole(s.units, postLink, s.configuration);

                    // output .exe
                    File file = FileUtil.openFile(s.outputName, true);
                    swfOut = new BufferedOutputStream(new FileOutputStream(file));

                    Mxmlc.createProjector(s.configuration, projector, s.app, swfOut);

                    swfOut.flush();
                    swfOut.close();

                    ThreadLocalToolkit.log(new OutputMessage(s.outputName, Long.toString(file.length())));
                }
                else
                {
                    // link
                    s.movie = LinkerAPI.link(s.units, postLink, s.configuration);

                    // output SWF
                    File file = FileUtil.openFile(s.outputName, true);
                    swfOut = new BufferedOutputStream(new FileOutputStream(file));

                    if (projector != null)
                    {
                        Mxmlc.createProjector(s.configuration, projector, s.movie, swfOut);
                    }
                    else
                    {
                        CompilerAPI.encode(s.configuration, s.movie, swfOut);
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
            if (Trace.error)
            {
                t.printStackTrace();
            }
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
        Benchmark benchmark = null;
        OutputStream swfOut = null;
        Target s = new Target();
        s.id = id;

        try
        {
            // setup the path resolver
            CompilerAPI.usePathResolver();

            // process configuration
            ConfigurationBuffer cfgbuf = new ConfigurationBuffer(CommandLineConfiguration.class, Configuration.getAliases());
            cfgbuf.setDefaultVar(Mxmlc.FILE_SPECS);
            DefaultsConfigurator.loadDefaults( cfgbuf );
            CommandLineConfiguration configuration = (CommandLineConfiguration) Mxmlc.processConfiguration(
                ThreadLocalToolkit.getLocalizationManager(), "mxmlc", args, cfgbuf, CommandLineConfiguration.class, Mxmlc.FILE_SPECS);
            s.configuration = configuration;

            CompilerAPI.setupHeadless(configuration);

            if (configuration.benchmark())
            {
                benchmark = CompilerAPI.runBenchmark();
                benchmark.startTime(Benchmark.PRECOMPILE);
            }
            else
            {
                CompilerAPI.disableBenchmark();
            }

            String target = configuration.getTargetFile();
            targets.put("" + id, s);
            s.args = args;

            // make sure targetFile abstract pathname is an absolute path...
            VirtualFile targetFile = CompilerAPI.getVirtualFile(target);
            WebTierAPI.checkSupportedTargetMimeType(targetFile);
            List<VirtualFile> virtualFileList = CompilerAPI.getVirtualFileList(configuration.getFileList());

            CompilerConfiguration compilerConfig = configuration.getCompilerConfiguration();
            NameMappings mappings = CompilerAPI.getNameMappings(configuration);

            Transcoder[] transcoders = WebTierAPI.getTranscoders( configuration );
            SubCompiler[] compilers = WebTierAPI.getCompilers(compilerConfig, mappings, transcoders);

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
            s.fileSpec = new FileSpec(Collections.<VirtualFile>emptyList(), WebTierAPI.getFileSpecMimeTypes());

            // create a SourceList
            s.sourceList = new SourceList(virtualFileList, asClasspath, targetFile, WebTierAPI.getSourcePathMimeTypes());

            // create a SourcePath...
            s.sourcePath = new SourcePath(asClasspath, targetFile, WebTierAPI.getSourcePathMimeTypes(),
                                          compilerConfig.allowSourcePathOverlap());

            // create a ResourceContainer...
            s.resources = new ResourceContainer();

            // create a ResourceBundlePath...
            s.bundlePath = new ResourceBundlePath(configuration.getCompilerConfiguration(), targetFile);

            if (benchmark != null)
            {
                benchmark.benchmark(l10n.getLocalizedTextString(new InitialSetup()));
            }

            // load SWCs
            s.swcCache = new SwcCache();

            CompilerSwcContext swcContext = new CompilerSwcContext(true);
            swcContext.load( compilerConfig.getLibraryPath(),
                             Configuration.getAllExcludedLibraries(compilerConfig, configuration),
                             compilerConfig.getThemeFiles(),
                             compilerConfig.getIncludeLibraries(),
                             mappings,
                             I18nUtils.getTranslationFormat(compilerConfig),
                             s.swcCache );
            configuration.addExterns( swcContext.getExterns() );
            configuration.addIncludes( swcContext.getIncludes() );
            configuration.getCompilerConfiguration().addThemeCssFiles( swcContext.getThemeStyleSheets() );

               s.checksum = cfgbuf.checksum_ts() + swcContext.checksum();

            final SymbolTable symbolTable = new SymbolTable(configuration);
            s.perCompileData = symbolTable.perCompileData;

            Map licenseMap = configuration.getLicensesConfiguration().getLicenseMap();
            PostLink postLink = null;

            if (configuration.optimize() && !configuration.debug())
            {
                postLink = new PostLink(configuration);
            }

            if (projector != null && projector.getName().endsWith("avmplus.exe"))
            {
                if (benchmark != null)
                {
                    benchmark.stopTime(Benchmark.PRECOMPILE, false);
                }

                List<CompilationUnit> units = CompilerAPI.compile(s.fileSpec, s.sourceList, null, s.sourcePath, s.resources, s.bundlePath,
                                                        swcContext, symbolTable, mappings, configuration, compilers,
                                                        null, licenseMap, new ArrayList<Source>());

                if (benchmark != null)
                {
                    benchmark.startTime(Benchmark.POSTCOMPILE);
                }

                s.units = units;
                s.sourcePath.clearCache();
                s.bundlePath.clearCache();
                s.resources.refresh();

                // link
                s.app = LinkerAPI.linkConsole(units, postLink, configuration);

                // output .exe
                File file = FileUtil.openFile(s.outputName, true);
                swfOut = new BufferedOutputStream(new FileOutputStream(file));

                Mxmlc.createProjector(configuration, projector, s.app, swfOut);

                swfOut.flush();
                swfOut.close();

                ThreadLocalToolkit.log(new OutputMessage(s.outputName, Long.toString(file.length())));
            }
            else
            {
                if (benchmark != null)
                {
                    benchmark.stopTime(Benchmark.PRECOMPILE, false);
                }

                List<CompilationUnit> units = CompilerAPI.compile(s.fileSpec, s.sourceList, null, s.sourcePath, s.resources, s.bundlePath,
                                                        swcContext, symbolTable, mappings, configuration, compilers,
                                                        new PreLink(), licenseMap, new ArrayList<Source>());

                if (benchmark != null)
                {
                    benchmark.startTime(Benchmark.POSTCOMPILE);
                }

                s.units = units;
                s.sourcePath.clearCache();
                s.bundlePath.clearCache();
                s.resources.refresh();

                // link
                s.movie = LinkerAPI.link(units, postLink, configuration);

                // output SWF
                File file = FileUtil.openFile(s.outputName, true);
                swfOut = new BufferedOutputStream(new FileOutputStream(file));

                if (projector != null)
                {
                    Mxmlc.createProjector(configuration, projector, s.movie, swfOut);
                }
                else
                {
                    CompilerAPI.encode(configuration, s.movie, swfOut);
                }

                swfOut.flush();
                swfOut.close();

                ThreadLocalToolkit.log(new OutputMessage(s.outputName, Long.toString(file.length())));
            }
        }
        catch (ConfigurationException ex)
        {
            Mxmlc.processConfigurationException(ex, "mxmlc");
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
            if (Trace.error)
            {
                thr.printStackTrace();
            }
        }
        finally
        {
            if (benchmark != null)
            {
                if ((ThreadLocalToolkit.errorCount() == 0) &&
                    benchmark.hasStarted(Benchmark.POSTCOMPILE))
                {
                    benchmark.stopTime(Benchmark.POSTCOMPILE, false);
                }
                benchmark.totalTime();
                benchmark.peakMemoryUsage(true);
            }

            CompilerAPI.removePathResolver();

            if (swfOut != null) try { swfOut.close(); } catch (IOException ioe) {}
        }
    }

    private static void compc(String[] args, int id)
    {
        LocalizationManager l10n = ThreadLocalToolkit.getLocalizationManager();
        Benchmark benchmark = null;

        SwcTarget s = new SwcTarget();
        s.id = id;

        try
        {
            // setup the path resolver
            CompilerAPI.usePathResolver();

            // process configuration
            ConfigurationBuffer cfgbuf = new ConfigurationBuffer(CompcConfiguration.class, CompcConfiguration.getAliases());
            cfgbuf.setDefaultVar("include-classes");
            DefaultsConfigurator.loadCompcDefaults( cfgbuf );
            CompcConfiguration configuration = (CompcConfiguration) Mxmlc.processConfiguration(
                ThreadLocalToolkit.getLocalizationManager(), "compc", args, cfgbuf, CompcConfiguration.class, "include-classes");
            s.configuration = configuration;

            CompilerAPI.setupHeadless(configuration);

            if (configuration.benchmark())
            {
                benchmark = CompilerAPI.runBenchmark();
                benchmark.startTime(Benchmark.PRECOMPILE);
            }
            else
            {
                CompilerAPI.disableBenchmark();
            }

            targets.put("" + id, s);
            s.args = args;

            String[] sourceMimeTypes = WebTierAPI.getSourcePathMimeTypes();

            CompilerConfiguration compilerConfig = configuration.getCompilerConfiguration();

            // create a SourcePath...
            s.sourcePath = new SourcePath(sourceMimeTypes, compilerConfig.allowSourcePathOverlap());
            s.sourcePath.addPathElements( compilerConfig.getSourcePath() );

            List<VirtualFile>[] array
                = CompilerAPI.getVirtualFileList(configuration.getIncludeSources(), configuration.getStylesheets().values(),
                                                        new HashSet<String>(Arrays.asList(sourceMimeTypes)), s.sourcePath.getPaths());

            NameMappings mappings = CompilerAPI.getNameMappings(configuration);

            // note: if Configuration is ever shared with other parts of the system, then this part will need
            // to change, since we're setting a compc-specific setting below
            compilerConfig.setMetadataExport(true);

            //    get standard bundle of compilers, transcoders
            Transcoder[] transcoders = WebTierAPI.getTranscoders( configuration );
            SubCompiler[] compilers = WebTierAPI.getCompilers(compilerConfig, mappings, transcoders);

            // construct the SWC file name...
            s.outputName = FileUtil.getCanonicalPath(FileUtil.openFile(configuration.getOutput()));

            // create a FileSpec...
            s.fileSpec = new FileSpec(array[0], WebTierAPI.getFileSpecMimeTypes(), false);

            // create a SourceList...
            s.sourceList = new SourceList(array[1], compilerConfig.getSourcePath(), null,
                                                   WebTierAPI.getSourceListMimeTypes(), false);

            // create a ResourceContainer...
            s.resources = new ResourceContainer();

            // create a ResourceBundlePath...
            s.bundlePath = new ResourceBundlePath(configuration.getCompilerConfiguration(), null);

            if (benchmark != null)
            {
                benchmark.benchmark(l10n.getLocalizedTextString(new InitialSetup()));
            }

            // load SWCs
            s.swcCache = new SwcCache();

            CompilerSwcContext swcContext = new CompilerSwcContext(true);
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

               s.checksum = cfgbuf.checksum_ts() + swcContext.checksum();

            final SymbolTable symbolTable = new SymbolTable(configuration);
            s.perCompileData = symbolTable.perCompileData;

            Map licenseMap = configuration.getLicensesConfiguration().getLicenseMap();

            s.classes = new HashMap<String, Source>();
            s.nsComponents = SwcAPI.setupNamespaceComponents(configuration, mappings, s.sourcePath, s.sourceList, s.classes);
            SwcAPI.setupClasses(configuration, s.sourcePath, s.sourceList, s.classes);

            Map<String, VirtualFile> rbFiles = new HashMap<String, VirtualFile>();

            if (benchmark != null)
            {
                benchmark.stopTime(Benchmark.PRECOMPILE, false);
            }

            List<CompilationUnit> units = CompilerAPI.compile(s.fileSpec, s.sourceList, s.classes.values(), s.sourcePath,
                                                              s.resources, s.bundlePath, swcContext, symbolTable,
                                                              mappings, configuration, compilers,
                                                              new CompcPreLink(rbFiles, configuration.getIncludeResourceBundles(),
                                                            		  false),
                                                              licenseMap, new ArrayList<Source>());

            if (benchmark != null)
            {
                benchmark.startTime(Benchmark.POSTCOMPILE);
            }

            s.units = units;
            s.rbFiles = rbFiles;
            s.sourcePath.clearCache();
            s.bundlePath.clearCache();
            s.resources.refresh();

            // export SWC
            SwcAPI.exportSwc( configuration, units, s.nsComponents, s.swcCache, s.rbFiles );

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
            Mxmlc.processConfigurationException(ex, "compc");
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
            if (Trace.error)
            {
                t.printStackTrace();
            }
        }
        finally
        {
            if (benchmark != null)
            {
                if ((ThreadLocalToolkit.errorCount() == 0) &&
                    benchmark.hasStarted(Benchmark.POSTCOMPILE))
                {
                    benchmark.stopTime(Benchmark.POSTCOMPILE, false);
                }
                benchmark.totalTime();
                benchmark.peakMemoryUsage(true);
            }

            CompilerAPI.removePathResolver();
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
