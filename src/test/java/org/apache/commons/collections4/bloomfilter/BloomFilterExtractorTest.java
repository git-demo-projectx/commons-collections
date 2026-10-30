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
    public void testMixedFilterArrayReferencesAndCopies() {
        final Shape shape = Shape.fromKM(2, 64);
        final SimpleBloomFilter simple = new SimpleBloomFilter(shape);
        final SparseBloomFilter sparse = new SparseBloomFilter(shape);
        simple.merge(IndexExtractor.fromIndexArray(1));
        sparse.merge(IndexExtractor.fromIndexArray(2));
        final BloomFilter<?>[] original = {simple, sparse};
        final BloomFilterExtractor references = BloomFilterExtractor.fromBloomFilterArray(original);
        final BloomFilterState[] referenced = references.asBloomFilterArray();
        assertNotSame(original, referenced);
        assertSame(simple, referenced[0]);
        assertSame(sparse, referenced[1]);

        final BloomFilterExtractor copies = references::processBloomFilters;
        final BloomFilterState[] copied = copies.asBloomFilterArray();
        assertNotSame(simple, copied[0]);
        assertNotSame(sparse, copied[1]);
        assertEquals(SimpleBloomFilter.class, copied[0].getClass());
        assertEquals(SparseBloomFilter.class, copied[1].getClass());
        assertArrayEquals(simple.asBitMapArray(), copied[0].asBitMapArray());
        assertArrayEquals(sparse.asBitMapArray(), copied[1].asBitMapArray());
        copied[0].clear();
        copied[1].clear();
        assertEquals(1, simple.cardinality());
        assertEquals(1, sparse.cardinality());
        assertArrayEquals(new int[] {1, 2}, copies.flatten().asIndexArray());
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
