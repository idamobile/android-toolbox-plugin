package com.shaubert.idea.android.toolbox;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;

import java.util.Map;

public class ViewPresenterPattern extends AbstractCodeGenerationPattern {

    private PsiClass dataClass;

    @Override
    public String getName() {
        return "ViewPresenter pattern";
    }

    @Override
    public String getSuggestedClassName(String layoutFileName) {
        return super.getSuggestedClassName(layoutFileName) + "Presenter";
    }

    @Override
    public void setup(Project project) {
        super.setup(project);
        TreeClassChooser classChooser =
                TreeClassChooserFactory.getInstance(project).createAllProjectScopeChooser("Select data class");
        classChooser.showDialog();
        dataClass = classChooser.getSelected();
    }

    @Override
    protected void generateBody(AndroidView androidView, String layoutFileName, boolean useButterKnife, final PsiClass psiClass, Project project) {
        FieldGenerator fieldGenerator = new FieldGenerator();
        Map<AndroidView, PsiField> fieldMappings = fieldGenerator.generateFields(
                androidView, project, useButterKnife, new FieldGenerator.AddToPsiClassCallback(psiClass));
        PsiField mainViewField = createMainViewField(psiClass);

        PsiField dataField = null;
        if (dataClass != null) {
            dataField = createDataField(psiClass);
        }

        generateConstructor(androidView, layoutFileName, useButterKnife, fieldMappings, mainViewField, psiClass);
        psiClass.add(PropertyUtil.generateGetterPrototype(mainViewField));

        if (dataField != null) {
            psiClass.add(PropertyUtil.generateGetterPrototype(dataField));
            PsiMethod refreshMethod = generateRefreshMethod(psiClass, dataField, mainViewField);
            generateSwapDataMethod(psiClass, dataField, refreshMethod);
        }
    }

    private PsiField createMainViewField(PsiClass psiClass) {
        String name = "view";
        PsiClass viewClass = ClassHelper.findClass(psiClass.getProject(), ANDROID_VIEW_CLASS);
        FieldGenerator generator = new FieldGenerator();
        PsiField field = generator.createField(viewClass, name);
        psiClass.add(field);
        return field;
    }

    private PsiField createDataField(PsiClass psiClass) {
        String name = "data";
        FieldGenerator generator = new FieldGenerator();
        PsiField field = generator.createField(dataClass, name);
        psiClass.add(field);
        return field;
    }

    @SuppressWarnings("ConstantConditions")
    private PsiMethod generateRefreshMethod(PsiClass psiClass, PsiField dataField, PsiField mainViewField) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiMethod refreshMethod = factory.createMethod("refresh", PsiType.VOID);

        PsiStatement statement = factory.createStatementFromText(
                "if (" + dataField.getName() + " != null) { "
                        +  mainViewField.getName() + ".setVisibility(View.VISIBLE);"
                        + "} else {"
                        +  mainViewField.getName() + ".setVisibility(View.GONE);"
                        + "}",
                refreshMethod.getContext());
        refreshMethod.getBody().add(statement);
        return (PsiMethod) psiClass.add(refreshMethod);
    }

    @SuppressWarnings("ConstantConditions")
    private void generateSwapDataMethod(PsiClass psiClass, PsiField dataField, PsiMethod refreshMethod) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiMethod swapMethod = factory.createMethod("swapData", PsiType.VOID);
        PsiParameter parameter = factory.createParameter("data", factory.createType(dataClass));
        swapMethod.getParameterList().add(parameter);

        swapMethod.getBody().add(factory.createStatementFromText(
                "this." + dataField.getName() + " = " + parameter.getName() + ";", swapMethod.getContext()));
        swapMethod.getBody().add(factory.createStatementFromText(refreshMethod.getName() + "();", swapMethod.getContext()));
        psiClass.add(swapMethod);
    }

    @SuppressWarnings("ConstantConditions")
    private void generateConstructor(AndroidView androidView, String layoutFileName, boolean useButterKnife, Map<AndroidView, PsiField> fieldMappings,
                                     PsiField mainViewField, PsiClass psiClass) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiMethod constructor = factory.createConstructor();
        PsiClass contextClass = ClassHelper.findClass(psiClass.getProject(), ANDROID_CONTEXT_CLASS);
        PsiParameter contextParam = factory.createParameter("context", factory.createType(contextClass));
        PsiClass viewGroupClass = ClassHelper.findClass(psiClass.getProject(), ANDROID_VIEW_GROUP_CLASS);
        PsiParameter viewParentParam = factory.createParameter("parent", factory.createType(viewGroupClass));
        constructor.getParameterList().add(contextParam);
        constructor.getParameterList().add(viewParentParam);

        if (constructor.getBody() == null) {
            throw new GenerateViewPresenterAction.CancellationException("Failed to create ViewPresenter constructor");
        }

        PsiClass layoutInflaterClass = ClassHelper.findClass(psiClass.getProject(), ANDROID_LAYOUT_INFLATER_CLASS);
        PsiExpression iflaterInitialization = factory.createExpressionFromText(
                layoutInflaterClass.getName() + ".from(" + contextParam.getName() + ")", constructor.getContext());
        PsiDeclarationStatement inflaterDeclaration = factory.createVariableDeclarationStatement("inflater",
                factory.createType(layoutInflaterClass), iflaterInitialization);
        constructor.getBody().add(inflaterDeclaration);


        PsiStatement inflateStatement = factory.createStatementFromText(mainViewField.getName()
                + " = inflater.inflate(R.layout." + FileUtil.removeExtension(layoutFileName)
                    + ", " + viewParentParam.getName()
                    + ", false);", constructor.getContext());
        constructor.getBody().add(inflateStatement);

        androidView.setTagName(ANDROID_VIEW_CLASS);
        androidView.setIdValue(mainViewField.getName());
        if (useButterKnife) {
            PsiStatement injectStatement =
                    factory.createStatementFromText("Views.inject(this, " + mainViewField.getName() + ");", constructor.getContext());
            constructor.getBody().add(injectStatement);
        } else {
            addFindViewStatements(factory, constructor, androidView, fieldMappings);
        }

        psiClass.add(constructor);
    }

    @SuppressWarnings("ConstantConditions")
    private void addFindViewStatements(final PsiElementFactory factory, final PsiMethod constructor,
                                       final AndroidView view, final Map<AndroidView, PsiField> fieldMappings) {
        FindViewByIdStatementGenerator findViewByIdStatementGenerator = new FindViewByIdStatementGenerator();
        FindViewByIdStatementGenerator.ClassFieldAssigner fieldAssigner =
                new FindViewByIdStatementGenerator.ClassFieldAssigner(fieldMappings, view.getIdValue()) {
                    @Override
                    protected void onStatementCreated(String statement, PsiField field, AndroidView view) {
                        PsiStatement assignmentStatement =
                                factory.createStatementFromText(statement, constructor.getContext());
                        constructor.getBody().add(assignmentStatement);
                    }

                    @Override
                    public boolean shouldProcessView(AndroidView view) {
                        return view.getParent() != null;
                    }
                };
        findViewByIdStatementGenerator.createFindViewStatements(view, fieldAssigner);
    }
}
