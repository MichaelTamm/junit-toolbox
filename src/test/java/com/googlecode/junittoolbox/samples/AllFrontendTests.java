package com.googlecode.junittoolbox.samples;

import com.googlecode.junittoolbox.ParallelSuite;
import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuiteTest;
import org.junit.runner.RunWith;

/**
 * For {@link WildcardPatternSuiteTest}.
 */
@RunWith(ParallelSuite.class)
@SuiteClasses("frontend/*FrontendTest.class")
public class AllFrontendTests {}
