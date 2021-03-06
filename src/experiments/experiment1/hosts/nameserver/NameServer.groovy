package experiments.experiment1.hosts.nameserver

import common.utils.Utils

/**
 * Ein Server der Gerätenamen in IPv4-Adressen auflöst. Als Transport-Protokoll wird UDP verwendet.
 */
class NameServer {

    //========================================================================================================
    // Vereinbarungen ANFANG
    //========================================================================================================

    /** Der Netzwerk-Protokoll-Stack */
    experiments.experiment1.stack.Stack stack

    /** Konfigurations-Objekt */
    ConfigObject config

    /** Stoppen der Threads wenn false */
    Boolean run = true

    /** Tabelle zur Umsetzung von Namen in IP-Adressen */
    Map<String, String> nameTable = [
            "meinhttpserver": "192.168.1.1",
            "alice": "0.0.0.0",
            "bob": "0.0.0.0",

            ]


            //========================================================================================================
            // Methoden ANFANG
            //========================================================================================================

            //------------------------------------------------------------------------------
            /**
             * Start der Anwendung
             */
            static void main(String[] args) {
                // Client-Klasse instanziieren
                NameServer application = new NameServer()
                // und starten
                application.nameserver()
            }
            //------------------------------------------------------------------------------

            /**
             * Der Namens-Dienst
             */
            void nameserver() {

                //------------------------------------------------

                // Konfiguration holen
                config = Utils.getConfig("experiment1", "nameserver")

                // ------------------------------------------------------------

                // Netzwerkstack initialisieren
                stack = new experiments.experiment1.stack.Stack()
                stack.start(config)

                Utils.writeLog("NameServer", "nameserver", "startet", 1)

                String data
                String srcIpAddr
                int srcPort

                while (run) {
                    // Hier Protokoll implementieren:
                    // auf Empfang ueber UDP warten
                    // Namen über nameTable in IP-Adresse aufloesen
                    // IP-Adresse ueber UDP zuruecksenden


                    (srcIpAddr, srcPort, data) = stack.udpReceive()
                    Utils.writeLog("NameServer", "nameserver", "Reqeust to resolve ${data} was received", 1)
                    String ipAddr = nameTable.get(data)
                    if (ipAddr){
                        Utils.writeLog("NameServer", "nameserver", "Resolved into $ipAddr", 1)
                    } else {
                        Utils.writeLog("NameServer", "nameserver", "Error: Host $data was not found", 1)
                        ipAddr = "0.0.0.0"
                    }

                    /** Answer Format */

                    String ans ="ANSWER SECTION:" + ipAddr
            /** End of answer Format */


            stack.udpSend(dstIpAddr: srcIpAddr, dstPort: srcPort,
                    srcPort: config.ownPort, sdu: ans)

        }
    }
    //------------------------------------------------------------------------------
}
