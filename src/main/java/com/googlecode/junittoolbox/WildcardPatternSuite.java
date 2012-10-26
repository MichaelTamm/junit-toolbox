package com.googlecode.junittoolbox;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * A replacement for the JUnit {@link Suite} runner, which allows you to specify
 * the children classes of your test suite class using a
 * <a href="http://ant.apache.org/manual/dirtasks.html#patterns">wildcard pattern</a>.<br />
 * Example:<pre>
 *     &#64;RunWith(WildcardPatternSuite.class)
 *     &#64;SuiteClasses("&#42;&#42;/&#42;Test.class")
 *     public class AllTests {}
 * </pre>
 */
public class WildcardPatternSuite extends Suite {

    private static Class<?>[] getSuiteClasses(Class<?> klass) throws InitializationError {
        final org.junit.runners.Suite.SuiteClasses annotation1 = klass.getAnnotation(org.junit.runners.Suite.SuiteClasses.class);
        final com.googlecode.junittoolbox.SuiteClasses annotation2 = klass.getAnnotation(com.googlecode.junittoolbox.SuiteClasses.class);
        if (annotation1 == null && annotation2 == null) {
            throw new InitializationError("class " + klass.getName() + " must have a SuiteClasses annotation");
        }
        final Class<?>[] suiteClasses1 = (annotation1 == null ? null : annotation1.value());
        final Class<?>[] suiteClasses2 = (annotation2 == null ? null : findSuiteClasses(klass, annotation2.value()));
        return union(suiteClasses1, suiteClasses2);
    }

    private static Class<?>[] findSuiteClasses(Class<?> klass, String wildcardPattern) throws InitializationError {
        if (wildcardPattern.startsWith("/")) {
            throw new InitializationError("the wildcard pattern for the SuiteClasses annotation must not start with a '/' character");
        }
        if (!wildcardPattern.endsWith(".class")) {
            throw new InitializationError("the wildcard pattern for the SuiteClasses annotation must end with \".class\"");
        }
        final File baseDir = getBaseDir(klass);
        try {
            final String basePath = baseDir.getCanonicalPath().replace('\\', '/');
            final Pattern wildcardPatternRegex = convertWildcardPatternToRegex("/" + wildcardPattern);
            final IOFileFilter fileFilter = new AbstractFileFilter() {
                @Override
                public boolean accept(File file) {
                    try {
                        final String canonicalPath = file.getCanonicalPath().replace('\\', '/');
                        if (canonicalPath.startsWith(basePath)) {
                            final String path = canonicalPath.substring(basePath.length());
                            return wildcardPatternRegex.matcher(path).matches();
                        } else {
                            return false;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            };
            final IOFileFilter dirFilter = (wildcardPattern.contains("/") ? TrueFileFilter.INSTANCE : FalseFileFilter.INSTANCE);
            final Collection<File> classFiles = FileUtils.listFiles(baseDir, fileFilter, dirFilter);
            if (classFiles.isEmpty()) {
                throw new InitializationError("did not find any *.class file using the specified wildcard pattern " + wildcardPattern + " in directory " + basePath);
            }
            final String classNamePrefix = (klass.getPackage() == null ? "" : klass.getPackage().getName() + ".");
            final Class<?>[] result = new Class<?>[classFiles.size()];
            int i = 0;
            final ClassLoader classLoader = klass.getClassLoader();
            for (File file : classFiles) {
                final String canonicalPath = file.getCanonicalPath().replace('\\', '/');
                assert canonicalPath.startsWith(basePath) && canonicalPath.endsWith(".class");
                final String path = canonicalPath.substring(basePath.length() + 1);
                final String className = classNamePrefix + path.substring(0, path.length() - ".class".length()).replace('/', '.');
                result[i++] = classLoader.loadClass(className);
            }
            return result;
        } catch (Exception e) {
            throw new InitializationError("failed to find " + wildcardPattern + " files in " + baseDir + " -- " + e.getMessage());
        }
    }

    private static File getBaseDir(Class<?> klass) throws InitializationError {
        final URL klassUrl = klass.getResource(klass.getSimpleName() + ".class");
        try {
            return new File(klassUrl.toURI()).getParentFile();
        } catch (URISyntaxException e) {
            throw new InitializationError("failed to determine directory of " + klass.getSimpleName() + ".class file: " + e.getMessage());
        }
    }

    private static Pattern convertWildcardPatternToRegex(String globPattern) {
        String s = globPattern;
        while (s.contains("***")) {
            s = s.replace("***", "**");
        }
        s = s.replace(".", "[.]");
        s = s.replace("**", "::");
        s = s.replace("*", "([^/]*)");
        s = s.replace("::", "(.*)");
        s = s.replace("?", ".");
        return Pattern.compile(s);
    }

    private static Class<?>[] union(Class<?>[] suiteClasses1, Class<?>[] suiteClasses2) {
        if (suiteClasses1 == null) {
            return suiteClasses2;
        } else if (suiteClasses2 == null) {
            return suiteClasses1;
        } else {
            final HashSet<Class<?>> temp = new HashSet<Class<?>>();
            temp.addAll(Arrays.asList(suiteClasses1));
            temp.addAll(Arrays.asList(suiteClasses2));
            final Class<?>[] result = new Class<?>[temp.size()];
            temp.toArray(result);
            return result;
        }
    }

    public WildcardPatternSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(builder, klass, getSuiteClasses(klass));
    }
}
