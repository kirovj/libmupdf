package com.artifex.mupdf.fitz;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

// This class handles the loading of the MuPDF shared library, together
// with the ThreadLocal magic to get the required context.
//
// The only publicly accessible method here is Context.setStoreSize, which
// sets the store size to use. This must be called before any other MuPDF
// function.
public class Context
{
	private static boolean inited = false;
	private static native int initNative();
	public static native int gprfSupportedNative();

	public synchronized static void init() {
		if (!inited) {
			String libname = libname();
			try {
				extractResource("windows/mupdf_java64.dll", libname);
				System.load(libpath());
			} catch (UnsatisfiedLinkError | IOException e) {
				System.loadLibrary(libname);
			}
			if (initNative() < 0) {
				throw new RuntimeException("cannot initialize mupdf library");
			}
			inited = true;
		}
	}

	private static void extractResource(String path, String name) throws IOException, FileNotFoundException {
		String dest = System.getProperty("java.io.tmpdir") + name;
		if (new File(dest).exists()) {
			return;
		}
		System.out.println("Extracting native lib " + name);
		InputStream libStream = Context.class.getClassLoader().getResourceAsStream(path);
		OutputStream fileStream = Files.newOutputStream(Paths.get(dest));
		assert libStream != null;
		copyStream(libStream, fileStream);
		libStream.close();
		fileStream.close();
	}

	private static void copyStream(InputStream source, OutputStream target) throws IOException {
		byte[] buf = new byte[8192];
		int length;
		int bytesCopied = 0;
		while ((length = source.read(buf)) > 0) {
			target.write(buf, 0, length);
			bytesCopied += length;
		}
		System.out.println("Copied " + bytesCopied + " bytes");
	}
	
	public static String libpath() {
		String tmpdir = System.getProperty("java.io.tmpdir");
		File libFile = new File(tmpdir, libname());
		return libFile.getAbsolutePath();
	}
	
	public static String libname() {
		String libname = "mupdf_java";
		String os_arch = System.getProperty("os.arch");
		if (os_arch.contains("64")) {
			libname = libname + "64";
		}
		return System.mapLibraryName(libname);
	}

	// FIXME: We should support the store size being changed dynamically.
	// This requires changes within the MuPDF core.
	//public native static void setStoreSize(long newSize);
}