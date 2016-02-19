package com.example.demonetik_carte;

import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
import java.util.Arrays;

public class HostApduServiceTest extends HostApduService {
	public static final String TAG = "HceTest";

    protected final static byte[] HCE = StringUtils.convertASCIIStringToByteArray("HCE");
    protected final static byte[] SW_OK = StringUtils.convertHexStringToByteArray("9000");
    protected final static byte[] SW_INS_NOT_SUPPORTED = StringUtils.convertHexStringToByteArray("6D00");
    protected final static byte[] PIN = StringUtils.convertASCIIStringToByteArray("1234");
    protected final static byte[] SW_PIN_VERIFICATION_NOT_SUCCESSFUL = StringUtils.convertHexStringToByteArray("6300");
    
    private int debit_amount;
    private String Nom_porteur;
    private String Num_carte;

    
    public HostApduServiceTest() {
        debit_amount =0;
        Nom_porteur = "Jean Ticipe ";
        Num_carte = "7253 3256 7895 1245";
        
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        byte [] ret = null;
        Log.i(TAG, "Received APDU: " + StringUtils.convertByteArrayToHexString(commandApdu));
        Log.i(TAG, "Received APDU_: " + commandApdu);
        
        switch(commandApdu[1]) {
            case (byte)0xA4:
                Log.i(TAG, "this is a select apdu");
                ret = info_porteur();
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
    	
    	if(Apdu[4] == 1)
    		debit_amount = (int)Apdu[5] & 0x000000FF;
    	else
    		debit_amount = ((((int)Apdu[5] & 0x000000FF) << 8) | ((int)Apdu[6] & 0x000000FF));
    	
    	Log.i(TAG, "montant de :"+ Integer.toHexString(debit_amount));
        Intent intent = new Intent(getApplicationContext(), AfficheInfo.class);
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("montant", String.valueOf(debit_amount));
        intent.putExtra("nom", Nom_porteur);
        intent.putExtra("num", Num_carte);
        startActivity(intent);
    	
    	return SW_OK;
    }
    
    public byte[] info_porteur(){ 	
    	byte[] info_porteur = null;
    	byte[] porteur  = StringUtils.convertASCIIStringToByteArray(Nom_porteur);
    	byte[] carte    = StringUtils.convertASCIIStringToByteArray(Num_carte);
    	
    	info_porteur = ConcatArrays( porteur, carte);
    	
    	Log.i(TAG, StringUtils.convertByteArrayToASCIIString(info_porteur));
    	
    	return info_porteur;
    	
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
