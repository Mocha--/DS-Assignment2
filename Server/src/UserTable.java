import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;

public class UserTable {

	public static ArrayList<User> users = new ArrayList<User>();

	public static User find(String id, String password) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, SignatureException, IOException, JWTVerifyException{
		for(User user: UserTable.users){
			String decryptedPassword = new JWTVerifier(Server.secret).verify(user.password).get("password").toString();
			if(user.id.equals(id) && decryptedPassword.equals(password)){
				return user;
			}
		}
		return null;
	}

	public static User findById(String id){
		for( User user: UserTable.users){
			if(user.id.equals(id)){
				return user;
			}
		}
		return null;
	}

	public static User create(String id, String password){
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("password", password);
		String encryptedPassword = new JWTSigner(Server.secret).sign(payload);
		
		User user = new User(id, encryptedPassword);
		UserTable.users.add(user);
		return user;
	}

}
