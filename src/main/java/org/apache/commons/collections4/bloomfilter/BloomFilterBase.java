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

/**
 * Common operations on Bloom filters with unrelated copy types.
 *
 * @since 4.5.0
 */
public interface BloomFilterBase extends IndexExtractor, BitMapExtractor {

    /** @return the number of enabled bits. */
    int cardinality();

    /** @return the filter characteristics. */
    int characteristics();

    /** Resets the filter to its initial, unpopulated state. */
    void clear();

    /**
     * @param other the bit maps to test.
     * @return whether all specified bits are enabled.
     */
    boolean contains(BitMapExtractor other);

    /**
     * @param other the filter to test.
     * @return whether all specified bits are enabled.
     */
    boolean contains(BloomFilter<?> other);

    /**
     * @param hasher the hasher to test.
     * @return whether all specified bits are enabled.
     */
    boolean contains(Hasher hasher);

    /**
     * @param other the indices to test.
     * @return whether all specified bits are enabled.
     */
    boolean contains(IndexExtractor other);

    /** @return an independent copy of the same type. */
    BloomFilterBase copy();

    /**
     * @param other the filter to intersect.
     * @return the estimated number of items in the intersection.
     */
    int estimateIntersection(BloomFilter<?> other);

    /** @return the estimated number of items in the filter. */
    int estimateN();

    /**
     * @param other the filter to combine.
     * @return the estimated number of items in the union.
     */
    int estimateUnion(BloomFilter<?> other);

    /** @return the shape used to build this filter. */
    Shape getShape();

    /** @return whether all bits are disabled. */
    boolean isEmpty();

    /** @return whether all bits are enabled. */
    boolean isFull();

    /**
     * @param other the bit maps to merge.
     * @return whether the merge was successful.
     */
    boolean merge(BitMapExtractor other);

    /**
     * @param other the filter to merge.
     * @return whether the merge was successful.
     */
    boolean merge(BloomFilter<?> other);

    /**
     * @param hasher the hasher to merge.
     * @return whether the merge was successful.
     */
    boolean merge(Hasher hasher);

    /**
     * @param other the indices to merge.
     * @return whether the merge was successful.
     */
    boolean merge(IndexExtractor other);
}
