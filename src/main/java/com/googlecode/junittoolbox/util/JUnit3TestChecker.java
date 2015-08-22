package com.googlecode.junittoolbox.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Copied from Maven Surefire source code.
 *
 * @author Kristian Rosenvold
 */
public class JUnit3TestChecker
{
    private final Class junitClass;

    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];


    private final NonAbstractClassFilter nonAbstractClassFilter = new NonAbstractClassFilter();


    public JUnit3TestChecker( ClassLoader testClassLoader )
    {
        junitClass = ReflectionUtils.tryLoadClass( testClassLoader, "junit.framework.Test" );
    }

    public boolean accept( Class testClass )
    {
        return nonAbstractClassFilter.accept( testClass ) && isValidJUnit3Test( testClass );
    }

    private boolean isValidJUnit3Test( Class testClass )
    {
        return junitClass != null && ( junitClass.isAssignableFrom( testClass ) || isSuiteOnly( testClass ) );
    }

    public boolean isSuiteOnly( Class testClass )
    {
        final Method suite = ReflectionUtils.tryGetMethod( testClass, "suite", EMPTY_CLASS_ARRAY );
        if ( suite != null )
        {

            final int modifiers = suite.getModifiers();
            if ( Modifier.isPublic( modifiers ) && Modifier.isStatic( modifiers ) )
            {
                return junit.framework.Test.class.isAssignableFrom( suite.getReturnType() );
            }
        }
        return false;
    }

}
