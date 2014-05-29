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

package com.googlecode.junittoolbox.util;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows multiple exceptions to be thrown as a single exception -- adapted from Jetty.
 */
@ThreadSafe
public class MultiException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private static final String EXCEPTION_SEPARATOR = "\n\t______________________________________________________________________\n";

    private final List<Throwable> nested = new ArrayList<>();

    public MultiException() {
        super("Multiple exceptions");
    }

    /**
     * @param throwable will be ignored if <code>null</code>
     */
    public void add(@Nullable Throwable throwable) {
        if (throwable != null) {
            synchronized (nested) {
                if (throwable instanceof MultiException) {
                    MultiException other = (MultiException) throwable;
                    synchronized (other.nested) {
                        nested.addAll(other.nested);
                    }
                } else {
                    nested.add(throwable);
                }
            }
        }
    }

    public boolean isEmpty() {
        synchronized (nested) {
            return nested.isEmpty();
        }
    }

    /**
     * If this <code>MultiException</code> is empty then no action is taken,
     * if it contains a single <code>Throwable</code> that is thrown,
     * otherwise this <code>MultiException</code> is thrown.
     */
    public void throwIfNotEmpty() {
        synchronized (nested) {
            if (nested.isEmpty()) {
                // Do nothing
            } else if (nested.size() == 1) {
                Throwable t = nested.get(0);
                TigerThrower.sneakyThrow(t);
            } else {
                throw this;
            }
        }
    }

    @Override
    public String getMessage() {
        synchronized (nested) {
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
}
