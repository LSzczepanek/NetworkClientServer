package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client
{
   public static final int PORT=9000;
   public static final String HOST = "127.0.0.1";
   
   
   public static void main(String[] args) throws IOException                            
   {      
	   String msg = null;
                                            
      Socket sock;                                                                     
      sock=new Socket(HOST,PORT);                                                      
      System.out.println("Nawiazalem polaczenie: "+sock);                              
                                                                                       
      //tworzenie strumieni danych pobieranych z klawiatury i dostarczanych do socketu 
      BufferedReader input;                                                             
      input=new BufferedReader(new InputStreamReader(System.in));  
      BufferedReader fromServer;                                                             
      fromServer=new BufferedReader(new InputStreamReader(sock.getInputStream()));         
      PrintWriter output;                                                                
      output=new PrintWriter(sock.getOutputStream());                                    
                                                                                       
      //komunikacja - czytanie danych z klawiatury i przekazywanie ich do strumienia   
      do
      {
      System.out.print("<Wysylamy:> ");                                                
      msg=input.readLine();                                                      
      output.println(msg);                                                               
      output.flush(); 
      }while (!(msg.equals("close")));                                                                                       
      //zamykanie polaczenia                                                           
      input.close();                                                                    
      output.close();                                                                    
      sock.close();                                                                    
   }                                                                                   
}
