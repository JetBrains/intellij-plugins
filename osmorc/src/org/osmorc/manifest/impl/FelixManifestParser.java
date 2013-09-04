/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.osmorc.manifest.impl;

import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.apache.felix.framework.BundleRevisionImpl;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.capabilityset.SimpleFilter;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.framework.util.VersionRange;
import org.apache.felix.framework.util.manifestparser.ParsedHeaderClause;
import org.apache.felix.framework.util.manifestparser.R4Library;
import org.apache.felix.framework.util.manifestparser.R4LibraryClause;
import org.apache.felix.framework.wiring.BundleCapabilityImpl;
import org.apache.felix.framework.wiring.BundleRequirementImpl;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.*;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;

import java.util.*;
import java.util.Map.Entry;

/**
 * Augmented copy of {@link org.apache.felix.framework.util.manifestparser.ManifestParser}.
 */
@SuppressWarnings("ALL")
class FelixManifestParser
{
    private final Logger m_logger;
    private final Map m_configMap;
    private final Map m_headerMap;
    private volatile int m_activationPolicy = BundleRevisionImpl.EAGER_ACTIVATION;
    private volatile String m_activationIncludeDir;
    private volatile String m_activationExcludeDir;
    private volatile boolean m_isExtension = false;
    private volatile String m_bundleSymbolicName;
    private volatile Version m_bundleVersion;
    private volatile List<BundleCapability> m_capabilities;
    private volatile List<BundleRequirement> m_requirements;
    private volatile List<R4LibraryClause> m_libraryClauses;
    private volatile boolean m_libraryHeadersOptional = false;

    public FelixManifestParser(Logger logger, Map configMap, BundleRevision owner, Map headerMap)
        throws BundleException
    {
        m_logger = logger;
        m_configMap = configMap;
        m_headerMap = headerMap;

        // Verify that only manifest version 2 is specified.
        String manifestVersion = getManifestVersion(m_headerMap);
        if ((manifestVersion != null) && !manifestVersion.equals("2"))
        {
            throw new BundleException(
                "Unknown 'Bundle-ManifestVersion' value: " + manifestVersion);
        }

        // Create lists to hold capabilities and requirements.
        List<BundleCapabilityImpl> capList = new ArrayList();

        //
        // Parse bundle version.
        //

        m_bundleVersion = Version.emptyVersion;
        if (headerMap.get(Constants.BUNDLE_VERSION) != null)
        {
            try
            {
                m_bundleVersion = Version.parseVersion(
                    (String) headerMap.get(Constants.BUNDLE_VERSION));
            }
            catch (RuntimeException ex)
            {
                // R4 bundle versions must parse, R3 bundle version may not.
                if (getManifestVersion().equals("2"))
                {
                    throw ex;
                }
                m_bundleVersion = Version.emptyVersion;
            }
        }

        //
        // Parse bundle symbolic name.
        //

        BundleCapabilityImpl bundleCap = parseBundleSymbolicName(owner, m_headerMap);
        if (bundleCap != null)
        {
            m_bundleSymbolicName = (String)
                bundleCap.getAttributes().get(BundleRevision.BUNDLE_NAMESPACE);

            // Add a bundle capability and a host capability to all
            // non-fragment bundles. A host capability is the same
            // as a require capability, but with a different capability
            // namespace. Bundle capabilities resolve required-bundle
            // dependencies, while host capabilities resolve fragment-host
            // dependencies.
            if (headerMap.get(Constants.FRAGMENT_HOST) == null)
            {
                // All non-fragment bundles have host capabilities.
                capList.add(bundleCap);
                // A non-fragment bundle can choose to not have a host capability.
                String attachment =
                    bundleCap.getDirectives().get(Constants.FRAGMENT_ATTACHMENT_DIRECTIVE);
                attachment = (attachment == null)
                    ? Constants.FRAGMENT_ATTACHMENT_RESOLVETIME
                    : attachment;
                if (!attachment.equalsIgnoreCase(Constants.FRAGMENT_ATTACHMENT_NEVER))
                {
                    Map<String, Object> hostAttrs =
                        new HashMap<String, Object>(bundleCap.getAttributes());
                    Object value = hostAttrs.remove(BundleRevision.BUNDLE_NAMESPACE);
                    hostAttrs.put(BundleRevision.HOST_NAMESPACE, value);
                    capList.add(new BundleCapabilityImpl(
                        owner, BundleRevision.HOST_NAMESPACE,
                        bundleCap.getDirectives(),
                        hostAttrs));
                }
            }
        }

        // Verify that bundle symbolic name is specified.
        if (getManifestVersion().equals("2") && (m_bundleSymbolicName == null))
        {
            throw new BundleException(
                "R4 bundle manifests must include bundle symbolic name.");
        }

        //
        // Parse Fragment-Host.
        //

        List<BundleRequirementImpl> hostReqs = parseFragmentHost(m_logger, owner, m_headerMap);

        //
        // Parse Require-Bundle
        //

        List<ParsedHeaderClause> rbClauses =
            parseStandardHeader((String) headerMap.get(Constants.REQUIRE_BUNDLE));
        rbClauses = normalizeRequireClauses(m_logger, rbClauses, getManifestVersion());
        List<BundleRequirementImpl> rbReqs = convertRequires(rbClauses, owner);

        //
        // Parse Import-Package.
        //

        List<ParsedHeaderClause> importClauses =
            parseStandardHeader((String) headerMap.get(Constants.IMPORT_PACKAGE));
        importClauses = normalizeImportClauses(m_logger, importClauses, getManifestVersion());
        List<BundleRequirement> importReqs = convertImports(importClauses, owner);

        //
        // Parse DynamicImport-Package.
        //

        List<ParsedHeaderClause> dynamicClauses =
            parseStandardHeader((String) headerMap.get(Constants.DYNAMICIMPORT_PACKAGE));
        dynamicClauses = normalizeDynamicImportClauses(m_logger, dynamicClauses, getManifestVersion());
        List<BundleRequirement> dynamicReqs = convertImports(dynamicClauses, owner);

        //
        // Parse Require-Capability.
        //

        List<ParsedHeaderClause> requireClauses =
            parseStandardHeader((String) headerMap.get(Constants.REQUIRE_CAPABILITY));
        importClauses = normalizeRequireCapabilityClauses(
            m_logger, requireClauses, getManifestVersion());
        List<BundleRequirement> requireReqs = convertRequireCapabilities(importClauses, owner);

        //
        // Parse Export-Package.
        //

        List<ParsedHeaderClause> exportClauses =
            parseStandardHeader((String) headerMap.get(Constants.EXPORT_PACKAGE));
        exportClauses = normalizeExportClauses(logger, exportClauses,
            getManifestVersion(), m_bundleSymbolicName, m_bundleVersion);
        List<BundleCapability> exportCaps = convertExports(exportClauses, owner);

        //
        // Parse Provide-Capability.
        //

        List<ParsedHeaderClause> provideClauses =
            parseStandardHeader((String) headerMap.get(Constants.PROVIDE_CAPABILITY));
        exportClauses = normalizeProvideCapabilityClauses(
            logger, provideClauses, getManifestVersion());
        List<BundleCapability> provideCaps = convertProvideCapabilities(provideClauses, owner);

        //
        // Calculate implicit imports.
        //

        if (!getManifestVersion().equals("2"))
        {
            List<ParsedHeaderClause> implicitClauses =
                calculateImplicitImports(exportCaps, importClauses);
            importReqs.addAll(convertImports(implicitClauses, owner));

            List<ParsedHeaderClause> allImportClauses =
                new ArrayList<ParsedHeaderClause>(implicitClauses.size() + importClauses.size());
            allImportClauses.addAll(importClauses);
            allImportClauses.addAll(implicitClauses);

            exportCaps = calculateImplicitUses(exportCaps, allImportClauses);
        }

        // Combine all capabilities.
        m_capabilities = new ArrayList(
             capList.size() + exportCaps.size() + provideCaps.size());
        m_capabilities.addAll(capList);
        m_capabilities.addAll(exportCaps);
        m_capabilities.addAll(provideCaps);

        // Combine all requirements.
        m_requirements = new ArrayList(
            hostReqs.size() + importReqs.size() + rbReqs.size()
            + requireReqs.size() + dynamicReqs.size());
        m_requirements.addAll(hostReqs);
        m_requirements.addAll(importReqs);
        m_requirements.addAll(rbReqs);
        m_requirements.addAll(requireReqs);
        m_requirements.addAll(dynamicReqs);

        //
        // Parse Bundle-NativeCode.
        //

        // Parse native library clauses.
        m_libraryClauses =
            parseLibraryStrings(
                m_logger,
                parseDelimitedString((String) m_headerMap.get(Constants.BUNDLE_NATIVECODE), ","));

        // Check to see if there was an optional native library clause, which is
        // represented by a null library header; if so, record it and remove it.
        if (!m_libraryClauses.isEmpty() &&
            (m_libraryClauses.get(m_libraryClauses.size() - 1).getLibraryEntries() == null))
        {
            m_libraryHeadersOptional = true;
            m_libraryClauses.remove(m_libraryClauses.size() - 1);
        }

        //
        // Parse activation policy.
        //

        // This sets m_activationPolicy, m_includedPolicyClasses, and
        // m_excludedPolicyClasses.
        parseActivationPolicy(headerMap);

        m_isExtension = checkExtensionBundle(headerMap);
    }

    private static List<ParsedHeaderClause> normalizeImportClauses(
        Logger logger, List<ParsedHeaderClause> clauses, String mv)
        throws BundleException
    {
        // Verify that the values are equals if the package specifies
        // both version and specification-version attributes.
        Set dupeSet = new HashSet();
        for (ParsedHeaderClause clause : clauses)
        {
            // Check for "version" and "specification-version" attributes
            // and verify they are the same if both are specified.
            Object v = clause.m_attrs.get(Constants.VERSION_ATTRIBUTE);
            Object sv = clause.m_attrs.get(Constants.PACKAGE_SPECIFICATION_VERSION);
            if ((v != null) && (sv != null))
            {
                // Verify they are equal.
                if (!((String) v).trim().equals(((String) sv).trim()))
                {
                    throw new IllegalArgumentException(
                        "Both version and specification-version are specified, but they are not equal.");
                }
            }

            // Ensure that only the "version" attribute is used and convert
            // it to the VersionRange type.
            if ((v != null) || (sv != null))
            {
                clause.m_attrs.remove(Constants.PACKAGE_SPECIFICATION_VERSION);
                v = (v == null) ? sv : v;
                clause.m_attrs.put(
                    Constants.VERSION_ATTRIBUTE,
                    VersionRange.parse(v.toString()));
            }

            // If bundle version is specified, then convert its type to VersionRange.
            v = clause.m_attrs.get(Constants.BUNDLE_VERSION_ATTRIBUTE);
            if (v != null)
            {
                clause.m_attrs.put(
                    Constants.BUNDLE_VERSION_ATTRIBUTE,
                    VersionRange.parse(v.toString()));
            }

            // Verify java.* is not imported, nor any duplicate imports.
            for (String pkgName : clause.m_paths)
            {
                if (!dupeSet.contains(pkgName))
                {
                    // Verify that java.* packages are not imported.
                    if (pkgName.startsWith("java."))
                    {
                        throw new BundleException(
                            "Importing java.* packages not allowed: " + pkgName);
                    }
                    // The character "." has no meaning in the OSGi spec except
                    // when placed on the bundle class path. Some people, however,
                    // mistakenly think it means the default package when imported
                    // or exported. This is not correct. It is invalid.
                    else if (pkgName.equals("."))
                    {
                        throw new BundleException("Imporing '.' is invalid.");
                    }
                    // Make sure a package name was specified.
                    else if (pkgName.length() == 0)
                    {
                        throw new BundleException(
                            "Imported package names cannot be zero length.");
                    }
                    dupeSet.add(pkgName);
                }
                else
                {
                    throw new BundleException("Duplicate import: " + pkgName);
                }
            }

            if (!mv.equals("2"))
            {
                // R3 bundles cannot have directives on their imports.
                if (!clause.m_dirs.isEmpty())
                {
                    throw new BundleException("R3 imports cannot contain directives.");
                }

                // Remove and ignore all attributes other than version.
                // NOTE: This is checking for "version" rather than "specification-version"
                // because the package class normalizes to "version" to avoid having
                // future special cases. This could be changed if more strict behavior
                // is required.
                if (!clause.m_attrs.isEmpty())
                {
                    // R3 package requirements should only have version attributes.
                    Object pkgVersion = clause.m_attrs.get(BundleCapabilityImpl.VERSION_ATTR);
                    pkgVersion = (pkgVersion == null)
                        ? new VersionRange(Version.emptyVersion, true, null, true)
                        : pkgVersion;
                    for (Entry<String, Object> entry : clause.m_attrs.entrySet())
                    {
                        if (!entry.getKey().equals(BundleCapabilityImpl.VERSION_ATTR))
                        {
                            logger.log(Logger.LOG_WARNING,
                                "Unknown R3 import attribute: "
                                    + entry.getKey());
                        }
                    }

                    // Remove all other attributes except package version.
                    clause.m_attrs.clear();
                    clause.m_attrs.put(BundleCapabilityImpl.VERSION_ATTR, pkgVersion);
                }
            }
        }

        return clauses;
    }

    public static List<BundleRequirement> parseDynamicImportHeader(
        Logger logger, BundleRevision owner, String header)
        throws BundleException
    {

        List<ParsedHeaderClause> importClauses = parseStandardHeader(header);
        importClauses = normalizeDynamicImportClauses(logger, importClauses, "2");
        List<BundleRequirement> reqs = convertImports(importClauses, owner);
        return reqs;
    }

    private static List<BundleRequirement> convertImports(
        List<ParsedHeaderClause> clauses, BundleRevision owner)
    {
        // Now convert generic header clauses into requirements.
        List reqList = new ArrayList();
        for (ParsedHeaderClause clause : clauses)
        {
            for (String path : clause.m_paths)
            {
                // Prepend the package name to the array of attributes.
                Map<String, Object> attrs = clause.m_attrs;
                // Note that we use a linked hash map here to ensure the
                // package attribute is first, which will make indexing
                // more efficient.
// TODO: OSGi R4.3 - This is ordering is kind of hacky.
                // Prepend the package name to the array of attributes.
                Map<String, Object> newAttrs = new LinkedHashMap<String, Object>(attrs.size() + 1);
                // We want this first from an indexing perspective.
                newAttrs.put(
                    BundleRevision.PACKAGE_NAMESPACE,
                    path);
                newAttrs.putAll(attrs);
                // But we need to put it again to make sure it wasn't overwritten.
                newAttrs.put(
                    BundleRevision.PACKAGE_NAMESPACE,
                    path);

                // Create filter now so we can inject filter directive.
                SimpleFilter sf = SimpleFilter.convert(newAttrs);

                // Inject filter directive.
// TODO: OSGi R4.3 - Can we insert this on demand somehow?
                Map<String, String> dirs = clause.m_dirs;
                Map<String, String> newDirs = new HashMap<String, String>(dirs.size() + 1);
                newDirs.putAll(dirs);
                newDirs.put(
                    Constants.FILTER_DIRECTIVE,
                    sf.toString());

                // Create package requirement and add to requirement list.
                reqList.add(
                    new BundleRequirementImpl(
                        owner,
                        BundleRevision.PACKAGE_NAMESPACE,
                        newDirs,
                        Collections.EMPTY_MAP,
                        sf));
            }
        }

        return reqList;
    }

    private static List<ParsedHeaderClause> normalizeDynamicImportClauses(
        Logger logger, List<ParsedHeaderClause> clauses, String mv)
        throws BundleException
    {
        // Verify that the values are equals if the package specifies
        // both version and specification-version attributes.
        for (ParsedHeaderClause clause : clauses)
        {
            if (!mv.equals("2"))
            {
                // R3 bundles cannot have directives on their imports.
                if (!clause.m_dirs.isEmpty())
                {
                    throw new BundleException("R3 imports cannot contain directives.");
                }
            }

            // Add the resolution directive to indicate that these are
            // dynamic imports.
            clause.m_dirs.put(Constants.RESOLUTION_DIRECTIVE,
                FelixConstants.RESOLUTION_DYNAMIC);

            // Check for "version" and "specification-version" attributes
            // and verify they are the same if both are specified.
            Object v = clause.m_attrs.get(Constants.VERSION_ATTRIBUTE);
            Object sv = clause.m_attrs.get(Constants.PACKAGE_SPECIFICATION_VERSION);
            if ((v != null) && (sv != null))
            {
                // Verify they are equal.
                if (!((String) v).trim().equals(((String) sv).trim()))
                {
                    throw new IllegalArgumentException(
                        "Both version and specification-version are specified, but they are not equal.");
                }
            }

            // Ensure that only the "version" attribute is used and convert
            // it to the VersionRange type.
            if ((v != null) || (sv != null))
            {
                clause.m_attrs.remove(Constants.PACKAGE_SPECIFICATION_VERSION);
                v = (v == null) ? sv : v;
                clause.m_attrs.put(
                    Constants.VERSION_ATTRIBUTE,
                    VersionRange.parse(v.toString()));
            }

            // If bundle version is specified, then convert its type to VersionRange.
            v = clause.m_attrs.get(Constants.BUNDLE_VERSION_ATTRIBUTE);
            if (v != null)
            {
                clause.m_attrs.put(
                    Constants.BUNDLE_VERSION_ATTRIBUTE,
                    VersionRange.parse(v.toString()));
            }

            // Dynamic imports can have duplicates, so verify that java.*
            // packages are not imported.
            for (String pkgName : clause.m_paths)
            {
                if (pkgName.startsWith("java."))
                {
                    throw new BundleException(
                        "Dynamically importing java.* packages not allowed: " + pkgName);
                }
                else if (!pkgName.equals("*") && pkgName.endsWith("*") && !pkgName.endsWith(".*"))
                {
                    throw new BundleException(
                        "Partial package name wild carding is not allowed: " + pkgName);
                }
            }
        }

        return clauses;
    }

    private static List<ParsedHeaderClause> normalizeRequireCapabilityClauses(
        Logger logger, List<ParsedHeaderClause> clauses, String mv)
        throws BundleException
    {

        if (!mv.equals("2") && !clauses.isEmpty())
        {
            // Should we error here if we are not an R4 bundle?
        }

        return clauses;
    }

    private static List<BundleRequirement> convertRequireCapabilities(
        List<ParsedHeaderClause> clauses, BundleRevision owner)
        throws BundleException
    {
        // Now convert generic header clauses into requirements.
        List reqList = new ArrayList();
        for (ParsedHeaderClause clause : clauses)
        {
            try
            {
                String filterStr = clause.m_dirs.get(Constants.FILTER_DIRECTIVE);
                SimpleFilter sf = (filterStr != null)
                    ? SimpleFilter.parse(filterStr)
                    : new SimpleFilter(null, null, SimpleFilter.MATCH_ALL);
                for (String path : clause.m_paths)
                {
                    if (path.startsWith("osgi.wiring."))
                    {
                        throw new BundleException("Manifest cannot use Require-Capability for '"
                            + path
                            + "' namespace.");
                    }

                    // Create requirement and add to requirement list.
                    reqList.add(
                        new BundleRequirementImpl(
                            owner,
                            path,
                            clause.m_dirs,
                            clause.m_attrs,
                            sf));
                }
            }
            catch (Exception ex)
            {
                throw new BundleException("Error creating requirement: " + ex);
            }
        }

        return reqList;
    }

    private static List<ParsedHeaderClause> normalizeProvideCapabilityClauses(
        Logger logger, List<ParsedHeaderClause> clauses, String mv)
        throws BundleException
    {

        if (!mv.equals("2") && !clauses.isEmpty())
        {
            // Should we error here if we are not an R4 bundle?
        }

        // Convert attributes into specified types.
        for (ParsedHeaderClause clause : clauses)
        {
            for (Entry<String, String> entry : clause.m_types.entrySet())
            {
                String type = entry.getValue();
                if (!type.equals("String"))
                {
                    if (type.equals("Double"))
                    {
                        clause.m_attrs.put(
                            entry.getKey(),
                            new Double(clause.m_attrs.get(entry.getKey()).toString().trim()));
                    }
                    else if (type.equals("Version"))
                    {
                        clause.m_attrs.put(
                            entry.getKey(),
                            new Version(clause.m_attrs.get(entry.getKey()).toString().trim()));
                    }
                    else if (type.equals("Long"))
                    {
                        clause.m_attrs.put(
                            entry.getKey(),
                            new Long(clause.m_attrs.get(entry.getKey()).toString().trim()));
                    }
                    else if (type.startsWith("List"))
                    {
                        int startIdx = type.indexOf('<');
                        int endIdx = type.indexOf('>');
                        if (((startIdx > 0) && (endIdx <= startIdx))
                            || ((startIdx < 0) && (endIdx > 0)))
                        {
                            throw new BundleException(
                                "Invalid Provide-Capability attribute list type for '"
                                + entry.getKey()
                                + "' : "
                                + type);
                        }

                        String listType = "String";
                        if (endIdx > startIdx)
                        {
                            listType = type.substring(startIdx + 1, endIdx).trim();
                        }

                        List<String> tokens = parseDelimitedString(
                            clause.m_attrs.get(entry.getKey()).toString(), ",", false);
                        List<Object> values = new ArrayList<Object>(tokens.size());
                        for (String token : tokens)
                        {
                            if (listType.equals("String"))
                            {
                                values.add(token);
                            }
                            else if (listType.equals("Double"))
                            {
                                values.add(new Double(token.trim()));
                            }
                            else if (listType.equals("Version"))
                            {
                                values.add(new Version(token.trim()));
                            }
                            else if (listType.equals("Long"))
                            {
                                values.add(new Long(token.trim()));
                            }
                            else
                            {
                                throw new BundleException(
                                    "Unknown Provide-Capability attribute list type for '"
                                    + entry.getKey()
                                    + "' : "
                                    + type);
                            }
                        }
                        clause.m_attrs.put(
                            entry.getKey(),
                            values);
                    }
                    else
                    {
                        throw new BundleException(
                            "Unknown Provide-Capability attribute type for '"
                            + entry.getKey()
                            + "' : "
                            + type);
                    }
                }
            }
        }

        return clauses;
    }

    private static List<BundleCapability> convertProvideCapabilities(
        List<ParsedHeaderClause> clauses, BundleRevision owner)
        throws BundleException
    {
        List<BundleCapability> capList = new ArrayList();
        for (ParsedHeaderClause clause : clauses)
        {
            for (String path : clause.m_paths)
            {
                if (path.startsWith("osgi.wiring."))
                {
                    throw new BundleException("Manifest cannot use Provide-Capability for '"
                        + path
                        + "' namespace.");
                }

                // Create package capability and add to capability list.
                capList.add(
                    new BundleCapabilityImpl(
                        owner,
                        path,
                        clause.m_dirs,
                        clause.m_attrs));
            }
        }

        return capList;
    }

    private static List<ParsedHeaderClause> normalizeExportClauses(
        Logger logger, List<ParsedHeaderClause> clauses,
        String mv, String bsn, Version bv)
        throws BundleException
    {
        // Verify that "java.*" packages are not exported.
        for (ParsedHeaderClause clause : clauses)
        {
            // Verify that the named package has not already been declared.
            for (String pkgName : clause.m_paths)
            {
                // Verify that java.* packages are not exported.
                if (pkgName.startsWith("java."))
                {
                    throw new BundleException(
                        "Exporting java.* packages not allowed: "
                        + pkgName);
                }
                // The character "." has no meaning in the OSGi spec except
                // when placed on the bundle class path. Some people, however,
                // mistakenly think it means the default package when imported
                // or exported. This is not correct. It is invalid.
                else if (pkgName.equals("."))
                {
                    throw new BundleException("Exporing '.' is invalid.");
                }
                // Make sure a package name was specified.
                else if (pkgName.length() == 0)
                {
                    throw new BundleException(
                        "Exported package names cannot be zero length.");
                }
            }

            // Check for "version" and "specification-version" attributes
            // and verify they are the same if both are specified.
            Object v = clause.m_attrs.get(Constants.VERSION_ATTRIBUTE);
            Object sv = clause.m_attrs.get(Constants.PACKAGE_SPECIFICATION_VERSION);
            if ((v != null) && (sv != null))
            {
                // Verify they are equal.
                if (!((String) v).trim().equals(((String) sv).trim()))
                {
                    throw new IllegalArgumentException(
                        "Both version and specification-version are specified, but they are not equal.");
                }
            }

            // Always add the default version if not specified.
            if ((v == null) && (sv == null))
            {
                v = Version.emptyVersion;
            }

            // Ensure that only the "version" attribute is used and convert
            // it to the appropriate type.
            if ((v != null) || (sv != null))
            {
                // Convert version attribute to type Version.
                clause.m_attrs.remove(Constants.PACKAGE_SPECIFICATION_VERSION);
                v = (v == null) ? sv : v;
                clause.m_attrs.put(
                    Constants.VERSION_ATTRIBUTE,
                    Version.parseVersion(v.toString()));
            }

            // If this is an R4 bundle, then make sure it doesn't specify
            // bundle symbolic name or bundle version attributes.
            if (mv.equals("2"))
            {
                // Find symbolic name and version attribute, if present.
                if (clause.m_attrs.containsKey(Constants.BUNDLE_VERSION_ATTRIBUTE)
                    || clause.m_attrs.containsKey(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE))
                {
                    throw new BundleException(
                        "Exports must not specify bundle symbolic name or bundle version.");
                }

                // Now that we know that there are no bundle symbolic name and version
                // attributes, add them since the spec says they are there implicitly.
                clause.m_attrs.put(Constants.BUNDLE_SYMBOLICNAME_ATTRIBUTE, bsn);
                clause.m_attrs.put(Constants.BUNDLE_VERSION_ATTRIBUTE, bv);
            }
            else if (!mv.equals("2"))
            {
                // R3 bundles cannot have directives on their exports.
                if (!clause.m_dirs.isEmpty())
                {
                    throw new BundleException("R3 exports cannot contain directives.");
                }

                // Remove and ignore all attributes other than version.
                // NOTE: This is checking for "version" rather than "specification-version"
                // because the package class normalizes to "version" to avoid having
                // future special cases. This could be changed if more strict behavior
                // is required.
                if (!clause.m_attrs.isEmpty())
                {
                    // R3 package capabilities should only have a version attribute.
                    Object pkgVersion = clause.m_attrs.get(BundleCapabilityImpl.VERSION_ATTR);
                    pkgVersion = (pkgVersion == null)
                        ? Version.emptyVersion
                        : pkgVersion;
                    for (Entry<String, Object> entry : clause.m_attrs.entrySet())
                    {
                        if (!entry.getKey().equals(BundleCapabilityImpl.VERSION_ATTR))
                        {
                            logger.log(
                                Logger.LOG_WARNING,
                                "Unknown R3 export attribute: "
                                + entry.getKey());
                        }
                    }

                    // Remove all other attributes except package version.
                    clause.m_attrs.clear();
                    clause.m_attrs.put(BundleCapabilityImpl.VERSION_ATTR, pkgVersion);
                }
            }
        }

        return clauses;
    }

    private static List<BundleCapability> convertExports(
        List<ParsedHeaderClause> clauses, BundleRevision owner)
    {
        List<BundleCapability> capList = new ArrayList();
        for (ParsedHeaderClause clause : clauses)
        {
            for (String pkgName : clause.m_paths)
            {
                // Prepend the package name to the array of attributes.
                Map<String, Object> attrs = clause.m_attrs;
                Map<String, Object> newAttrs = new HashMap<String, Object>(attrs.size() + 1);
                newAttrs.putAll(attrs);
                newAttrs.put(
                    BundleRevision.PACKAGE_NAMESPACE,
                    pkgName);

                // Create package capability and add to capability list.
                capList.add(
                    new BundleCapabilityImpl(
                        owner,
                        BundleRevision.PACKAGE_NAMESPACE,
                        clause.m_dirs,
                        newAttrs));
            }
        }

        return capList;
    }

    public String getManifestVersion()
    {
        String manifestVersion = getManifestVersion(m_headerMap);
        return (manifestVersion == null) ? "1" : manifestVersion;
    }

    private static String getManifestVersion(Map headerMap)
    {
        String manifestVersion = (String) headerMap.get(Constants.BUNDLE_MANIFESTVERSION);
        return (manifestVersion == null) ? null : manifestVersion.trim();
    }

    public int getActivationPolicy()
    {
        return m_activationPolicy;
    }

    public String getActivationIncludeDirective()
    {
        return m_activationIncludeDir;
    }

    public String getActivationExcludeDirective()
    {
        return m_activationExcludeDir;
    }

    public boolean isExtension()
    {
        return m_isExtension;
    }

    public String getSymbolicName()
    {
        return m_bundleSymbolicName;
    }

    public Version getBundleVersion()
    {
        return m_bundleVersion;
    }

    public List<BundleCapability> getCapabilities()
    {
        return m_capabilities;
    }

    public List<BundleRequirement> getRequirements()
    {
        return m_requirements;
    }

    public List<R4LibraryClause> getLibraryClauses()
    {
        return m_libraryClauses;
    }

    /**
     * <p>
     * This method returns the selected native library metadata from
     * the manifest. The information is not the raw metadata from the
     * manifest, but is the native library clause selected according
     * to the OSGi native library clause selection policy. The metadata
     * returned by this method will be attached directly to a module and
     * used for finding its native libraries at run time. To inspect the
     * raw native library metadata refer to <tt>getLibraryClauses()</tt>.
     * </p>
     * <p>
     * This method returns one of three values:
     * </p>
     * <ul>
     * <li><tt>null</tt> - if the are no native libraries for this module;
     *     this may also indicate the native libraries are optional and
     *     did not match the current platform.</li>
     * <li>Zero-length <tt>R4Library</tt> array - if no matching native library
     *     clause was found; this bundle should not resolve.</li>
     * <li>Nonzero-length <tt>R4Library</tt> array - the native libraries
     *     associated with the matching native library clause.</li>
     * </ul>
     *
     * @return <tt>null</tt> if there are no native libraries, a zero-length
     *         array if no libraries matched, or an array of selected libraries.
    **/
    public List<R4Library> getLibraries()
    {
        ArrayList<R4Library> libs = null;
        try
        {
            R4LibraryClause clause = getSelectedLibraryClause();
            if (clause != null)
            {
                String[] entries = clause.getLibraryEntries();
                libs = new ArrayList<R4Library>(entries.length);
                int current = 0;
                for (int i = 0; i < entries.length; i++)
                {
                    String name = getName(entries[i]);
                    boolean found = false;
                    for (int j = 0; !found && (j < current); j++)
                    {
                        found = getName(entries[j]).equals(name);
                    }
                    if (!found)
                    {
                        libs.add(new R4Library(
                            clause.getLibraryEntries()[i],
                            clause.getOSNames(), clause.getProcessors(), clause.getOSVersions(),
                            clause.getLanguages(), clause.getSelectionFilter()));
                    }
                }
                libs.trimToSize();
            }
        }
        catch (Exception ex)
        {
            libs = new ArrayList<R4Library>(0);
        }
        return libs;
    }

    private String getName(String path)
    {
        int idx = path.lastIndexOf('/');
        if (idx > -1)
        {
            return path.substring(idx);
        }
        return path;
    }

    private R4LibraryClause getSelectedLibraryClause() throws BundleException
    {
        if ((m_libraryClauses != null) && (m_libraryClauses.size() > 0))
        {
            List clauseList = new ArrayList();

            // Search for matching native clauses.
            for (R4LibraryClause libraryClause : m_libraryClauses)
            {
                if (libraryClause.match(m_configMap))
                {
                    clauseList.add(libraryClause);
                }
            }

            // Select the matching native clause.
            int selected = 0;
            if (clauseList.isEmpty())
            {
                // If optional clause exists, no error thrown.
                if (m_libraryHeadersOptional)
                {
                    return null;
                }
                else
                {
                    throw new BundleException("Unable to select a native library clause.");
                }
            }
            else if (clauseList.size() == 1)
            {
                selected = 0;
            }
            else if (clauseList.size() > 1)
            {
                selected = firstSortedClause(clauseList);
            }
            return ((R4LibraryClause) clauseList.get(selected));
        }

        return null;
    }

    private int firstSortedClause(List<R4LibraryClause> clauseList)
    {
        ArrayList indexList = new ArrayList();
        ArrayList selection = new ArrayList();

        // Init index list
        for (int i = 0; i < clauseList.size(); i++)
        {
            indexList.add("" + i);
        }

        // Select clause with 'osversion' range declared
        // and get back the max floor of 'osversion' ranges.
        Version osVersionRangeMaxFloor = new Version(0, 0, 0);
        for (int i = 0; i < indexList.size(); i++)
        {
            int index = Integer.parseInt(indexList.get(i).toString());
            String[] osversions = ((R4LibraryClause) clauseList.get(index)).getOSVersions();
            if (osversions != null)
            {
                selection.add("" + indexList.get(i));
            }
            for (int k = 0; (osversions != null) && (k < osversions.length); k++)
            {
                VersionRange range = VersionRange.parse(osversions[k]);
                if ((range.getFloor()).compareTo(osVersionRangeMaxFloor) >= 0)
                {
                    osVersionRangeMaxFloor = range.getFloor();
                }
            }
        }

        if (selection.size() == 1)
        {
            return Integer.parseInt(selection.get(0).toString());
        }
        else if (selection.size() > 1)
        {
            // Keep only selected clauses with an 'osversion'
            // equal to the max floor of 'osversion' ranges.
            indexList = selection;
            selection = new ArrayList();
            for (int i = 0; i < indexList.size(); i++)
            {
                int index = Integer.parseInt(indexList.get(i).toString());
                String[] osversions = ((R4LibraryClause) clauseList.get(index)).getOSVersions();
                for (int k = 0; k < osversions.length; k++)
                {
                    VersionRange range = VersionRange.parse(osversions[k]);
                    if ((range.getFloor()).compareTo(osVersionRangeMaxFloor) >= 0)
                    {
                        selection.add("" + indexList.get(i));
                    }
                }
            }
        }

        if (selection.isEmpty())
        {
            // Re-init index list.
            selection.clear();
            indexList.clear();
            for (int i = 0; i < clauseList.size(); i++)
            {
                indexList.add("" + i);
            }
        }
        else if (selection.size() == 1)
        {
            return Integer.parseInt(selection.get(0).toString());
        }
        else
        {
            indexList = selection;
            selection.clear();
        }

        // Keep only clauses with 'language' declared.
        for (int i = 0; i < indexList.size(); i++)
        {
            int index = Integer.parseInt(indexList.get(i).toString());
            if (((R4LibraryClause) clauseList.get(index)).getLanguages() != null)
            {
                selection.add("" + indexList.get(i));
            }
        }

        // Return the first sorted clause
        if (selection.isEmpty())
        {
            return 0;
        }
        else
        {
            return Integer.parseInt(selection.get(0).toString());
        }
    }

    private static List<ParsedHeaderClause> calculateImplicitImports(
        List<BundleCapability> exports, List<ParsedHeaderClause> imports)
        throws BundleException
    {
        List<ParsedHeaderClause> clauseList = new ArrayList();

        // Since all R3 exports imply an import, add a corresponding
        // requirement for each existing export capability. Do not
        // duplicate imports.
        Map map =  new HashMap();
        // Add existing imports.
        for (int impIdx = 0; impIdx < imports.size(); impIdx++)
        {
            for (int pathIdx = 0; pathIdx < imports.get(impIdx).m_paths.size(); pathIdx++)
            {
                map.put(
                    imports.get(impIdx).m_paths.get(pathIdx),
                    imports.get(impIdx).m_paths.get(pathIdx));
            }
        }
        // Add import requirement for each export capability.
        for (int i = 0; i < exports.size(); i++)
        {
            if (map.get(exports.get(i).getAttributes()
                .get(BundleRevision.PACKAGE_NAMESPACE)) == null)
            {
                // Convert Version to VersionRange.
                Map<String, Object> attrs = new HashMap<String, Object>();
                Object version = exports.get(i).getAttributes().get(Constants.VERSION_ATTRIBUTE);
                if (version != null)
                {
                    attrs.put(
                        Constants.VERSION_ATTRIBUTE,
                        VersionRange.parse(version.toString()));
                }

                List<String> paths = new ArrayList();
                paths.add((String)
                    exports.get(i).getAttributes().get(BundleRevision.PACKAGE_NAMESPACE));
                clauseList.add(
                    new ParsedHeaderClause(
                        paths, Collections.EMPTY_MAP, attrs, Collections.EMPTY_MAP));
            }
        }

        return clauseList;
    }

    private static List<BundleCapability> calculateImplicitUses(
        List<BundleCapability> exports, List<ParsedHeaderClause> imports)
        throws BundleException
    {
        // Add a "uses" directive onto each export of R3 bundles
        // that references every other import (which will include
        // exports, since export implies import); this is
        // necessary since R3 bundles assumed a single class space,
        // but R4 allows for multiple class spaces.
        String usesValue = "";
        for (int i = 0; i < imports.size(); i++)
        {
            for (int pathIdx = 0; pathIdx < imports.get(i).m_paths.size(); pathIdx++)
            {
                usesValue = usesValue
                    + ((usesValue.length() > 0) ? "," : "")
                    + imports.get(i).m_paths.get(pathIdx);
            }
        }
        for (int i = 0; i < exports.size(); i++)
        {
            Map<String, String> dirs = new HashMap<String, String>(1);
            dirs.put(Constants.USES_DIRECTIVE, usesValue);
            exports.set(i, new BundleCapabilityImpl(
                exports.get(i).getRevision(),
                BundleRevision.PACKAGE_NAMESPACE,
                dirs,
                exports.get(i).getAttributes()));
        }

        return exports;
    }

    private static boolean checkExtensionBundle(Map headerMap) throws BundleException
    {
        Object extension = parseExtensionBundleHeader(
            (String) headerMap.get(Constants.FRAGMENT_HOST));

        if (extension != null)
        {
            if (!(Constants.EXTENSION_FRAMEWORK.equals(extension) ||
                Constants.EXTENSION_BOOTCLASSPATH.equals(extension)))
            {
                throw new BundleException(
                    "Extension bundle must have either 'extension:=framework' or 'extension:=bootclasspath'");
            }
            if (headerMap.containsKey(Constants.IMPORT_PACKAGE) ||
                headerMap.containsKey(Constants.REQUIRE_BUNDLE) ||
                headerMap.containsKey(Constants.BUNDLE_NATIVECODE) ||
                headerMap.containsKey(Constants.DYNAMICIMPORT_PACKAGE) ||
                headerMap.containsKey(Constants.BUNDLE_ACTIVATOR))
            {
                throw new BundleException("Invalid extension bundle manifest");
            }
            return true;
        }
        return false;
    }

    private static BundleCapabilityImpl parseBundleSymbolicName(
        BundleRevision owner, Map headerMap)
        throws BundleException
    {
        List<ParsedHeaderClause> clauses = parseStandardHeader(
            (String) headerMap.get(Constants.BUNDLE_SYMBOLICNAME));
        if (clauses.size() > 0)
        {
            if (clauses.size() > 1)
            {
                throw new BundleException(
                    "Cannot have multiple symbolic names: "
                        + headerMap.get(Constants.BUNDLE_SYMBOLICNAME));
            }
            else if (clauses.get(0).m_paths.size() > 1)
            {
                throw new BundleException(
                    "Cannot have multiple symbolic names: "
                        + headerMap.get(Constants.BUNDLE_SYMBOLICNAME));
            }

            // Get bundle version.
            Version bundleVersion = Version.emptyVersion;
            if (headerMap.get(Constants.BUNDLE_VERSION) != null)
            {
                try
                {
                    bundleVersion = Version.parseVersion(
                        (String) headerMap.get(Constants.BUNDLE_VERSION));
                }
                catch (RuntimeException ex)
                {
                    // R4 bundle versions must parse, R3 bundle version may not.
                    String mv = getManifestVersion(headerMap);
                    if (mv != null)
                    {
                        throw ex;
                    }
                    bundleVersion = Version.emptyVersion;
                }
            }

            // Create a require capability and return it.
            String symName = (String) clauses.get(0).m_paths.get(0);
            clauses.get(0).m_attrs.put(BundleRevision.BUNDLE_NAMESPACE, symName);
            clauses.get(0).m_attrs.put(Constants.BUNDLE_VERSION_ATTRIBUTE, bundleVersion);
            return new BundleCapabilityImpl(
                owner,
                BundleRevision.BUNDLE_NAMESPACE,
                clauses.get(0).m_dirs,
                clauses.get(0).m_attrs);
        }

        return null;
    }

    private static List<BundleRequirementImpl> parseFragmentHost(
        Logger logger, BundleRevision owner, Map headerMap)
        throws BundleException
    {
        List<BundleRequirementImpl> reqs = new ArrayList();

        String mv = getManifestVersion(headerMap);
        if ((mv != null) && mv.equals("2"))
        {
            List<ParsedHeaderClause> clauses = parseStandardHeader(
                (String) headerMap.get(Constants.FRAGMENT_HOST));
            if (clauses.size() > 0)
            {
                // Make sure that only one fragment host symbolic name is specified.
                if (clauses.size() > 1)
                {
                    throw new BundleException(
                        "Fragments cannot have multiple hosts: "
                            + headerMap.get(Constants.FRAGMENT_HOST));
                }
                else if (clauses.get(0).m_paths.size() > 1)
                {
                    throw new BundleException(
                        "Fragments cannot have multiple hosts: "
                            + headerMap.get(Constants.FRAGMENT_HOST));
                }

                // If the bundle-version attribute is specified, then convert
                // it to the proper type.
                Object value = clauses.get(0).m_attrs.get(Constants.BUNDLE_VERSION_ATTRIBUTE);
                value = (value == null) ? "0.0.0" : value;
                if (value != null)
                {
                    clauses.get(0).m_attrs.put(
                        Constants.BUNDLE_VERSION_ATTRIBUTE,
                        VersionRange.parse(value.toString()));
                }

                // Note that we use a linked hash map here to ensure the
                // host symbolic name is first, which will make indexing
                // more efficient.
// TODO: OSGi R4.3 - This is ordering is kind of hacky.
                // Prepend the host symbolic name to the map of attributes.
                Map<String, Object> attrs = clauses.get(0).m_attrs;
                Map<String, Object> newAttrs = new LinkedHashMap<String, Object>(attrs.size() + 1);
                // We want this first from an indexing perspective.
                newAttrs.put(
                    BundleRevision.HOST_NAMESPACE,
                    clauses.get(0).m_paths.get(0));
                newAttrs.putAll(attrs);
                // But we need to put it again to make sure it wasn't overwritten.
                newAttrs.put(
                    BundleRevision.HOST_NAMESPACE,
                    clauses.get(0).m_paths.get(0));

                // Create filter now so we can inject filter directive.
                SimpleFilter sf = SimpleFilter.convert(newAttrs);

                // Inject filter directive.
// TODO: OSGi R4.3 - Can we insert this on demand somehow?
                Map<String, String> dirs = clauses.get(0).m_dirs;
                Map<String, String> newDirs = new HashMap<String, String>(dirs.size() + 1);
                newDirs.putAll(dirs);
                newDirs.put(
                    Constants.FILTER_DIRECTIVE,
                    sf.toString());

                reqs.add(new BundleRequirementImpl(
                    owner, BundleRevision.HOST_NAMESPACE,
                    newDirs,
                    newAttrs));
            }
        }
        else if (headerMap.get(Constants.FRAGMENT_HOST) != null)
        {
            String s = (String) headerMap.get(Constants.BUNDLE_SYMBOLICNAME);
            s = (s == null) ? (String) headerMap.get(Constants.BUNDLE_NAME) : s;
            s = (s == null) ? headerMap.toString() : s;
            logger.log(
                Logger.LOG_WARNING,
                "Only R4 bundles can be fragments: " + s);
        }

        return reqs;
    }

    public static List<BundleCapability> parseExportHeader(
        Logger logger, BundleRevision owner, String header, String bsn, Version bv)
    {

        List<BundleCapability> caps = null;
        try
        {
            List<ParsedHeaderClause> exportClauses = parseStandardHeader(header);
            exportClauses = normalizeExportClauses(logger, exportClauses, "2", bsn, bv);
            caps = convertExports(exportClauses, owner);
        }
        catch (BundleException ex)
        {
            caps = null;
        }
        return caps;
    }

    private static List<ParsedHeaderClause> normalizeRequireClauses(
        Logger logger, List<ParsedHeaderClause> clauses, String mv)
    {
        // R3 bundles cannot require other bundles.
        if (!mv.equals("2"))
        {
            clauses.clear();
        }
        else
        {
            // Convert bundle version attribute to VersionRange type.
            for (ParsedHeaderClause clause : clauses)
            {
                Object value = clause.m_attrs.get(Constants.BUNDLE_VERSION_ATTRIBUTE);
                if (value != null)
                {
                    clause.m_attrs.put(
                        Constants.BUNDLE_VERSION_ATTRIBUTE,
                        VersionRange.parse(value.toString()));
                }
            }
        }

        return clauses;
    }

    private static List<BundleRequirementImpl> convertRequires(
        List<ParsedHeaderClause> clauses, BundleRevision owner)
    {
        List<BundleRequirementImpl> reqList = new ArrayList();
        for (ParsedHeaderClause clause : clauses)
        {
            for (String path : clause.m_paths)
            {
                // Prepend the bundle symbolic name to the array of attributes.
                Map<String, Object> attrs = clause.m_attrs;
                // Note that we use a linked hash map here to ensure the
                // symbolic name attribute is first, which will make indexing
                // more efficient.
// TODO: OSGi R4.3 - This is ordering is kind of hacky.
                // Prepend the symbolic name to the array of attributes.
                Map<String, Object> newAttrs = new LinkedHashMap<String, Object>(attrs.size() + 1);
                // We want this first from an indexing perspective.
                newAttrs.put(
                    BundleRevision.BUNDLE_NAMESPACE,
                    path);
                newAttrs.putAll(attrs);
                // But we need to put it again to make sure it wasn't overwritten.
                newAttrs.put(
                    BundleRevision.BUNDLE_NAMESPACE,
                    path);

                // Create filter now so we can inject filter directive.
                SimpleFilter sf = SimpleFilter.convert(newAttrs);

                // Inject filter directive.
// TODO: OSGi R4.3 - Can we insert this on demand somehow?
                Map<String, String> dirs = clause.m_dirs;
                Map<String, String> newDirs = new HashMap<String, String>(dirs.size() + 1);
                newDirs.putAll(dirs);
                newDirs.put(
                    Constants.FILTER_DIRECTIVE,
                    sf.toString());

                // Create package requirement and add to requirement list.
                reqList.add(
                    new BundleRequirementImpl(
                        owner,
                        BundleRevision.BUNDLE_NAMESPACE,
                        newDirs,
                        newAttrs));
            }
        }

        return reqList;
    }

    public static String parseExtensionBundleHeader(String header)
        throws BundleException
    {
        List<ParsedHeaderClause> clauses = parseStandardHeader(header);

        String result = null;

        if (clauses.size() == 1)
        {
            // See if there is the "extension" directive.
            for (Entry<String, String> entry : clauses.get(0).m_dirs.entrySet())
            {
                if (Constants.EXTENSION_DIRECTIVE.equals(entry.getKey()))
                {
                    // If the extension directive is specified, make sure
                    // the target is the system bundle.
                    if (FelixConstants.SYSTEM_BUNDLE_SYMBOLICNAME.equals(clauses.get(0).m_paths.get(0)) ||
                        Constants.SYSTEM_BUNDLE_SYMBOLICNAME.equals(clauses.get(0).m_paths.get(0)))
                    {
                        return entry.getValue();
                    }
                    else
                    {
                        throw new BundleException(
                            "Only the system bundle can have extension bundles.");
                    }
                }
            }
        }

        return result;
    }

    private void parseActivationPolicy(Map headerMap)
    {
        m_activationPolicy = BundleRevisionImpl.EAGER_ACTIVATION;

        List<ParsedHeaderClause> clauses = parseStandardHeader(
            (String) headerMap.get(Constants.BUNDLE_ACTIVATIONPOLICY));

        if (clauses.size() > 0)
        {
            // Just look for a "path" matching the lazy policy, ignore
            // everything else.
            for (String path : clauses.get(0).m_paths)
            {
                if (path.equals(Constants.ACTIVATION_LAZY))
                {
                    m_activationPolicy = BundleRevisionImpl.LAZY_ACTIVATION;
                    for (Entry<String, String> entry : clauses.get(0).m_dirs.entrySet())
                    {
                        if (entry.getKey().equalsIgnoreCase(Constants.INCLUDE_DIRECTIVE))
                        {
                            m_activationIncludeDir = entry.getValue();
                        }
                        else if (entry.getKey().equalsIgnoreCase(Constants.EXCLUDE_DIRECTIVE))
                        {
                            m_activationExcludeDir = entry.getValue();
                        }
                    }
                    break;
                }
            }
        }
    }

    // Like this: path; path; dir1:=dirval1; dir2:=dirval2; attr1=attrval1; attr2=attrval2,
    //            path; path; dir1:=dirval1; dir2:=dirval2; attr1=attrval1; attr2=attrval2
    public static void main(String[] headers)
    {
        String header = headers[0];
        if (header != null)
        {
            if (header.length() == 0)
            {
                throw new IllegalArgumentException(
                    "A header cannot be an empty string.");
            }
            List<ParsedHeaderClause> clauses = parseStandardHeader(header);

            for (ParsedHeaderClause clause : clauses)
            {
                System.out.println("PATHS " + clause.m_paths);
                System.out.println("    DIRS  " + clause.m_dirs);
                System.out.println("    ATTRS " + clause.m_attrs);
                System.out.println("    TYPES " + clause.m_types);
            }

        }
    }

    private static final char EOF = (char) -1;

    private static char charAt(int pos, String headers, int length)
    {
        if (pos >= length)
        {
            return EOF;
        }
        return headers.charAt(pos);
    }

    private static final int CLAUSE_START = 0;
    private static final int PARAMETER_START = 1;
    private static final int KEY = 2;
    private static final int DIRECTIVE_OR_TYPEDATTRIBUTE = 4;
    private static final int ARGUMENT = 8;
    private static final int VALUE = 16;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static List<ParsedHeaderClause> parseStandardHeader(String header)
    {
        List<ParsedHeaderClause> clauses = new ArrayList<ParsedHeaderClause>();
        if (header == null)
        {
            return clauses;
        }
        ParsedHeaderClause clause = null;
        String key = null;
        Map targetMap = null;
        int state = CLAUSE_START;
        int currentPosition = 0;
        int startPosition = 0;
        int length = header.length();
        boolean quoted = false;
        boolean escaped = false;

        char currentChar = EOF;
        do
        {
            currentChar = charAt(currentPosition, header, length);
            switch (state)
            {
                case CLAUSE_START:
                    clause = new ParsedHeaderClause(
                            new ArrayList<String>(),
                            new HashMap<String, String>(),
                            new HashMap<String, Object>(),
                            new HashMap<String, String>());
                    clauses.add(clause);
                    state = PARAMETER_START;
                case PARAMETER_START:
                    startPosition = currentPosition;
                    state = KEY;
                case KEY:
                    switch (currentChar)
                    {
                        case ':':
                        case '=':
                            key = header.substring(startPosition, currentPosition).trim();
                            startPosition = currentPosition + 1;
                            targetMap = clause.m_attrs;
                            state = currentChar == ':' ? DIRECTIVE_OR_TYPEDATTRIBUTE : ARGUMENT;
                            break;
                        case EOF:
                        case ',':
                        case ';':
                            clause.m_paths.add(header.substring(startPosition, currentPosition).trim());
                            state = currentChar == ',' ? CLAUSE_START : PARAMETER_START;
                            break;
                        default:
                            break;
                    }
                    currentPosition++;
                    break;
                case DIRECTIVE_OR_TYPEDATTRIBUTE:
                    switch(currentChar)
                    {
                        case '=':
                            if (startPosition != currentPosition)
                            {
                                clause.m_types.put(key, header.substring(startPosition, currentPosition).trim());
                            }
                            else
                            {
                                targetMap = clause.m_dirs;
                            }
                            state = ARGUMENT;
                            startPosition = currentPosition + 1;
                            break;
                        default:
                            break;
                    }
                    currentPosition++;
                    break;
                case ARGUMENT:
                    if (currentChar == '\"')
                    {
                        quoted = true;
                        currentPosition++;
                    }
                    else
                    {
                        quoted = false;
                    }
                    if (!Character.isWhitespace(currentChar)) {
                    	state = VALUE;
                    }
                    else {
                    	currentPosition++;
                    }
                    break;
                case VALUE:
                    if (escaped)
                    {
                        escaped = false;
                    }
                    else
                    {
                        if (currentChar == '\\' )
                        {
                            escaped = true;
                        }
                        else if (quoted && currentChar == '\"')
                        {
                            quoted = false;
                        }
                        else if (!quoted)
                        {
                            String value = null;
                            switch(currentChar)
                            {
                                case EOF:
                                case ';':
                                case ',':
                                    value = header.substring(startPosition, currentPosition).trim();
                                    if (value.startsWith("\"") && value.endsWith("\""))
                                    {
                                        value = value.substring(1, value.length() - 1);
                                    }
                                    if (targetMap.put(key, value) != null)
                                    {
                                        throw new IllegalArgumentException(
                                                "Duplicate '" + key + "' in: " + header);
                                    }
                                    state = currentChar == ';' ? PARAMETER_START : CLAUSE_START;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    currentPosition++;
                    break;
                default:
                    break;
            }
        } while ( currentChar != EOF);

        if (state > PARAMETER_START)
        {
            throw new IllegalArgumentException("Unable to parse header: " + header);
        }
        return clauses;
    }

    public static List<String> parseDelimitedString(String value, String delim)
    {
        return parseDelimitedString(value, delim, true);
    }

    /**
     * Parses delimited string and returns an array containing the tokens. This
     * parser obeys quotes, so the delimiter character will be ignored if it is
     * inside of a quote. This method assumes that the quote character is not
     * included in the set of delimiter characters.
     * @param value the delimited string to parse.
     * @param delim the characters delimiting the tokens.
     * @return a list of string or an empty list if there are none.
    **/
    public static List<String> parseDelimitedString(String value, String delim, boolean trim)
    {
        if (value == null)
        {
           value = "";
        }

        List<String> list = new ArrayList();

        int CHAR = 1;
        int DELIMITER = 2;
        int STARTQUOTE = 4;
        int ENDQUOTE = 8;

        StringBuffer sb = new StringBuffer();

        int expecting = (CHAR | DELIMITER | STARTQUOTE);

        boolean isEscaped = false;
        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);

            boolean isDelimiter = (delim.indexOf(c) >= 0);

            if (!isEscaped && (c == '\\'))
            {
                isEscaped = true;
                continue;
            }

            if (isEscaped)
            {
                sb.append(c);
            }
            else if (isDelimiter && ((expecting & DELIMITER) > 0))
            {
                if (trim)
                {
                    list.add(sb.toString().trim());
                }
                else
                {
                    list.add(sb.toString());
                }
                sb.delete(0, sb.length());
                expecting = (CHAR | DELIMITER | STARTQUOTE);
            }
            else if ((c == '"') && ((expecting & STARTQUOTE) > 0))
            {
                sb.append(c);
                expecting = CHAR | ENDQUOTE;
            }
            else if ((c == '"') && ((expecting & ENDQUOTE) > 0))
            {
                sb.append(c);
                expecting = (CHAR | STARTQUOTE | DELIMITER);
            }
            else if ((expecting & CHAR) > 0)
            {
                sb.append(c);
            }
            else
            {
                throw new IllegalArgumentException("Invalid delimited string: " + value);
            }

            isEscaped = false;
        }

        if (sb.length() > 0)
        {
            if (trim)
            {
                list.add(sb.toString().trim());
            }
            else
            {
                list.add(sb.toString());
            }
        }

        return list;
    }

    /**
     * Parses native code manifest headers.
     * @param libStrs an array of native library manifest header
     *        strings from the bundle manifest.
     * @return an array of <tt>LibraryInfo</tt> objects for the
     *         passed in strings.
    **/
    private static List<R4LibraryClause> parseLibraryStrings(
        Logger logger, List<String> libStrs)
        throws IllegalArgumentException
    {
        if (libStrs == null)
        {
            return new ArrayList<R4LibraryClause>(0);
        }

        List<R4LibraryClause> libList = new ArrayList(libStrs.size());

        for (int i = 0; i < libStrs.size(); i++)
        {
            R4LibraryClause clause = R4LibraryClause.parse(logger, libStrs.get(i));
            libList.add(clause);
        }

        return libList;
    }

  // added stuff

  private static final Logger NULL_LOGGER = new Logger() {
    @Override
    protected void doLog(Bundle bundle, ServiceReference sr, int level, String msg, Throwable throwable) { }
  };

  @Nullable
  public static List<BundleCapability> parseExportHeader(String header, String bsn, String bv) {
    try {
      List<ParsedHeaderClause> exportClauses = parseStandardHeader(header);
      exportClauses = normalizeExportClauses(NULL_LOGGER, exportClauses, "2", bsn, new Version(bv));
      return convertExports(exportClauses, null);
    }
    catch (Exception e) {
      return null;
    }
  }

  @Nullable
  public static List<BundleRequirement> parseImportHeader(String header) {
    try {
      List<ParsedHeaderClause> importClauses = parseStandardHeader(header);
      importClauses = normalizeDynamicImportClauses(NULL_LOGGER, importClauses, "2");
      return convertImports(importClauses, null);
    }
    catch (Exception e) {
      return null;
    }
  }

  @Nullable
  public static BundleCapability constructBundleCapability(String symbolicName, String version) {
    try {
      Map<String, String> headers = ContainerUtil.newHashMap();
      headers.put(Constants.BUNDLE_SYMBOLICNAME, symbolicName);
      headers.put(Constants.BUNDLE_VERSION, version);
      return parseBundleSymbolicName(null, headers);
    }
    catch (Exception e) {
      return null;
    }
  }

  @Nullable
  public static List<BundleRequirement> parseRequireBundleHeader(String header) {
    try {
      List<ParsedHeaderClause> rbClauses = parseStandardHeader(header);
      rbClauses = normalizeRequireClauses(NULL_LOGGER, rbClauses, "2");
      return ContainerUtil.map(convertRequires(rbClauses, null), Function.ID);
    }
    catch (Exception e) {
      return null;
    }
  }
}