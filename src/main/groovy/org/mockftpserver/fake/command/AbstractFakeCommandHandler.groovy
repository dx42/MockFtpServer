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
package org.mockftpserver.fake.command

import org.mockftpserver.fake.ServerConfigurationAware
import org.mockftpserver.fake.ServerConfiguration
import org.mockftpserver.core.session.Sessionimport org.apache.log4j.Loggerimport org.apache.log4j.Loggerimport java.text.MessageFormatimport org.mockftpserver.core.command.Command

/**
 * Abstract superclass for CommandHandler classes for the "Fake" server.
 */
abstract class AbstractFakeCommandHandler implements ServerConfigurationAware {

     final Logger LOG = Logger.getLogger(this.class)
     ServerConfiguration serverConfiguration
     
     /**
      * Use template method to centralize and ensure common validation
      */
     protected final void handleCommand(Command command, Session session) {
         assert serverConfiguration != null
         assert command != null
         assert session != null
             
         handle(command, session)
     }
     
     /**
      * Subclasses must implement this
      */
     protected abstract void handle(Command command, Session session)
     
     // -------------------------------------------------------------------------
     // Utility methods for subclasses
     // -------------------------------------------------------------------------

     /**
      * Send a reply for this command on the control connection. 
      * 
      * The reply code is designated by the <code>replyCode</code> property, and the reply text  
      * is retrieved from the <code>replyText</code> ResourceBundle, using the reply code as the key.
      * 
      * @param session - the Session
      * @param replyCode - the reply code
      * 
      * @throws AssertionError - if session is null
      * 
      * @see MessageFormat
      */
     protected void sendReply(Session session, int replyCode) {
         assert session
         assertValidReplyCode(replyCode);

         //String key = (replyMessageKey != null) ? replyMessageKey : Integer.toString(replyCode);
         String key = Integer.toString(replyCode);
         //String text = getTextForReplyCode(replyCode, key, null);
         String text = serverConfiguration.getTextForReplyCode(replyCode)
         String replyTextToLog = (text == null) ? "" : " " + text;
         LOG.debug("Sending reply [" + replyCode + replyTextToLog + "]");
         session.sendReply(replyCode, text);
     }
     
      /**
       * Return the text for the specified reply code, formatted using the message arguments, if
       * supplied. Return the text mapped to the code from the replyText ResourceBundle. If the 
       * ResourceBundle contains no mapping, then return null.
       * <p>
       * If arguments is not null, then the returned reply text if formatted using the
       * {@link MessageFormat} class.
       * 
       * @param code - the reply code
       * @param messageKey - the key used to retrieve the reply text from the replyTextBundle
       * @param arguments - the array of arguments to be formatted and substituted within the reply
       *        text; may be null
       * @return the text for the reply code; may be null
       */
      private String getTextForReplyCode(int code, String messageKey, Object[] arguments) {
          try {
              String t = serverConfiguration.getTextForReplyCode(code)
              String formattedMessage = MessageFormat.format(t, arguments);
              return (formattedMessage == null) ? null : formattedMessage.trim();
          }
          catch (MissingResourceException e) {
              // No reply text is mapped for the specified key
              LOG.warn("No reply text defined for reply code [" + code + "]");
              return null;
          }
      }

     /**
      * Assert that the specified number is a valid reply code
      * @param replyCode - the reply code to check
      * 
      * @throws AssertionError - if the replyCode is invalid
      */
     protected void assertValidReplyCode(int replyCode) {
         assert replyCode > 0, "The number [" + replyCode + "] is not a valid reply code"
     }
     
      
}