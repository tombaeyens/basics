/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ai.shape.basics.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static ai.shape.basics.util.Exceptions.exceptionWithCause;

public class Io {

  public static final Charset UTF8 = Charset.forName("UTF-8");
  public static final Charset UTF16 = Charset.forName("UTF-16");

  public static String getString(InputStream inputStream) {
    return getString(inputStream, UTF8);
  }

  public static String getString(InputStream inputStream, String charset) {
    return getString(inputStream, Charset.forName(charset));
  }

  public static String getString(InputStream inputStream, Charset charset) {
    if (inputStream==null) {
      return null;
    }
    Scanner scanner = new Scanner(inputStream, charset.toString());
    scanner.useDelimiter("\\A");
    try {
      if (scanner.hasNext()) {
        return scanner.next();
      } else {
        return "";
      } 
    } finally {
      scanner.close();
    }
  }

  public static String getString(Reader reader) {
    if (reader==null) {
      return null;
    }
    char[] charBuffer = new char[8 * 1024];
    StringBuilder stringBuilder = new StringBuilder();
    int numCharsRead;
    try {
      while ((numCharsRead = reader.read(charBuffer, 0, charBuffer.length)) != -1) {
        stringBuilder.append(charBuffer, 0, numCharsRead);
      }
    } catch (IOException e) {
      throw new RuntimeException("Couldn't read from reader: "+e.toString(), e);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return stringBuilder.toString();
  }


  public static Reader getResourceAsReader(String resource) {
    return getResourceAsReader(resource, UTF8);
  }

  public static Reader getResourceAsReader(String resource, Charset charset) {
    return new InputStreamReader(getResourceAsStream(resource), charset);
  }

  public static String getResourceAsString(String resource) {
    return getResourceAsString(resource, UTF8);
  }

  public static String getResourceAsString(String resource, Charset charset) {
    InputStream resourceStream = getResourceAsStream(resource);
    RuntimeException exception = null;
    try {
      return getString(resourceStream, charset);
    } catch (RuntimeException e) {
      exception = e;
      throw e;
    } finally {
      closeResourceStream(resourceStream, exception);
    }
  }

  public static byte[] getResourceAsBytes(String resource) {
    if (resource==null) {
      return null;
    }
    InputStream resourceStream = getResourceAsStream(resource);
    if (resourceStream==null) {
      return null;
    }
    RuntimeException exception = null;
    try {
      return getBytes(resourceStream);
    } catch (RuntimeException e) {
      exception = e;
      throw e;
    } finally {
      closeResourceStream(resourceStream, exception);
    }
  }

  public static InputStream getResourceAsStream(String resource) {
    return Io.class.getClassLoader().getResourceAsStream(resource);
  }

  private static void closeResourceStream(InputStream resourceStream, Exception cause) {
    try {
      if (resourceStream!=null) {
        resourceStream.close();
      }
    } catch (Exception e) {
      if (cause != null) {
        throw exceptionWithCause("close resource stream", cause);
      } else {
        throw exceptionWithCause("close resource stream", e);
      }
    }
  }

  public static boolean hasResource(String resource) {
    return Io.class.getClassLoader().getResource(resource)!=null;
  }



  /** performs flush and close on the input stream */
  public static byte[] getBytes(InputStream inputStream) {
    if (inputStream==null) {
      return null;
    }
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    RuntimeException exception = null;
    try {
      int nRead;
      byte[] data = new byte[16384];
      while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      buffer.flush();
      return buffer.toByteArray();
    } catch (IOException e) {
      exception = new RuntimeException("Couldn't read chars from stream: "+e.getMessage(), e);
      throw exception;
    } finally {
      try {
        buffer.close();
      } catch (IOException e) {
        if (exception!=null) {
          throw exceptionWithCause("close buffer", exception);
        } else {
          throw exceptionWithCause("close buffer", e);
        }
      }
    }
  }

  /** flushes writer and closes both reader and writer */
  public static void transferAndClose(Reader reader, Writer writer) {
    try {
      transfer(reader, writer, 16384);
    } finally {
      IOException first = null;
      try {
        writer.flush();
      } catch (IOException e) {
        e.printStackTrace();
        first = e;
      }
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
        first = first!=null ? first : e;
      }
      try {
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
        first = first!=null ? first : e;
      }
      if (first!=null) {
        throw exceptionWithCause("flush transfer streams", first);
      }
    }
  }

  /** does not perform flush and close on the input stream */
  public static void transfer(Reader reader, Writer writer) {
    transfer(reader, writer, 16384);
  }

  /** does not perform flush and close on the input stream */
  public static void transfer(Reader reader, Writer writer, int bufferSize) {
    if (reader==null) {
      return;
    }
    if (writer==null) {
      throw new RuntimeException("Writer is null and reader is not");
    }
    RuntimeException exception = null;
    try {
      int nRead;
      char[] data = new char[bufferSize];
      while ((nRead = reader.read(data, 0, data.length)) != -1) {
        writer.write(data, 0, nRead);
      }
    } catch (IOException e) {
      exception = new RuntimeException("Couldn't transfer chars from reader to writer: "+e.getMessage(), e);
      throw exception;
    }
  }

  /** does not perform flush and close on the input stream */
  public static void transfer(InputStream inputStream, OutputStream outputStream) {
    transfer(inputStream, outputStream, 16384);
  }

  /** does not perform flush and close on the input stream */
  public static void transfer(InputStream inputStream, OutputStream outputStream, int bufferSize) {
    if (inputStream==null) {
      return;
    }
    if (outputStream==null) {
      throw new RuntimeException("Writer is null and reader is not");
    }
    RuntimeException exception = null;
    try {
      int nRead;
      byte[] data = new byte[bufferSize];
      while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
        outputStream.write(data, 0, nRead);
      }
    } catch (IOException e) {
      exception = new RuntimeException("Couldn't transfer chars from reader to writer: "+e.getMessage(), e);
      throw exception;
    }
  }

  public static void loadPropertiesFromResource(Properties properties, String resource) {
    if (properties!=null && resource!=null) {
      InputStream resourceStream = Io.getResourceAsStream(resource);
      loadPropertiesFromStream(properties, resourceStream);
    }
  }

  public static void loadPropertiesFromFile(Properties properties, String fileName) {
    if (properties!=null && fileName!=null) {
      File file = new File(fileName);
      if (file.exists()) {
        try {
          InputStream resourceStream = new FileInputStream(file);
          loadPropertiesFromStream(properties, resourceStream);
        } catch (FileNotFoundException e) {
          throw exceptionWithCause("find proeprties file "+fileName, e);
        }
      }
    }
  }

  public static void loadPropertiesFromStream(Properties properties, InputStream stream) {
    if (stream!=null) {
      Exception exception = null;
      try {
        properties.load(stream);
      } catch (Exception e) {
        exception = e;
        throw exceptionWithCause("read properties", e);
      } finally {
        closeResourceStream(stream, exception);
      }
    }
  }

  public static String readFileAsString(String fileName) {
    return readFileAsString(fileName, UTF8);
  }

  public static String readFileAsString(String fileName, Charset charset) {
    if (fileName==null) {
      return null;
    }
    File file = new File(fileName);
    if (!file.exists()) {
      return null;
    }
    try {
      FileInputStream inputStream = new FileInputStream(file);
      byte[] bytes = getBytes(inputStream);
      return new String(bytes, charset!=null ? charset : UTF8);
    } catch (Exception e) {
      throw exceptionWithCause("read file "+fileName, e);
    }
  }

  public static File createTempFile(String prefix, String suffix) {
    return createTempFile(prefix, suffix, null);
  }

  public static File createTempFile(String prefix, String suffix, File directory) {
    try {
      return File.createTempFile(prefix, suffix, directory);
    } catch (IOException e) {
      throw exceptionWithCause("create temp file "+prefix+"..."+suffix, e);
    }
  }

  public static FileWriter createFileWriter(String fileName) {
    return createFileWriter(new File(fileName));
  }

  public static FileWriter createFileWriter(File file) {
    try {
      return new FileWriter(file);
    } catch (IOException e) {
      throw exceptionWithCause("create file writer for "+file, e);
    }
  }

  public static FileReader createFileReader(String fileName) {
    return createFileReader(new File(fileName));
  }

  public static FileReader createFileReader(File file) {
    try {
      return new FileReader(file);
    } catch (IOException e) {
      throw exceptionWithCause("create file reader for "+file, e);
    }
  }

  public static String getCanonicalPath(File file) {
    try {
      return file!=null ? file.getCanonicalPath() : null;
    } catch (IOException e) {
      throw exceptionWithCause("get canonical path for "+file, e);
    }
  }

  /** Creates the directories for a given file path.
   * File.mkdirs() will also create a directory for the last file name */
  public static void mkdirsForFilePath(String filePath) {
    if (filePath!=null) {
      new File(filePath).getParentFile().mkdirs();
    }
  }

  public static void mkdirs(String dirPath) {
    if (dirPath!=null) {
      new File(dirPath).mkdirs();
    }
  }

  public static boolean isDirectory(String fileName) {
    return new File(fileName).isDirectory();
  }

  public static OutputStreamBlock writeTo(OutputStream outputStream) {
    return new OutputStreamBlock(outputStream);
  }

  public static OutputStream createFileOutputStream(String fileName) {
    try {
      return new FileOutputStream(fileName);
    } catch (FileNotFoundException e) {
      throw exceptionWithCause("create FileOutputStream "+fileName, e);
    }
  }

  public static byte[] getBytes(File file) {
    return getBytes(createFileInputStream(file));
  }

  public static FileInputStream createFileInputStream(File file) {
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException e) {
      throw exceptionWithCause("create file input stream", e);
    }
  }

  public static class OutputStreamBlock {
    String description;
    OutputStream outputStream;
    public OutputStreamBlock(OutputStream outputStream) {
      this.outputStream = outputStream;
    }
    public OutputStreamBlock description(String description) {
      this.description = description;
      return this;
    }
    public void write(CheckedFunction<OutputStream> function) {
      Exception exception = null;
      try {
        function.apply(outputStream);
        outputStream.flush();
      } catch (Exception e) {
        throw exceptionWithCause(description, e);
      } finally {
        try {
          outputStream.close();
        } catch (IOException e) {
          if (exception!=null) {
            throw exceptionWithCause(description, exception);
          } else {
            throw exceptionWithCause(description, e);
          }
        }
      }
    }
  }

  public interface CheckedFunction<T> {
    void apply(T t) throws Exception;
  }

  public static class WriteBlock {
    String description;
    Writer writer;
    public WriteBlock(Writer writer) {
      this.writer = writer;
    }
    public WriteBlock description(String description) {
      this.description = description;
      return this;
    }
    public void write(CheckedFunction<Writer> function) {
      Exception exception = null;
      try {
        function.apply(writer);
        writer.flush();
      } catch (Exception e) {
        throw exceptionWithCause(description, e);
      } finally {
        try {
          writer.close();
        } catch (IOException e) {
          if (exception!=null) {
            throw exceptionWithCause(description, exception);
          } else {
            throw exceptionWithCause(description, e);
          }
        }
      }
    }
  }

  public static WriteBlock writeTo(Writer writer) {
    return new WriteBlock(writer);
  }

  public interface CheckedFunctionWithResult<T,R> {
    R apply(T t) throws Exception;
  }

  public static class ReadBlock {
    String description;
    Reader reader;
    public ReadBlock(Reader reader) {
      this.reader = reader;
    }
    public ReadBlock description(String description) {
      this.description = description;
      return this;
    }
    public <T> T readWithResult(CheckedFunctionWithResult<Reader,T> function) {
      Exception exception = null;
      try {
        return function.apply(reader);
      } catch (Exception e) {
        throw exceptionWithCause(description, e);
      } finally {
        try {
          reader.close();
        } catch (IOException e) {
          if (exception!=null) {
            throw exceptionWithCause(description, exception);
          } else {
            throw exceptionWithCause(description, e);
          }
        }
      }
    }
    public void read(CheckedFunction<Reader> function) {
      Exception exception = null;
      try {
        function.apply(reader);
      } catch (Exception e) {
        throw exceptionWithCause(description, e);
      } finally {
        try {
          reader.close();
        } catch (IOException e) {
          if (exception!=null) {
            throw exceptionWithCause(description, exception);
          } else {
            throw exceptionWithCause(description, e);
          }
        }
      }
    }
  }

  public static ReadBlock readFrom(Reader reader) {
    Exceptions.assertNotNullParameter(reader, "reader");
    return new ReadBlock(reader);
  }

  /** Be careful! */
  public static void deleteDirectoryContentsRecursive(File file) {
    File[] nestedFiles = file.listFiles();
    if (nestedFiles!=null) {
      for (File nestedFile: nestedFiles) {
        deleteRecursive(nestedFile);
      }
    }
  }

  /** Be careful! */
  public static void deleteRecursive(File file) {
    if (file.isDirectory()) {
      deleteDirectoryContentsRecursive(file);
    }
    file.delete();
  }

  public static void unzip(String sourceZipFilePath, String destinationDir) {
    try {
      FileInputStream sourceZipInputStream = new FileInputStream(sourceZipFilePath);
      unzip(sourceZipInputStream, destinationDir);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void unzipFiles(InputStream sourceZipInputStream, Pattern filePattern, Consumer<InputStream> fileOperation) {
    ZipInputStream zipInputStream = new ZipInputStream(sourceZipInputStream);
    try {
      ZipEntry zipEntry = zipInputStream.getNextEntry();
      while (zipEntry != null) {
        String zipEntryFileName = zipEntry.getName();
        if (filePattern.matcher(zipEntryFileName).matches()) {
          fileOperation.accept(zipInputStream);
        }
        while (zipInputStream.skip(1000)>0);
        zipEntry = zipInputStream.getNextEntry();
      }
      zipInputStream.closeEntry();
    } catch (IOException e) {
      e.printStackTrace();
      try {
        zipInputStream.close();
      } catch (IOException closeException) {
        closeException.printStackTrace();
      }
    }
  }

  public static void unzip(InputStream sourceZipInputStream, String destinationDir) {
    try {
      File destDir = new File(destinationDir);
      destDir.mkdirs();
      byte[] buffer = new byte[1024];
      ZipInputStream zipInputStream = new ZipInputStream(sourceZipInputStream);
      ZipEntry zipEntry = zipInputStream.getNextEntry();
      while (zipEntry != null) {
        File newFile = unzipNewFile(destDir, zipEntry);
        newFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while ((len = zipInputStream.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
        zipEntry = zipInputStream.getNextEntry();
      }
      zipInputStream.closeEntry();
      zipInputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static File unzipNewFile(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName());

    String destDirPath = destinationDir.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }

    return destFile;
  }

  public static void zip(String sourceDirectory, String destinationZipFilePath) {
    try {
      File srcDirectoryFile = new File(sourceDirectory);
      File destZipFile = new File(destinationZipFilePath);
      try (OutputStream destinationZipOutputStream = new FileOutputStream(destZipFile)) {
        try (ZipOutputStream zip = new ZipOutputStream(destinationZipOutputStream)) {
          addFolderToZip(srcDirectoryFile, srcDirectoryFile, zip);
        }
      }
    } catch (Exception e) {
      throw exceptionWithCause("create zip file "+destinationZipFilePath, e);
    }
  }

  private static void addFileToZip(File rootPath, File srcFile, ZipOutputStream zip) throws Exception {
    if (srcFile.isDirectory()) {
      addFolderToZip(rootPath, srcFile, zip);
    } else {
      byte[] buf = new byte[4096];
      int len;
      try (FileInputStream in = new FileInputStream(srcFile)) {
        String name = srcFile.getPath();
        name = name.replace(rootPath.getPath(), "");
        zip.putNextEntry(new ZipEntry(name));
        while ((len = in.read(buf)) > 0) {
          zip.write(buf, 0, len);
        }
      }
    }
  }

  private static void addFolderToZip(File rootPath, File srcFolder, ZipOutputStream zip) throws Exception {
    for (File fileName : srcFolder.listFiles()) {
      addFileToZip(rootPath, fileName, zip);
    }
  }
}
