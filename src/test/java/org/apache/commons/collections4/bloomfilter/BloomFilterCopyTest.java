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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BloomFilterCopyTest {

    @TempDir
    Path outputDirectory;

    private boolean compile(final String body, final DiagnosticCollector<JavaFileObject> diagnostics) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        final String source = "import org.apache.commons.collections4.bloomfilter.*; class CopyTyping { " + body + " }";
        final JavaFileObject input = new SimpleJavaFileObject(URI.create("string:///CopyTyping.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return source;
            }
        };
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            return compiler.getTask(null, fileManager, diagnostics,
                    Arrays.asList("-proc:none", "-source", "8", "-target", "8", "-classpath",
                            System.getProperty("java.class.path"), "-d", outputDirectory.toString()),
                    null, Collections.singletonList(input)).call();
        }
    }

    @Test
    public void testCopyRetainsFilterType() throws IOException {
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        assertTrue(compile("void copy(BloomFilter<SimpleBloomFilter> simple, BloomFilter<SparseBloomFilter> sparse, "
                + "CountingBloomFilter<ArrayCountingBloomFilter> counting, "
                + "BloomFilter<LayeredBloomFilter<SimpleBloomFilter>> layered) { "
                + "SimpleBloomFilter a = simple.copy(); SparseBloomFilter b = sparse.copy(); "
                + "ArrayCountingBloomFilter c = counting.copy(); LayeredBloomFilter<SimpleBloomFilter> d = layered.copy(); }",
                diagnostics), diagnostics.getDiagnostics().toString());
    }

    @Test
    public void testCopyRejectsUnrelatedFilterType() throws IOException {
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        assertFalse(compile("void copy(BloomFilter<SimpleBloomFilter> simple) { "
                + "SparseBloomFilter a = simple.copy(); }", diagnostics));
        assertTrue(diagnostics.getDiagnostics().stream().anyMatch(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR));
    }

    @Test
    public void testConcreteCopyRejectsUnrelatedFilterType() throws IOException {
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        assertFalse(compile("void copy() { "
                + "SparseBloomFilter filter = new SimpleBloomFilter(Shape.fromNP(10000, 0.01)).copy(); }", diagnostics));
        assertTrue(diagnostics.getDiagnostics().stream().anyMatch(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR));
    }
}
