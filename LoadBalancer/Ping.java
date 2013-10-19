import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class Ping extends Thread {

	private boolean running;
	private ArrayList<RemoteHost> servidores;
	private RemoteHost primario;

	public Ping(ArrayList servidores) {

		this.servidores = servidores;
		this.running = true;
	}

	public void run() {

		while (running) {

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}

			for (Iterator<RemoteHost> it = servidores.iterator(); it.hasNext();) {
				RemoteHost server = it.next();
				if (!ping(server.getHost(), server.getPort())) {
					if (server.equals(primario)) {
						if (servidores.size() > 0) {
							primario = servidores.get(1);
							System.out.println(primario + " promovido a principal");
						} else
							primario = null;
					}
					it.remove();

					System.out.println("server removido " + server);
				}
			}

		}
	}

	public void setPrimario(RemoteHost r) {
		primario = r;
	}

	public RemoteHost getPrimario() {
		return primario;
	}

	private boolean ping(String ip, int port) {
		try {
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(1000);

			byte[] send = "PING".getBytes();

			InetAddress ipA = InetAddress.getByName(ip);

			DatagramPacket sendPacket = new DatagramPacket(send, send.length, ipA, port);
			try {
				// System.out.println("PINGING " + ip + ":" + port);
				socket.send(sendPacket);

				byte[] receive = new byte[512];
				DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);
				socket.receive(receivePacket);

				socket.close();
				String pong = new String(receivePacket.getData());
				if (pong.contains("PONG")) {
					return true;
				}

			} catch (SocketTimeoutException e) {
				socket.close();
				return false;
			} catch (IOException e) {
				socket.close();
				return false;
			}

		} catch (SocketException | UnknownHostException e) {
			System.out.println("Erro criar socket UDP");
		}
		return false;
	}
}