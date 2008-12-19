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
package org.narc.rule

import org.narc.source.SourceCode

/**
 * Represents a source code analysis rule 
 *
 * @author Chris Mair
 * @version $Revision: 182 $ - $Date: 2008-11-30 21:37:49 -0500 (Sun, 30 Nov 2008) $
 */
interface Rule {

    /**
     * Apply this rule to the specified source and return a list of violations (or an empty List)
     * @param source - the source to apply this rule to
     * @return the List of violations; may be empty 
     */
    List applyTo(SourceCode sourceCode)

    /**
     * @return the priority of this rule; must be 1, 2 or 3
     */
    int getPriority()
}