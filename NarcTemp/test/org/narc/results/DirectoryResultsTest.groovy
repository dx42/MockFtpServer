package org.narc.results

import org.narc.rule.Violation
import org.narc.rule.StubRule
import org.narc.test.AbstractTest
/*
 * Copyright 2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Tests for DirectoryResults 
 *
 * @author Chris Mair
 * @version $Revision: 182 $ - $Date: 2008-11-30 21:37:49 -0500 (Sun, 30 Nov 2008) $
 */
class DirectoryResultsTest extends AbstractTest {

    static VIOLATION1 = new Violation(rule:new StubRule(1))
    static VIOLATION2 = new Violation(rule:new StubRule(2))
    static VIOLATION3 = new Violation(rule:new StubRule(3))

    void testNoChildren() {
        def results = new DirectoryResults()
        assert results.children == []
        assert results.getViolationsWithPriority(1) == []
        assert results.getViolationsWithPriority(2) == []
        assert results.getViolationsWithPriority(3) == []
        assert results.totalNumberOfFiles == 0
        assert results.numberOfFilesWithViolations == 0
    }

    void testWithOneChild() {
        def results = new DirectoryResults(totalNumberOfFiles:7)
        def fileResults = new FileResults([VIOLATION1, VIOLATION3, VIOLATION3, VIOLATION1, VIOLATION2])
        results.addChild(fileResults)
        assert results.children == [fileResults]
        assert results.getViolationsWithPriority(1) == [VIOLATION1, VIOLATION1]
        assert results.getViolationsWithPriority(2) == [VIOLATION2]
        assert results.getViolationsWithPriority(3) == [VIOLATION3, VIOLATION3]
        assert results.totalNumberOfFiles == 7
        assert results.numberOfFilesWithViolations == 1
    }

    void testWithMultipleChildren() {
        def results = new DirectoryResults(totalNumberOfFiles:9)
        def fileResults1 = new FileResults([VIOLATION1, VIOLATION3, VIOLATION3, VIOLATION1, VIOLATION2])
        def fileResults2 = new FileResults([VIOLATION2, VIOLATION3])
        results.addChild(fileResults1)
        results.addChild(fileResults2)
        assert results.children == [fileResults1, fileResults2]
        assert results.getViolationsWithPriority(1) == [VIOLATION1, VIOLATION1]
        assert results.getViolationsWithPriority(2) == [VIOLATION2, VIOLATION2]
        assert results.getViolationsWithPriority(3) == [VIOLATION3, VIOLATION3, VIOLATION3]
        assert results.totalNumberOfFiles == 9
        assert results.numberOfFilesWithViolations == 2
    }

}