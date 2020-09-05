/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package authenticator.CTAP;

import java.util.List;

/**
 *
 * @author Mateus
 */
class PublicKeyCredentialDescriptor {
    private String type, id;
    private List<String> transports;

    public PublicKeyCredentialDescriptor(String type, String id, List<String> transports) {
        this.type = type;
        this.id = id;
        this.transports = transports;
    }
}
