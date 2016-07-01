package zmodem.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileAdapter {
	public String getName();
	public InputStream getInputStream() throws IOException;
	public OutputStream getOutputStream() throws IOException;
	public OutputStream getOutputStream(boolean append) throws IOException;
	public FileAdapter getChild(String name);
	public long length();
	public boolean isDirectory();
	public boolean exists();
	public String toString();
}
