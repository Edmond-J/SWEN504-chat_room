package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cipher.RSA;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Login implements Initializable {
	static String serverName;
	static int serverPort;
	Socket client;
	PrintWriter out;
	BufferedReader in;
	String configPath;
	String algorithm;
	int bit;
	@FXML
	TextField userName, password;
	@FXML
	ImageView userAvatar;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		configPath = System.getProperty("user.home")+"\\EdmondChatRoom\\";
		readConfig();
		try {
			client = new Socket(serverName, serverPort);
			out = new PrintWriter(client.getOutputStream(), true);
			InputStreamReader inputStream = new InputStreamReader(client.getInputStream());
			in = new BufferedReader(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readConfig() {
		File file = new File(configPath+"config.txt");
		if (!file.exists()) {
			serverName = "Localhost";
			serverPort = 6069;
			algorithm = "AES";
			bit = 1024;
//			userAvatarPath = configPath+"user_avatar\\default_avatar.png";
//			userAvatarPath=configPath+"user_avatar\\"+userName.getText()+".png";
		} else {
			// 读取配置文件内的内容
		}
	}

	@FXML
	public void login() {
		if (userName.getText().length() == 0 || password.getText().length() == 0) {
			System.out.println("user name or password can't be empty");
			return;
		}
		try {
			System.out.println("Connecting to server："+serverName+" ，port："+serverPort);
			System.out.println("Local socket："+client.getLocalSocketAddress());
			File file = new File(configPath+"server_rsa\\public");
			if (!file.exists()) {
				requireKey(client);
			}
			validate(client);
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void requireKey(Socket client) {
		try {
			JsonObject messageObject = new JsonObject();
			messageObject.addProperty("req_type", "public_key");
			messageObject.addProperty("user", userName.getText());// 加入user为了防止恶意攻击,需要用一个合理的user身份证明，使用用户输入值不可靠
			Gson gson = new Gson();
			out.println(gson.toJson(messageObject));
			File file = new File(configPath+"rsa_server\\publicKey");
			String response = in.readLine();
			JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
			String resType = jsonObject.get("res_type").getAsString();
			if (resType.equals("key")) {
				RSA.writeStringToFile(file, jsonObject.get("key").getAsString());
			}
//			String result = in.readLine();
//			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void validate(Socket client) {
		try {
			JsonObject messageObject = new JsonObject();
			messageObject.addProperty("req_type", "validate");
			messageObject.addProperty("user", userName.getText());
			messageObject.addProperty("password", password.getText());
//			messageObject.addProperty("algorithm", algorithm);
//			messageObject.addProperty("bit", bit);
//			messageObject.addProperty("key", bit);
			Gson gson = new Gson();
			String toSend=gson.toJson(messageObject);
			out.println(RSA.encrypt(toSend, RSA.getPublicFromFile(configPath+"rsa_server\\publicKey")));
			// 发送用户名密码
			String result = in.readLine();
			System.out.println(result);
			if (result.contains("200")) {
				// 读取token, friend list
				// 启动聊天界面
				System.out.println("login succeed");
			} else if (result.contains("300")) {
				System.out.println("no such user or user name and password not match");
			} else if (result.contains("400")) {
				System.out.println("too much attamptions, please try later");
			} else System.out.println("server has no response");
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//	private void negotiage(Socket client) {
//		JsonObject messageObject = new JsonObject();
//		messageObject.addProperty("req_type", "negotiate");
//		messageObject.addProperty("user", userName.getText());// 可以使用服务器发来的token
//		messageObject.addProperty("algorithm", algorithm);
//		messageObject.addProperty("bit", bit);
//		Gson gson = new Gson();
//		out.println(gson.toJson(messageObject));
//		// 收到token则启动聊天界面
//	}

	@FXML
	private void loadChatPage() {
		Parent chatBox;
		try {
			Stage stage = (Stage)userName.getScene().getWindow();// 必须从一个节点获取
			stage.close();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatRoom.fxml"));
			chatBox = loader.load();
			Chat chatCon = loader.getController();
			Scene scene = new Scene(chatBox);
			Stage newStage = new Stage();
			newStage.setScene(scene);
			newStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
