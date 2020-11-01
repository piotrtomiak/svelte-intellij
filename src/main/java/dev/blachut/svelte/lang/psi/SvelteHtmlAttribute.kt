package dev.blachut.svelte.lang.psi

import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.impl.source.xml.XmlAttributeImpl
import com.intellij.psi.tree.DefaultRoleFinder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.RoleFinder
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.IXmlAttributeElementType
import com.intellij.psi.xml.XmlElement
import dev.blachut.svelte.lang.SvelteHTMLLanguage

class SvelteHtmlAttributeElementType(debugName: String) : IElementType(debugName, SvelteHTMLLanguage.INSTANCE),
    IXmlAttributeElementType

val SVELTE_HTML_ATTRIBUTE = SvelteHtmlAttributeElementType("SVELTE_HTML_ATTRIBUTE")

val SPREAD_OR_SHORTHAND_FINDER: RoleFinder = DefaultRoleFinder(SvelteJSLazyElementTypes.SPREAD_OR_SHORTHAND)

class SvelteHtmlAttribute : XmlAttributeImpl(SVELTE_HTML_ATTRIBUTE) {
    override fun getNameElement(): XmlElement {
        if (firstChild is SveltePsiElement) {
            return this
        }

        return super.getNameElement()
    }

    override fun getName(): String {
        if (firstChild !is SveltePsiElement) {
            return super.getName()
        }

        val jsNode = SPREAD_OR_SHORTHAND_FINDER.findChild(firstChildNode) ?: return ""

        return if (jsNode.firstChildNode.elementType === JSElementTypes.SPREAD_EXPRESSION) {
            "<spread>"
        } else {
            jsNode.text
        }
    }

    override fun getReferences(hints: PsiReferenceService.Hints): Array<PsiReference> {
        if (name.startsWith("let:") && valueElement == null) {
            return arrayOf(ShorthandLetReference(this, TextRange(4, textLength)))
        }

        return super.getReferences(hints)
    }

    override fun getTextOffset(): Int {
        if (name.startsWith("let:")) {
            return super.getTextOffset() + 4
        }
        return super.getTextOffset()
    }

    fun getShorthandLetImplicitParameter(): JSImplicitElement? {
        val attribute = this
        return CachedValuesManager.getCachedValue(attribute) {
            val implicit = if (attribute.name.startsWith("let:") && attribute.valueElement == null) {
//                JSLocalImplicitElementImpl(attribute.localName, null, attribute, JSImplicitElement.Type.Variable)
                // TODO check if localName is valid identifier (?)
                ShorthandLetImplicitElement(attribute.localName, attribute)
            } else null

            CachedValueProvider.Result(implicit, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }


    override fun toString(): String {
        return "SvelteHtmlAttribute: $name"
    }

    class ShorthandLetReference(element: SvelteHtmlAttribute, rangeInElement: TextRange) :
        PsiReferenceBase<SvelteHtmlAttribute>(element, rangeInElement, false) {
        override fun resolve(): PsiElement? {
            return element.getShorthandLetImplicitParameter()
        }
    }

    class ShorthandLetImplicitElement(name: String, provider: PsiElement) : JSImplicitElementImpl(name, provider)
}
