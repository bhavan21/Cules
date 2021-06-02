package com.example.drathod.targerian;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by bhavan on 27/4/17.
 */

public class SocketManagerBackground {
    private static SocketManagerBackground instance;

    private SocketManagerBackground() {
    }

    public synchronized static SocketManagerBackground getInstance() {
        if (instance == null) {
            instance = new SocketManagerBackground();
        }
        return instance;
    }

    private Socket socket;
    private OutputStreamWriter writer;
    private BufferedReader reader;
    //private List<OnSocketConnectionListener> onSocketConnectionListenerList;

    public Boolean isConnected(){
        if(socket==null){
            return false;
        }else
            return true;
    }

    public Integer connectSocket(String host,Integer port) {
        try {
            socket = new Socket(host,port);
            return  1;
        } catch (IOException e) {
            e.printStackTrace();
            return  -1;
        }
    }




    /**
     * Fire socket status intent.
     *
     */
//    public synchronized void fireSocketStatus(final int socketState) {
//        if(onSocketConnectionListenerList !=null && lastState!=socketState){
//            lastState = socketState;
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    for(OnSocketConnectionListener listener: onSocketConnectionListenerList){
//                        listener.onSocketConnectionStateChange(socketState);
//                    }
//                }
//            });
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    lastState=-1;
//                }
//            },1000);
//        }
//    }
//
//
    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Integer send(String message){
        try {
            writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            destroy();
            return -1;
        }

    }

    public String recv(){

        String output="!!!";
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output=reader.readLine();
            return output;
        } catch (IOException e) {
            e.printStackTrace();
            return output;
        }

    }
    /**
     * Destroy.
     */
    public void destroy()  {
        if (socket != null) {
            try {
                writer = new OutputStreamWriter(socket.getOutputStream());
                writer.close();
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                reader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
    }





}



