import java.io.*;
import java.net.Socket;


public class FTP {
    private static DataInputStream commDataInputStream;
    private static PrintStream commPrintStream;
    private static Socket commSocket;
    private static Socket dataSocket;
    private static BufferedInputStream dataBufferedInputStream;
    private static BufferedOutputStream dataBufferedOutputStream;
    public static String connect() throws Exception {
        commSocket = new Socket("localhost", 21);
        commDataInputStream = new DataInputStream(commSocket.getInputStream());
        commPrintStream = new PrintStream(commSocket.getOutputStream());
        return commDataInputStream.readLine() + "\n" +
                commDataInputStream.readLine() + "\n" + commDataInputStream.readLine();
    }

    public static void dataConnect(int[] address) throws Exception {
        String ip = address[0] + "." + address[1] + "." + address[2] + "." + address[3];
        dataSocket = new Socket(ip, address[4]);
        dataBufferedInputStream = new BufferedInputStream(dataSocket.getInputStream());
        dataBufferedOutputStream = new BufferedOutputStream(dataSocket.getOutputStream());
    }

    public static String login(String username, String password) throws Exception {
        String send = "USER " + username + "\r\n";
        commPrintStream.print(send);
        String ret = new String();
        ret = commDataInputStream.readLine() + "\n";
        send = "PASS " + password + "\r\n";
        commPrintStream.print(send);
        ret += commDataInputStream.readLine();
        return ret;
    }

    public static String passivemode() throws Exception {
        commPrintStream.print("PASV \r\n");
        return commDataInputStream.readLine();
    }

    public static String size(String filename) throws Exception {
        commPrintStream.print("SIZE " + filename + "\r\n");
        return commDataInputStream.readLine();
    }

    public static String retr(String filename, FileOutputStream fileOutputStream) throws Exception {
        String stemp = passivemode();
        commPrintStream.print("RETR " + filename + "\r\n");
        dataConnect(stringAnalysis4DataPort(stemp));
        int temp = dataBufferedInputStream.read();
        while (temp != -1) {
            fileOutputStream.write(temp);
            temp = dataBufferedInputStream.read();
        }
        dataBufferedInputStream.close();
        dataBufferedOutputStream.close();
        dataSocket.close();
        fileOutputStream.close();
        return commDataInputStream.readLine() + "\n" + commDataInputStream.readLine();
    }

    public static String stor(String filename, FileInputStream fileInputStream) throws Exception {
        String stemp = passivemode();
        commPrintStream.print("STOR " + filename + "\r\n");
        dataConnect(stringAnalysis4DataPort(stemp));
        int temp = fileInputStream.read();
        while (temp != -1) {
            dataBufferedOutputStream.write(temp);
            temp = fileInputStream.read();
        }
        fileInputStream.close();
        dataBufferedOutputStream.close();
        dataBufferedInputStream.close();
        dataSocket.close();
        return commDataInputStream.readLine() + "\n" + commDataInputStream.readLine();
    }

    public static String list(String name) throws Exception {
        String stemp = passivemode();
        commPrintStream.print("LIST\r\n");
        dataConnect(stringAnalysis4DataPort(stemp));
        DataInputStream dataInputStream = new DataInputStream(dataSocket.getInputStream());
        String file = dataInputStream.readLine();
        while (file != null) {
            System.out.println(file);
            file = dataInputStream.readLine();
        }
        dataInputStream.close();
        dataBufferedOutputStream.close();
        dataBufferedInputStream.close();
        dataSocket.close();
        return commDataInputStream.readLine() + "\n" + commDataInputStream.readLine();
    }

    public static String quit() throws Exception {
        commPrintStream.print("QUIT\r\n");
        String ret = commDataInputStream.readLine();
        commPrintStream.close();
        commDataInputStream.close();
        commSocket.close();
        return ret;
    }

    private static int[] stringAnalysis4DataPort(String s) throws Exception {
        int[] ret = new int[5];
        StringBuffer stringBuffer = new StringBuffer(s);
        int index = stringBuffer.indexOf("(") + 1;
        int index1 = stringBuffer.indexOf(",", index);
        for (int i = 0; i < 4; i++) {
            String temp = stringBuffer.substring(index, index1);
            ret[i] = Integer.parseInt(temp);
            index = index1 + 1;
            index1 = stringBuffer.indexOf(",", index1 + 1);
        }
        String temp = stringBuffer.substring(index, index1);
        int port = Integer.parseInt(temp) * 256;
        index = index1 + 1;
        index1 = stringBuffer.length() - 1;
        temp = stringBuffer.substring(index, index1);
        port += Integer.parseInt(temp);
        ret[4] = port;
        return ret;
    }

    public static void main(String[] args) throws Exception {
        String retMesg = new String();

        retMesg = connect();
        System.out.println(retMesg);
//        DataInputStream dataInputStream = new DataInputStream(System.in);
//        System.out.println(dataInputStream.readLine());
        retMesg = login("admin", "admin");
        System.out.println(retMesg);


        retMesg = retr("downloadtest.txt", new FileOutputStream("downloadtest.txt", false));
        System.out.println(retMesg);
//        retMesg = list("");
//        System.out.println(retMesg);
//        commPrintStream.print("CWD folder\r\n");
//        System.out.println(commDataInputStream.readLine());
//        retMesg = list("");
//        System.out.println(retMesg);
        retMesg = stor("uploadtest.txt", new FileInputStream("uploadtest.txt"));
        System.out.println(retMesg);
        retMesg = quit();
        System.out.println(retMesg);
    }
}