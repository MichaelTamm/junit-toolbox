package com.googlecode.junittoolbox.samples;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.TreeTraversalStrategy;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import com.googlecode.junittoolbox.WildcardPatternSuiteTest;
import org.junit.runner.RunWith;

/**
 * For {@link WildcardPatternSuiteTest}.
 */
@RunWith(WildcardPatternSuite.class)
@SuiteClasses(value = "**/*Test.class", treeTraversalStrategy = TreeTraversalStrategy.BREADTH_FIRST)
public class AllTestsBreadthFirst {}
