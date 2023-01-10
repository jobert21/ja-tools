package ja.tools.common;

import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Path;
import java.util.Scanner;

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
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
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

	public static void copyStream(InputStream in, OutputStream os) throws IOException {
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

	public static void imageToGrayScale(InputStream in, Path output) throws IOException {
		BufferedImage grayImg = imageToGrayScale(in);
		ImageIO.write(grayImg, "png", output.toFile());
	}

	public static BufferedImage imageToGrayScale(InputStream in) throws IOException {
		BufferedImage img = ImageIO.read(in);
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorConvertOp op = new ColorConvertOp(cs, null);
		BufferedImage grayImg = op.filter(img, null);
		return grayImg;
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

	public static Image resizeImage(BufferedImage img, int size, boolean higherQuality) {
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
		return img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
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

					Class[] classArgs = new Class[] { String.class, String.class };

					Method privateMethod = dClass.getDeclaredMethod("ShellExecute", classArgs);

					privateMethod.setAccessible(true);
					Object[] invokeArgs = new Object[] { file.getCanonicalPath(), "open" };
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

	public static String getCpuSerial() {
		String serial = null;
		try {
			Process process = Runtime.getRuntime().exec(new String[] { "wmic", "bios", "get", "serialnumber" });
			Scanner sc = new Scanner(process.getInputStream());
			sc.next();
			String value = sc.next();
			process.destroy();
			sc.close();

			serial = value;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return serial;
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> genericParameterClass(Class<?> clazzOwner, int parameterIndex) {
		Class<T> parameter = null;
		Type superClass = clazzOwner.getGenericSuperclass();
		while (!ParameterizedType.class.isInstance(superClass) && superClass != null) {
			superClass = (superClass instanceof Class<?>) ? ((Class<?>) superClass).getGenericSuperclass() : null;
		}

		if (superClass instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) superClass;
			Type arguments[] = paramType.getActualTypeArguments();

			if (arguments != null && arguments.length > parameterIndex) {
				Type type = arguments[parameterIndex];

				if (type instanceof Class) {
					parameter = (Class<T>) type;
				}
			}
		}
		return parameter;
	}

	public static long ipToLong(InetAddress ip) {
		byte[] octets = ip.getAddress();
		long result = 0;
		for (byte octet : octets) {
			result <<= 8;
			result |= octet & 0xff;
		}
		return result;
	}

	public static BufferedImage toBufferedImage(Image image) {
		BufferedImage newImage = null;
		if (!(image instanceof BufferedImage)) {
			newImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = newImage.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
		}
		return newImage;
	}
}
