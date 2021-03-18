// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import org.angular2.refactoring.extractComponent.Angular2CliComponentGenerator
import org.angular2.refactoring.extractComponent.extractPaths

class Angular2CliComponentGeneratorMockImpl(val project: Project) : Angular2CliComponentGenerator {
  private val requestor = this

  override fun showDialog(): Array<String> {
    return arrayOf("child")
  }

  override fun generateComponent(cliDir: VirtualFile, workingDir: VirtualFile, arguments: Array<String>): () -> List<String> {
    val name = arguments.first()

    val ref = Ref<String>(null)
    ApplicationManager.getApplication().invokeAndWait {
      ApplicationManager.getApplication().runWriteAction {
        val componentDir = workingDir.createChildDirectory(requestor, name)

        componentDir.createChild("$name.component.css")

        componentDir.createChild("$name.component.html", """
        <p>$name works!</p>
        """.trimIndent())

        componentDir.createChild("$name.component.spec.ts")

        componentDir.createChild("$name.component.ts", """
        import { Component, OnInit } from '@angular/core';
  
        @Component({
          selector: 'app-$name',
          templateUrl: './$name.component.html',
          styleUrls: ['./$name.component.css']
        })
        export class ${name.capitalize()}Component implements OnInit {
  
          constructor() { }
  
          ngOnInit(): void {
          }
  
        }
        """.trimIndent())

        // real CLI also edits NgModule

        """
        CREATE src/app/$name/$name.component.css (0 bytes)
        CREATE src/app/$name/$name.component.html (20 bytes)
        CREATE src/app/$name/$name.component.spec.ts (619 bytes)
        CREATE src/app/$name/$name.component.ts (271 bytes)
        UPDATE src/app/app.module.ts (550 bytes)
        """.trimIndent().let(ref::set)
      }
    }

    return { extractPaths(ref.get()) }
  }

  private fun VirtualFile.createChild(name: String, content: String = "") {
    createChildData(requestor, name).setBinaryContent(content.toByteArray())
  }
}