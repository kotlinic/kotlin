/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.nj2k.inference.common;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("nj2k/testData/inference/common")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class CommonConstraintCollectorTestGenerated extends AbstractCommonConstraintCollectorTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
    }

    public void testAllFilesPresentInCommon() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("nj2k/testData/inference/common"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @TestMetadata("arrayAssignment.kt")
    public void testArrayAssignment() throws Exception {
        runTest("nj2k/testData/inference/common/arrayAssignment.kt");
    }

    @TestMetadata("arrayOfArrays.kt")
    public void testArrayOfArrays() throws Exception {
        runTest("nj2k/testData/inference/common/arrayOfArrays.kt");
    }

    @TestMetadata("callFunctionWithTypeParamFromOuterScope.kt")
    public void testCallFunctionWithTypeParamFromOuterScope() throws Exception {
        runTest("nj2k/testData/inference/common/callFunctionWithTypeParamFromOuterScope.kt");
    }

    @TestMetadata("callWithTypeParams.kt")
    public void testCallWithTypeParams() throws Exception {
        runTest("nj2k/testData/inference/common/callWithTypeParams.kt");
    }

    @TestMetadata("constructorCall.kt")
    public void testConstructorCall() throws Exception {
        runTest("nj2k/testData/inference/common/constructorCall.kt");
    }

    @TestMetadata("delegationConstructorCall.kt")
    public void testDelegationConstructorCall() throws Exception {
        runTest("nj2k/testData/inference/common/delegationConstructorCall.kt");
    }

    @TestMetadata("elementOfArrayAssignment.kt")
    public void testElementOfArrayAssignment() throws Exception {
        runTest("nj2k/testData/inference/common/elementOfArrayAssignment.kt");
    }

    @TestMetadata("elementOfListAssignment.kt")
    public void testElementOfListAssignment() throws Exception {
        runTest("nj2k/testData/inference/common/elementOfListAssignment.kt");
    }

    @TestMetadata("forLoop.kt")
    public void testForLoop() throws Exception {
        runTest("nj2k/testData/inference/common/forLoop.kt");
    }

    @TestMetadata("functionCall.kt")
    public void testFunctionCall() throws Exception {
        runTest("nj2k/testData/inference/common/functionCall.kt");
    }

    @TestMetadata("functionReturn.kt")
    public void testFunctionReturn() throws Exception {
        runTest("nj2k/testData/inference/common/functionReturn.kt");
    }

    @TestMetadata("functionWithTypeParamCall.kt")
    public void testFunctionWithTypeParamCall() throws Exception {
        runTest("nj2k/testData/inference/common/functionWithTypeParamCall.kt");
    }

    @TestMetadata("lambdaAsParameter.kt")
    public void testLambdaAsParameter() throws Exception {
        runTest("nj2k/testData/inference/common/lambdaAsParameter.kt");
    }

    @TestMetadata("lambdaAssign.kt")
    public void testLambdaAssign() throws Exception {
        runTest("nj2k/testData/inference/common/lambdaAssign.kt");
    }

    @TestMetadata("lambdaImplicitReturn.kt")
    public void testLambdaImplicitReturn() throws Exception {
        runTest("nj2k/testData/inference/common/lambdaImplicitReturn.kt");
    }

    @TestMetadata("lambdaReturn.kt")
    public void testLambdaReturn() throws Exception {
        runTest("nj2k/testData/inference/common/lambdaReturn.kt");
    }

    @TestMetadata("listAssignment.kt")
    public void testListAssignment() throws Exception {
        runTest("nj2k/testData/inference/common/listAssignment.kt");
    }

    @TestMetadata("listGenerator.kt")
    public void testListGenerator() throws Exception {
        runTest("nj2k/testData/inference/common/listGenerator.kt");
    }

    @TestMetadata("listOfLists.kt")
    public void testListOfLists() throws Exception {
        runTest("nj2k/testData/inference/common/listOfLists.kt");
    }

    @TestMetadata("listOfListsGenerator.kt")
    public void testListOfListsGenerator() throws Exception {
        runTest("nj2k/testData/inference/common/listOfListsGenerator.kt");
    }

    @TestMetadata("memberCall.kt")
    public void testMemberCall() throws Exception {
        runTest("nj2k/testData/inference/common/memberCall.kt");
    }

    @TestMetadata("newExpression.kt")
    public void testNewExpression() throws Exception {
        runTest("nj2k/testData/inference/common/newExpression.kt");
    }

    @TestMetadata("returnFromLambda.kt")
    public void testReturnFromLambda() throws Exception {
        runTest("nj2k/testData/inference/common/returnFromLambda.kt");
    }

    @TestMetadata("sequenceOfCalls.kt")
    public void testSequenceOfCalls() throws Exception {
        runTest("nj2k/testData/inference/common/sequenceOfCalls.kt");
    }

    @TestMetadata("sequenceOfCallsWIthLambda.kt")
    public void testSequenceOfCallsWIthLambda() throws Exception {
        runTest("nj2k/testData/inference/common/sequenceOfCallsWIthLambda.kt");
    }

    @TestMetadata("simpleAssignment.kt")
    public void testSimpleAssignment() throws Exception {
        runTest("nj2k/testData/inference/common/simpleAssignment.kt");
    }

    @TestMetadata("superCall.kt")
    public void testSuperCall() throws Exception {
        runTest("nj2k/testData/inference/common/superCall.kt");
    }

    @TestMetadata("superConstuctorCall.kt")
    public void testSuperConstuctorCall() throws Exception {
        runTest("nj2k/testData/inference/common/superConstuctorCall.kt");
    }

    @TestMetadata("superFunctionReturnTypeWithTypeParameters.kt")
    public void testSuperFunctionReturnTypeWithTypeParameters() throws Exception {
        runTest("nj2k/testData/inference/common/superFunctionReturnTypeWithTypeParameters.kt");
    }

    @TestMetadata("superFunctionReturnTypeWithTypeParametersSubstitution.kt")
    public void testSuperFunctionReturnTypeWithTypeParametersSubstitution() throws Exception {
        runTest("nj2k/testData/inference/common/superFunctionReturnTypeWithTypeParametersSubstitution.kt");
    }

    @TestMetadata("superFunctionType.kt")
    public void testSuperFunctionType() throws Exception {
        runTest("nj2k/testData/inference/common/superFunctionType.kt");
    }

    @TestMetadata("typeParamsBounds.kt")
    public void testTypeParamsBounds() throws Exception {
        runTest("nj2k/testData/inference/common/typeParamsBounds.kt");
    }

    @TestMetadata("varargsInCall.kt")
    public void testVarargsInCall() throws Exception {
        runTest("nj2k/testData/inference/common/varargsInCall.kt");
    }
}
