package zmodem.xfer.io;

import java.io.IOException;

public abstract class ObjectOutputStream<T> {
	public abstract void write(T o) throws IOException;
}
