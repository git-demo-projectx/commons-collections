/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4.bloomfilter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BloomFilterCopyTest {

    private static class Source extends SimpleJavaFileObject {
        private final String code;

        Source(final String code) {
            super(URI.create("string:///InvalidCopy.java"), JavaFileObject.Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
            return code;
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "new SimpleBloomFilter(Shape.fromNP(10000, 0.01))",
        "filter",
        "counting",
        "wrapped",
        "layered"
    })
    public void testCopyCannotReturnUnrelatedType(final String expression) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "A JDK is required to verify copy typing");
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final Source source = new Source("import org.apache.commons.collections4.bloomfilter.*;"
                + "class InvalidCopy { void copy(BloomFilter<SimpleBloomFilter> filter,"
                + " CountingBloomFilter<ArrayCountingBloomFilter> counting,"
                + " WrappedBloomFilter<?> wrapped, LayeredBloomFilter<SimpleBloomFilter> layered) {"
                + " SparseBloomFilter result = " + expression + ".copy(); } }");
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics,
                    Arrays.asList("-proc:none", "-classpath", System.getProperty("java.class.path")),
                    null, Collections.singletonList(source));
            assertFalse(task.call(), "An unrelated copy type must not compile");
        }
        assertTrue(diagnostics.getDiagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR
                        && diagnostic.getMessage(Locale.ROOT).contains("incompatible types")),
                diagnostics.getDiagnostics().toString());
    }
}
