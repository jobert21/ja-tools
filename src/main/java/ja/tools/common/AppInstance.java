package ja.tools.common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class AppInstance extends Thread {
	private static final int CHECKER_PORT = 9998;
	private static Logger log = Logger.getLogger(AppInstance.class.getName());
	private ServerSocket server;

	public AppInstance() {
		setName("AppInstance");
		setDaemon(true);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				if (server != null) {
					try {
						server.close();
					} catch (IOException e) {
					}
					server = null;
				}
			}
		}));
	}

	public static void checkOrExit() {
		try {
			log.info("Check if app is already running.");
			new Socket(InetAddress.getLocalHost().getHostAddress(),
					CHECKER_PORT);
			log.info("Exiting. Instance is already running");
			System.exit(0);
		} catch (Exception e) {
			log.info("Creating new instance.");
			new AppInstance().start();
		}
	}

	public void run() {
		try {
			server = new ServerSocket(CHECKER_PORT, 1);
			while (true) {
				Socket client = server.accept();
				client.close();
			}
		} catch (Exception e) {
		}
	}
}
