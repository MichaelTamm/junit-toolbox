package com.googlecode.junittoolbox;

import com.googlecode.junittoolbox.util.JUnit4TestChecker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.experimental.categories.Categories;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.experimental.categories.Categories.*;

/**
 * A replacement for the JUnit runners {@link Suite} and {@link Categories},
 * which allows you to specify the children classes of your test suite class
 * using a <a href="http://ant.apache.org/manual/dirtasks.html#patterns" target="_blank">wildcard pattern</a>.
 * Example:<pre>
 *     &#64;RunWith(WildcardPatternSuite.class)
 *     &#64;SuiteClasses("&#42;&#42;/&#42;Test.class")
 *     public class AllTests {}
 * </pre>
 * You can also specify multiple patterns as well as exclude patterns:<pre>
 *     &#64;RunWith(WildcardPatternSuite.class)
 *     &#64;SuiteClasses({"&#42;&#42;/&#42;Test.class", "!gui/&#42;&#42;"})
 *     public class AllButGuiTests {}
 * </pre>
 * Because it is also a replacement for the {@link Categories} runner,
 * you can use the standard JUnit annotations {@link IncludeCategory @IncludeCategory}
 * and/or {@link ExcludeCategory @ExcludeCategory}:<pre>
 *     &#64;RunWith(WildcardPatternSuite.class)
 *     &#64;SuiteClasses("&#42;&#42;/&#42;Test.class")
 *     &#64;IncludeCategory(SlowTests.class)
 *     public class OnlySlowTests {}
 * </pre>
 * If you want to specify more than one category to include/exclude,
 * you can also use {@link IncludeCategories @IncludeCategories} and/or
 * {@link ExcludeCategories @ExcludeCategories} annotations provided
 * by JUnit Toolbox:<pre>
 *     &#64;RunWith(WildcardPatternSuite.class)
 *     &#64;SuiteClasses("&#42;&#42;/&#42;Test.class")
 *     &#64;ExcludeCategories({SlowTests.class, FlakyTests.class})
 *     public class NormalTests {}
 * </pre>
 */
public class WildcardPatternSuite extends Suite {

    private static Class<?>[] getSuiteClasses(Class<?> klass) throws InitializationError {
        org.junit.runners.Suite.SuiteClasses annotation1 = klass.getAnnotation(org.junit.runners.Suite.SuiteClasses.class);
        com.googlecode.junittoolbox.SuiteClasses annotation2 = klass.getAnnotation(com.googlecode.junittoolbox.SuiteClasses.class);
        if (annotation1 == null && annotation2 == null) {
            throw new InitializationError("class " + klass.getName() + " must have a SuiteClasses annotation");
        }
        Class<?>[] suiteClasses1 = (annotation1 == null ? null : annotation1.value());
        Class<?>[] suiteClasses2 = (annotation2 == null ? null : findSuiteClasses(klass, annotation2.value()));
        return union(suiteClasses1, suiteClasses2);
    }

    private static Class<?>[] findSuiteClasses(Class<?> klass, String... wildcardPatterns) throws InitializationError {
        File baseDir = getBaseDir(klass);
        Set<File> classFiles = findFiles(baseDir, wildcardPatterns);
        if (classFiles.isEmpty()) {
            throw new InitializationError("Did not find any *.class file using the specified wildcard patterns " + Arrays.toString(wildcardPatterns) + " relative to directory " + baseDir);
        }
        File testClassesDir = getClassesDir(klass);
        String testClassesPath;
        try {
            testClassesPath = testClassesDir.getCanonicalPath().replace('\\', '/');
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        List<Class<?>> testClasses = new ArrayList<>();
        ClassLoader classLoader = klass.getClassLoader();
        JUnit4TestChecker junit4TestChecker = new JUnit4TestChecker(classLoader);
        for (File file : classFiles) {
            try {
                String canonicalPath = file.getCanonicalPath().replace('\\', '/');
                if (!canonicalPath.startsWith(testClassesPath)) {
                    // Ignore all *.class files not contained in testClassesDir ...
                    continue;
                }
                String path = canonicalPath.substring(testClassesPath.length() + 1);
                String className = path.substring(0, path.length() - ".class".length()).replace('/', '.');
                Class<?> clazz = classLoader.loadClass(className);
                if (junit4TestChecker.accept(clazz)) {
                    testClasses.add(clazz);
                }
            } catch (Exception e) {
                throw new InitializationError("Failed to load " + file + " -- " + e.getMessage());
            }
        }
        if (testClasses.isEmpty()) {
            throw new InitializationError("Did not find any test classes using the specified wildcard patterns " + Arrays.toString(wildcardPatterns) + " relative to directory " + baseDir);
        }
        return testClasses.toArray(new Class[testClasses.size()]);
    }

    private static Set<File> findFiles(File baseDir, String... wildcardPatterns) throws InitializationError {
        try {
            Set<File> included = new HashSet<>();
            Set<File> excluded = new HashSet<>();
            for (String wildcardPattern: wildcardPatterns) {
                if (wildcardPattern == null) {
                    throw new InitializationError("wildcard pattern for the SuiteClasses annotation must not be null");
                } else if (wildcardPattern.startsWith("!")) {
                    excluded.addAll(findFiles(baseDir, wildcardPattern.substring(1)));
                } else {
                    if (!wildcardPattern.endsWith(".class")) {
                        throw new InitializationError("wildcard pattern for the SuiteClasses annotation must end with \".class\"");
                    }
                    included.addAll(findFiles(baseDir, wildcardPattern));
                }
            }
            included.removeAll(excluded);
            return included;
        } catch (IOException e) {
            throw new InitializationError("Failed to scan " + baseDir + " using wildcard patterns " + Arrays.toString(wildcardPatterns) + " -- " + e);
        }
    }

    private static Collection<File> findFiles(File baseDir, String wildcardPattern) throws InitializationError, IOException {
        if (wildcardPattern.startsWith("/")) {
            throw new InitializationError("wildcard pattern for the SuiteClasses annotation must not start with a '/' character");
        }
        while (wildcardPattern.startsWith("../")) {
            baseDir = baseDir.getParentFile();
            wildcardPattern = wildcardPattern.substring(3);
        }
        Pattern regex = convertWildcardPatternToRegex("/" + wildcardPattern);
        String basePath = baseDir.getCanonicalPath().replace('\\', '/');
        IOFileFilter fileFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                try {
                    // Never accept directories, hidden files, and inner classes ...
                    if (file.isDirectory() || file.isHidden() || file.getName().contains("$")) {
                        return false;
                    }
                    String canonicalPath = file.getCanonicalPath().replace('\\', '/');
                    if (canonicalPath.startsWith(basePath)) {
                        String path = canonicalPath.substring(basePath.length());
                        if (regex.matcher(path).matches()) {
                            return true;
                        }
                    }
                    return false;
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        };
        return FileUtils.listFiles(baseDir, fileFilter, TrueFileFilter.INSTANCE);
    }

    private static File getBaseDir(Class<?> klass) throws InitializationError {
        URL klassUrl = klass.getResource(klass.getSimpleName() + ".class");
        try {
            return new File(klassUrl.toURI()).getParentFile();
        } catch (URISyntaxException e) {
            throw new InitializationError("Failed to determine directory of " + klass.getSimpleName() + ".class file: " + e.getMessage());
        }
    }

    private static File getClassesDir(Class<?> klass) throws InitializationError {
        URL classesDirUrl = klass.getProtectionDomain().getCodeSource().getLocation();
        try {
            return new File(classesDirUrl.toURI());
        } catch (URISyntaxException e) {
            throw new InitializationError("Failed to determine classes directory of " + klass.getName() + " class: " + e.getMessage());
        }
    }

    private static Pattern convertWildcardPatternToRegex(String wildCardPattern) throws InitializationError {
        String s = wildCardPattern;
        while (s.contains("***")) {
            s = s.replace("***", "**");
        }
        String suffix;
        if (s.endsWith("/**")) {
            s = s.substring(0, s.length() - 3);
            suffix = "(.*)";
        } else {
            suffix ="";
        }
        s = s.replace(".", "[.]");
        s = s.replace("/**/", "/::/");
        s = s.replace("*", "([^/]*)");
        s = s.replace("/::/", "((/.*/)|(/))");
        s = s.replace("?", ".");
        if (s.contains("**")) {
            throw new InitializationError("Invalid wildcard pattern \"" + wildCardPattern + "\"");
        }
        return Pattern.compile(s + suffix);
    }

    private static Class<?>[] union(Class<?>[] suiteClasses1, Class<?>[] suiteClasses2) {
        if (suiteClasses1 == null) {
            return suiteClasses2;
        } else if (suiteClasses2 == null) {
            return suiteClasses1;
        } else {
            HashSet<Class<?>> temp = new HashSet<>();
            temp.addAll(Arrays.asList(suiteClasses1));
            temp.addAll(Arrays.asList(suiteClasses2));
            Class<?>[] result = new Class<?>[temp.size()];
            temp.toArray(result);
            return result;
        }
    }

    public WildcardPatternSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(builder, klass, getSuiteClasses(klass));
        Filter filter = CategoriesFilter.forTestSuite(klass);
        if (filter != null) {
            try {
                filter(filter);
            } catch (NoTestsRemainException e) {
                throw new InitializationError(e);
            }
        }
    }
}
