package org.zeromeaner.util;

import java.io.BufferedWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;

public class PropertyOutputStream extends FilterOutputStream {

	protected Writer writer;
	
	public PropertyOutputStream(OutputStream out) throws IOException {
		this(out, null);
	}
	
	public PropertyOutputStream(OutputStream out, String comments) throws IOException {
		super(out);
		writer = new OutputStreamWriter(out, "8859_1");
		if(comments != null) {
			writeComments(writer, comments);
		}
		writer.write("#" + new Date() + "\n");
	}

	public void setProperty(String key, String value) {
		try {
			set(key, value);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void set(String key, String value) throws IOException {
		if(key == null || value == null)
			throw new IllegalArgumentException();
		key = saveConvert(key, true, true);
		value = saveConvert(value, false, true);
		writer.write(key + "=" + value);
	}
	
	@Override
	public void flush() throws IOException {
		writer.flush();
		super.flush();
	}
	
	@Override
	public void close() throws IOException {
		writer.close();
		super.close();
	}
	
    private static void writeComments(Writer w, String comments)
            throws IOException {
            w.write("#");
            int len = comments.length();
            int current = 0;
            int last = 0;
            char[] uu = new char[6];
            uu[0] = '\\';
            uu[1] = 'u';
            while (current < len) {
                char c = comments.charAt(current);
                if (c > '\u00ff' || c == '\n' || c == '\r') {
                    if (last != current)
                        w.write(comments.substring(last, current));
                    if (c > '\u00ff') {
                        uu[2] = toHex((c >> 12) & 0xf);
                        uu[3] = toHex((c >>  8) & 0xf);
                        uu[4] = toHex((c >>  4) & 0xf);
                        uu[5] = toHex( c        & 0xf);
                        w.write(new String(uu));
                    } else {
                        w.write("\n");
                        if (c == '\r' &&
                            current != len - 1 &&
                            comments.charAt(current + 1) == '\n') {
                            current++;
                        }
                        if (current == len - 1 ||
                            (comments.charAt(current + 1) != '#' &&
                            comments.charAt(current + 1) != '!'))
                            w.write("#");
                    }
                    last = current + 1;
                }
                current++;
            }
            if (last != current)
                w.write(comments.substring(last, current));
            w.write("\n");
        }
    
    /*
     * Converts unicodes to encoded &#92;uxxxx and escapes
     * special characters with a preceding slash
     */
    private static String saveConvert(String theString,
                               boolean escapeSpace,
                               boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\'); outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch(aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                          break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                          break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                          break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                          break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\'); outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode ) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    /**
     * Convert a nibble to a hex character
     * @param   nibble  the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static final char[] hexDigit = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

}
