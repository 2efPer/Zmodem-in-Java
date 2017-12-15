package zmodem.xfer.zm.util;

import zmodem.xfer.util.CRC16;
import zmodem.xfer.util.CRC8;
import zmodem.xfer.util.XCRC;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This is core Modem class supporting XModem (and some extensions XModem-1K, XModem-CRC), and YModem.<br/>
 * YModem support is limited (currently block 0 is ignored).<br/>
 * <br/>
 * Created by Anton Sirotinkin (aesirot@mail.ru), Moscow 2014 <br/>
 * I hope you will find this program useful.<br/>
 * You are free to use/modify the code for any purpose, but please leave a reference to me.<br/>
 */
public class Modem {

    public static final byte SOH = 0x01; /* Start Of Header */
    public static final byte STX = 0x02; /* Start Of Text (used like SOH but means 1024 block size) */
    public static final byte EOT = 0x04; /* End Of Transmission */
    public static final byte ACK = 0x06; /* ACKnowlege */
    public static final byte NAK = 0x15; /* Negative AcKnowlege */
    public static final byte CAN = 0x18; /* CANcel character */

    public static final byte CPMEOF = 0x1A;
    public static final byte ST_C = 'C';

    public static final int MAXERRORS = 10;

    public static final int BLOCK_TIMEOUT = 1000;
    public static final int REQUEST_TIMEOUT = 3000;
    public static final int WAIT_FOR_RECEIVER_TIMEOUT = 60_000;
    public static final int SEND_BLOCK_TIMEOUT = 10_000;

    private final InputStream inputStream;
    private final OutputStream outputStream;

    private final byte[] shortBlockBuffer;
    private final byte[] longBlockBuffer;

    /**
     * Constructor
     *
     * @param inputStream  stream for reading received data from other side
     * @param outputStream stream for writing data to other side
     */
    public Modem(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        shortBlockBuffer = new byte[128];
        longBlockBuffer = new byte[1024];
    }

    /**
     * Wait for receiver request for transmission
     *
     * @return TRUE if receiver requested CRC-16 checksum, FALSE if 8bit checksum
     * @throws java.io.IOException
     */
    public boolean waitReceiverRequest() throws IOException {
        int character;
        while (true) {
            character = readByte();
            if (character == NAK)
                return false;
            if (character == ST_C) {
                return true;
            }
        }
    }

    /**
     * Send a file. <br/>
     * <p>
     * This method support correct thread interruption, when thread is interrupted "cancel of transmission" will be send.
     * So you can move long transmission to other thread and interrupt it according to your algorithm.
     *
     * @param file
     * @param useBlock1K
     * @throws java.io.IOException
     */
    public void send(Path file, boolean useBlock1K) throws IOException {
        //open file
        try (DataInputStream dataStream = new DataInputStream(Files.newInputStream(file))) {

            boolean useCRC16 = waitReceiverRequest();
            XCRC crc;
            if (useCRC16)
                crc = new CRC16();
            else
                crc = new CRC8();

            byte[] block;
            if (useBlock1K)
                block = new byte[1024];
            else
                block = new byte[128];
            sendDataBlocks(dataStream, 1, crc, block);

            sendEOT();
        }
    }

    public void sendDataBlocks(DataInputStream dataStream, int blockNumber, XCRC crc, byte[] block) throws IOException {
        int dataLength;
        while ((dataLength = dataStream.read(block)) != -1) {
            sendBlock(blockNumber++, block, dataLength, crc);
        }
    }

    public void sendEOT() throws IOException {
        int errorCount = 0;
        int character;
        while (errorCount < 10) {
            sendByte(EOT);
            character = readByte();

            if (character == ACK) {
                return;
            } else if (character == CAN) {
                throw new IOException("Transmission terminated");
            }
            errorCount++;
        }
    }

    public void sendBlock(int blockNumber, byte[] block, int dataLength, XCRC crc) throws IOException {
        int errorCount;
        int character;

        if (dataLength < block.length) {
            block[dataLength] = CPMEOF;
        }
        errorCount = 0;

        while (errorCount < MAXERRORS) {

            if (block.length == 1024)
                outputStream.write(STX);
            else //128
                outputStream.write(SOH);
            outputStream.write(blockNumber);
            outputStream.write(~blockNumber);

            outputStream.write(block);
            writeCRC(block, crc);
            outputStream.flush();

            while (true) {
                character = readByte();
                if (character == ACK) {
                    return;
                } else if (character == NAK) {
                    errorCount++;
                    break;
                } else if (character == CAN) {
                    throw new IOException("Transmission terminated");
                }
            }

        }

        throw new IOException("Too many errors caught, abandoning transfer");
    }

    private void writeCRC(byte[] block, XCRC crc) throws IOException {
        byte[] crcBytes = new byte[crc.getCRCLength()];
        long crcValue = crc.calcCRC(block);
        for (int i = 0; i < crc.getCRCLength(); i++) {
            crcBytes[crc.getCRCLength() - i - 1] = (byte) ((crcValue >> (8 * i)) & 0xFF);
        }
        outputStream.write(crcBytes);
    }

    /**
     * Receive file <br/>
     * <p>
     * This method support correct thread interruption, when thread is interrupted "cancel of transmission" will be send.
     * So you can move long transmission to other thread and interrupt it according to your algorithm.
     *
     * @param file file path for storing
     * @throws java.io.IOException
     */
    public void receive(Path file, boolean useCRC16) throws IOException {
        try (DataOutputStream dataOutput = new DataOutputStream(Files.newOutputStream(file))) {
            int available;
            // clean input stream
            if ((available = inputStream.available()) > 0) {
                inputStream.skip(available);
            }

            int character = requestTransmissionStart(useCRC16);

            XCRC crc;
            if (useCRC16)
                crc = new CRC16();
            else
                crc = new CRC8();


            processDataBlocks(crc, 1, character, dataOutput);
        }
    }

    public void processDataBlocks(XCRC crc, int blockNumber, int blockInitialCharacter, DataOutputStream dataOutput) throws IOException {
        // read blocks until EOT
        boolean result = false;
        boolean shortBlock;
        byte[] block;
        while (true) {
            int errorCount = 0;
            if (blockInitialCharacter == EOT) {
                // end of transmission
                sendByte(ACK);
                return;
            }

            //read and process block
            shortBlock = (blockInitialCharacter == SOH);
            try {
                block = readBlock(blockNumber, shortBlock, crc);
                dataOutput.write(block);
                blockNumber++;
                errorCount = 0;
                result = true;
                sendByte(ACK);
            } catch (InvalidBlockException e) {
                errorCount++;
                if (errorCount == MAXERRORS) {
                    interruptTransmission();
                    throw new IOException("Transmission aborted, error count exceeded max");
                }
                sendByte(NAK);
                result = false;
            } catch (RepeatedBlockException e) {
                //thats ok, accept and wait for next block
                sendByte(ACK);
            } catch (SynchronizationLostException e) {
                //fatal transmission error
                interruptTransmission();
                throw new IOException("Fatal transmission error", e);
            }

            //wait for next block
            blockInitialCharacter = readNextBlockStart(result);
        }
    }

    public void sendByte(byte b) throws IOException {
        outputStream.write(b);
        outputStream.flush();
    }

    /**
     * Request transmission start and return first byte of "first" block from sender (block 1 for XModem, block 0 for YModem)
     *
     * @param useCRC16
     * @return
     * @throws java.io.IOException
     */
    public int requestTransmissionStart(boolean useCRC16) throws IOException {
        int character;
        int errorCount = 0;
        byte requestStartByte;
        if (!useCRC16) {
            requestStartByte = NAK;
        } else {
            requestStartByte = ST_C;
        }

        // wait for first block start
        // request transmission start (will be repeated after 10 second timeout for 10 times)
        sendByte(requestStartByte);
        while (true) {
            character = readByte();

            if (character == SOH || character == STX) {
                return character;
            }
        }
    }

    public int readNextBlockStart(boolean lastBlockResult) throws IOException {
        int character;
        int errorCount = 0;
        while (true) {
            while (true) {
                character = readByte();
                if (character == SOH || character == STX || character == EOT) {
                    return character;
                }
            }
            // repeat last block result and wait for next block one more time
//                if (++errorCount < MAXERRORS) {
//                    sendByte(lastBlockResult ? ACK : NAK);
//                } else {
//                    interruptTransmission();
//                    throw new RuntimeException("Timeout, no data received from transmitter");
//                }
        }
    }

    private void shortSleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            try {
                interruptTransmission();
            } catch (IOException ignore) {
            }
            throw new RuntimeException("Transmission was interrupted", e);
        }
    }

    /**
     * send CAN to interrupt seance
     *
     * @throws java.io.IOException
     */
    public void interruptTransmission() throws IOException {
        sendByte(CAN);
        sendByte(CAN);
    }

    public byte[] readBlock(int blockNumber, boolean shortBlock, XCRC crc) throws IOException, RepeatedBlockException, SynchronizationLostException, InvalidBlockException {
        byte[] block;

        if (shortBlock) {
            block = shortBlockBuffer;
        } else {
            block = longBlockBuffer;
        }
        byte character;

        character = readByte();

        if (character == blockNumber - 1) {
            // this is repeating of last block, possible ACK lost
            throw new RepeatedBlockException();
        }
        if (character != blockNumber) {
            // wrong block - fatal loss of synchronization
            throw new SynchronizationLostException();
        }

        character = readByte();

        if (character != ~blockNumber) {
            throw new InvalidBlockException();
        }

        // data
        for (int i = 0; i < block.length; i++) {
            block[i] = readByte();
        }

        while (true) {
            if (inputStream.available() >= crc.getCRCLength()) {
                if (crc.calcCRC(block) != readCRC(crc)) {
                    throw new InvalidBlockException();
                }
                break;
            }

            shortSleep();

        }

        return block;
    }

    private long readCRC(XCRC crc) throws IOException {
        long checkSumma = 0;
        for (int j = 0; j < crc.getCRCLength(); j++) {
            checkSumma = (checkSumma << 8) + inputStream.read();
        }
        return checkSumma;
    }

    private byte readByte() throws IOException {
        while (true) {
            if (inputStream.available() > 0) {
                int b = inputStream.read();
                return (byte) b;
            }
            shortSleep();
        }
    }

    public class RepeatedBlockException extends Exception {
    }

    public class SynchronizationLostException extends Exception {
    }

    public class InvalidBlockException extends Exception {
    }
}
