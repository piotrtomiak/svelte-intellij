// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import dev.blachut.svelte.lang.psi.SveltePsiElementImpl;
import dev.blachut.svelte.lang.psi.*;

public class SvelteOpeningTagImpl extends SveltePsiElementImpl implements SvelteOpeningTag {

  public SvelteOpeningTagImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull SvelteVisitor visitor) {
    visitor.visitOpeningTag(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SvelteVisitor) accept((SvelteVisitor)visitor);
    else super.accept(visitor);
  }

}