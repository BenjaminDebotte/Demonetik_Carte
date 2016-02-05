package com.example.demonetik_carte;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by Joan on 02/12/2015.
 */
public class HostApduServiceTest extends HostApduService {
    public static final String TAG = "HceTest";

    protected final static byte[] HCE = StringUtils.convertASCIIStringToByteArray("HCE");
    protected final static byte[] SW_OK = StringUtils.convertHexStringToByteArray("9000");
    protected final static byte[] SW_INS_NOT_SUPPORTED = StringUtils.convertHexStringToByteArray("6D00");

    private short counter;

    public HostApduServiceTest() {
        counter = 0;
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        byte [] ret = null;
        Log.i(TAG, "Received APDU: " + StringUtils.convertByteArrayToHexString(commandApdu));
        switch(commandApdu[1]) {
            case (byte)0xA4:
                Log.i(TAG, "this is a select apdu");
                ret = ConcatArrays(HCE, SW_OK);
                break;
            case (byte)0x10:
                Log.i(TAG, "this is a get counter");
                byte [] count_resp = convertShortToByteArray(++counter);
                ret =  ConcatArrays(count_resp, SW_OK);
                break;
            default:
                ret = SW_INS_NOT_SUPPORTED;
                break;
        }
        Log.i(TAG, "Response = "+StringUtils.convertByteArrayToHexString(ret));
        return ret;
    }

    @Override
    public void onDeactivated(int reason) {

    }

    /**
     * Utility method to concatenate two byte arrays.
     *
     * @param first
     *            First array
     * @param rest
     *            Any remaining arrays
     * @return Concatenated copy of input arrays
     */
    public static byte[] ConcatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static byte[] convertShortToByteArray(short s) {
        byte [] ret = new byte[2];
        ret[0] = (byte)(s >> 8);
//        ret[1] = (byte)(s & (short)0x00FF);
        ret[1] = (byte)s;
        return ret;
    }
}
