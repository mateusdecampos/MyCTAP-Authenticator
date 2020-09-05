/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package authenticator.CTAP;

import com.upokecenter.cbor.CBORObject;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mateus.campos
 */
public class Credential {
    private KeyPair keyPair;
    private byte[] id;
    private PublicKeyCredentialUserEntity user;
    private PublicKeyCredentialRpEntity relyingParty;
    private SecureRandom random;
    private int counter;

    public Credential(PublicKeyCredentialUserEntity user, PublicKeyCredentialRpEntity relyingParty) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        this.id = new byte[32];
        this.random = new SecureRandom();
        this.random.nextBytes(id);
        keyPair = newKeyPair();
        this.user = user;
        this.relyingParty = relyingParty;
        counter = 0;
    }

    private KeyPair newKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException{
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec spec = new ECGenParameterSpec("secp256r1");
        kpg.initialize(spec);
        KeyPair keyPair = kpg.generateKeyPair();
        return keyPair;
    }

    public PublicKeyCredentialUserEntity getUser() {
        return user;
    }

    public PublicKeyCredentialRpEntity getRelyingParty() {
        return relyingParty;
    }

    public byte[] getId() {
        return id;
    }
    
    public int getCounter() {
        return counter;
    }
    
    public byte[] signChallenge(byte[] challenge) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, SignatureException, SignatureException, NoSuchAlgorithmException{
        Signature sig = null;
        try {
            sig = Signature.getInstance("SHA256WithECDSA");
            sig.initSign(keyPair.getPrivate());
            sig.update(challenge);
            byte[] signatureBytes = sig.sign();
            return signatureBytes;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Credential.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
 
    private byte[] coseEncodePublicKey(){
        ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();
        ECPoint point = ecPublicKey.getW();
        byte[] xVariableLength = point.getAffineX().toByteArray();
        byte[] yVariableLength = point.getAffineY().toByteArray();
        byte[] x = toUnsignedFixedLength(xVariableLength, 32);
        assert x.length == 32;
        byte[] y = toUnsignedFixedLength(yVariableLength, 32);
        assert y.length == 32;
        CBORObject coseEncodePublicKey = CBORObject.NewMap()
                .Add(1, 2)
                .Add(3, -7)
                .Add(-1, 1)
                .Add(-2, x)
                .Add(-3, y);
        return coseEncodePublicKey.EncodeToBytes();
    }
    
    public byte[] getCredentialData(){
        byte[] coseEncodePublicKey = coseEncodePublicKey();
        ByteBuffer credentialData = ByteBuffer.allocate(16 + 2 + id.length + coseEncodePublicKey.length);
        credentialData.position(16);
        credentialData.putShort((short) id.length); // L
        credentialData.put(id); // credentialId
        credentialData.put(coseEncodePublicKey);

        return credentialData.array();
    }
         
    private static byte[] toUnsignedFixedLength(byte[] arr, int fixedLength) {
        byte[] fixed = new byte[fixedLength];
        int offset = fixedLength - arr.length;
        int srcPos = Math.max(-offset, 0);
        int dstPos = Math.max(offset, 0);
        int copyLength = Math.min(arr.length, fixedLength);
        System.arraycopy(arr, srcPos, fixed, dstPos, copyLength);
        return fixed;
    }
}
