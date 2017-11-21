package WithRMI;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Voice_server extends Application {

    Label lblUserType;
    Label lblUsername;
    Label lblEmail;
    Button stopC1;
    Button stopC2;

    int serverSocketPort = 7070;
    int sendPort = 8080;
    int sendPort1 = 8081;

    VoiceUDPServer voiceUDP;

    boolean stopaudioCapture = false;

    Stage mainStage;


    public static void main(String[] args) {
//        Thread thread = new Thread(new Voice_server());
//        thread.setDaemon(true);
//        thread.start();
        Application.launch();
    }

    public void run(){
        try {
            mainStage = new Stage();
            start(mainStage);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setTitle(String title) {
        mainStage.setTitle(title);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //component initialization
        lblUserType = new Label("Agent");
        lblUsername = new Label("dummyAgent");
        lblEmail = new Label("dummyvalue@gmail.com");
        stopC1 = new Button("Stop Client 1");
        stopC2 = new Button("Stop Client 2");

        setTitle("Agent Voice");

        //root pane
        HBox root = new HBox();
        //leftmost Pane for agent infomation
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

        //rightmost Pane voice button
        VBox vBox = new VBox();
        vBox.setSpacing(20.0);
        vBox.setPadding(new Insets(20, 20, 20, 20));
        vBox.getChildren().addAll(stopC1, stopC2);

        //setting pane into one another
        root.getChildren().addAll(gridPaneInfo, vBox);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Agent Server");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();


        //RMI for server
        System.out.println("Storing Object into registry " + InetAddress.getLocalHost());
        LocateRegistry.createRegistry(1099);
        UDPInterface clientVoiceUDP = new VoiceUDP();
        Naming.bind("clientObject", clientVoiceUDP);

        stopC1.setOnAction(event -> {
            try {
                voiceUDP.stop();
            }catch (Exception e){
                e.printStackTrace();
            }
            stopC1.setDisable(true);
        });

        stopC2.setOnAction(event -> {
            try{
                voiceUDP.stop();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        voiceUDP = new VoiceUDPServer();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                System.out.println("Server UDP");

                voiceUDP.serverSocketPort = serverSocketPort;
                voiceUDP.sendPort = sendPort;
                voiceUDP.sendPort1 = sendPort1;
                System.out.println("sPort: " + voiceUDP.serverSocketPort);
                System.out.println("sendPort: " + voiceUDP.sendPort);
                System.out.println("sendPort1: " + voiceUDP.sendPort1);
                voiceUDP.captureAudio();
                voiceUDP.runVOIP();
                return null;
            }
        };

        new Thread(task).start();

//        Task task = new Task() {
//            @Override
//            protected Object call() throws Exception {
//                System.out.println("Server UDP");
//                voiceUDP = new VoiceUDP();
//                voiceUDP.serverSocketPort = serverSocketPort;
//                voiceUDP.sendPort = sendPort;
//                System.out.println("sPort: " + voiceUDP.serverSocketPort);
//                System.out.println("sendPort: " + voiceUDP.sendPort);
//                voiceUDP.captureAudio();
//                voiceUDP.runVOIP();
//                System.out.println("capture Audio");
//                return null;
//            }
//        };
//
//        Task task1 = new Task() {
//            @Override
//            protected Object call() throws Exception {
//                System.out.println("Server UDP 1");
//                voiceUDP1 = new VoiceUDP();
//                voiceUDP1.serverSocketPort = serverSocketPort1;
//                voiceUDP1.sendPort = sendPort1;
//                System.out.println("sPort1: " + voiceUDP1.serverSocketPort);
//                System.out.println("sendPort1: " + voiceUDP1.sendPort);
//                voiceUDP1.captureAudio();
//                voiceUDP1.runVOIP();
//                return null;
//            }
//        };
//
////        task.setOnSucceeded(event -> new Thread(task1).start());
//        new Thread(task).start();
//        new Thread(task1).start();

    }
}
