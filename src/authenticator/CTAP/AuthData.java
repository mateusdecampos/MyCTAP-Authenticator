/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package authenticator.CTAP;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Mateus
 */
public class AuthData {
    private byte[] rpIdHash;
    private byte[] attestedCredentialData;

    public AuthData(String rpId, byte[] credentialData) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(rpId.getBytes());
        this.rpIdHash = md.digest();
        this.attestedCredentialData = credentialData;
    }
    
    public byte[] getAuthData(int authCounter){
        byte flags = 0x00;
        flags |= 0x01; // presença do usuário
        flags |= (0x01 << 2); // verificação do usuário
        if (attestedCredentialData != null){
            flags |= (0x01 << 6); // presença de attestedCredentialData
        }
        ByteBuffer authData = ByteBuffer.allocate(37 + (attestedCredentialData == null ? 0 : attestedCredentialData.length));
        authData.put(rpIdHash);
        authData.put(flags);
        authData.putInt(authCounter);
        if (attestedCredentialData != null) {
            authData.put(attestedCredentialData);
        }
        return authData.array();
    }
}