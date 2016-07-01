package zmodem;

import zmodem.xfer.zm.util.Modem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class XModem {
    private Modem modem;

    public XModem(InputStream inputStream, OutputStream outputStream) {
        this.modem = new Modem(inputStream, outputStream);
    }

    public void send(Path file,boolean useBlock1K) throws IOException, InterruptedException {
        modem.send(file, useBlock1K);
    }

    public void receive(Path file) throws IOException {
        modem.receive(file, false);
    }
}
