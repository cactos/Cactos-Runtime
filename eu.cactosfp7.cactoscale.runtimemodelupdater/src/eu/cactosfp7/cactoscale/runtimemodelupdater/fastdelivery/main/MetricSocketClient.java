package eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.main;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import eu.cactosfp7.cactoscale.runtimemodelupdater.SettingsInitializer;
import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.listeners.FastDeliveryListener;
import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.listeners.Metric;
import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.listeners.MetricListener;
import eu.cactosfp7.cactoscale.runtimemodelupdater.fastdelivery.listeners.MetricSource;

public final class MetricSocketClient extends Thread {

	private static final Logger logger = Logger.getLogger(MetricSocketClient.class);

	private final ExecutorService outExecutor = Executors.newFixedThreadPool(10);

	private final MetricSource metricSource = new MetricSource();
	// needs to be volatile, as it will be used
	// by different threads
	private volatile boolean running = true;
	private final String chukwaHost;
	private final int chukwaPort;
	private final Socket chukwaSocket;

	public MetricSocketClient() {
		chukwaHost = SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.CHUKWA_COLLECTOR_HOST);
		chukwaPort = Integer.valueOf(SettingsInitializer.INSTANCE.getProperty(SettingsInitializer.CHUKWA_COLLECTOR_PORT));
		chukwaSocket = initSocket();
		logger.info("Connected to " + chukwaHost + ":" + chukwaPort);
	}

	private Socket initSocket() {
		try {
			return new Socket(chukwaHost, chukwaPort);
		} catch (IOException ioe) {
			logger.error("cannot create socket to connect to chukwa", ioe);
			throw new IllegalStateException(ioe);
		}
	}

	private DataInputStream openConnectionAndGetInputStream() throws IOException {
		chukwaSocket.getOutputStream().write("HEADER all\n".getBytes());
		chukwaSocket.getOutputStream().flush();
		DataInputStream dis = new DataInputStream(chukwaSocket.getInputStream());
		InputStreamReader isr = new InputStreamReader(dis);
		BufferedReader br = new BufferedReader(isr);
		String firstline = br.readLine();
		logger.info("Firstline from server: " + firstline);
		return dis;
	}

	private void closeStreams(DataInputStream dis) {
		try {
			if (dis != null)
				dis.close();
		} catch (IOException ioe) {
			logger.warn("could not close bufferedReader.", ioe);
		}
		try {
			if (chukwaSocket != null)
				chukwaSocket.close();
		} catch (IOException ioe) {
			logger.warn("could not close chukwa socket.", ioe);
		}
	}

	private Runner doRead(DataInputStream dis) throws IOException {
		int len = dis.readInt();
		// if state changed in the mean time, skip working with incoming
		// data
		if (!running)
			return null;
		byte[] data = new byte[len];
		dis.readFully(data);
		return new Runner(data);
	}

	class Runner implements Runnable {
		private final byte[] data;

		Runner(byte[] dataParam) {
			data = dataParam;
		}

		@SuppressWarnings("unchecked")
		private Map<String, String> readMap() {
			try {
				final ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(data));
				return (Map<String, String>) oin.readObject();
			} catch (IOException ioe) {
				logger.warn("could not read object from data chunks.", ioe);
			} catch (ClassNotFoundException ex) {
				logger.warn("could not read object from data chunks.", ex);
			}
			return Collections.EMPTY_MAP;
		}

		@Override
		public void run() {
			Map<String, String> map = null;
			try {
				map = readMap();
				Metric metric = new Metric(metricSource, map);
				metricSource.fireEvent(metric);
			} finally {
				if (map != null)
					map.clear();
			}
		}
	}

	@Override
	public void run() {
		MetricListener fastDeliveryListener = new FastDeliveryListener();
		metricSource.addEventListener(fastDeliveryListener);
		DataInputStream dis = null;
		try {
			dis = openConnectionAndGetInputStream();

			while (running) {
				Runner r = doRead(dis);
				outExecutor.submit(r);
			}
		} catch (Throwable x) {
			logger.error(x);
		} finally {
			closeStreams(dis);
		}
	}

	public void stopClient() {
		running = false;
		outExecutor.shutdown();
	}

}
