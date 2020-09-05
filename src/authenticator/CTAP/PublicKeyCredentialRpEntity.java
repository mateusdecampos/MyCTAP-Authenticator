/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package authenticator.CTAP;
/**
 *
 * @author Mateus
 */
public class PublicKeyCredentialRpEntity {
    private String rpName, url;

    public PublicKeyCredentialRpEntity(String rpName, String url) {
        this.rpName = rpName;
        this.url = url;
    }
    
    public String toString(){
        return "Server: " + rpName + "\nURL: " + url;
    }  

    public String getRpName() {
        return rpName;
    }

    public String getUrl() {
        return url;
    }
    
}
