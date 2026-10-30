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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BloomFilterCopyTest {

    @TempDir
    Path directory;

    private int compile(final String body, final ByteArrayOutputStream diagnostics) throws IOException {
        final Path source = directory.resolve("CopyCheck.java");
        Files.write(source, ("package org.apache.commons.collections4.bloomfilter; "
                + "class CopyCheck { " + body + " }").getBytes(StandardCharsets.UTF_8));
        return ToolProvider.getSystemJavaCompiler().run(null, diagnostics, diagnostics,
                "-source", "8", "-target", "8", "-classpath", System.getProperty("java.class.path"),
                "-d", directory.toString(), source.toString());
    }

    @Test
    void testCopyPreservesType() throws IOException {
        final ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        assertEquals(0, compile(
                "SimpleBloomFilter simple(BloomFilter<SimpleBloomFilter> f) { return f.copy(); } "
                + "SparseBloomFilter sparse(BloomFilter<SparseBloomFilter> f) { return f.copy(); } "
                + "ArrayCountingBloomFilter counting(CountingBloomFilter<ArrayCountingBloomFilter> f) { return f.copy(); } "
                + "LayeredBloomFilter<SimpleBloomFilter> layered(BloomFilter<LayeredBloomFilter<SimpleBloomFilter>> f) { return f.copy(); } "
                + "WrappedBloomFilterTest.Fixture wrapped(WrappedBloomFilter<WrappedBloomFilterTest.Fixture> f) { return f.copy(); }",
                diagnostics), diagnostics::toString);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SparseBloomFilter f = new SimpleBloomFilter(Shape.fromNP(10000, 0.01)).copy();",
        "SparseBloomFilter wrong(BloomFilter<SimpleBloomFilter> f) { return f.copy(); }",
        "SimpleBloomFilter wrong(CountingBloomFilter<ArrayCountingBloomFilter> f) { return f.copy(); }",
        "SimpleBloomFilter wrong(WrappedBloomFilter<WrappedBloomFilterTest.Fixture> f) { return f.copy(); }",
        "LayeredBloomFilter<SparseBloomFilter> wrong(BloomFilter<LayeredBloomFilter<SimpleBloomFilter>> f) { return f.copy(); }"
    })
    void testCopyRejectsIncompatibleType(final String body) throws IOException {
        final ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        assertNotEquals(0, compile(body, diagnostics), diagnostics::toString);
    }
}
