// ========================================================================
// Copyright 1999-2005 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package com.googlecode.junittoolbox;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows multiple exceptions to be thrown as a single exception -- adapted from Jetty.
 */
public class MultiException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private static final String EXCEPTION_SEPARATOR = "\n______________________________________________________________________\n";

    private final List<Throwable> nested = new ArrayList<Throwable>();

    public MultiException() {
        super("Multiple exceptions");
    }

    public void add(Throwable throwable) {
        if (throwable != null) {
            if (throwable instanceof MultiException) {
                MultiException me = (MultiException) throwable;
                nested.addAll(me.nested);
            } else {
                nested.add(throwable);
            }
        }
    }

    public boolean isEmpty() {
        return nested.isEmpty();
    }

    /**
     * If this multi exception is empty then no action is taken,
     * if it contains a single <code>Throwable</code> that is thrown,
     * otherwise this <code>MultiException</code> is thrown.
     */
    public void throwExceptionIfNotEmpty() throws Exception {
        if (nested.isEmpty()) {
            // Do nothing
        } else if (nested.size() == 1) {
            Throwable throwable = nested.get(0);
            if (throwable instanceof Error) {
                throw (Error) throwable;
            } else if (throwable instanceof Exception) {
                throw (Exception) throwable;
            } else {
                throw new Exception(throwable);
            }
        } else {
            throw this;
        }
    }

    /**
     * If this multi exception is empty then no action is taken,
     * if it contains a single <code>RuntimeException<code> that is thrown,
     * if it contains a single <code>Error<code> that is thrown,
     * otherwise this <code>MultiException</code> is thrown.
     */
    public void throwRuntimeExceptionIfNotEmpty() throws RuntimeException {
        if (nested.isEmpty()) {
            // Do nothing
        } else if (nested.size() == 1) {
            Throwable throwable = nested.get(0);
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else if (throwable instanceof Error) {
                throw (Error) throwable;
            } else {
                throw new RuntimeException(throwable);
            }
        } else {
            throw this;
        }
    }

    @Override
    public String getMessage() {
        if (nested.isEmpty()) {
            return "<no nested exceptions>";
        } else {
            StringBuilder sb = new StringBuilder();
            int n = nested.size();
            sb.append(n).append(n == 1 ? " nested exception:" : " nested exceptions:");
            for (Throwable t : nested) {
                sb.append(EXCEPTION_SEPARATOR).append("\n\t");
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                sb.append(sw.toString().replace("\n", "\n\t").trim());
            }
            sb.append(EXCEPTION_SEPARATOR);
            return sb.toString();
        }
    }
}
