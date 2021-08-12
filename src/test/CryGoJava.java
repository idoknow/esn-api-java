package test;

import crypto.RSAUtil;

import java.nio.charset.StandardCharsets;

public class CryGoJava {
    public static String pubKeyFromGo="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2Wfj5T9lwALueG15t62lO6517QxUyeU/dSdSIvucKSPh9wYDiZfrez0T3HpjXSRBrIi5cnQH3f5BPBnDVy7WhK2i8QU2ebMHGMbRxbNuZAGhvZ/DUfjBLQRMSzYaZvqXU3CCrnvpagvs9T87k1LJDwi9woTe4GlZ+pqkfODclvpo40fNczFsh6IX8qrysBvIFOeUWExFUD+Dl/oOkGFc2M0MLQ6O1RhupHz/T1jAGlN3sNol91mK1VAbjhMGY/uJvfbKJhdSm4U1gjfStL2P6Zzwjybf24A5IOY2oCMpiLTGqfuXbveSVtkoAztiKCF6sqGSOiOIfXFIGlI4s3hYnQIDAQAB";
    public static void main(String[] args) {
        try {
            String en=RSAUtil.encByGoPubKey(pubKeyFromGo,"{\"User\":\"root\",\"Pass\":\"changeMe\"}");
            byte[] b=en.getBytes(StandardCharsets.UTF_8);
            int index=0;
            for (byte b1:b){
                System.out.print(b1+" ");
                index++;
                if (index>=30)
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
