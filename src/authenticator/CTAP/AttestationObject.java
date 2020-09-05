/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package authenticator.CTAP;

import com.upokecenter.cbor.CBORObject;

/**
 *
 * @author Mateus
 */
public class AttestationObject {
    private CBORObject attestationObject;
    private byte[] authData;

    public AttestationObject(byte[] authData){
        this.authData = authData;
        attestationObject = CBORObject.NewMap()
                .Add("authData", authData)
                .Add("fmt","none")
                .Add("attStmt", CBORObject.NewMap());        
    }    

    public byte[] getAttestationObject() {
        return attestationObject.EncodeToBytes();
    } 
}
