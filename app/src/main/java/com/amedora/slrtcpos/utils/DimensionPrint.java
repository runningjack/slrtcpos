package com.amedora.slrtcpos.utils;

import java.io.UnsupportedEncodingException;
import android.util.Log;

/**
 * Created by USER on 2/29/2016.
 */
public class DimensionPrint {
    @SuppressWarnings("unused")
    public String OneDimensionPrint(String str) {
        StringBuilder hex = new StringBuilder();
        hex.append("1D6B45");
        hex.append("" + IntToHexString(StringToHex(str).length() / 2));
        hex.append(StringToHex(str));
        hex.append("0A");
        return hex.toString();

    }

    @SuppressWarnings("unused")
    public String TwoDimensionPrint(String str, String size) {
        StringBuilder hex = new StringBuilder();
		/*hex.append("1D286B03003143");
		try {
			hex.append("" + IntToHexString(Integer.parseInt(size)));
		} catch (Exception e) {
			hex.append("" + IntToHexString(0));
		}

		hex.append("1D286B");*/

        Log.e("TwoDimensionPrint", "str:"+str);

        hex.append("1D286B0300314308");
        hex.append("1D286B");

        hex.append(""
                + IntToHexString((StringToHex(str).length() / 2 + 3) % 256));
        hex.append(""
                + IntToHexString((StringToHex(str).length() / 2 + 3) / 256));
        hex.append("315030");
        hex.append(StringToHex(str));
        hex.append("1D286B0300315130");
        return hex.toString();

    }

    public String StringToHex(String str) {

        byte[] b;
        String s = "";
        try {
            b = str.getBytes("GBK");

            for (int i = 0; i < b.length; i++) {
                String hex = Integer.toHexString(b[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                s = s + hex.toUpperCase();
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return s;
    }



    public String IntToHexString(int num) {

        String value = Integer.toHexString(num);

        if (value != null && value.length() < 2) {
            if (value.length() == 0) {
                value = "00";
            } else if (value.length() == 1) {
                value = "0" + value;
            }
        }

        return value.toUpperCase();

    }
}
