/* Utilizando multihilos pero no threadpool
public final class ServidorWeb{

    public static void main(String argv[]) throws Exception{
        
        //elegimos el puerto
        int puerto = 6789;

        //establecemos el socket de escucha (TCP)
        ServerSocket socketEscucha = new ServerSocket(puerto);

        //procesamos las solicitudes HTTP en un ciclo infinito
        while(true){

            //escuchando las solicitudes HTTP
            Socket socketConexion = socketEscucha.accept();

            //construimos un objeto para procesar la solicitud HTTP una vez recibida
            SolicitudHttp solicitudRecibida = new SolicitudHttp(socketConexion);

            //Creando el hilo para procesar la solicitud
            Thread hilo = new Thread(solicitudRecibida); //USAR THREADPOOL OJOOOOOOOOOOOOO

            //Iniciando el hilo
            hilo.start();

        }
    }
} */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public final class ServidorWeb {
    
    private static final int PUERTO = 6789;
    private static final int MAX_HILOS = 10; // Número máximo de hilos en el pool

    public static void main(String argv[]) throws Exception {
        
        // Establecemos el socket de escucha (TCP)
        ServerSocket socketEscucha = new ServerSocket(PUERTO);
        
        // Creamos un ThreadPool con un número fijo de hilos
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_HILOS);

        System.out.println("Servidor web iniciado en el puerto " + PUERTO);

        // Procesamos las solicitudes HTTP en un ciclo infinito
        while (true) {
            // Escuchando solicitudes HTTP
            Socket socketConexion = socketEscucha.accept();

            // Construimos un objeto para procesar la solicitud HTTP y lo pasamos al ThreadPool
            threadPool.execute(new SolicitudHttp(socketConexion));
        }
    }
}


final class SolicitudHttp implements Runnable{

    final static String CRLF = "\r\n";
    Socket socket;
    
    //Constructor
    public SolicitudHttp(Socket socket) throws Exception{
        this.socket = socket;
    }

    //Implementacion del método run (obligatorio para la interfaz Runnable)
    public void run(){
        try{
            proceseSolicitud();
        }catch(Exception e){
            System.out.println("Error en el hilo: " + e);
        }
    }

    private void proceseSolicitud() throws Exception{
        
        //referencia al stram de salida del socket
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        
        //Referencia y filtros (InputStreamReader y BufferedReader) para el strema de entrada.
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //Recoge la linea de la solicitud HTTP del mensaje
        String lineaSolicitud = br.readLine();

        //Muestra la línea de solicitud en pantalla
        System.out.println();
        System.out.println(lineaSolicitud);

        //recoger y mostrar las lineas del header de la solicitud HTTP
        String lineaDelHeader = null;
        while((lineaDelHeader = br.readLine()).length() != 0){
            System.out.println(lineaDelHeader);
        }

        //Extraer el nombre del archivo de la línea de solicitud
        StringTokenizer partesLinea = new StringTokenizer(lineaSolicitud);
        partesLinea.nextToken(); //Para "saltar" el método HTTP, que se supone es GET
        String nombreArchivo = partesLinea.nextToken();

        //Anexa un "." para que el archivo solicitado sea relativo al directorio actual.
        nombreArchivo = "." + nombreArchivo;

        //Abrir el archivo solicitado
        FileInputStream fis = null;
        boolean existeArchivo = true;
        try{
            fis = new FileInputStream(nombreArchivo);
        }catch(FileNotFoundException e){
            existeArchivo = false;
        }

        //Contruir el mensaje de respuesta
        String lineaDeEstado = null;
        String lineaDeTipoContenido = null;
        String cuerpoMensaje = null;
        if(existeArchivo){
            lineaDeEstado = "HTTP/1.0 200 OK" + CRLF;
            lineaDeTipoContenido = "Content-type: " + contentType( nombreArchivo ) + CRLF;
        }else{
            lineaDeEstado = "HTTP/1.0 404 Not Found" + CRLF;
            lineaDeTipoContenido = "Content-type: text/html" + CRLF;
            cuerpoMensaje = "<HTML>" + "<HEAD><TITLE>404 Not Found</TITLE></HEAD>" + "<BODY><b>404</b> Not Found</BODY></HTML>";
        }

        //Enviar la línea de estado
        os.writeBytes(lineaDeEstado);

        //Enviar el contenido de la línea content-type
        os.writeBytes(lineaDeTipoContenido);

        //Enviar una línea en blanco para indicar el final de las líneas de header (encabezado)
        os.writeBytes(CRLF);

        //Enviar el cuerpo del mensaje 
        if(existeArchivo){
            enviarBytes(fis, os);
            fis.close();
        }else{
            os.writeBytes(cuerpoMensaje);
        }

        //Cerrando los streams y el socket
        os.close();
        br.close();
        socket.close();

    }

    private static void enviarBytes(FileInputStream fis, OutputStream os) throws Exception{

        //Construye un buffer de 1KB para guardar los bytes cuando van hacia el socket
        byte[] buffer = new byte[1024];
        int bytes = 0;

        //Copia el archivo solicitado hacia el output stream del socket
        while((bytes = fis.read(buffer)) != -1){
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String nombreArchivo){
        if(nombreArchivo.endsWith(".htm") || nombreArchivo.endsWith(".html")){
            return "text/html";
        }
        if (nombreArchivo.endsWith(".jpg") || nombreArchivo.endsWith(".jpeg")){
            return "image/jpeg";
        }   
        if(nombreArchivo.endsWith(".gif")){
            return "image/gif";
        }
        return "application/octet-stream";
    }
    
}

