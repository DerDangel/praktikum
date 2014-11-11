#!/usr/bin/env groovy

package experiments.experiment1.hosts.server1

import com.sun.org.apache.xml.internal.resolver.helpers.FileURL
import common.utils.Utils
import experiments.experiment1.stack.Stack

import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Matcher

/**
 * Ein einfacher HTTP-Server.<br/>
 * Liefert bei Übergabe des Dokumentsnamen "index.html" den Namen des angeforderten Dokuments zurück.<br/>
 * Soll bei Übergabe des Dokumentsnamens "daten" eine größere Datenmenge zu Testzwecken liefern.<br/>
 * Zum Transport wird UDP verwendet. Der Server ist dadurch nicht für die Kommunikation mit realen HTTP-Clients
 * verwendbar, da diese stets TCP verwenden!
 */
class Server {

    //========================================================================================================
    // Vereinbarungen ANFANG
    //========================================================================================================

    // Der Netzwerk-Protokoll-Stack
    Stack stack

    /** Konfigurations-Objekt */
    ConfigObject config

    /** Stoppen der Threads wenn false */
    Boolean run = true

    /** Der im HTTP-Request gelieferte Name des angeforderten Objekts*/
    String name = ""

    /** IP-Adresse und Portnummer des client */
    String srcIpAddr
    int srcPort

    /** Eigene Portnummer */
    int ownPort

    /** Anwendungsprotokolldaten als String */
    String data

    String dataBody
    /** Länge der gesendeten Daten */
    int dataLength = 0

    /** Antwort */
    GString reply_header =
            """\
HTTP/1.1 200 OK
Content-Length: ${->dataLength}
Content-Type: text/plain

"""

    GString reply2 =
            """\
Das Objekt ${->name} wurde angefragt!
"""

    /** Ein Matcher-Objekt zur Verwendung regulärer Ausdruecke */
    Matcher matcher

    /** Daten empfangen solange false */
    boolean ready = false


    //========================================================================================================
    // Methoden ANFANG
    //========================================================================================================

    //------------------------------------------------------------------------------
    /**
     * Start der Anwendung
     */
    static void main(String[] args) {
        // Client-Klasse instanziieren
        Server application = new Server()
        // und starten
        application.server()
    }

    //------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
    /**
     * Ein HTTP-Server mit rudimentärer Implementierung des Protokolls HTTP (Hypertext Transfer Protocol)
     */
    void server() {

        // ------------------------------------------------------------

        // Konfiguration holen
        config = Utils.getConfig("experiment1", "server")

        // ------------------------------------------------------------

        // Netzwerkstack initialisieren
        stack = new Stack()
        stack.start(config)
        ownPort = config.ownPort

        //------------------------------------------------

        Utils.writeLog("Server", "server", "startet", 1)


        //------------------------------------------------
        int begin = 0
        while (run) {

            // Auf Empfang warten
            (srcIpAddr, srcPort, data) = stack.udpReceive()

            Utils.writeLog("Server", "server", "empfängt: $data", 1)

            // Abbruch wenn Länge der empfangenen Daten == 0
            if (!data)
                break

            // Parsen des HTTP-Kommandos
            matcher = (data =~ /GET\s*\/(.*?)\s*HTTP\/1\.1/)
            name = ""

            // Wurde das Header-Feld gefunden?
            if (matcher) {
                // Ja
                // Name des zu liefernden Objekts
                name = (matcher[0] as List<String>)[1]
                int sending = 1
                while(sending)
                {
                    switch(name){
                        case("daten"):

                            int currentByte = 0

                            int len = 900
                            while (dataLength > currentByte) {

                                int step = (currentByte+len>dataLength) ? dataLength-currentByte-1: len-1

                                stack.udpSend(dstIpAddr: srcIpAddr, dstPort: srcPort,
                                        srcPort: ownPort, sdu: dataBody.subSequence(currentByte,currentByte+step))
                                currentByte += len
                                // System.out.println("\n the ${-> currentByte} was sended.")

                                sleep(300)
                            }
                            sending = 0
                            break
                        default:

                            Path rootPath = Paths.get(Server.class.getProtectionDomain().getCodeSource().getLocation().getPath()).parent.parent.parent
                            String path ="$rootPath/src/experiments/experiment1/hosts/server1/${->name}"
                            File target = new File(path)

                            if(!target.exists()){
                                printf("File not found at: $path")
                                return
                            }
                            dataBody = target.text.toString()
                            dataLength = target.bytes.size()

                            /* send header to the client */
                            stack.udpSend(dstIpAddr: srcIpAddr, dstPort: srcPort,
                                    srcPort: ownPort, sdu: reply_header)

                            name = "daten"

                            sleep(300)
                    }




                    /*new Thread(){
                        public void run() {
                            while (dataLength <= currentByte) {
                                reply = ""
                                data.getChars(currentByte, currentByte + 999, bOut, 0)
                                stack.udpSend(dstIpAddr: srcIpAddr, dstPort: srcPort,
                                        srcPort: ownPort, sdu: bOut.toString())
                                currentByte += 1000
                                System.out.println("\n the ${-> currentByte} was sended.")

                                //  sleep(300)
                            }
                        }
                    }.start()*/
                    Utils.writeLog("Server", "server", "sendet: somthig was sended", 11)
                }

                // Antwort senden

            }
        } // while
    }
}

//------------------------------------------------------------------------------