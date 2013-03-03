package ja.tools.common;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;

import javax.imageio.ImageIO;

/**
 * 
 * @author jobert
 * 
 */
public class JTools {
	public static final String WIN_DESKTOP_PEER = "sun.awt.windows.WDesktopPeer";
	public static final String X11_DESKTOP_PEER = "sun.awt.X11.XDesktopPeer";

	public static String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}

	public static byte[] getResourceFromClasspath(Class<?> clazz, String path) {
		InputStream in = clazz.getResourceAsStream(path);
		if (in != null) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				JTools.copyStream(in, out);
				return out.toByteArray();
			} catch (IOException e) {
			}
		}
		return null;
	}

	public static byte[] getResourceFromClasspath(String path) {
		return getResourceFromClasspath(JTools.class, path);
	}

	public static void copyStream(InputStream in, OutputStream os)
			throws IOException {
		int read = 0;
		byte[] buff = new byte[1024];
		while ((read = in.read(buff, 0, buff.length)) > 0) {
			os.write(buff, 0, read);
		}
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte hashByte : bytes) {
			int intVal = 0xff & hashByte;
			if (intVal < 0x10) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(intVal));
		}
		return sb.toString();
	}

	public static Image resizeImage(byte[] data, int size) {
		return resizeImage(data, size, false);
	}

	public static Image resizeImage(BufferedImage img, int size) {
		return resizeImage(img, size, false);
	}

	public static Image resizeImage(byte[] data, int size, boolean higherQuality) {
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
			return resizeImage(img, size, higherQuality);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Image resizeImage(BufferedImage img, int size,
			boolean higherQuality) {
		// int type = (img.getTransparency() == Transparency.OPAQUE) ?
		// BufferedImage.TYPE_INT_RGB
		// : BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage) img;
		int w = img.getWidth();
		int h = img.getHeight();

		double scale = (double) size / (double) h;
		if (w > h) {
			scale = (double) size / (double) w;
		}

		int targetWidth = (int) (scale * w);
		int targetHeight = (int) (scale * h);
		if (!higherQuality) {
			w = targetWidth;
			h = targetHeight;
		}
		return ret.getScaledInstance(targetWidth, targetHeight,
				Image.SCALE_SMOOTH);
		// do {
		// if (higherQuality) {
		// if (w > targetWidth) {
		// w /= 2;
		// if (w < targetWidth) {
		// w = targetWidth;
		// }
		// }
		// if (h > targetHeight) {
		// h /= 2;
		// if (h < targetHeight) {
		// h = targetHeight;
		// }
		// }
		// }
		//
		// BufferedImage tmp = new BufferedImage(w, h, type);
		// Graphics2D g2 = tmp.createGraphics();
		// g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		// RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		// g2.drawImage(ret, 0, 0, w, h, null);
		// g2.dispose();
		//
		// ret = tmp;
		// } while (w != targetWidth || h != targetHeight);
		//
		// return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void openDefaultApp(File file) throws IOException {
		if (file.exists()) {
			try {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(file);
			} catch (IOException e) {
				try {

					Class dClass = null;
					try {
						dClass = Class.forName(WIN_DESKTOP_PEER);
					} catch (Exception ex) {
						dClass = Class.forName(X11_DESKTOP_PEER);
					}

					if (dClass == null) {
						return;
					}

					Class[] classArgs = new Class[] { String.class,
							String.class };

					Method privateMethod = dClass.getDeclaredMethod(
							"ShellExecute", classArgs);

					privateMethod.setAccessible(true);
					Object[] invokeArgs = new Object[] {
							file.getCanonicalPath(), "open" };
					privateMethod.invoke(null, invokeArgs);
				} catch (Throwable t) {
				}
			}
		}
	}

	public static void openBrowser(String url) throws Exception {
		Desktop desktop = Desktop.getDesktop();
		desktop.browse(new URI(url));
	}
}
