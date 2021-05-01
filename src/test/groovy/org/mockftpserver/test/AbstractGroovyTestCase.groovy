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
package org.mockftpserver.test


import org.mockftpserver.test.LoggingUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Abstract superclass for Groovy tests
 *
 * @author Chris Mair
 */
abstract class AbstractGroovyTestCase {

    protected final Logger LOG = LoggerFactory.getLogger(this.class)
    private LoggingUtil testLogger

    /**
     * Write out the specified log message, prefixing with the current class name.
     * @param message - the message to log; toString() is applied first
     */
    protected void log(message) {
        println "[${classNameNoPackage()}] ${message.toString()}"
    }

    private String classNameNoPackage() {
        def className = getClass().name
        def index = className.lastIndexOf('.')
        return index > -1 ? className.substring(index+1) : className
    }
    
    /**
     * Assert that the specified code throws an exception with an error message
     * containing the specified text.
     * @param text - the text expected within the exception message
     * @param code - the Closure containing the code to be executed, which is expected to throw an exception of the specified type
     * @return the thrown Exception instance
     *
     * @throws AssertionError - if no exception is thrown by the code or if the thrown
     * 	exception message does not contain the expected text
     */
    protected Throwable shouldFailWithMessageContaining(String text, Closure code) {
        try {
            code.call()
        } catch(Throwable t) {
            def message = t.message
            assert message.contains(text), "message=[$message], text=[$text]"
            return t
        }
        assert false, "No exception thrown"
    }

    protected Throwable shouldFail(Closure code) {
        try {
            code.call()
        } catch(Throwable t) {
            return t
        }
        assert false, "No exception thrown"
    }

    protected Throwable shouldFail(Class theClass, Closure code) {
        try {
            code.call()
        } catch(Throwable t) {
            assert theClass.isAssignableFrom(t.class)
            return t
        }
        assert false, "No exception thrown"
    }

    /**
     * Return the specified paths concatenated with the path separator in between
     * @param paths - the varargs list of path components to concatenate
     * @return p[0] + '/' + p[1] + '/' + p[2] + ...
     */
    protected static String p(String[] paths) {
        return paths.join("/").replace('\\', '/').replace("//", "/")
    }

    /**
     * Create a new InetAddress from the specified host String, using the
     * {@link InetAddress#getByName(String)}   method.
     * @param host
     * @return an InetAddress for the specified host
     */
    protected static InetAddress inetAddress(String host) {
        return InetAddress.getByName(host);
    }

}