// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package dev.blachut.svelte.lang.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import dev.blachut.svelte.lang.codeInsight.DirectiveSupport
import dev.blachut.svelte.lang.icons.SvelteIcons

class SvelteAttributeNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val xmlTag = PsiTreeUtil.getParentOfType(parameters.position, XmlTag::class.java, false)
        val attribute = parameters.position.parent as? XmlAttribute
        if (xmlTag == null || attribute == null) return

        if (xmlTag.name == "script" && xmlTag.getAttribute("lang") == null) {
            result.addElement(createLookupElement("lang=\"ts\"", 100))
        }

        if (xmlTag.name == "script" && xmlTag.getAttribute("context") == null) {
            result.addElement(createLookupElement("context=\"module\"", 90))
        }

        // TODO refactor into proper descriptors
        if (xmlTag.name == "style" && xmlTag.getAttribute("global") == null) {
            result.addElement(createLookupElement("global"))
        }

        if (xmlTag.name == "style" && xmlTag.getAttribute("src") == null) {
            result.addElement(createLookupElement("src"))
        }

        val definition = DirectiveSupport.getDefinition(StringUtil.trimEnd(attribute.name,
            CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED))
        definition.shorthandCompletionFactory?.invoke(attribute, result)
    }

    private fun createLookupElement(text: String, priority: Int? = null): LookupElement {
        return LookupElementBuilder
            .create(text)
            .withIcon(SvelteIcons.GRAY)
            .let {
                if (priority != null) {
                    PrioritizedLookupElement.withPriority(it, priority.toDouble())
                } else {
                    it
                }
            }
    }
}
