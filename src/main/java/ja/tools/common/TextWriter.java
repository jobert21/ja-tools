package ja.tools.common;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Can be a CSV or anything you want to write.
 * 
 * @author jobert
 * 
 */
public class TextWriter {
	public static final char DEFAULT_MISSING_CHAR_APPEND = '0';

	public static enum AppendType {
		HEADING, TRAILING
	}

	private static final Logger log = Logger.getLogger(TextWriter.class
			.getName());
	private PrintWriter writer;
	private String delimeter;
	private StringBuilder builder;

	private TextWriter(OutputStream out, String delimeter) {
		writer = new PrintWriter(out, true);
		builder = new StringBuilder();
		this.delimeter = delimeter;
	}

	public static TextWriter create(OutputStream out) {
		return create(out, null);
	}

	public static TextWriter create(OutputStream out, String delimeter) {
		return new TextWriter(out, delimeter);
	}

	public TextWriter writeRow(String... contents) {
		if (contents != null) {
			for (String content : contents) {
				writeColumn(content);
			}
			endRow();
		}
		return this;
	}

	public TextWriter endRow() {
		if (builder.length() > 0) {
			if (delimeter != null) {
				int index = builder.lastIndexOf(delimeter);
				if (index >= 0) {
					builder.delete(index, builder.length());
				}
			}
			String rowContent = builder.toString();
			log.info("Row Content >>> " + rowContent);
			writer.println(rowContent);
			builder.delete(0, builder.length());
		}
		return this;
	}

	public TextWriter writeColumn(String content) {
		builder.append(content == null ? "" : content);
		if (delimeter != null) {
			builder.append(delimeter);
		}
		return this;
	}

	public TextWriter writeColumn(String content,
			TextWriter.AppendType appendType, int maxChars) {
		if (content != null) {
			int len = content.length();
			if (len < maxChars) {
				StringBuilder missingBuilder = new StringBuilder(maxChars - len);
				for (int i = 0; i < missingBuilder.capacity(); i++) {
					missingBuilder.append(DEFAULT_MISSING_CHAR_APPEND);
				}
				String missing = missingBuilder.toString();
				switch (appendType) {
				case HEADING:
					content = missing + content;
					break;
				default:
					content += missing;
					break;
				}
			}
			writeColumn(content);
		}
		return this;
	}

	public TextWriter done() {
		endRow();
		writer.flush();
		writer.close();
		writer = null;
		return this;
	}

}
