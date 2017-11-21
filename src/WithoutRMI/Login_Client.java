package WithoutRMI;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by User on 20/10/2017.
 */
public class Login_Client extends Application {
    Label lblUsername;
    Label lblPassword;
    TextField tfUsername;
    TextField tfPassword;
    Button btnLogin;
    RadioButton rbText;
    RadioButton rbVoice;
    ToggleGroup toggleGroup;


    @Override
    public void start(Stage primaryStage) throws Exception {
        lblUsername = new Label("Username: ");
        lblPassword = new Label("Password: ");
        tfUsername = new TextField();
        tfPassword = new PasswordField();
        btnLogin = new Button("LOGIN");
        rbText = new RadioButton("Text");
        rbVoice = new RadioButton("Voice");
        toggleGroup = new ToggleGroup();
        rbText.setToggleGroup(toggleGroup);
        rbVoice.setToggleGroup(toggleGroup);

        GridPane root = new GridPane();
        root.setVgap(10);
        root.setHgap(10);
        root.add(lblUsername, 0, 0);
        root.add(lblPassword, 0, 1);
        root.add(tfUsername, 1, 0);
        root.add(tfPassword, 1, 1);
        root.add(btnLogin, 1, 3);
        root.add(rbText, 0, 2);
        root.add(rbVoice, 1, 2);
        root.setHalignment(btnLogin, HPos.RIGHT);

        Scene scene = new Scene(root);
        primaryStage.setTitle("CLIENT LOGIN");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.sizeToScene();

        btnLogin.setOnAction(event -> {
            String username = tfUsername.getText();
            String password = tfPassword.getText();

            try {
                Socket socket = new Socket("127.0.0.1", 7777);
                boolean isRealUser = isRealUser(username, password, socket);

                if(isRealUser){
                    System.out.println("User authenticated");
                    try {
                        System.out.println("Asking for port from MainServer");

                        int port = getClientPort(socket);
                        if (port == -1) {
                            System.out.println("No port received from server. Exiting program");
                            Alert noAgentAlert = new Alert(Alert.AlertType.INFORMATION, "No free agent is online to connect");
                            noAgentAlert.setTitle("Can't find agent");
                            noAgentAlert.showAndWait();
                            primaryStage.close();
                        } else {//TODO voice
                            if(rbText.isSelected()){
                                Chat_client chatClient = new Chat_client();
                                chatClient.connection.setPort(port);
                                chatClient.run();
                                chatClient.setTitle(username);
                                primaryStage.close();
                            }
                            else if(rbVoice.isSelected()){
                                isVoice(socket);
                                int portC3 = getAnotherPort(socket);
                                System.out.println("gotten port " + portC3);


                                Voice_client voice_client = new Voice_client();
                                voice_client.serverSocketPort = port;
                                voice_client.sendPort = portC3;

                                voice_client.run();
                                primaryStage.close();
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Failed to get port from socket due to IOException.");
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.out.println("Exception occurred getting port from socket");
                        e.printStackTrace();
                    }
                }else{
                    System.out.println("fake user");
                }
            }catch (Exception e) {
                System.out.println("Socket asking for is real user failed.");
                e.printStackTrace();
            }




    });
}

    public int getClientPort(Socket socket) throws Exception {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());

            int port = in.readInt();
            System.out.println("Client received port: " + port);
            return port;
        } catch (Exception e) {
            System.out.println("Connection Handler method error. Socket failed");
        }
        return -1;
    }

    public boolean isRealUser(String username, String password, Socket socket) throws Exception{
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        out.writeUTF("client");
        out.writeUTF(username);
        out.writeUTF(password);

        boolean isReal;
        isReal = in.readBoolean();

        return isReal;
    }
    public void isVoice(Socket socket) throws Exception{
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        if(rbVoice.isSelected()){
            dataOutputStream.writeBoolean(true);
        }
    }

    public int getAnotherPort(Socket socket) throws Exception{
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        int port = dataInputStream.readInt();
        return port;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
