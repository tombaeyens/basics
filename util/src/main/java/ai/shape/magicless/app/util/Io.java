/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */
package ai.shape.magicless.app.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Scanner;

public class Io {

  public static final Charset UTF8 = Charset.forName("UTF-8");

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
      throw new RuntimeException("Couldn't read reader to string: "+e.toString(), e);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return stringBuilder.toString();
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

  private static void closeResourceStream(InputStream resourceStream, Exception cause) {
    try {
      if (resourceStream!=null) {
        resourceStream.close();
      }
    } catch (Exception e) {
      if (cause != null) {
        throw Exceptions.exceptionWithCause("close resource stream", cause);
      } else {
        throw Exceptions.exceptionWithCause("close resource stream", e);
      }
    }
  }

  public static boolean hasResource(String resource) {
    return Io.class.getClassLoader().getResource(resource)!=null;
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
      exception = new RuntimeException("Couldn't read bytes from stream: "+e.getMessage(), e);
      throw exception;
    } finally {
      try {
        buffer.close();
      } catch (IOException e) {
        if (exception!=null) {
          throw Exceptions.exceptionWithCause("close buffer", exception);
        } else {
          throw Exceptions.exceptionWithCause("close buffer", e);
        }
      }
    }
  }

  public static InputStream getResourceAsStream(String resource) {
    return Io.class.getClassLoader().getResourceAsStream(resource);
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
          throw Exceptions.exceptionWithCause("find proeprties file "+fileName, e);
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
        throw Exceptions.exceptionWithCause("read properties", e);
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
      throw Exceptions.exceptionWithCause("read file "+fileName, e);
    }
  }

  public static File createTempFile(String prefix, String suffix) {
    return createTempFile(prefix, suffix, null);
  }

  public static File createTempFile(String prefix, String suffix, File directory) {
    try {
      return File.createTempFile(prefix, suffix, directory);
    } catch (IOException e) {
      throw Exceptions.exceptionWithCause("create temp file "+prefix+"..."+suffix, e);
    }
  }

  public static Writer createFileWriter(String fileName) {
    return createFileWriter(new File(fileName));
  }

  public static Writer createFileWriter(File file) {
    try {
      return new FileWriter(file);
    } catch (IOException e) {
      throw Exceptions.exceptionWithCause("create file writer for "+file, e);
    }
  }

  public static String getCanonicalPath(File file) {
    try {
      return file!=null ? file.getCanonicalPath() : null;
    } catch (IOException e) {
      throw Exceptions.exceptionWithCause("get canonical path for "+file, e);
    }
  }

  /** Creates the directories for a given file path.
   * File.mkdirs() will also create a directory for the last file name */
  public static void mkdirsForFilePath(String filePath) {
    if (filePath!=null) {
      new File(filePath).getParentFile().mkdirs();
    }
  }

  public interface CheckedFunction<T> {
    void apply(T t) throws Exception;
  }

  public static class WriterCloser {
    String description;
    Writer writer;
    public WriterCloser(Writer writer) {
      this.writer = writer;
    }
    public WriterCloser description(String description) {
      this.description = description;
      return this;
    }
    public void execute(CheckedFunction<Writer> function) {
      Exception exception = null;
      try {
        function.apply(writer);
        writer.flush();
      } catch (Exception e) {
        throw Exceptions.exceptionWithCause(description, e);
      } finally {
        try {
          writer.close();
        } catch (IOException e) {
          if (exception!=null) {
            throw Exceptions.exceptionWithCause(description, exception);
          } else {
            throw Exceptions.exceptionWithCause(description, e);
          }
        }
      }
    }
  }

  public static WriterCloser flushAndClose(Writer writer) {
    return new WriterCloser(writer);
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
}
