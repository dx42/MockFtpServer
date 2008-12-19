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
package org.narc.report

import org.narc.rule.Violation
import org.narc.rule.StubRule
import org.narc.results.FileResults
import org.narc.test.AbstractTest
import org.narc.results.ReportResults

/**
 * Tests for HtmlReportWriter
 *
 * @author Chris Mair
 * @version $Revision: 182 $ - $Date: 2008-11-30 21:37:49 -0500 (Sun, 30 Nov 2008) $
 */
class HtmlReportWriterTest extends AbstractTest {

    static VIOLATION1 = new Violation(rule:new StubRule(1))
    static VIOLATION2 = new Violation(rule:new StubRule(2))
    static VIOLATION3 = new Violation(rule:new StubRule(3))
    static OUTPUT_DIR = "."
    static REPORT_FILENAME = "HtmlReport.html"

    def reportWriter

    void testWriteOutReport() {
        def fileResults = new FileResults([VIOLATION1, VIOLATION3, VIOLATION3, VIOLATION1, VIOLATION2])
        def results = new ReportResults()
        results.addResults('/src/main', fileResults)
        reportWriter.writeOutReport(results)

        def reportText = new File('NarcReport.html').text
        assertContainsAll reportText, ['html']
    }

    void setUp() {
        super.setUp()
        reportWriter = new HtmlReportWriter()

    }

}