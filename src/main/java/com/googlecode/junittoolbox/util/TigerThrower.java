package com.googlecode.junittoolbox.util;

/**
 * This class provides the method {@link #sneakyThrow(Throwable)} which
 * enables you to throw any checked {@link Exception} from any method,
 * even if its type is not listed in the method signature. Copied from
 * <a href="http://books.google.de/books?id=RM9sLE0ntQ0C&pg=RA1-PT116&dq=TigerThrower">Java Puzzlers</a>.
 */
public class TigerThrower<T extends Throwable> {

    /**
     * Will throw the given {@link Throwable}.
     */
    public static void sneakyThrow(Throwable t) {
        new TigerThrower<Error>().sneakyThrow2(t);
    }

    private void sneakyThrow2(Throwable t) throws T {
        throw (T) t;
    }
}