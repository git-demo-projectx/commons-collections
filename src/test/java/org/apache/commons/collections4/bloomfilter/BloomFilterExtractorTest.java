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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link BloomFilterExtractor}.
 */
public class BloomFilterExtractorTest {

    @Test
    public void testArrayFactoryWithCopies() {
        final Shape shape = Shape.fromKM(3, 72);
        final SimpleBloomFilter simple = new SimpleBloomFilter(shape);
        simple.merge(IndexExtractor.fromIndexArray(1));
        final SparseBloomFilter sparse = new SparseBloomFilter(shape);
        sparse.merge(IndexExtractor.fromIndexArray(2, 3));
        final BloomFilterExtractor extractor = predicate -> predicate.test(simple) && predicate.test(sparse);
        final BloomFilter<?>[] result = extractor.asBloomFilterArray(BloomFilter<?>[]::new);
        assertEquals(2, result.length);
        assertEquals(SimpleBloomFilter.class, result[0].getClass());
        assertEquals(SparseBloomFilter.class, result[1].getClass());
        assertNotSame(simple, result[0]);
        assertNotSame(sparse, result[1]);
        assertArrayEquals(simple.asBitMapArray(), result[0].asBitMapArray());
        assertArrayEquals(sparse.asBitMapArray(), result[1].asBitMapArray());
        result[0].clear();
        result[1].clear();
        assertEquals(1, simple.cardinality());
        assertEquals(2, sparse.cardinality());
        assertThrows(ArrayStoreException.class, () -> extractor.asBloomFilterArray(SimpleBloomFilter[]::new));
    }

    @Test
    public void testArrayFactoryWithReferences() {
        final Shape shape = Shape.fromKM(3, 72);
        final SimpleBloomFilter simple = new SimpleBloomFilter(shape);
        final SparseBloomFilter sparse = new SparseBloomFilter(shape);
        final BloomFilter<?>[] filters = {simple, sparse};
        final BloomFilterExtractor extractor = BloomFilterExtractor.fromBloomFilterArray(filters);
        final BloomFilter<?>[] result = extractor.asBloomFilterArray(BloomFilter<?>[]::new);
        assertNotSame(filters, result);
        assertEquals(2, result.length);
        assertSame(simple, result[0]);
        assertSame(sparse, result[1]);
        result[0].merge(IndexExtractor.fromIndexArray(1));
        assertEquals(1, simple.cardinality());
        result[0] = sparse;
        assertSame(simple, filters[0]);
        assertThrows(ArrayStoreException.class, () -> extractor.asBloomFilterArray(SimpleBloomFilter[]::new));
        final SimpleBloomFilter[] typed = BloomFilterExtractor.fromBloomFilterArray(simple)
                .asBloomFilterArray(SimpleBloomFilter[]::new);
        assertSame(simple, typed[0]);
        assertEquals(0, BloomFilterExtractor.fromBloomFilterArray().asBloomFilterArray(BloomFilter<?>[]::new).length);
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
