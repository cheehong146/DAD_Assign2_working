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
public class Login_Agent extends Application {
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
        primaryStage.setTitle("AGENT LOGIN");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.sizeToScene();

        btnLogin.setOnAction(event -> {
            String username = tfUsername.getText();
            String password = tfPassword.getText();

            try{
                Socket socket = new Socket("127.0.0.1", 7777);
                boolean isRealAgent = isRealAgent(username, password, socket);

                if(isRealAgent){
                    System.out.println("agent authenticated");
                    //receive port here
                    int portC1 = getPort(socket);
                    int portC2 = getPort(socket);
                    System.out.println("Port received from mainServer: " + portC1 + " " + portC2);

                    if(rbText.isSelected()) {
                        Chat_server server = new Chat_server();

                        NetworkConnection connectionC1 = server.connection;
                        NetworkConnection connectionC2 = server.connectionC2;
                        connectionC1.setPort(portC1);
                        connectionC2.setPort(portC2);

                        try {
                            server.run();
                            server.setTitle(username);
                            primaryStage.close();
                        } catch (Exception e) {
                            System.out.println("ConnectionHandler in Login_Agent failed.");
                        }
                    }else if(rbVoice.isSelected()){//TODO
                        isVoice(socket);
                        int portC3 = getAnotherPort(socket);
                        System.out.println("gotten port3 " + portC3);
                        Voice_server voice_server = new Voice_server();
                        voice_server.sendPort = portC1;
                        voice_server.sendPort1 = portC2;
                        voice_server.serverSocketPort = portC3;

                        voice_server.run();

                        primaryStage.close();
                    }
                    else{
                        System.out.println("Please select either text or voice");
                    }

                }else{
                    System.out.println("not real agent");
                }

            }catch (IOException e){
                System.out.println("IOException occurred when receiving port from mainServer");
                e.printStackTrace();
            }catch (Exception e){
                System.out.println("Exception occured when receiving port from mainServer");
                e.printStackTrace();
            }
        });
    }

    public boolean isRealAgent(String username, String password, Socket socket) throws Exception{
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        out.writeUTF("agent");
        out.writeUTF(username);
        out.writeUTF(password);

        boolean isRealAgent;
        isRealAgent = in.readBoolean();

        return isRealAgent;
    }

    public int getPort(Socket socket) throws Exception{
        DataInputStream in = new DataInputStream(socket.getInputStream());

        int port = in.readInt();
        return port;
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
