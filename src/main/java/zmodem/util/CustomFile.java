package zmodem.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CustomFile implements FileAdapter{
	File file = null;
	
	public CustomFile(File file) {
		super();
		this.file = file;
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new FileOutputStream(file);
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws IOException {
		return new FileOutputStream(file, append);
	}

	@Override
	public FileAdapter getChild(String name) {
		if(name.equals(file.getName())){
			return this;
		}else if(file.isDirectory()){
			File son = new File(file.getAbsolutePath()+"\\"+name);
			try {
				son.createNewFile();
			} catch (IOException e) {
				System.out.println("Create New File Error:"+e.getMessage());
			}
			return new CustomFile(son);
		}
		return null;
		
	}

	@Override
	public long length() {
		return file.length();
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

}
