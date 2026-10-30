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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BloomFilterCopyTest {

    @Test
    public void testCopyThroughInterfaces() {
        final Shape shape = Shape.fromNP(10000, 0.01);
        final BloomFilter<SimpleBloomFilter> simple = new SimpleBloomFilter(shape);
        final SimpleBloomFilter simpleCopy = simple.copy();
        assertEquals(simple.getShape(), simpleCopy.getShape());

        final BloomFilter<SparseBloomFilter> sparse = new SparseBloomFilter(shape);
        final SparseBloomFilter sparseCopy = sparse.copy();
        assertEquals(sparse.getShape(), sparseCopy.getShape());

        final CountingBloomFilter<ArrayCountingBloomFilter> counting = new ArrayCountingBloomFilter(shape);
        final ArrayCountingBloomFilter countingCopy = counting.copy();
        assertEquals(counting.getShape(), countingCopy.getShape());

        final LayeredBloomFilter<SimpleBloomFilter> layered = LayeredBloomFilterTest.fixed(shape, 2);
        final BloomFilter<LayeredBloomFilter<SimpleBloomFilter>> filter = layered;
        final LayeredBloomFilter<SimpleBloomFilter> layeredCopy = filter.copy();
        assertEquals(layered.getDepth(), layeredCopy.getDepth());

        final WrappedBloomFilter<WrappedBloomFilterTest.Fixture> wrapped = new WrappedBloomFilterTest.Fixture(simple);
        final WrappedBloomFilterTest.Fixture wrappedCopy = wrapped.copy();
        assertEquals(wrapped.getShape(), wrappedCopy.getShape());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SparseBloomFilter copy = new SimpleBloomFilter(shape).copy();",
        "BloomFilter<SimpleBloomFilter> filter = new SimpleBloomFilter(shape); SparseBloomFilter copy = filter.copy();",
        "CountingBloomFilter<ArrayCountingBloomFilter> filter = new ArrayCountingBloomFilter(shape); SimpleBloomFilter copy = filter.copy();"
    })
    public void testCopyRejectsUnrelatedType(final String statement) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        final String source = "import org.apache.commons.collections4.bloomfilter.*; "
                + "class InvalidCopy { void test(Shape shape) { " + statement + " } }";
        final JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///InvalidCopy.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
                return source;
            }
        };
        final StringWriter output = new StringWriter();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null)) {
            assertFalse(compiler.getTask(output, manager, diagnostics,
                    Arrays.asList("-proc:none", "-classpath", System.getProperty("java.class.path")),
                    null, Collections.singletonList(file)).call(), output.toString());
            assertTrue(diagnostics.getDiagnostics().stream()
                    .anyMatch(diagnostic -> "compiler.err.prob.found.req".equals(diagnostic.getCode())),
                    diagnostics.getDiagnostics().toString());
        }
    }
}
