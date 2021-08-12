package crypto;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


public class RSAUtil {
// * RSA公钥加密
// *
// * @param str
// *            加密字符串
// * @param publicKey
// *            公钥
// * @return 密文
// * @throws Exception
// *             加密过程中的异常信息
// */
//public static String encrypt( String str, String publicKey ) throws Exception{
//    //base64编码的公钥
//    byte[] decoded = Base64.decodeBase64(publicKey);
//    RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
//    //RSA加密
//    Cipher cipher = Cipher.getInstance("RSA");
//    cipher.init(Cipher.ENCRYPT_MODE, pubKey);
//    String outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
//    return outStr;
//}
//
//    /**
//     * RSA私钥解密
//     *
//     * @param str
//     *            加密字符串
//     * @param privateKey
//     *            私钥
//     * @return 铭文
//     * @throws Exception
//     *             解密过程中的异常信息
//     */
//    public static String decrypt(String str, String privateKey) throws Exception{
//        //64位解码加密后的字符串
//        byte[] inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
//        //base64编码的私钥
//        byte[] decoded = Base64.decodeBase64(privateKey);
//        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
//        //RSA解密
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.DECRYPT_MODE, priKey);
//        String outStr = new String(cipher.doFinal(inputByte));
//        return outStr;
//    }

    public static byte[] encryptByPublicKey(byte[] data, byte[] key) throws Exception {

        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //初始化公钥
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
        //产生公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);

        //数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return cipher.doFinal(data);
    }
    public static byte[] decryptByPublicKey(byte[] data, byte[] key) throws Exception {

        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //初始化公钥
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
        //产生公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
        //数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        return cipher.doFinal(data);
    }

    /**
     * 使用Go生成的公钥加密
     * @param pubkey_from_go
     * @param plainText
     * @return
     * @throws Exception
     */
    public static String encByGoPubKey(String pubkey_from_go,String plainText) throws Exception {
        //加解密类
        Cipher cipher = Cipher.getInstance("RSA");//Cipher.getInstance("RSA/ECB/PKCS1Padding");
        byte[] plainTextBytes = plainText.getBytes();
        //用Go语言产生的公钥加密
        PublicKey pubkey_go=getPublicKey(pubkey_from_go);
        cipher.init(Cipher.ENCRYPT_MODE, pubkey_go);
        byte[] enBytes = cipher.doFinal(plainTextBytes);
        return (new BASE64Encoder()).encode(enBytes);
    }
    /**
     * 得到公钥
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = (new BASE64Decoder()).decodeBuffer(key);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

}
