/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package authenticator.CTAP;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import javax.swing.table.DefaultTableModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author mateus.campos
 */
public class Authenticator {
    private List<Credential> credentials;
    
    public Authenticator(){
        credentials = new ArrayList<Credential>();
    }
    
    //Gera uma tabela com as credenciais geradas
    public DefaultTableModel listCredentials(){
        DefaultTableModel model;
        model = new DefaultTableModel(new Object [][] {}, new String [] {"ID", "Usuário", "Servidor"});
        for(Credential credential : credentials){
            model.addRow(new Object[]{encode(credential.getId()),
            credential.getUser().getUserUsername(),
            credential.getRelyingParty().getRpName()});
        }
        return model;
    }
    
    
    public JSONObject makeCredential (JSONObject makeCredentialOptions) throws NoSuchAlgorithmException, JSONException, InvalidAlgorithmParameterException{
        //Otenção do objeto JSON
        JSONObject publicKey = makeCredentialOptions.getJSONObject("publicKey");     
        //Criação do objeto usuário
        PublicKeyCredentialUserEntity user = new PublicKeyCredentialUserEntity(
                                                publicKey.getJSONObject("user").getString("displayName"),
                                                publicKey.getJSONObject("user").getString("name"),
                                                publicKey.getJSONObject("user").getString("id"));
        //Criação do objeto servidor
        PublicKeyCredentialRpEntity rp = new PublicKeyCredentialRpEntity(
                                                publicKey.getJSONObject("rp").getString("name"),
                                                publicKey.getJSONObject("rp").getString("id"));
        //Criação da credencial
        Credential credential = new Credential(user, rp);
        //Adição da credencial na lista de credenciais do autenticador
        credentials.add(credential);        
        //Criação do objeto clientData que contém os valores
            //type com webauthn.create
            //origin com o endereço do servidor
            //challenge com o valor recebido no objeto JSON
            //crossOrigin com false
        ClientDataObj clientDataJSON = new ClientDataObj(
                publicKey.getString("challenge").replace("/", "_").replace("+", "-").replace("=", ""), 
                makeCredentialOptions.getString("Referer"),
                "webauthn.create");     
        //Criação do objeto authData
        AuthData authData = new AuthData(
                publicKey.getJSONObject("rp").getString("id"), 
                credential.getCredentialData());        
        //Criação do objeto attestationObject que contém:
            //o objeto authData, 
            //um valor ftm definido como none 
            //e o objeto attStmt vazio
        AttestationObject attestationObject = new AttestationObject(
                authData.getAuthData(credential.getCounter()));  
        //Criação do objeto JSON que reúne o valor response
            // adicionando o clientDataJSON e o attestationObject
        JSONObject response = new JSONObject();
        response.accumulate(
                "clientDataJSON", 
                encode(clientDataJSON.toString().getBytes()));
        response.accumulate(
                "attestationObject", 
                encode(attestationObject.getAttestationObject()));
        JSONObject webAuthNCredential = new JSONObject();
        //Criação do objeto JSON que é enviado ao servidor.
        webAuthNCredential.accumulate(
                "id", 
                encode(credential.getId()));
        webAuthNCredential.accumulate(
                "rawId", 
                encode(credential.getId()));
        webAuthNCredential.accumulate(
                "response", 
                response);
        webAuthNCredential.accumulate(
                "type", 
                "public-key");
        return webAuthNCredential;
    } 
    
    public JSONObject getAssertion (JSONObject getCredentialOptions) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, SignatureException, JSONException{
        //Otenção do objeto JSON
        JSONObject publicKey = getCredentialOptions.getJSONObject("publicKey");
        //Busca pela credencial no autenticador 
            //baseada na lista de credenciais aceitas
        Credential credential = getCredentialById(publicKey.getJSONArray("allowCredentials"));
        if(credential == null){
            return null;
        }
        //Criação do objeto AuthData
        AuthData authData = new AuthData(credential.getRelyingParty().getUrl(), null);   
        //
        ClientDataObj clientDataJSON = new ClientDataObj(
                publicKey.getString("challenge").replace("/", "_").replace("+", "-").replace("=", ""),
                getCredentialOptions.getString("Referer"),"webauthn.get");
        //Gera um Hash SHA-256 do objeto ClientDataJSON
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(clientDataJSON.toString().getBytes());
        byte[] clientDataHash = digest.digest();
        //Reúne o objeto AuthData e o Hash gerado anteriormente em um array de bytes
            //para serem assinados pela chave privada da credencial
        ByteBuffer byteBuffer = ByteBuffer.allocate(authData.getAuthData(credential.getCounter()).length + clientDataHash.length);
        byteBuffer.put(authData.getAuthData(credential.getCounter()));
        byteBuffer.put(clientDataHash);
        byte[] toSign = byteBuffer.array();
        //cria o objeto response e acumula os objetos
            //clientDataJSON, authenticatorData, signature e userHandle
        JSONObject response = new JSONObject();
        response.accumulate("clientDataJSON", encode(clientDataJSON.toString().getBytes()));
        response.accumulate("authenticatorData", encode(authData.getAuthData(credential.getCounter())));
        response.accumulate("signature", encode(credential.signChallenge(toSign)));
        response.accumulate("userHandle", credential.getUser().getId().replace("/", "_").replace("+", "-").replace("=", ""));
        //criar o objeto que é enviado ao servidor
        JSONObject webAuthNCredential = new JSONObject();
        webAuthNCredential.accumulate("id", encode(credential.getId()));
        webAuthNCredential.accumulate("rawId", encode(credential.getId()));
        webAuthNCredential.accumulate("response", response);
        webAuthNCredential.accumulate("type", "public-key");
        return webAuthNCredential;
    }
    
    //Busca a credencial pelo ID
    private Credential getCredentialById (JSONArray allowCredentials) throws JSONException {
        ArrayList<String> listdata = new ArrayList<String>();     
        if (allowCredentials != null) { 
            for (int i=0;i<allowCredentials.length();i++){ 
                listdata.add(allowCredentials.getJSONObject(i).getString("id"));
            } 
        }
        for(Credential credential : credentials){
            for (String id : listdata){
                if (Base64.getEncoder().encodeToString(credential.getId()).equals(id)){
                    return credential;
                }
            }    
        }
        return null;
    }
    
    public String encode (byte[] bytes){
        String string = Base64.getEncoder().encodeToString(bytes);
        return string.replace("/", "_").replace("+", "-").replace("=", "");
    }
}
