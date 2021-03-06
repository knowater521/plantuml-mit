/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2020, Arnaud Roques
 *
 * Project Info:  https://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * https://plantuml.com/patreon (only 1$ per month!)
 * https://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * Licensed under The MIT License (Massachusetts Institute of Technology License)
 * 
 * See http://opensource.org/licenses/MIT
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 *
 * Original Author:  Arnaud Roques
 */
package net.sourceforge.plantuml.version;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import net.sourceforge.plantuml.Log;
import net.sourceforge.plantuml.OptionFlags;
import net.sourceforge.plantuml.SignatureUtils;
import net.sourceforge.plantuml.security.ImageIO;
import net.sourceforge.plantuml.security.SFile;

public class LicenseInfo {

	private final static Preferences prefs = Preferences.userNodeForPackage(LicenseInfo.class);
	public final static LicenseInfo NONE = new LicenseInfo(LicenseType.NONE, 0, 0, null, null, null);

	private final LicenseType type;
	private final long generationDate;
	private final long expirationDate;
	private final String owner;
	private final String context;
	private final byte[] sha;

	public LicenseInfo(LicenseType type, long generationDate, long expirationDate, String owner, String context,
			byte[] sha) {
		this.type = type;
		this.generationDate = generationDate;
		this.expirationDate = expirationDate;
		this.owner = owner;
		this.context = context;
		this.sha = sha;
	}

	public static void persistMe(String key) throws BackingStoreException {
		prefs.sync();
		prefs.put("license", key);
	}

	private static LicenseInfo cache;

	public static synchronized LicenseInfo retrieveQuick() {
		if (cache == null) {
			cache = retrieveDistributor();
		}
		if (cache == null) {
			cache = retrieveNamedSlow();
		}
		return cache;
	}

	public static boolean retrieveNamedOrDistributorQuickIsValid() {
		return retrieveQuick().isValid();
	}

	public static synchronized LicenseInfo retrieveNamedSlow() {
		cache = LicenseInfo.NONE;
		if (OptionFlags.ALLOW_INCLUDE == false) {
			return cache;
		}
		final String key = prefs.get("license", "");
		if (key.length() > 0) {
			cache = setIfValid(retrieveNamed(key), cache);
			if (cache.isValid()) {
				return cache;
			}
		}
		for (SFile f : fileCandidates()) {
			try {
				if (f.exists() && f.canRead()) {
					final LicenseInfo result = retrieve(f);
					if (result == null) {
						return null;
					}
					cache = setIfValid(result, cache);
					if (cache.isValid()) {
						return cache;
					}
				}
			} catch (IOException e) {
				Log.info("Error " + e);
				// e.printStackTrace();
			}
		}
		return cache;
	}

	public static LicenseInfo retrieveNamed(final String key) {
		if (key.length() > 99 && key.matches("^[0-9a-z]+$")) {
			try {
				final String sig = SignatureUtils.toHexString(PLSSignature.signature());
				return PLSSignature.retrieveNamed(sig, key, true);
			} catch (Exception e) {
				// e.printStackTrace();
				Log.info("Error retrieving license info" + e);
			}
		}
		return LicenseInfo.NONE;
	}

	public static BufferedImage retrieveDistributorImage(LicenseInfo licenseInfo) {
		if (licenseInfo.getLicenseType() != LicenseType.DISTRIBUTOR) {
			return null;
		}
		try {
			final byte[] s1 = PLSSignature.retrieveDistributorImageSignature();
			if (SignatureUtils.toHexString(s1).equals(SignatureUtils.toHexString(licenseInfo.sha)) == false) {
				return null;
			}
			final InputStream dis = PSystemVersion.class.getResourceAsStream("/distributor.png");
			if (dis == null) {
				return null;
			}
			try {
				final BufferedImage result = ImageIO.read(dis);
				return result;
			} finally {
				dis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static LicenseInfo retrieveDistributor() {
		final InputStream dis = PSystemVersion.class.getResourceAsStream("/distributor.txt");
		if (dis == null) {
			return null;
		}
		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			final String licenseString = br.readLine();
			br.close();
			final LicenseInfo result = PLSSignature.retrieveDistributor(licenseString);
			final Throwable creationPoint = new Throwable();
			creationPoint.fillInStackTrace();
			for (StackTraceElement ste : creationPoint.getStackTrace()) {
				if (ste.toString().contains(result.context)) {
					return result;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Collection<SFile> fileCandidates() {
		final Set<SFile> result = new TreeSet<SFile>();
		final String classpath = System.getProperty("java.class.path");
		String[] classpathEntries = classpath.split(SFile.pathSeparator);
		for (String s : classpathEntries) {
			SFile dir = new SFile(s);
			if (dir.isFile()) {
				dir = dir.getParentFile();
			}
			if (dir != null && dir.isDirectory()) {
				result.add(dir.file("license.txt"));
			}
		}
		return result;
	}

	private static LicenseInfo setIfValid(LicenseInfo value, LicenseInfo def) {
		if (value.isValid() || def.isNone()) {
			return value;
		}
		return def;
	}

	private static LicenseInfo retrieve(SFile f) throws IOException {
		final BufferedReader br = f.openBufferedReader();
		if (br == null) {
			return null;
		}
		final String s = br.readLine();
		br.close();
		final LicenseInfo result = retrieveNamed(s);
		if (result != null) {
			Log.info("Reading license from " + f.getAbsolutePath());
		}
		return result;
	}

	public static void main(String[] args) {
		LicenseInfo info = retrieveNamedSlow();
		System.err.println("valid=" + info.isValid());
		System.err.println("info=" + info.owner);

	}

	public final Date getGenerationDate() {
		return new Date(generationDate);
	}

	public final Date getExpirationDate() {
		return new Date(expirationDate);
	}

	public final String getOwner() {
		return owner;
	}

	public boolean isNone() {
		return owner == null;
	}

	public boolean isValid() {
		return owner != null && System.currentTimeMillis() <= this.expirationDate;
	}

	public boolean hasExpired() {
		return owner != null && System.currentTimeMillis() > this.expirationDate;
	}

	public final LicenseType getLicenseType() {
		return type;
	}

	public final String getContext() {
		return context;
	}

}
