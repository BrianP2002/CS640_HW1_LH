import java.io.*;
import java.net.*;

public class IPerfer {

    private static Boolean testMode = false;

    public static void main(String args[]) {
        if (args.length != 3 && args.length != 7) {
            System.out.println("Error: missing or additional arguments");
            if (testMode)
                System.out.println("location 1");
            System.exit(1);
        }
        parseArgs(args);
        System.exit(0);
    }

    private static void print_error(int type, int location){
        if(type == 1){
            System.out.println("Error: missing or additional arguments");
        }
        else if(type == 2){
            System.out.println("Error: port number must be in the range 1024 to 65535");
        }
        if (testMode)
            System.out.printf("location %d\n", location);
        System.exit(1);
    }

    private static void parseArgs(String[] args) {
        String hostName = "127.0.0.1";
        int portNumber = 1234, timeInterval = 0;

        if(args[0].equals("-s")){
            if(args.length != 3 || !args[1].equals("-p")){
                print_error(1, 2);
            }
            try{
                portNumber = Integer.parseInt(args[2]);
                startServer(portNumber);
            } catch(NumberFormatException e){
                print_error(2, 3);
            }
        } else if(args[0].equals("-c")){
            if(args.length != 7 || !args[1].equals("-h") || !args[3].equals("-p") || !args[5].equals("-t")){
                print_error(1, 4);
            }
            hostName = args[2];
            try{
                portNumber = Integer.parseInt(args[4]);
            }catch(NumberFormatException e){
                print_error(2, 5);
            }
            try{
                timeInterval = Integer.parseInt(args[6]);
            }catch(NumberFormatException e){
                print_error(1, 6);
            }
            startClient(hostName, portNumber, timeInterval);
        } else{
            print_error(1, 7);
        }

        return;
    }

    private static void startServer(int portNumber) {
        long receivedBytes = 0;
        double receivedKBs = 0, receivedMBs = 0;
        long startTime = 0, endTime = 0;
        double timeInterval = 0, rate = 0;

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();
            DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            byte[] buffer = new byte[1000];
            int bytesRead;
            startTime = System.currentTimeMillis();
            while ((bytesRead = in.read(buffer)) != -1) {
                receivedBytes += bytesRead;
            }
            endTime = System.currentTimeMillis();
            serverSocket.close();

            timeInterval = ((endTime - startTime) / 1000.0);
            receivedKBs = receivedBytes / 1000.;
            receivedMBs = receivedKBs / 1000.;
            rate = (8 * receivedMBs) / timeInterval;
            System.out.printf("received=%.0f KB rate=%.3f Mbps\n", receivedKBs, rate);
        } catch (IOException e) {
            System.out.printf("server IO process aborted: %s\n", e.getMessage());
        }
    }

    private static void startClient(String hostName, int portNumber, int timeInterval) {
        long sentBytes = 0;
        double sentKBs = 0, sentMBs = 0;
        long startTime = 0;
        double rate = 0;
        byte[] packet = new byte[1000];

        try {
            Socket clientSocket = new Socket(hostName, portNumber);
            OutputStream out = clientSocket.getOutputStream();

            startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < (timeInterval * 1000)) {
                out.write(packet);
                out.flush();
                sentBytes += packet.length;
            }
            clientSocket.close();

            sentKBs = sentBytes / 1000.;
            sentMBs = sentKBs / 1000.;
            rate = (8 * sentMBs) / timeInterval;
            System.out.printf("sent=%.0f KB rate=%.3f Mbps\n", sentKBs, rate);
        } catch (IOException e) {
            System.out.printf("client IO process aborted: %s\n", e.getMessage());
        }
    }
}
