package zmodem;

import zmodem.xfer.util.CRC16;
import zmodem.xfer.util.CRC8;
import zmodem.xfer.util.XCRC;
import zmodem.xfer.zm.util.Modem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

public class YModem {
    private Modem modem;
    public YModem(InputStream inputStream, OutputStream outputStream) {
        this.modem = new Modem(inputStream, outputStream);
    }
    public void send(Path file) throws IOException {
        //check filename
//        if (!file.getFileName().toString().matches("\\w{1,8}\\.\\w{1,3}")) {
//            throw new IOException("Filename must be in DOS style (no spaces, max 8.3)");
//        }

        //open file
        try (DataInputStream dataStream = new DataInputStream(Files.newInputStream(file))) {

            boolean useCRC16 = modem.waitReceiverRequest();
            XCRC crc;
            if (useCRC16)
                crc = new CRC16();
            else
                crc = new CRC8();

            //send block 0
            BasicFileAttributes readAttributes = Files.readAttributes(file, BasicFileAttributes.class);
            String fileNameString = file.getFileName().toString() + (char)0 + ((Long) Files.size(file)).toString()+" "+ Long.toOctalString(readAttributes.lastModifiedTime().toMillis() / 1000);
            byte[] fileNameBytes = Arrays.copyOf(fileNameString.getBytes(), 128);
            modem.sendBlock(0, Arrays.copyOf(fileNameBytes, 128), 128, crc);

            modem.waitReceiverRequest();
            //send data
            byte[] block = new byte[1024];
            modem.sendDataBlocks(dataStream, 1, crc, block);

            modem.sendEOT();
        }
    }

    public void batchSend(Path... files) throws IOException {
        for (Path file : files) {
            send(file);
        }

        sendBatchStop();
    }

    private void sendBatchStop() throws IOException {
        boolean useCRC16 = modem.waitReceiverRequest();
        XCRC crc;
        if (useCRC16)
            crc = new CRC16();
        else
            crc = new CRC8();

        //send block 0
        byte[] bytes = new byte[128];
        modem.sendBlock(0, bytes, bytes.length, crc);
    }

    public Path receiveSingleFileInDirectory(Path directory) throws IOException {
        return receive(directory, true);
    }
    public void receiveFilesInDirectory(Path directory) throws IOException {
        while (receive(directory, true) != null) {
        }
    }
    public Path receive(Path path) throws IOException {
        return receive(path, false);
    }

    private Path receive(Path path, boolean inDirectory) throws IOException {
        DataOutputStream dataOutput = null;
        Path filePath;
        try {
            XCRC crc = new CRC16();
            int errorCount = 0;

            // process block 0
            byte[] block;
            int character;
            while (true) {
                character = modem.requestTransmissionStart(true);
                try {
                    // read file name from zero block
                    block = modem.readBlock(0, (character == Modem.SOH), crc);

                    if (inDirectory) {
                        StringBuilder sb = new StringBuilder();
                        if (block[0] == 0) {
                            //this is stop block of batch file transfer
                            modem.sendByte(Modem.ACK);
                            return null;
                        }
                        for (int i = 0; i < block.length; i++) {
                            if (block[i] == 0) {
                                break;
                            }
                            sb.append((char) block[i]);
                        }
                        filePath = path.resolve(sb.toString());
                    } else {
                        filePath = path;
                    }
                    dataOutput = new DataOutputStream(Files.newOutputStream(filePath));
                    modem.sendByte(Modem.ACK);
                    break;
                } catch (Modem.InvalidBlockException e) {
                    errorCount++;
                    if (errorCount == Modem.MAXERRORS) {
                        modem.interruptTransmission();
                        throw new IOException("Transmission aborted, error count exceeded max");
                    }
                    modem.sendByte(Modem.NAK);
                } catch (Modem.RepeatedBlockException | Modem.SynchronizationLostException e) {
                    //fatal transmission error
                    modem.interruptTransmission();
                    throw new IOException("Fatal transmission error", e);
                }
            }

            //receive data blocks
            modem.receive(filePath, true);
        } finally {
            if (dataOutput != null) {
                dataOutput.close();
            }
        }
        return filePath;
    }
}
