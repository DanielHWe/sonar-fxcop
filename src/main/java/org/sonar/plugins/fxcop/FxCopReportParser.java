/*
 * SonarQube FxCop Library
 * Copyright (C) 2014-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.fxcop;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class FxCopReportParser {

  public List<FxCopIssue> parse(File file) {
    return new Parser().parse(file);
  }

  private static class Parser {

    private File file;
    private XMLStreamReader stream;
    private final ImmutableList.Builder<FxCopIssue> filesBuilder = ImmutableList.builder();
    private String ruleConfigKey;
    private boolean isSuppressed = false;

    public List<FxCopIssue> parse(File file) {
      this.file = file;

      XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

      try (InputStream is = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(is, Charsets.UTF_8)) {
        stream = xmlFactory.createXMLStreamReader(reader);

        while (stream.hasNext()) {
          if (stream.next() == XMLStreamConstants.START_ELEMENT) {
            parseStartElement();
          }
        }
      } catch (IOException | XMLStreamException e) {
        throw Throwables.propagate(e);
      } finally {
        closeXmlStream();
      }

      return filesBuilder.build();
    }

    private void parseStartElement() throws XMLStreamException {
      String tagName = stream.getLocalName();

      if ("Message".equals(tagName)) {
        handleMessageTag();
      } else if (!isSuppressed && "Issue".equals(tagName)) {
        handleIssueTag();
      }
    }

    private void closeXmlStream() {
      if (stream != null) {
        try {
          stream.close();
        } catch (XMLStreamException e) {
          throw Throwables.propagate(e);
        }
      }
    }

    private void handleMessageTag() {
      this.ruleConfigKey = getRequiredAttribute("CheckId");
      this.isSuppressed = "ExcludedInSource".equals(getAttribute("Status"));
    }

    private void handleIssueTag() throws XMLStreamException {
      String path = getAttribute("Path");
      String fileAttribute = getAttribute("File");
      Integer line = getIntAttribute("Line");
      String message = stream.getElementText();
      filesBuilder.add(new FxCopIssue(stream.getLocation().getLineNumber(), ruleConfigKey, path, fileAttribute, line, message));
    }

    private String getRequiredAttribute(String name) {
      String value = getAttribute(name);
      if (value == null) {
        throw parseError("Missing attribute \"" + name + "\" in element <" + stream.getLocalName() + ">");
      }

      return value;
    }

    @Nullable
    private Integer getIntAttribute(String name) {
      String value = getAttribute(name);

      if (value == null) {
        return null;
      }

      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        throw parseError("Expected an integer instead of \"" + value + "\" for the attribute \"" + name + "\"");
      }
    }

    @Nullable
    private String getAttribute(String name) {
      for (int i = 0; i < stream.getAttributeCount(); i++) {
        if (name.equals(stream.getAttributeLocalName(i))) {
          return stream.getAttributeValue(i);
        }
      }

      return null;
    }

    private ParseErrorException parseError(String message) {
      return new ParseErrorException(message + " in " + file.getAbsolutePath() + " at line " + stream.getLocation().getLineNumber());
    }

  }

  private static class ParseErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ParseErrorException(String message) {
      super(message);
    }

  }

}
