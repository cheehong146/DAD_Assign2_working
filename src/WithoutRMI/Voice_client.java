package WithoutRMI;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Voice_client extends Application{

    Label lblUserType;
    Label lblUsername;
    Label lblEmail;

    int serverSocketPort = 8080;//listening on port
    int clientSocketPort = 10101;
    int sendPort = 7070;

    VoiceUDP voiceUDP;

    Stage mainStage;

    public static void main(String[] args) {
        Application.launch(args);
    }

    public void run(){
        try{
            mainStage = new Stage();
            start(mainStage);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        lblUserType = new Label("Customer");
        lblUsername = new Label("dummyClient");
        lblEmail = new Label("dummy@gmail.com");

        //gridPane for leftmost side containing user info
        GridPane gridPaneInfo = new GridPane();
        gridPaneInfo.setHgap(10);
        gridPaneInfo.setVgap(10);
        gridPaneInfo.add(lblUserType, 0, 0);
        gridPaneInfo.add(new Label("Username: "), 0, 1);
        gridPaneInfo.add(new Label("Email: "), 0, 2);
        gridPaneInfo.add(lblUsername, 1, 1);
        gridPaneInfo.add(lblEmail, 1, 2);
        gridPaneInfo.setColumnSpan(lblUserType, gridPaneInfo.REMAINING);
        gridPaneInfo.setHalignment(lblUserType, HPos.CENTER);

        Scene scene = new Scene(gridPaneInfo);
        primaryStage.setTitle("Client");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.sizeToScene();

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {

                System.out.println("Worker task Voice_Client");
                voiceUDP = new VoiceUDP();
                voiceUDP.serverSocketPort = serverSocketPort;
                voiceUDP.sendPort = sendPort;
                System.out.println("sPort: " + voiceUDP.serverSocketPort);
                System.out.println("sendPort: " + voiceUDP.sendPort);
                voiceUDP.captureAudio();
                voiceUDP.runVOIP();
                return null;
            }
        };

        new Thread(task).start();
    }
}
