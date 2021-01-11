package com.j256.testcheckpublisher;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

/**
 * Some static IO utility methods.
 * 
 * @author graywatson
 */
public class IoUtils {

	public static String inputStreamToString(InputStream inputStream)
			throws UnsupportedOperationException, IOException {
		try (Reader reader = new InputStreamReader(inputStream)) {
			return readerToString(reader);
		}
	}

	public static String readerToString(Reader reader) throws UnsupportedOperationException, IOException {
		try (StringWriter writer = new StringWriter()) {
			char[] chars = new char[1024];
			while (true) {
				int len = reader.read(chars);
				if (len < 0) {
					break;
				}
				writer.write(chars, 0, len);
			}
			return writer.toString();
		}
	}
}
