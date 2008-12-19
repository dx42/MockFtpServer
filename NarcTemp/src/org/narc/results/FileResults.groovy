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

/**
 * Represents the results of applying a set of rules against a single sourcefile
 *
 * @author Chris Mair
 * @version $Revision: 182 $ - $Date: 2008-11-30 21:37:49 -0500 (Sun, 30 Nov 2008) $
 */
class FileResults implements Results {

    private List violations

    FileResults(List violations) {
        this.violations = violations
    }

    /**
     * @return the List of violations with the specified priority; may be empty
     */
    List getViolationsWithPriority(int priority) {
        return violations.findAll { violation -> violation.rule.priority == priority}
    }

    /**
     * @return the total number of files (with or without violations)
     */
    int getTotalNumberOfFiles() {
        return 1
    }

    /**
     * @return the number of files containing violations
     */
    int getNumberOfFilesWithViolations() {
        return violations.empty ? 0 : 1
    }

    String toString() {
        return"FileResults $violations"
    }
}