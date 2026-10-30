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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link BloomFilterExtractor}.
 */
public class BloomFilterExtractorTest {

    @Test
    public void testArrayFactoryPreservesReferences() {
        final Shape shape = Shape.fromKM(17, 72);
        final SimpleBloomFilter simple = new SimpleBloomFilter(shape);
        final SparseBloomFilter sparse = new SparseBloomFilter(shape);
        final BloomFilter<?>[] filters = {simple, sparse};
        final BloomFilter<?>[] result = BloomFilterExtractor.fromBloomFilterArray(filters)
                .asBloomFilterArray(BloomFilter<?>[]::new);
        assertNotSame(filters, result);
        assertSame(simple, result[0]);
        assertSame(sparse, result[1]);
        final SimpleBloomFilter[] typed = BloomFilterExtractor.fromBloomFilterArray(simple)
                .asBloomFilterArray(SimpleBloomFilter[]::new);
        assertSame(simple, typed[0]);
        assertThrows(ArrayStoreException.class, () -> BloomFilterExtractor.fromBloomFilterArray(filters)
                .asBloomFilterArray(SimpleBloomFilter[]::new));
    }

    @Test
    public void testDefaultArrayFactoryCopiesFilters() {
        final Shape shape = Shape.fromKM(17, 72);
        final SimpleBloomFilter simple = new SimpleBloomFilter(shape);
        final SparseBloomFilter sparse = new SparseBloomFilter(shape);
        simple.merge(IndexExtractor.fromIndexArray(1));
        sparse.merge(IndexExtractor.fromIndexArray(2));
        final BloomFilterExtractor extractor = predicate -> predicate.test(simple) && predicate.test(sparse);
        final BloomFilter<?>[] result = extractor.asBloomFilterArray(BloomFilter<?>[]::new);
        assertNotSame(simple, result[0]);
        assertNotSame(sparse, result[1]);
        assertArrayEquals(simple.asBitMapArray(), result[0].asBitMapArray());
        assertArrayEquals(sparse.asBitMapArray(), result[1].asBitMapArray());
        result[0].clear();
        result[1].clear();
        assertArrayEquals(new int[] {1}, simple.asIndexArray());
        assertArrayEquals(new int[] {2}, sparse.asIndexArray());
        final BloomFilterExtractor homogeneous = predicate -> predicate.test(simple);
        final SimpleBloomFilter[] typed = homogeneous.asBloomFilterArray(SimpleBloomFilter[]::new);
        assertNotSame(simple, typed[0]);
        assertArrayEquals(simple.asBitMapArray(), typed[0].asBitMapArray());
        assertThrows(ArrayStoreException.class, () -> extractor.asBloomFilterArray(SimpleBloomFilter[]::new));
    }

    @Test
    public void testFlattenEmpty() {
        assertThrows(NullPointerException.class, () -> BloomFilterExtractor.fromBloomFilterArray().flatten());
    }

    @Test
    public void testFromBloomFilterArray() {
        assertThrows(NullPointerException.class, () -> BloomFilterExtractor.fromBloomFilterArray((BloomFilter<?>[]) null));
    }

}
