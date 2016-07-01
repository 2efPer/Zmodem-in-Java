package zmodem.xfer.util;

/**
 * Created by Muzeffer on 2016/6/30.
 */
public interface XCRC {
    int getCRCLength();
    long calcCRC(byte[] block);
}
