/**
 * Copyright (C) 2015 Johannes Taelman
 *
 * This file is part of Axoloti.
 *
 * Axoloti is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Axoloti is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Axoloti. If not, see <http://www.gnu.org/licenses/>.
 */
package axoloti;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 *
 * @author jtaelman
 */
public class HWSignature {

    public static final String PRIVATE_KEY_FILE = "private_key.der";
    public static final String PUBLIC_KEY_FILE = "public_key.der";
    public static final int length = 256;

    static PrivateKey ReadPrivateKey(String privateKeyPath) throws Exception {
        File f = new File(privateKeyPath);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) f.length()];
        dis.readFully(keyBytes);
        dis.close();
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(pkcs8EncodedKeySpec);
    }

    static PublicKey ReadPublicKey(String publicKeyPath) throws Exception {
        File f = new File(publicKeyPath);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) f.length()];
        dis.readFully(keyBytes);
        dis.close();
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);
    }

    static public void printByteArray(byte[] b) {
        for (int i = 0; i < b.length; i++) {
            if ((i % 32) == 0) {
                System.out.println();
            }
            System.out.print(String.format("%02X ", (int) b[i] & 0xFF));
        }
        System.out.println();
    }

    public static byte[] Sign(byte[] cpuid) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(ReadPrivateKey(PRIVATE_KEY_FILE));
        sig.update(cpuid);
        byte[] signature = sig.sign();
        return signature;
    }

    public static boolean Verify(byte[] cpuid, byte[] signature) throws Exception {
        Signature sig2 = Signature.getInstance("SHA256withRSA");
        sig2.initVerify(ReadPublicKey(PUBLIC_KEY_FILE));
        sig2.update(cpuid);
        return sig2.verify(signature);
    }

}
