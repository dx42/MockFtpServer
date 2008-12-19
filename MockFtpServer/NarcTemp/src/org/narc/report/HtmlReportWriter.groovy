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

import groovy.xml.StreamingMarkupBuilder
import java.text.SimpleDateFormat
import org.narc.results.CompositeResults


/**
 * ReportWriter that generates an HTML report
 *
 * @author Chris Mair
 * @version $Revision: 182 $ - $Date: 2008-11-30 21:37:49 -0500 (Sun, 30 Nov 2008) $
 */
class HtmlReportWriter implements ReportWriter {

    static final DEFAULT_REPORT_FILENAME = 'NarcReport.html'

    String title = "Sample Project"

    /**
     * Write out a report for the specified analysis results
     * @param results - the analysis results
     */
    public void writeOutReport(CompositeResults results) {
        def builder = new StreamingMarkupBuilder()
        def reportFile = new File(DEFAULT_REPORT_FILENAME)
        reportFile.withWriter { writer ->
            def html = builder.bind() {
                html {
                    out << headerSection
                    out << buildBodySection(results)
                }
            }
            writer << html
        }
    }

    private headerSection = {
        head {
            title(buildTitle())
        }
    }

    private buildBodySection(results) {
        return {
            body {
                h1(buildTitle())
                out << reportTimestamp
                out << buildSourceDirectories(results)
                out << buildSummaryByPackage(results)
            }
        }
    }

    private reportTimestamp = {
        def timestamp = new SimpleDateFormat('MM/dd/yyyy hh:mmaa').format(new Date())
        p("Report timestamp: $timestamp")
    }

    private buildSourceDirectories(results) {
        return {
            p("Source Directories:")
            ul {
                results.sourceDirectories.each { sourceDir ->
                    li(sourceDir)
                }
            }
        }
    }

    private buildSummaryByPackage(results) {
        return {
            h2("Summary by Package")
            table {
                tr {
                    th('Package')
                    th('Total Files')
                    th('Files with Violations')
                    th('Priority 1')
                    th('Priority 2')
                    th('Priority 3')
                }

                tr {
                    td('All Packages')
                    td(results.totalNumberOfFiles)
                    td(results.numberOfFilesWithViolations)
                    td(results.getViolationsWithPriority(1).size())
                    td(results.getViolationsWithPriority(2).size())
                    td(results.getViolationsWithPriority(3).size())
                }

            }
        }
    }

    private String buildTitle() {
        return "Narc Report: $title"
    }

}