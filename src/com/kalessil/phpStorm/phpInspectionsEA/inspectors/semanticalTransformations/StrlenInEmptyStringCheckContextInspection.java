package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import org.jetbrains.annotations.NotNull;

public class StrlenInEmptyStringCheckContextInspection extends BasePhpInspection {
    private static final String strProblemDescription = "Can be replaced by comparing with empty string";

    @NotNull
    public String getDisplayName() {
        return "Performance: 'strlen(...)' used to check if string is empty";
    }

    @NotNull
    public String getShortName() {
        return "StrlenInEmptyStringCheckContextInspection";
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpBinaryExpression(BinaryExpression expression)  {
                PsiElement objRightOperand = expression.getRightOperand();
                if (
                    !(objRightOperand instanceof PhpExpression) ||
                    !(objRightOperand.getNode().getElementType() == PhpElementTypes.NUMBER)
                ) {
                    return;
                }

                String strRightOperand = objRightOperand.getText();
                IElementType operationType = expression.getOperation().getNode().getElementType();

                /** tests types: zero any comparison, one: less, greater or equals */
                boolean isEmptyTestByZeroComparison = (strRightOperand.equals("0"));
                boolean isEmptyTestByOneComparison = (
                    strRightOperand.equals("1") && (
                        operationType == PhpTokenTypes.opLESS ||
                        operationType == PhpTokenTypes.opGREATER_OR_EQUAL
                    )
                );

                if (!isEmptyTestByZeroComparison && !isEmptyTestByOneComparison) {
                    return;
                }


                PsiElement objLeftOperand = expression.getLeftOperand();
                //noinspection ConstantConditions
                if (
                    !(objLeftOperand instanceof FunctionReference) ||
                    null == ((FunctionReference) objLeftOperand).getName() ||
                    !((FunctionReference) objLeftOperand).getName().equals("strlen")
                ) {
                    return;
                }


                holder.registerProblem(expression, strProblemDescription, ProblemHighlightType.WEAK_WARNING);
            }
        };
    }
}