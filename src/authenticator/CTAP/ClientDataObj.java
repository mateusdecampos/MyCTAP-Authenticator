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
public class ClientDataObj {
    String challenge, origin, type;

    public ClientDataObj(String challenge, String origin, String type) {
        this.challenge = challenge;
        this.origin = origin;
        this.type = type;
    }

//    public String getChallenge() {
//        return challenge;
//    }
//
//    public String getOrigin() {
//        return origin;
//    }
//
//    public String getType() {
//        return type;
//    }

    @Override
    public String toString() {
        return "{" + "\"challenge\":\"" + challenge + "\",\"origin\":\"" + origin + "\",\"type\":\"" + type + "\",\"crossOrigin\":false}";
    }
    
}
