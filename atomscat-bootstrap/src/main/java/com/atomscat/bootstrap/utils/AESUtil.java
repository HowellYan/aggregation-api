package com.atomscat.bootstrap.utils;



import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;


public class AESUtil {

	private static final int SRC_POS = 0;
	private static final int DEST_POS = 0;
	private static final int LENGTH = 16;




	public static void main(String[] args) {
//		String pvgDwmcvfs1uV3d1 = decryptIV("PVGDwmcvfs1uV3d1", "7k7HI/Sz2Ayx5zDyqTJK9qcPD5d8+c60O2/8u+iIqbugxyK0IIDkZZ2MV/FE4Ex0arYbRlfKEsrIw0dGq8AjlJ6nJCJsIvKhl+Sef/PU+1tC4UfhWHcow9nMVIvdoSWSj4tJtGW2Qj1EX6ncU0UuCMv5bocVH3J9WFDkxgMA5KI=");
//		System.out.println("pvgDwmcvfs1uV3d1 = " + pvgDwmcvfs1uV3d1);
//

		File file = new File("C:\\tools\\work\\github\\aggregation-api\\atomscat-bootstrap\\src\\main\\resources\\new+2.txt");
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Map map = objectMapper.readValue(file, Map.class);
			String encryptIV = encryptIV( "PVGDwmcvfs1uV3d1",JSONObject.toJSONString(map));
			System.out.println("encryptIV = " + encryptIV);
			System.out.println(decryptIV("PVGDwmcvfs1uV3d1", encryptIV));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		String text="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";
//		System.out.println(decryptIV("PVGDwmcvfs1uV3d1", text));

	}

	/**
	 * 动态iv加密
	 *
	 * @param dataPassword 密钥
	 * @param cleartext    明文
	 * @return
	 */
	public static String encryptIV(String dataPassword, String cleartext) {
		try {
			SecretKeySpec key = new SecretKeySpec(dataPassword.getBytes(StandardCharsets.UTF_8), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			//新增动态iv
			byte[] iv = new byte[LENGTH];
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextBytes(iv);
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
			byte[] encryptedData = cipher.doFinal(cleartext.getBytes(StandardCharsets.UTF_8));
			byte[] ivEncryptData = new byte[iv.length + encryptedData.length];
			System.arraycopy(iv, SRC_POS, ivEncryptData, DEST_POS, iv.length);
			System.arraycopy(encryptedData, SRC_POS, ivEncryptData, iv.length, encryptedData.length);
			return Base64.getEncoder().encodeToString(ivEncryptData);
		} catch (Exception e) {

			return "";
		}
	}

	/**
	 * 动态iv解密
	 *
	 * @param dataPassword 密钥
	 * @param encrypted    密文
	 * @return
	 */
	public static String decryptIV(String dataPassword, String encrypted) {
		try {
			byte[] byteMi = Base64.getDecoder().decode(encrypted);
			SecretKeySpec key = new SecretKeySpec(dataPassword.getBytes(StandardCharsets.UTF_8), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] iv = new byte[LENGTH];
			System.arraycopy(byteMi, SRC_POS, iv, DEST_POS, LENGTH);
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
			byte[] encryptedData = new byte[byteMi.length - iv.length];
			System.arraycopy(byteMi, iv.length, encryptedData, DEST_POS, encryptedData.length);
			byte[] decryptedData = cipher.doFinal(encryptedData);
			return new String(decryptedData, StandardCharsets.UTF_8);
		} catch (Exception e) {
			System.out.println(e);

			return "";
		}
	}
	/**解密
	 * 解密的时候要传入byte数组
	 * @param content  待解密内容
	 * @param password 解密密钥
	 * @return
	 */
	public static byte[] decrypt(byte[] content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128, new SecureRandom(password.getBytes()));
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(content);
			return result; // 加密
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**将二进制转换成16进制
	 * @param buf
	 * @return
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	/**将16进制转换为二进制
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length()/2];
		for (int i = 0;i< hexStr.length()/2; i++) {
			int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
			int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}



}
