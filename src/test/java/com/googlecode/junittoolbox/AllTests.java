package com.googlecode.junittoolbox;

import org.junit.runner.RunWith;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*Test.class", "!samples/**"})
public class AllTests {}
