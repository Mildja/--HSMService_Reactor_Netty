package me.veeraya.Reactor_HSM;

import java.net.SocketAddress;

class Log {

    static void log(String message){
        System.out.println(message);
    }

    static void log(SocketAddress socketAddress, String message){
        System.out.println("[ " + socketAddress + " ] : " + message);
    }


}
