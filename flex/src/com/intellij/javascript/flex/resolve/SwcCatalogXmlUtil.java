// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.parsing.xml.XmlBuilder;
import com.intellij.psi.impl.source.parsing.xml.XmlBuilderDriver;
import com.intellij.util.Consumer;
import com.intellij.util.containers.Interner;
import com.intellij.util.containers.Stack;
import gnu.trove.THashMap;
import gnu.trove.TObjectLongHashMap;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public final class SwcCatalogXmlUtil {

  /**
   * <code><b>Pair.first</b></code> is modification stamp of <i>catalog.xml</i> file when this user data was put<br>
   * <code><b>Pair.second</b></code> is {@code Map<String, TObjectLongHashMap<String>>} where:<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;<code><b>key</b></code> is swf file name inside swc file (so far I have seen only <i>library.swf</i> name, but swc format allows any file name, it is mentioned in <i>catalog.xml</i>)<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;<code><b>value</b></code> is map from FQN (of JSQualifiedElement) to its timestamp as written inside <i>catalog.xml</i>.
   */
  private static final Key<Pair<Long, THashMap<String, TObjectLongHashMap<String>>>> MOD_STAMP_AND_SWF_NAME_TO_QNAME_WITH_TIMESTAMP_MAP =
    Key.create("MOD_STAMP_AND_SWF_NAME_TO_QNAME_WITH_TIMESTAMP_MAP");
  private static final Key<Long> TIMESTAMP_IN_CATALOG_XML = Key.create("TIMESTAMP_IN_CATALOG_XML");

  private static final Key<Pair<Long, ComponentFromCatalogXml[]>> MOD_STAMP_AND_COMPONENTS_FROM_CATALOG_XML =
    Key.create("MOD_STAMP_AND_COMPONENTS_FROM_CATALOG_XML");

  private static final Key<Pair<Long, ComponentFromManifest[]>> MOD_STAMP_AND_COMPONENTS_FROM_MANIFEST =
    Key.create("MOD_STAMP_AND_COMPONENTS_FROM_MANIFEST");

  private SwcCatalogXmlUtil() {
  }

  public static final class ComponentFromCatalogXml {
    public final @NotNull String myName;
    public final @NotNull String myClassFqn;
    public final @NotNull String myUri;
    public final @Nullable String myIcon;

    private ComponentFromCatalogXml(final @NotNull String name,
                                    final @NotNull String classFqn,
                                    final @NotNull String uri,
                                    final @Nullable String icon) {
      myName = name;
      myClassFqn = classFqn;
      myUri = uri.intern(); // memory optimization
      myIcon = icon;
    }
  }

  public static final class ComponentFromManifest {
    public final @NotNull String myComponentName;
    public final @NotNull String myClassFqn;

    private ComponentFromManifest(final @NotNull String componentName, final @NotNull String classFqn) {
      myComponentName = componentName;
      myClassFqn = classFqn;
    }
  }

  public static class XmlBuilderAdapter implements XmlBuilder {
    private final Stack<String> myLocation = new Stack<>();

    public XmlBuilderAdapter() {
      myLocation.push("");
    }

    public String getLocation() {
      return myLocation.peek();
    }

    @Override
    public void doctype(@Nullable final CharSequence publicId, @Nullable final CharSequence systemId, final int start, final int end) {
    }

    @Override
    public ProcessingOrder startTag(final CharSequence localName,
                                    final String namespace,
                                    final int start,
                                    final int end,
                                    final int headerEnd) {
      myLocation.push(myLocation.peek() + "." + localName);
      return ProcessingOrder.TAGS_AND_ATTRIBUTES;
    }

    @Override
    public void endTag(final CharSequence localName, final String namespace, final int start, final int end) {
      myLocation.pop();
    }

    @Override
    public void attribute(final CharSequence name, final CharSequence value, final int start, final int end) {
    }

    @Override
    public void textElement(final CharSequence display, final CharSequence physical, final int start, final int end) {
    }

    @Override
    public void entityRef(final CharSequence ref, final int start, final int end) {
    }

    @Override
    public void error(@NotNull final String message, final int start, final int end) {
    }
  }

  public static long getTimestampFromCatalogXml(final @NotNull PsiElement psiElement) {
    final Long cachedTimestamp = psiElement.getUserData(TIMESTAMP_IN_CATALOG_XML);
    if (cachedTimestamp != null) {
      return cachedTimestamp;
    }

    if (!(psiElement instanceof JSQualifiedNamedElement)) {
      return -1;
    }

    final String qName = ((JSQualifiedNamedElement)psiElement).getQualifiedName();
    if (StringUtil.isEmpty(qName)) {
      return -1;
    }

    final PsiFile psiFile = psiElement.getContainingFile();
    if (JavaScriptIndex.ECMASCRIPT_JS2.equals(psiFile.getName())) return Integer.MIN_VALUE;

    final VirtualFile swfFile = psiFile.getVirtualFile();
    final VirtualFile dir = swfFile != null && "swf".equalsIgnoreCase(swfFile.getExtension()) ? swfFile.getParent() : null;
    final VirtualFile catalogFile = dir == null ? null : dir.findChild("catalog.xml");

    if (catalogFile == null) {
      return -1;
    }

    Pair<Long, THashMap<String, TObjectLongHashMap<String>>> modStampAndSwfNameToQnameWithTimestampMap =
      catalogFile.getUserData(MOD_STAMP_AND_SWF_NAME_TO_QNAME_WITH_TIMESTAMP_MAP);

    if (modStampAndSwfNameToQnameWithTimestampMap == null
        || modStampAndSwfNameToQnameWithTimestampMap.first != catalogFile.getModificationStamp()) {
      final THashMap<String, TObjectLongHashMap<String>> swfNameToQnameWithTimestampMap = parseTimestampsFromCatalogXml(catalogFile);
      modStampAndSwfNameToQnameWithTimestampMap = Pair.create(catalogFile.getModificationStamp(), swfNameToQnameWithTimestampMap);
      catalogFile.putUserData(MOD_STAMP_AND_SWF_NAME_TO_QNAME_WITH_TIMESTAMP_MAP, modStampAndSwfNameToQnameWithTimestampMap);
    }

    final TObjectLongHashMap<String> qnameWithTimestampMap = modStampAndSwfNameToQnameWithTimestampMap.second.get(swfFile.getName());
    final long timestamp = qnameWithTimestampMap == null ? -1 : qnameWithTimestampMap.get(qName);
    psiElement.putUserData(TIMESTAMP_IN_CATALOG_XML, timestamp);

    return timestamp;
  }

  private static THashMap<String, TObjectLongHashMap<String>> parseTimestampsFromCatalogXml(final @NotNull VirtualFile catalogFile) {
    //  <swc xmlns="http://www.adobe.com/flash/swccatalog/9">
    //    <libraries>
    //      <library path="library.swf">                                                                    take swf name here
    //        <script name="flash/sampler/StackFrame" mod="1256700285949" signatureChecksum="121164004" >   name attribute is not FQN, take only mod here
    //          <def id="flash.sampler:Sample" />                                                           multiple defs possible
    //          <def id="flash.sampler:clearSamples" />
    //          ...

    final THashMap<String, TObjectLongHashMap<String>> swfNameToQnameWithTimestampMap = new THashMap<>(1);

    try {
      final Element rootElement = JDOMUtil.load(catalogFile.getInputStream());
      if (rootElement != null && "swc".equals(rootElement.getName())) {
        for (final Element librariesElement : rootElement.getChildren("libraries", rootElement.getNamespace())) {
          for (final Element libraryElement : librariesElement.getChildren("library", librariesElement.getNamespace())) {
            final String swfName = libraryElement.getAttributeValue("path");
            if (StringUtil.isEmpty(swfName)) {
              continue;
            }

            final TObjectLongHashMap<String> qNameWithTimestampMap = new TObjectLongHashMap<>();
            swfNameToQnameWithTimestampMap.put(swfName, qNameWithTimestampMap);

            for (final Element scriptElement : libraryElement.getChildren("script", libraryElement.getNamespace())) {
              final String mod = scriptElement.getAttributeValue("mod");
              if (StringUtil.isEmpty(mod)) {
                continue;
              }

              try {
                final long timestamp = Long.parseLong(mod);

                for (final Element defElement : scriptElement.getChildren("def", scriptElement.getNamespace())) {
                  final String id = defElement.getAttributeValue("id");
                  if (!StringUtil.isEmpty(id)) {
                    final String fqn = id.replace(':', '.');
                    qNameWithTimestampMap.put(fqn, timestamp);
                  }
                }
              }
              catch (NumberFormatException ignored) {/*ignore*/}
            }
          }
        }
      }
    }
    catch (JDOMException ignored) {/*ignore*/}
    catch (IOException ignored) {/*ignore*/}

    return swfNameToQnameWithTimestampMap;
  }

  public static void processComponentsFromCatalogXml(final VirtualFile catalogFile, final Consumer<? super ComponentFromCatalogXml> consumer) {
    Pair<Long, ComponentFromCatalogXml[]> modStampAndComponents = catalogFile.getUserData(MOD_STAMP_AND_COMPONENTS_FROM_CATALOG_XML);

    if (modStampAndComponents == null || modStampAndComponents.first != catalogFile.getModificationStamp()) {
      final ComponentFromCatalogXml[] componentsFromCatalogXml = parseComponentsFromCatalogXml(catalogFile);
      modStampAndComponents = Pair.create(catalogFile.getModificationStamp(), componentsFromCatalogXml);
      catalogFile.putUserData(MOD_STAMP_AND_COMPONENTS_FROM_CATALOG_XML, modStampAndComponents);
    }

    for (final ComponentFromCatalogXml componentFromCatalogXml : modStampAndComponents.second) {
      consumer.consume(componentFromCatalogXml);
    }
  }

  private static ComponentFromCatalogXml[] parseComponentsFromCatalogXml(final VirtualFile catalogFile) {
    final Collection<ComponentFromCatalogXml> result = new ArrayList<>();

    final XmlBuilder xmlBuilder = new XmlBuilderAdapter() {
      private static final String COMPONENT_LOCATION = ".swc.components.component";
      private static final String NAME = "name";
      private static final String CLASS_NAME = "className";
      private static final String URI = "uri";
      private static final String ICON = "icon";

      private String myNameAttr = null;
      private String myClassNameAttr = null;
      private String myUriAttr = null;
      private String myIconAttr = null;

      @Override
      public void attribute(CharSequence name, CharSequence value, int start, int end) {
        if (COMPONENT_LOCATION.equals(getLocation())) {
          if (NAME.contentEquals(name)) {
            myNameAttr = value.toString().trim();
          }
          else if (CLASS_NAME.contentEquals(name)) {
            myClassNameAttr = value.toString().trim();
          }
          else if (URI.contentEquals(name)) {
            myUriAttr = value.toString().trim();
          }
          else if (ICON.contentEquals(name)) {
            myIconAttr = value.toString().trim();
          }
        }
      }

      private final Interner<String> myStringInterner = Interner.createStringInterner();

      @Override
      public void endTag(CharSequence localName, String namespace, int start, int end) {
        if (COMPONENT_LOCATION.equals(getLocation())) {
          if (StringUtil.isNotEmpty(myNameAttr) && StringUtil.isNotEmpty(myClassNameAttr) && StringUtil.isNotEmpty(myUriAttr)) {
            result.add(
              new ComponentFromCatalogXml(
                new String(myNameAttr),
                new String(myClassNameAttr.replace(":", ".")),
                myStringInterner.intern(new String(myUriAttr)),
                myIconAttr != null ? new String(myIconAttr):null
              )
            );
          }
          myNameAttr = null;
          myClassNameAttr = null;
          myUriAttr = null;
          myIconAttr = null;
        }

        super.endTag(localName, namespace, start, end);
      }
    };

    try {
      new XmlBuilderDriver(VfsUtilCore.loadText(catalogFile)).build(xmlBuilder);
    }
    catch (IOException ignored) {/*ignore*/}


    return result.toArray(new ComponentFromCatalogXml[0]);
  }

  public static void processManifestFile(final VirtualFile manifestFile, final Consumer<? super ComponentFromManifest> consumer) {
    Pair<Long, ComponentFromManifest[]> modStampAndComponents = manifestFile.getUserData(MOD_STAMP_AND_COMPONENTS_FROM_MANIFEST);

    if (modStampAndComponents == null || modStampAndComponents.first != manifestFile.getModificationStamp()) {
      final ComponentFromManifest[] componentsFromManifests = parseManifestFile(manifestFile);
      modStampAndComponents = Pair.create(manifestFile.getModificationStamp(), componentsFromManifests);
      manifestFile.putUserData(MOD_STAMP_AND_COMPONENTS_FROM_MANIFEST, modStampAndComponents);
    }

    for (final ComponentFromManifest componentFromManifest : modStampAndComponents.second) {
      consumer.consume(componentFromManifest);
    }
  }

  private static ComponentFromManifest[] parseManifestFile(final VirtualFile manifestFile) {
    final Collection<ComponentFromManifest> result = new ArrayList<>();

    final XmlBuilder builder = new XmlBuilderAdapter() {
      private static final String COMPONENT = "component";
      private static final String ID = "id";
      private static final String CLASS = "class";

      private String idAttr = null;
      private String classAttr = null;

      @Override
      public void attribute(final CharSequence name, final CharSequence value, final int start, final int end) {
        if (ID.equals(name.toString())) {
          idAttr = value.toString().trim();
        }
        else if (CLASS.equals(name.toString())) {
          classAttr = value.toString().trim();
        }
      }

      @Override
      public void endTag(final CharSequence localName, final String namespace, final int start, final int end) {
        if (COMPONENT.contentEquals(localName)) {
          if (StringUtil.isNotEmpty(classAttr)) {
            final String classFqn = classAttr.replace(":", ".");
            final String componentName = idAttr != null ? idAttr : classAttr.substring(classFqn.lastIndexOf('.') + 1);
            result.add(new ComponentFromManifest(new String(componentName), new String(classFqn)));
          }
        }

        idAttr = null;
        classAttr = null;

        super.endTag(localName, namespace, start, end);
      }
    };

    try {
      new XmlBuilderDriver(VfsUtilCore.loadText(manifestFile)).build(builder);
    }
    catch (IOException ignored) {/*ignore*/}

    return result.toArray(new ComponentFromManifest[0]);
  }
}
