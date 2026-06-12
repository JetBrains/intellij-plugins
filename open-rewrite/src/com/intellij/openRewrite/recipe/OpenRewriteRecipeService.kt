package com.intellij.openRewrite.recipe

import com.intellij.buildsystem.model.unified.UnifiedCoordinates
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.jarRepository.JarRepositoryManager
import com.intellij.jarRepository.RemoteRepositoryDescription
import com.intellij.java.library.JavaLibraryModificationTracker
import com.intellij.java.library.JavaLibraryUtil
import com.intellij.lang.jvm.JvmModifier
import com.intellij.navigation.ItemPresentation
import com.intellij.openRewrite.OPTION_CLASS_NAME
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.OpenRewriteIcons
import com.intellij.openRewrite.OpenRewriteRecipeLibraryContributor
import com.intellij.openRewrite.RECIPE_CLASS_NAME
import com.intellij.openRewrite.RECIPE_STYLE_SUFFIX
import com.intellij.openRewrite.RECIPE_TYPE_SUFFIX
import com.intellij.openRewrite.REWRITE_TYPE_REGEX
import com.intellij.openRewrite.STYLE_CLASS_NAME
import com.intellij.openRewrite.YAML_KEY_NAME
import com.intellij.openRewrite.YAML_KEY_TYPE
import com.intellij.openRewrite.isRecipe
import com.intellij.openRewrite.run.isRecipe
import com.intellij.openRewrite.run.isStyle
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.BinaryFileTypeDecompilers
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.RootsChangeRescanningInfo
import com.intellij.openapi.roots.JavaSyntheticLibrary
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnchor
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiArrayInitializerMemberValue
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassOwner
import com.intellij.psi.PsiCompiledFile
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.PsiReturnStatement
import com.intellij.psi.SmartTypePointerManager
import com.intellij.psi.impl.JavaConstantExpressionEvaluator
import com.intellij.psi.impl.compiled.ClsClassImpl
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PackageScope
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.FilteredQuery
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.containers.ConcurrentFactoryMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.concurrency.asDeferred
import org.jetbrains.idea.maven.utils.library.RepositoryLibraryDescription
import org.jetbrains.idea.maven.utils.library.RepositoryLibraryProperties
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import javax.swing.Icon
import kotlin.coroutines.EmptyCoroutineContext

internal const val OPEN_REWRITE_GROUP_ID = "org.openrewrite"

private const val META_INF = "META-INF"
private const val REWRITE_DIRECTORY_NAME = "rewrite"
private const val OPEN_REWRITE_CORE_MAVEN_ID = "org.openrewrite:rewrite-core"
private const val OPEN_REWRITE_VERSION: String = "8.16.0"
private const val OPEN_REWRITE_BOM = "rewrite-bom"

internal open class OpenRewriteRecipeService(private val project: Project, private val coroutineScope: CoroutineScope) : Disposable {
  companion object {
    @JvmStatic
    fun getInstance(project: Project): OpenRewriteRecipeService = project.service()
  }

  @Volatile
  private var library: SyntheticLibrary? = null
  @Volatile
  private var recipeArtifactCoordinates: List<String> = emptyList()

  @Volatile
  private var requireLoad: Boolean = true

  override fun dispose() {
  }

  @RequiresReadLock
  @RequiresBackgroundThread
  fun getDescriptors(psiFile: PsiFile?, type: OpenRewriteType): Collection<OpenRewriteRecipeDescriptor> {
    val descriptors = getLibraryDescriptors(type)
    val fileDescriptors = psiFile?.let { getCachedLocalDescriptors(it, type) } ?: emptyMap()
    return if (fileDescriptors.isEmpty()) descriptors.values else fileDescriptors.values + descriptors.values
  }

  @RequiresReadLock
  @RequiresBackgroundThread
  fun findDescriptor(name: String, psiFile: PsiFile?, type: OpenRewriteType): OpenRewriteRecipeDescriptor? {
    if (psiFile != null) {
      val localDescriptor = getCachedLocalDescriptors(psiFile, type)[name]
      if (localDescriptor != null) {
        return localDescriptor
      }
    }
    return getLibraryDescriptors(type)[name]
  }

  @RequiresReadLock
  @RequiresBackgroundThread
  fun getLocalDescriptors(psiFile: PsiFile, type: OpenRewriteType): Collection<OpenRewriteRecipeDescriptor> {
    return getCachedLocalDescriptors(psiFile, type).values
  }

  private fun getLibraryDescriptors(type: OpenRewriteType): Map<String, OpenRewriteRecipeDescriptor> {
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      val dumbService = DumbService.getInstance(project)
      if (dumbService.isDumb) {
        return@getCachedValue CachedValueProvider.Result.create(emptyMap(), dumbService.modificationTracker)
      }

      reload()
      CachedValueProvider.Result.create<Map<OpenRewriteType, Map<String, OpenRewriteRecipeDescriptor>>>(
        ConcurrentFactoryMap.createMap { parseDescriptors(it) },
        JavaLibraryModificationTracker.getInstance(project))
    }[type] ?: emptyMap()
  }

  private fun getCachedLocalDescriptors(psiFile: PsiFile, type: OpenRewriteType): Map<String, OpenRewriteRecipeDescriptor> {
    if (isRecipe(psiFile)) {
      return CachedValuesManager.getManager(project).getCachedValue(psiFile) {
        CachedValueProvider.Result.create<Map<OpenRewriteType, Map<String, OpenRewriteRecipeDescriptor>>>(
          ConcurrentFactoryMap.createMap { getYamlDescriptors(psiFile, it).associateBy { descriptor -> descriptor.name } },
          psiFile)
      }[type] ?: emptyMap()
    }
    if (psiFile is PsiClassOwner) {
      return CachedValuesManager.getManager(project).getCachedValue(psiFile) {
        val dumbService = DumbService.getInstance(project)
        if (dumbService.isDumb) {
          return@getCachedValue CachedValueProvider.Result.create(emptyMap(), dumbService.modificationTracker)
        }

        CachedValueProvider.Result.create<Map<OpenRewriteType, Map<String, OpenRewriteRecipeDescriptor>>>(
          ConcurrentFactoryMap.createMap { getClassDescriptors(psiFile, it).associateBy { descriptor -> descriptor.name } },
          psiFile,
          JavaLibraryModificationTracker.getInstance(project))
      }[type] ?: emptyMap()
    }
    return emptyMap()
  }

  fun getRoots(): Collection<VirtualFile> = library?.allRoots ?: emptyList()

  fun getLibraries(): Collection<SyntheticLibrary> = library?.let { listOf(it) } ?: emptyList()

  protected open fun getRecipeScope(): GlobalSearchScope = ProjectScope.getLibrariesScope(project)

  open fun reload(modalityState: ModalityState? = null): Job? {
    if (requireLoad || getRoots().any { !it.isValid }) {
      coroutineScope.coroutineContext.cancelChildren()
      return coroutineScope.launch(modalityState?.asContextElement() ?: EmptyCoroutineContext) {
        loadLibrary()
      }
    }
    return null
  }

  fun recipeArtifacts(): List<String> = recipeArtifactCoordinates

  private suspend fun loadLibrary() {
    withBackgroundProgress(project, OpenRewriteBundle.message("open.rewrite.recipe.library.load")) {
      val (contributorDependencies, existingContributorDependencies) = getContributorDependencies()
      val dependencies = getCoreDependencies() + contributorDependencies
      if (dependencies.isEmpty()) {
        recipeArtifactCoordinates = existingContributorDependencies.ifEmpty { emptyList() }
        requireLoad = false
        return@withBackgroundProgress
      }

      val classes = ArrayList<VirtualFile>()
      val sources = ArrayList<VirtualFile>()
      for (dependency in dependencies) {
        val properties = RepositoryLibraryProperties(dependency, false)
        val rootsPromise = readAction {
          JarRepositoryManager.loadDependenciesAsync(project, properties, true, false,
                                                     listOf(RemoteRepositoryDescription.MAVEN_CENTRAL), null)
        }
        val roots = rootsPromise.asDeferred().await()
        for (root in roots) {
          if (root.type == OrderRootType.SOURCES) {
            sources.add(root.file)
          }
          else if (root.type == OrderRootType.CLASSES) {
            classes.add(root.file)
          }
        }
      }

      library = OpenRewriteSyntheticLibrary("OpenRewrite", sources, classes, emptySet())
      if (contributorDependencies.isEmpty() && existingContributorDependencies.isEmpty()) {
        recipeArtifactCoordinates = emptyList()
      }
      else {
        recipeArtifactCoordinates = contributorDependencies + existingContributorDependencies
      }
      withContext(Dispatchers.EDT) {
        WriteAction.run<RuntimeException> {
          ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(
            EmptyRunnable.getInstance(), RootsChangeRescanningInfo.RESCAN_DEPENDENCIES_IF_NEEDED)
        }
        DaemonCodeAnalyzer.getInstance(project).restart("OpenRewriteRecipeService.loadLibrary")
        requireLoad = false
      }
    }
  }

  private suspend fun getCoreDependencies(): Collection<String> {
    val hasCore = readAction { JavaLibraryUtil.hasLibraryJar(project, OPEN_REWRITE_CORE_MAVEN_ID) }
    if (hasCore) return emptyList()

    val libraryDescription = RepositoryLibraryDescription.findDescription(OPEN_REWRITE_GROUP_ID, OPEN_REWRITE_BOM)
    val versionPromise = readAction {
      JarRepositoryManager.getAvailableVersions(project, libraryDescription,
                                                listOf(RemoteRepositoryDescription.MAVEN_CENTRAL))
    }
    val version = versionPromise.asDeferred().await().firstOrNull() ?: OPEN_REWRITE_VERSION
    val libraryProperties = RepositoryLibraryProperties("$OPEN_REWRITE_GROUP_ID:$OPEN_REWRITE_BOM:$version", "pom", true)
    val bomsPromise = readAction {
      JarRepositoryManager.loadDependenciesAsync(project, libraryProperties, false, false,
                                                 listOf(RemoteRepositoryDescription.MAVEN_CENTRAL), null)
    }
    val bom = bomsPromise.asDeferred().await().firstOrNull()?.file
    return readAction {
      OpenRewriteLibraryResolver.resolveDependencies(bom, version, project)
    }
  }

  private suspend fun getContributorDependencies(): Pair<List<String>, List<String>> {
    val (dependencies, existingDependencies) = smartReadAction(project) {
      val dependencies = ArrayList<UnifiedCoordinates>()
      val existingDependencies = ArrayList<String>()
      val coordinates = OpenRewriteRecipeLibraryContributor.EP_NAME.extensionList
        .flatMap { it.getRecipeLibraries(project) }
      for (coordinate in coordinates) {
        val mavenId = "${coordinate.groupId}:${coordinate.artifactId}"
        if (JavaLibraryUtil.hasLibraryJar(project, mavenId)) {
          val version = getExternalSystemLibraryVersion(project, mavenId) ?: coordinate.version
          existingDependencies.add("$mavenId:$version")
        }
        else {
          dependencies.add(coordinate)
        }
      }
      dependencies to existingDependencies
    }
    val result = ArrayList<String>()
    for (dependency in dependencies) {
      val libraryDescription = RepositoryLibraryDescription.findDescription(dependency.groupId, dependency.artifactId)
      val versionPromise = readAction {
        JarRepositoryManager.getAvailableVersions(project, libraryDescription,
                                                  listOf(RemoteRepositoryDescription.MAVEN_CENTRAL))
      }
      val version = versionPromise.asDeferred().await().firstOrNull() ?: dependency.version
      result.add("${dependency.groupId}:${dependency.artifactId}:$version")
    }
    return result to existingDependencies
  }

  private fun parseDescriptors(type: OpenRewriteType): Map<String, OpenRewriteRecipeDescriptor> {
    val scope = getRecipeScope()
    ProgressManager.checkCanceled()
    val javaDescriptors = if (type == OpenRewriteType.RECIPE) parseJavaRecipes(scope) else parseJavaStyles(scope)
    ProgressManager.checkCanceled()
    val yamlDescriptors = parseYamlDescriptors(scope, type)
    ProgressManager.checkCanceled()
    return (javaDescriptors + yamlDescriptors).associateBy { it.name }
  }

  private fun parseJavaRecipes(scope: GlobalSearchScope): List<OpenRewriteRecipeDescriptor> {
    val recipeClass = JavaPsiFacade.getInstance(project).findClass(RECIPE_CLASS_NAME, scope) ?: return emptyList()
    val query = FilteredQuery(ClassInheritorsSearch.search(recipeClass, scope, true)) {
      it.hasModifier(JvmModifier.PUBLIC) &&
      !it.hasModifier(JvmModifier.ABSTRACT) &&
      !(it.qualifiedName?.startsWith("org.openrewrite.config") ?: true)
    }
    return query.findAll().mapNotNull { getClassDescriptor(it) }
  }

  private fun parseJavaStyles(scope: GlobalSearchScope): List<OpenRewriteRecipeDescriptor> {
    val styleClass = JavaPsiFacade.getInstance(project).findClass(STYLE_CLASS_NAME, scope) ?: return emptyList()
    val query = FilteredQuery(ClassInheritorsSearch.search(styleClass, scope, true)) {
      it.hasModifier(JvmModifier.PUBLIC) &&
      !it.hasModifier(JvmModifier.ABSTRACT)
    }
    return query.findAll().mapNotNull { getStyleClassDescriptor(it) }
  }

  private fun parseYamlDescriptors(scope: GlobalSearchScope, type: OpenRewriteType): List<OpenRewriteRecipeDescriptor> {
    val rewritePackage = JavaPsiFacade.getInstance(project).findPackage("$META_INF.$REWRITE_DIRECTORY_NAME")
                         ?: return emptyList()
    val packageScope = PackageScope.packageScope(rewritePackage, false)
    val yamlScope = scope.intersectWith(packageScope)
    val recipeFiles = FilenameIndex.getAllFilesByExt(project, "yml", yamlScope)

    val psiManager = PsiManager.getInstance(project)
    val descriptors = ArrayList<OpenRewriteRecipeDescriptor>()
    for (recipeFile in recipeFiles) {
      val psiFile = psiManager.findFile(recipeFile) ?: continue
      descriptors.addAll(getYamlDescriptors(psiFile, type))
    }
    return descriptors
  }


  private fun getClassDescriptor(originalClass: PsiClass): OpenRewriteRecipeDescriptor? {
    val name = originalClass.qualifiedName ?: return null

    val psiClass = getSourcePsiClass(originalClass)
    val displayName = getMethodReturnValue(psiClass, "getDisplayName")
    val description = getMethodReturnValue(psiClass, "getDescription")
    val options = psiClass.fields.mapNotNull { getOptionDescriptor(it) }

    return OpenRewriteRecipeDescriptor(name,
                                       displayName,
                                       description,
                                       false,
                                       options.ifEmpty { emptyList() },
                                       PsiAnchor.create(psiClass))
  }

  private fun getStyleClassDescriptor(originalClass: PsiClass): OpenRewriteRecipeDescriptor? {
    val name = originalClass.qualifiedName ?: return null

    val psiClass = getSourcePsiClass(originalClass)
    val manager = SmartTypePointerManager.getInstance(originalClass.project)
    val options = psiClass.fields.mapNotNull {
      if (it.hasModifier(JvmModifier.STATIC)) return@mapNotNull null
      OpenRewriteOptionDescriptor(it.name,
                                  manager.createSmartTypePointer(it.type),
                                  null,
                                  null,
                                  null,
                                  emptyList(),
                                  false,
                                  PsiAnchor.create(it))
    }

    return OpenRewriteRecipeDescriptor(name,
                                       null,
                                       null,
                                       false,
                                       options.ifEmpty { emptyList() },
                                       PsiAnchor.create(psiClass))
  }

  private fun getSourcePsiClass(originalClass: PsiClass): PsiClass {
    if (originalClass is ClsClassImpl) {
      val sourceClass = originalClass.sourceMirrorClass
      if (sourceClass != null) {
        return sourceClass
      }
    }
    //todo fix IDEA-387057
    val sourceClasses = BinaryFileTypeDecompilers.getInstance()
                          .allowDecompilerSlowOperation { ((originalClass.containingFile as? PsiCompiledFile)?.decompiledPsiFile as? PsiJavaFile)?.classes }
                        ?: return originalClass
    return sourceClasses.find { it.name == originalClass.name } ?: originalClass
  }

  private fun getMethodReturnValue(psiClass: PsiClass, name: String): String? {
    val methods = psiClass.findMethodsByName(name, false)
    for (method in methods) {
      if (method.parameterList.parametersCount != 0) continue

      val body = method.body
      if (body != null && body.statements.size == 1) {
        val first = body.statements[0]
        if (first is PsiReturnStatement) {
          val value = first.returnValue
          val o = JavaConstantExpressionEvaluator.computeConstantExpression(value, false)
          if (o is String) {
            return o
          }
        }
      }
    }
    return null
  }

  private fun getOptionDescriptor(psiField: PsiField): OpenRewriteOptionDescriptor? {
    if (psiField.hasModifier(JvmModifier.STATIC)) return null
    val annotation = psiField.modifierList?.findAnnotation(OPTION_CLASS_NAME) ?: return null

    val initializers = (annotation.findDeclaredAttributeValue("valid") as? PsiArrayInitializerMemberValue)?.initializers
                       ?: PsiAnnotationMemberValue.EMPTY_ARRAY
    val valid = initializers.mapNotNull {
      JavaPsiFacade.getInstance(project).constantEvaluationHelper.computeConstantExpression(it, false) as? String
    }
    val required = psiField.type !is PsiPrimitiveType && (getAttributeValue(annotation, "required") as? Boolean ?: true)

    return OpenRewriteOptionDescriptor(psiField.name,
                                       SmartTypePointerManager.getInstance(psiField.project).createSmartTypePointer(psiField.type),
                                       getStringAttributeValue(annotation, "displayName"),
                                       getStringAttributeValue(annotation, "description"),
                                       getStringAttributeValue(annotation, "example"),
                                       valid.ifEmpty { emptyList() },
                                       required,
                                       PsiAnchor.create(psiField))
  }

  private fun getStringAttributeValue(annotation: PsiAnnotation, name: String): String? {
    return getAttributeValue(annotation, name) as? String
  }

  private fun getAttributeValue(annotation: PsiAnnotation, name: String): Any? {
    val attribute = annotation.findDeclaredAttributeValue(name) ?: return null
    return JavaPsiFacade.getInstance(project).constantEvaluationHelper.computeConstantExpression(attribute, false)
  }

  private fun getYamlDescriptors(psiFile: PsiFile, type: OpenRewriteType): List<OpenRewriteRecipeDescriptor> {
    val yamlFile = psiFile as? YAMLFile ?: return emptyList()
    return yamlFile.documents.mapNotNull { getYamlDescriptor(it, type) }
  }

  private fun getYamlDescriptor(document: YAMLDocument, type: OpenRewriteType): OpenRewriteRecipeDescriptor? {
    val topLevelValue = document.topLevelValue as? YAMLMapping ?: return null
    val typeValue = getYamlValue(topLevelValue, YAML_KEY_TYPE) ?: return null
    if (!REWRITE_TYPE_REGEX.matches(typeValue)) return null
    if ((type == OpenRewriteType.RECIPE && !typeValue.endsWith(RECIPE_TYPE_SUFFIX)) ||
        (type == OpenRewriteType.STYLE && !typeValue.endsWith(RECIPE_STYLE_SUFFIX))) {
      return null
    }
    val name = getYamlValue(topLevelValue, YAML_KEY_NAME) ?: return null
    val displayName = getYamlValue(topLevelValue, "displayName")
    val description = getYamlValue(topLevelValue, "description")
    return OpenRewriteRecipeDescriptor(name, displayName, description, true, emptyList(), PsiAnchor.create(document))
  }

  private fun getYamlValue(yamlMapping: YAMLMapping, key: String): String? {
    return (yamlMapping.getKeyValueByKey(key)?.value as? YAMLScalar)?.textValue
  }

  private fun getClassDescriptors(psiFile: PsiClassOwner, type: OpenRewriteType): List<OpenRewriteRecipeDescriptor> {
    return if (type == OpenRewriteType.RECIPE)
      getClassDescriptors(psiFile, ::isRecipe, ::getClassDescriptor)
    else
      getClassDescriptors(psiFile, ::isStyle, ::getStyleClassDescriptor)
  }

  private fun getClassDescriptors(psiFile: PsiClassOwner, condition: Condition<PsiClass>, parser: (PsiClass) -> OpenRewriteRecipeDescriptor?): List<OpenRewriteRecipeDescriptor> {
    return psiFile.classes.mapNotNull { if (condition.value(it)) parser(it) else null }
  }

  private class OpenRewriteSyntheticLibrary(comparisonId: String,
                                            sourceRoots: List<VirtualFile>,
                                            binaryRoots: List<VirtualFile>,
                                            excludedRoots: Set<VirtualFile>) :
    JavaSyntheticLibrary(comparisonId, sourceRoots, binaryRoots, excludedRoots), ItemPresentation {

    override fun getPresentableText(): String = OpenRewriteBundle.OPEN_REWRITE

    override fun getIcon(unused: Boolean): Icon = OpenRewriteIcons.OpenRewrite
  }
}

private fun getExternalSystemLibraryVersion(project: Project, mavenCoords: String): String? {
  val result = Ref<String>()
  OrderEnumerator.orderEntries(project).recursively()
    .forEachLibrary { library: Library? ->
      val coordinates = JavaLibraryUtil.getMavenCoordinates(library!!)
      if (coordinates != null) {
        val location = coordinates.groupId + ":" + coordinates.artifactId
        if (location == mavenCoords) {
          result.set(coordinates.version)
          return@forEachLibrary false
        }
      }
      true
    }
  return result.get()
}