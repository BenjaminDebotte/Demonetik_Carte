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
    protected final static byte[] PIN = StringUtils.convertASCIIStringToByteArray("1234");
    protected final static byte[] SW_PIN_VERIFICATION_NOT_SUCCESSFUL = StringUtils.convertHexStringToByteArray("6300");
    
    private short counter;
    private int debit_amount;
    
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
                //ret = ConcatArrays(HCE, SW_OK);
                ret = SW_OK;
                break;
            case (byte)0x10:
                Log.i(TAG, "this is a get counter");
                byte [] count_resp = convertShortToByteArray(++counter);
                ret =  ConcatArrays(count_resp, SW_OK);
                break;
            case (byte)0x40:  //Debit
                Log.i(TAG, "This is a Debit apdu");
            	ret = get_debit_amount(commandApdu);
            	break;
            case (byte)0x20:   //PIN
            	Log.i(TAG,"This is a verify PIN");
            	ret = verify(commandApdu);
            	break;
                
            default:
                ret = SW_INS_NOT_SUPPORTED;
                break;
        }
        Log.i(TAG, "Response = "+StringUtils.convertByteArrayToHexString(ret));
        return ret;
    }

    public byte[] verify(byte[] Apdu){
    	if(Apdu[4] != 4)
    		return SW_PIN_VERIFICATION_NOT_SUCCESSFUL;
    	
    	if(Apdu[5] == PIN[0] && Apdu[6] == PIN[1] && Apdu[7] == PIN[2] && Apdu[8] == PIN[3])
    		return SW_OK;
    	else
    		return SW_PIN_VERIFICATION_NOT_SUCCESSFUL;
    }
    
    public byte[] get_debit_amount(byte[] Apdu){
    	debit_amount = Apdu[5];
    	Log.i(TAG, "montant de :"+ debit_amount);
    	
    	return SW_OK;
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
