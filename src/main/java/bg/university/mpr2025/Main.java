//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
package bg.university.mpr2025;

import bg.university.mpr2025.server.Server;
import bg.university.mpr2025.client.Client;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java -jar <jar> server|client");
            return;
        }
        if (args[0].equalsIgnoreCase("server")) {
            Server server = new Server(5555);
            server.start();
        } else if (args[0].equalsIgnoreCase("client")) {
            Client client = new Client("localhost", 5555);
            client.runInteractive();
        } else {
            System.out.println("Unknown command");
        }
    }
}
