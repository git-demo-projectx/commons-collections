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

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BloomFilterCopyTypeTest {

    @TempDir
    Path outputDirectory;

    private boolean compiles(final String declaration) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Tests require a JDK compiler");
        final JavaFileObject source = new SimpleJavaFileObject(URI.create("string:///CopyType.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return "import org.apache.commons.collections4.bloomfilter.*; class CopyType { " + declaration + " }";
            }
        };
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            return compiler.getTask(null, fileManager, diagnostics,
                    Arrays.asList("-proc:none", "-d", outputDirectory.toString()), null,
                    Collections.singletonList(source)).call();
        }
    }

    @Test
    void testConcreteCopyCannotChangeType() throws IOException {
        assertFalse(compiles("SparseBloomFilter copy = new SimpleBloomFilter(Shape.fromNP(10000, 0.01)).copy();"));
    }

    @Test
    void testInterfaceCopyCannotChangeType() throws IOException {
        assertFalse(compiles("SparseBloomFilter copy(BloomFilter<SimpleBloomFilter> filter) { return filter.copy(); }"));
    }

    @Test
    void testCopyPreservesType() throws IOException {
        assertTrue(compiles("<T extends BloomFilter<T>> T genericCopy(T filter) { return filter.copy(); }"
                + "SimpleBloomFilter copy(BloomFilter<SimpleBloomFilter> filter) { return filter.copy(); }"
                + "SparseBloomFilter sparseCopy(BloomFilter<SparseBloomFilter> filter) { return filter.copy(); }"
                + "ArrayCountingBloomFilter copy(CountingBloomFilter<ArrayCountingBloomFilter> filter) { return filter.copy(); }"
                + "<T extends WrappedBloomFilter<T>> T wrappedCopy(WrappedBloomFilter<T> filter) { return filter.copy(); }"
                + "LayeredBloomFilter<SimpleBloomFilter> copy(LayeredBloomFilter<SimpleBloomFilter> filter) { return filter.copy(); }"));
    }
}
