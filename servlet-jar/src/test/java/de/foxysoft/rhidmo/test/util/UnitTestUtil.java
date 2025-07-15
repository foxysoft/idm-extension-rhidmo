/*******************************************************************************
 * Copyright 2025 Lambert Giese
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package de.foxysoft.rhidmo.test.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;

public class UnitTestUtil {

	public static File setUpTempDir() throws IOException {
		return Files.createTempDirectory(null, new FileAttribute<?>[] {}).toFile();
	}

	public static boolean tearDownTempDir(File tmpDir) {
		File[] allContents = tmpDir.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				tearDownTempDir(file);
			}
		}
		return tmpDir.delete();
	}

	public static void copyResourceToFile(String resourceName, File outputFile) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			URL resourceUrl = UnitTestUtil.class.getResource(resourceName);
			if (resourceUrl != null) {
				is = new BufferedInputStream(resourceUrl.openStream());
			} else {
				throw new IOException("Resource [" + resourceName + "] not found");
			}
			os = new FileOutputStream(outputFile);
			int numRead;
			byte[] buf = new byte[1024];
			while ((numRead = is.read(buf)) > 0) {
				os.write(buf, 0, numRead);
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ignore) {
				}

			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException ignore) {
				}

			}
		}

	}

	public static byte[] repeatBytes(byte[] pattern, int outLen) {
		byte[] out = new byte[outLen];
		int inLen = pattern.length;
		int div = outLen / inLen;
		int mod = outLen % inLen;
		for (int i = 0; i < div; i++) {
			System.arraycopy(pattern, 0, out, i * pattern.length, pattern.length);
		}
		if (mod > 0) {
			System.arraycopy(pattern, 0, out, div * pattern.length, mod);
		}
		return out;
	}
}
