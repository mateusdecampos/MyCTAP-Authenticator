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
public class PublicKeyCredentialUserEntity {
    private String userName, userUsername, id;

    public PublicKeyCredentialUserEntity(String userName, String userUsername, String id) {
        this.userName = userName;
        this.userUsername = userUsername;
        this.id = id;
    }
    
    public String toString(){
        return "Nome: " + userName + "\nUtilizador: " + userUsername;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public String getId() {
        return id;
    }
}
