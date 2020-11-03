package dev.blachut.svelte.lang.codeInsight

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.CompletionResultSink
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink
import com.intellij.lang.javascript.psi.resolve.SinkResolveProcessor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import dev.blachut.svelte.lang.isSvelteComponentTag
import dev.blachut.svelte.lang.psi.SvelteHtmlAttribute
import dev.blachut.svelte.lang.psi.SvelteJSLazyElementTypes

object DirectiveSupport {
    init {
        addDirectives(
            DirectiveTypeDefinition(
                name = null,
                target = DirectiveTarget.BOTH,
                shorthandReferenceFactory = ::ShorthandLocalReference, // won't do anything with current parser
                shorthandCompletionFactory = ::localScopeCompletionsProvider,  // won't do anything with current parser
            ),
            DirectiveTypeDefinition(
                name = "bind",
                target = DirectiveTarget.BOTH,
                shorthandReferenceFactory = ::ShorthandLocalReference,
                shorthandCompletionFactory = ::localScopeCompletionsProvider,
                valueElementType = SvelteJSLazyElementTypes.ATTRIBUTE_EXPRESSION // TODO only variable references
            ),
            DirectiveTypeDefinition(
                name = "on",
                target = DirectiveTarget.BOTH,
                modifiers = setOf("preventDefault"), // TODO etc
            ),
            DirectiveTypeDefinition(
                name = "class",
                target = DirectiveTarget.ELEMENT,
                shorthandReferenceFactory = ::ShorthandLocalReference,
                shorthandCompletionFactory = ::localScopeCompletionsProvider,
            ),
            DirectiveTypeDefinition(
                name = "use",
                target = DirectiveTarget.ELEMENT,
                nestedSpecifiers = 1,
                shorthandReferenceFactory = ::ShorthandLocalReference,
                shorthandCompletionFactory = ::localScopeCompletionsProvider,
            ),
            DirectiveTypeDefinition(
                name = "transition",
                target = DirectiveTarget.ELEMENT,
                modifiers = setOf("local"),
                shorthandReferenceFactory = ::ShorthandLocalReference,
                shorthandCompletionFactory = ::localScopeCompletionsProvider,
            ),
            DirectiveTypeDefinition(
                name = "in",
                target = DirectiveTarget.ELEMENT,
                modifiers = setOf("local"),
                shorthandReferenceFactory = ::ShorthandLocalReference,
                shorthandCompletionFactory = ::localScopeCompletionsProvider,
            ),
            DirectiveTypeDefinition(
                name = "out",
                target = DirectiveTarget.ELEMENT,
                modifiers = setOf("local"),
                shorthandReferenceFactory = ::ShorthandLocalReference,
                shorthandCompletionFactory = ::localScopeCompletionsProvider,
            ),
            DirectiveTypeDefinition(
                name = "animate",
                target = DirectiveTarget.ELEMENT,
                targetValidator = { true }, // TODO only directly in keyed each
                shorthandReferenceFactory = ::ShorthandLocalReference,
                shorthandCompletionFactory = ::localScopeCompletionsProvider,
            ),
            DirectiveTypeDefinition(
                name = "let",
                target = DirectiveTarget.BOTH,
                targetValidator = { isSvelteComponentTag(it.name) || it.getAttributeValue("slot") != null },
                valueElementType = SvelteJSLazyElementTypes.ATTRIBUTE_PARAMETER
            ),
        )
    }

    fun addDirectives(vararg def: DirectiveTypeDefinition) {

    }

    fun getDefinition(attributeName: String): DirectiveTypeDefinition {
        TODO()
    }

    data class DirectiveTypeDefinition(
        val name: String?,
        val target: DirectiveTarget,
        val targetValidator: (xmlTag: XmlTag) -> Boolean = { true },
        val modifiers: Set<String> = emptySet(),
        val nestedSpecifiers: Number? = null,
        val shorthandReferenceFactory: ((element: SvelteHtmlAttribute, rangeInElement: TextRange) -> PsiReference)? = null,
        val shorthandCompletionFactory: ((attribute: XmlAttribute, result: CompletionResultSet) -> Unit)? = null,
        val longhandReferenceFactory: () -> Unit = {},
        val longhandCompletionFactory: () -> Unit = {},
        val valueElementType: IElementType = SvelteJSLazyElementTypes.ATTRIBUTE_EXPRESSION,
        val uniquenessSelector: Unit = Unit, // TODO
    )

    enum class DirectiveTarget {
        BOTH, ELEMENT, COMPONENT
    }
}

fun localScopeCompletionsProvider(attribute: XmlAttribute, result: CompletionResultSet) {
    val sink = CompletionResultSink(attribute, result.prefixMatcher)
    val processor = SinkResolveProcessor(sink)
    JSReferenceExpressionImpl.doProcessLocalDeclarations(
        attribute,
        null,
        processor,
        false,
        true,
        null
    )

    result.addAllElements(sink.resultsAsObjects)
}

class ShorthandLocalReference(element: SvelteHtmlAttribute, rangeInElement: TextRange) :
    PsiPolyVariantReferenceBase<SvelteHtmlAttribute>(element, rangeInElement, false) {
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val resolver = ResolveCache.PolyVariantResolver<ShorthandLocalReference> { ref, _ ->
            val attribute = ref.element
            val specifier = attribute.localName

            val sink = ResolveResultSink(attribute, specifier, false, incompleteCode)
            val processor = SinkResolveProcessor(specifier, attribute, sink)
            JSResolveUtil.treeWalkUp(processor, attribute, attribute, attribute, attribute.containingFile)

            processor.resultsAsResolveResults
        }

        return JSResolveUtil.resolve(element.containingFile, this, resolver, incompleteCode)
    }
}
