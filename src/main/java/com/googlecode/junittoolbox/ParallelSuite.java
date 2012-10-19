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
import java.lang.annotation.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * An extension of the Junit 4 {@link Suite} runner
 * which executes test classes concurrently.
 * Furthermore it provides its own {@link ParallelSuite#@SuiteClasses SuiteClasses} annotation,
 * which enables you to define a glob pattern like <code>"**&#47;*Test.class"</code>.
 */
public class ParallelSuite extends Suite {

    /**
     * If you use this annotation instead of the {@link Suite.SuiteClasses SuiteClasses} annotation
     * provided by the JUnit class {@link Suite}, you can specify a glob pattern instead of
     * explicitly enumerating all classes which should belong to the test suite.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface SuiteClasses {
        /**
         * Glob pattern relative to the directory of the actual test suite class
         * annotated with <code>@RunWith(ParallelSuite.class)</code>.
         */
        public String value();
    }

    private static Class<?>[] getSuiteClasses(Class<?> klass) throws InitializationError {
        final Suite.SuiteClasses annotation1 = klass.getAnnotation(Suite.SuiteClasses.class);
        final SuiteClasses annotation2 = klass.getAnnotation(SuiteClasses.class);
        if (annotation1 == null && annotation2 == null) {
            throw new InitializationError("class " + klass.getName() + " must have a SuiteClasses annotation");
        }
        final Class<?>[] suiteClasses1 = (annotation1 == null ? null : annotation1.value());
        final Class<?>[] suiteClasses2 = (annotation2 == null ? null : findSuiteClasses(klass, annotation2.value()));
        return union(suiteClasses1, suiteClasses2);
    }

    private static Class<?>[] findSuiteClasses(Class<?> klass, String globPattern) throws InitializationError {
        if (globPattern.startsWith("/")) {
            throw new InitializationError("the glob pattern for the SuiteClasses annotation must not start with \"/\"");
        }
        if (!globPattern.endsWith(".class")) {
            throw new InitializationError("the glob pattern for the SuiteClasses annotation must end with \".class\"");
        }
        final File baseDir = getBaseDir(klass);
        try {
            final String basePath = baseDir.getCanonicalPath().replace('\\', '/');
            final Pattern globPatternRegex = convertGlobPatternToRegex("/" + globPattern);
            final IOFileFilter fileFilter = new AbstractFileFilter() {
                @Override
                public boolean accept(File file) {
                    try {
                        final String canonicalPath = file.getCanonicalPath().replace('\\', '/');
                        assert canonicalPath.startsWith(basePath);
                        final String path = canonicalPath.substring(basePath.length());
                        return globPatternRegex.matcher(path).matches();
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            };
            final IOFileFilter dirFilter = (globPattern.contains("/") ? TrueFileFilter.INSTANCE : FalseFileFilter.INSTANCE);
            final Collection<File> classFiles = FileUtils.listFiles(baseDir, fileFilter, dirFilter);
            if (classFiles.isEmpty()) {
                throw new InitializationError("did not found any *.class file using the specified glob pattern " + globPattern + " in directory " + basePath);
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
            throw new InitializationError("failed to find " + globPattern + " files in " + baseDir + " -- " + e.getMessage());
        }
    }

    private static Pattern convertGlobPatternToRegex(String globPattern) {
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

    private static File getBaseDir(Class<?> klass) throws InitializationError {
        final URL klassUrl = klass.getResource(klass.getSimpleName() + ".class");
        try {
            return new File(klassUrl.toURI()).getParentFile();
        } catch (URISyntaxException e) {
            throw new InitializationError("Failed to determine directory of " + klass.getSimpleName() + ".class file: " + e.getMessage());
        }
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

    public ParallelSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(builder, klass, getSuiteClasses(klass));
        setScheduler(new ParallelScheduler());
    }
}
