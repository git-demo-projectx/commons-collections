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
    public void testAsBloomFilterArrayMixedTypes() {
        final Shape shape = Shape.fromKM(3, 72);
        final SimpleBloomFilter simple = new SimpleBloomFilter(shape);
        final SparseBloomFilter sparse = new SparseBloomFilter(shape);
        simple.merge(IndexExtractor.fromIndexArray(1));
        sparse.merge(IndexExtractor.fromIndexArray(2, 3));
        final BloomFilter<?>[] filters = {simple, sparse};
        final BloomFilterExtractor references = BloomFilterExtractor.fromBloomFilterArray(filters);
        final BloomFilter<?>[] referenced = references.asBloomFilterArray();
        assertNotSame(filters, referenced);
        assertSame(simple, referenced[0]);
        assertSame(sparse, referenced[1]);

        final BloomFilterExtractor copies = references::processBloomFilters;
        final BloomFilter<?>[] copied = copies.asBloomFilterArray();
        assertEquals(filters.length, copied.length);
        for (int i = 0; i < filters.length; i++) {
            assertNotSame(filters[i], copied[i]);
            assertEquals(filters[i].getClass(), copied[i].getClass());
            assertArrayEquals(filters[i].asBitMapArray(), copied[i].asBitMapArray());
            copied[i].clear();
        }
        assertEquals(1, simple.cardinality());
        assertEquals(2, sparse.cardinality());
    }

    @Test
    public void testFlattenMixedTypes() {
        final Shape shape = Shape.fromKM(3, 72);
        final SimpleBloomFilter simple = new SimpleBloomFilter(shape);
        final SparseBloomFilter sparse = new SparseBloomFilter(shape);
        simple.merge(IndexExtractor.fromIndexArray(1));
        sparse.merge(IndexExtractor.fromIndexArray(2, 3));
        final BloomFilter<?> flattened = BloomFilterExtractor.fromBloomFilterArray(simple, sparse).flatten();
        assertEquals(SimpleBloomFilter.class, flattened.getClass());
        assertArrayEquals(new int[] {1, 2, 3}, flattened.asIndexArray());
        flattened.clear();
        assertEquals(1, simple.cardinality());
        assertEquals(2, sparse.cardinality());
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
