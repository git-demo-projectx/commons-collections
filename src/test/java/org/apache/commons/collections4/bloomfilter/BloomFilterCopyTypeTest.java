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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BloomFilterCopyTypeTest {

    @TempDir
    Path outputDirectory;

    @Test
    public void testCopyCannotReturnUnrelatedType() throws Exception {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///CopyType.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return "import org.apache.commons.collections4.bloomfilter.*;"
                        + "class CopyType { SparseBloomFilter filter = "
                        + "new SimpleBloomFilter(Shape.fromNP(10000, 0.01)).copy();"
                        + "BloomFilter<SimpleBloomFilter> typed = new SimpleBloomFilter(Shape.fromNP(10000, 0.01));"
                        + "SparseBloomFilter other = typed.copy(); }";
            }
        };
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics,
                    Arrays.asList("-proc:none", "-classpath", System.getProperty("java.class.path"),
                            "-d", outputDirectory.toString()), null, Collections.singletonList(source));
            assertFalse(task.call(), "A simple filter copy must not be assignable to a sparse filter");
            assertEquals(2, diagnostics.getDiagnostics().stream()
                    .filter(diagnostic -> diagnostic.getCode().equals("compiler.err.prob.found.req")).count(),
                    "Both concrete and interface references must reject unrelated copy types");
        }
    }
}
