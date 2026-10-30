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

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests that copy types cannot be selected by the caller. */
public class BloomFilterCopyTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "SparseBloomFilter copy = new SimpleBloomFilter(shape).copy();",
        "BloomFilter<SimpleBloomFilter> filter = new SimpleBloomFilter(shape); SparseBloomFilter copy = filter.copy();",
        "BloomFilter<SparseBloomFilter> filter = new SparseBloomFilter(shape); SimpleBloomFilter copy = filter.copy();",
        "CountingBloomFilter<ArrayCountingBloomFilter> filter = new ArrayCountingBloomFilter(shape); SimpleBloomFilter copy = filter.copy();",
        "BloomFilter<LayeredBloomFilter<SimpleBloomFilter>> filter = new LayeredBloomFilter<>(shape, "
            + "LayerManager.<SimpleBloomFilter>builder().setSupplier(() -> new SimpleBloomFilter(shape)).get()); "
            + "SimpleBloomFilter copy = filter.copy();"
    })
    public void testIncompatibleCopyDoesNotCompile(final String statement) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///CopyTypeCheck.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return "import org.apache.commons.collections4.bloomfilter.*; "
                    + "class CopyTypeCheck { void check(Shape shape) { " + statement + " } }";
            }
        };
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            assertFalse(compiler.getTask(null, fileManager, diagnostics,
                Arrays.asList("-proc:none", "-classpath", System.getProperty("java.class.path")), null,
                Collections.singletonList(source)).call());
        }
        assertTrue(diagnostics.getDiagnostics().stream().anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR
            && "compiler.err.prob.found.req".equals(d.getCode())), diagnostics.getDiagnostics().toString());
    }
}
