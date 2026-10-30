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

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class BloomFilterExtractorFromBloomFilterArrayTest extends AbstractBloomFilterExtractorTest {

    @Override
    protected BloomFilterExtractor createUnderTest(final BloomFilter<?>... filters) {
        return BloomFilterExtractor.fromBloomFilterArray(filters);
    }

    @Test
    void testAsBloomFilterArrayRetainsReferences() {
        final SimpleBloomFilter simple = new SimpleBloomFilter(getTestShape());
        final SparseBloomFilter sparse = new SparseBloomFilter(getTestShape());
        final BloomFilter<?>[] originals = {simple, sparse};
        final BloomFilterExtractor extractor = createUnderTest(originals);
        final BloomFilter<?>[] result = extractor.asBloomFilterArray(new BloomFilter<?>[0]);
        assertNotSame(originals, result);
        assertSame(simple, result[0]);
        assertSame(sparse, result[1]);
        assertThrows(ArrayStoreException.class, () -> extractor.asBloomFilterArray(new SimpleBloomFilter[0]));
    }
}
