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
package org.narc.results

import org.narc.rule.Violation
import org.narc.rule.StubRule
import org.narc.test.AbstractTest

/**
 * Tests for ReportResults
 *
 * @author Chris Mair
 * @version $Revision: 182 $ - $Date: 2008-11-30 21:37:49 -0500 (Sun, 30 Nov 2008) $
 */
class ReportResultsTest extends AbstractTest {

    static final VIOLATION1 = new Violation(rule:new StubRule(1))
    static final VIOLATION2 = new Violation(rule:new StubRule(2))
    static final VIOLATION3 = new Violation(rule:new StubRule(3))
    static final SRC_DIR1 = "/src/app1"
    static final SRC_DIR2 = "/src/app2/test"

    void testNoResults() {
        def results = new ReportResults()
        assert results.sourceDirectories == []
        assert results.getViolationsWithPriority(1) == []
        assert results.getViolationsWithPriority(2) == []
        assert results.getViolationsWithPriority(3) == []
        assert results.totalNumberOfFiles == 0
        assert results.numberOfFilesWithViolations == 0
    }

    void testWithOneSourceDirectory() {
        def results = new ReportResults()
        def fileResults = new FileResults([VIOLATION1, VIOLATION3, VIOLATION3, VIOLATION1, VIOLATION2])
        results.addResults(SRC_DIR1, fileResults)
        assert results.sourceDirectories == [SRC_DIR1]
        assert results.getViolationsWithPriority(1) == [VIOLATION1, VIOLATION1]
        assert results.getViolationsWithPriority(2) == [VIOLATION2]
        assert results.getViolationsWithPriority(3) == [VIOLATION3, VIOLATION3]
        assert results.totalNumberOfFiles == 1
        assert results.numberOfFilesWithViolations == 1
    }

    void testWithMultipleSourceDirectories() {
        def results = new ReportResults()
        def fileResults1 = new FileResults([VIOLATION1, VIOLATION3, VIOLATION3, VIOLATION1, VIOLATION2])
        def fileResults2 = new FileResults([VIOLATION2, VIOLATION3])
        results.addResults(SRC_DIR1, fileResults1)
        results.addResults(SRC_DIR2, fileResults2)
        assert results.sourceDirectories == [SRC_DIR1, SRC_DIR2]
        assert results.getViolationsWithPriority(1) == [VIOLATION1, VIOLATION1]
        assert results.getViolationsWithPriority(2) == [VIOLATION2, VIOLATION2]
        assert results.getViolationsWithPriority(3) == [VIOLATION3, VIOLATION3, VIOLATION3]
        assert results.totalNumberOfFiles == 2
        assert results.numberOfFilesWithViolations == 2
    }

}