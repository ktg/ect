/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Nottingham
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Nottingham
   nor the names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

</COPYRIGHT>

Created by: James Mathrick (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Shahram Izadi (University of Nottingham)
  James Mathrick (University of Nottingham)

 */
package equip.ect.webstart;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import equip.data.DataManager;
import equip.data.DataProxy;
import equip.data.Server;
import equip.discovery.DiscoveryClientAgent;
import equip.discovery.DiscoveryClientAgentImpl;
import equip.discovery.DiscoveryEventListenerImpl;
import equip.discovery.DiscoveryServerAgent;
import equip.discovery.DiscoveryServerAgentImpl;
import equip.discovery.ServerDiscoveryInfo;
import equip.discovery.ServerDiscoveryInfoImpl;
import equip.ect.http.SimpleHttpServer;
import equip.ect.http.UploadHttpServer;
import equip.ect.util.process.ProcessUtils;
import equip.net.Moniker;
import equip.net.MulticastUDPMoniker;
import equip.net.SimpleTCPMoniker;
import equip.net.SimpleUDPMoniker;
import equip.net.TraderMoniker;

/**
 * Object representing an installation. Sorts out re/start/joining of dataspace, secret management,
 * discovery adverts, and container (auto)starting.
 * 
 * @author jym
 */

class Installation
{

	/**
	 * Horrible way to so this, even if it does work....
	 */
	class DataspaceFinder extends DiscoveryEventListenerImpl
	{
		String[] urls = new String[0];
		protected boolean stopFlag = false;

		DataspaceFinder()
		{
		}

		@Override
		public synchronized void discoveryEvent(final DiscoveryClientAgent agent, final String url)
		{
			urls = agent.getKnownServers();
		}

		public synchronized String go(final String groupName)
		{
			final DiscoveryClientAgent agent = new DiscoveryClientAgentImpl();
			agent.startDefault(this, new String[] { BootGlobals.DEFAULT_SERVICE }, new String[] { groupName });
			stopFlag = false;
			try
			{
				// Gather a few urls...
				wait(BootGlobals.DISCOVER_WAIT);
			}
			catch (final Exception e)
			{
				// catch interruts
			}
			// timeout in for specific discovery
			final long timeout = System.currentTimeMillis() + BootGlobals.DISCOVER_TIMEOUT;
			while (!stopFlag && urls.length == 0)
			{
				/* heat that CPU up */
				try
				{
					wait(20);
				}
				catch (final Exception e)
				{
					// catch interrupts
				}
				if (System.currentTimeMillis() > timeout)
				{
					break;
				}
			}
			agent.stop();
			for (final String url : urls)
			{
				final String valid = validateDataspace(url);
				if (valid != null) { return valid; }
			}
			return null;
		}

		public synchronized void stop()
		{
			stopFlag = true;
			notify();
		}

		private synchronized String validateDataspace(final String surl)
		{
			// TODO ... return just encrypted url on valid surl
			// return null on invalid signature
			if (sharedSecret != null && surl.indexOf('*') >= 0)
			{
				final String sig = surl.substring(surl.lastIndexOf('*') + 1);
				if (!equip.data.Challenge.acceptResponse(sharedSecret, surl.substring(0, surl.lastIndexOf('*')), sig))
				{
					// no
					System.err.println("validateDataspace failed for " + surl);
					return null;
				}
			}
			// we'll take it if we don't have a sig
			return surl.substring(0, surl.indexOf('*', 0));
		}
	}

	public static final int HTTP_PORT = 8088;
	public static final int MASTER_MIGRATE_LOGS_INTERVAL_MS = (60 * 60 * 1000);
	// PRIVATE DATA MEMBERS
	private DataProxy dataspace = null;
	private Map<String, Container> containers = new HashMap<String, Container>();
	private String path = "";
	private boolean isValid = false;
	private String containerDirs = "";
	private boolean haveDataspaceFolder = false;
	private boolean iAmTheOneTrueMaster = false;
	private boolean theMasterIsAlive = false;
	private java.util.Properties instProperties = new java.util.Properties();
	private String autoStart = "";
	/**
	 * This one may be used in previously known installations...
	 */
	private boolean coldStart = false;
	/**
	 * This is so we know whether to create new directories or not
	 */
	private boolean createDirectories = false;
	/**
	 * we must know this in advance/or by end of constructor in order to start/join an installation
	 */
	private String sharedSecret = null;
	/**
	 * we must know this in advance/or by end of constructor in order to start/join an installation
	 */
	private String fullGroupName;
	/**
	 * we must know this in advance/or by end of constructor in order to start/join an installation
	 */
	private String dataspaceUrl;
	/**
	 * discovery signature (if received)
	 */
	private String discoverySignature;
	/**
	 * config download url
	 */
	private String configUrl;
	/**
	 * host manager
	 */
	protected HostManager hostManager;
	protected equip.data.GUIDFactory guids;
	protected boolean disableDiscovery = false;
	/**
	 * embedded webserver e.g. for clients to download stuff
	 */
	protected SimpleHttpServer httpServer;
	/**
	 * child process
	 */
	protected Process myProcess;
	/**
	 * download manager
	 */
	protected DownloadManager download;
	public static final int DISCOVER_STEP_MS = 500;
	public static final int MAX_DISCOVER_COUNT = (int) ((BootGlobals.DISCOVER_WAIT + BootGlobals.DISCOVER_TIMEOUT) / DISCOVER_STEP_MS) + 1;

	/**
	 * child process main. arg 1 = configuration directory path
	 */
	public static void main(final String args[])
	{
		File file = new File("install.ect");
		if(args.length != 0)
		{
			file = new File(args[0]);

			if (args.length > 1)
			{
				Boot.redirectOutput(args[1]);
			}
		}

		try
		{
			System.out.println("ECT Installation");
			System.getProperties().store(System.out, "All Java properties:");
		}
		catch (final java.io.IOException e)
		{
			System.err.println("ERROR: " + e);
		}
		final Installation i = new Installation(file, true);
		i.reallyStart();

		// clean shutdown
		try
		{
			final java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
			while (true)
			{
				final String line = in.readLine();
				if (line == null || line.startsWith("q") || line.startsWith("Q"))
				{
					System.err.println("Exiting on null/q input...");
					System.exit(0);
				}
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR doing clean terminate input: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Previously known installation constructor (don't call on the swing thread with
	 * discoverDataspace true)
	 */
	public Installation(final File path, final boolean discoverDataspace)
	{
		loadProperties(path.getAbsolutePath(), discoverDataspace);
	}

	/**
	 * New or discovered installation constructor. If master, builds {@link #fullGroupName} using
	 * {@link #generateGroupName(String)}. If not master, splits input at last '*' to give
	 * {@link #dataspaceUrl} and {@link #fullGroupName}. If not master, currently assume that master
	 * is alive.
	 */
	public Installation(final String info, final boolean master)
	{
		init(info, master, null);
	}

	/**
	 * slave for known DS
	 */
	public Installation(final String info, final String dsurl)
	{
		init(info, false, dsurl);
	}

	public DataProxy getDataspace()
	{
		return this.dataspace;
	}

	public String getDataspaceUrl()
	{
		return this.dataspaceUrl;
	}

	public String getDirectoryName()
	{
		return new File(path).getName();
	}

	public String getDownloadUrl()
	{
		return this.configUrl;
	}

	/**
	 * path getter
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * get shared secret
	 */
	public String getSharedSecret()
	{
		return sharedSecret;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getWebserverUrl()
	{
		if (httpServer == null) { return null; }
		return this.httpServer.getBaseURL();
	}

	public boolean hasDataspaceFolder()
	{
		return this.haveDataspaceFolder;
	}

	/**
	 * is container autostart?
	 */
	public synchronized boolean isContainerAutostart(final String container)
	{
		if (container == null) { return false; }
		// System.out.println("Is container "+container+" autostart?");
		if (this.autoStart != null && !this.autoStart.equals(BootGlobals.NULL_VALUE) && !this.autoStart.equals(""))
		{
			final String[] bootConts = BootGlobals.parseStringArray(this.autoStart);
			// if (BootGlobals.DEBUG) System.err.println("Wheelarg! Found..."+bootConts.length);
			for (final String bootCont : bootConts)
			{
				// System.out.println("  does it match "+bootConts[i]+"?");
				if (container.indexOf(bootCont) > -1) {

				return true; }
			}
		}
		System.out.println("  no");
		return false;
	}

	public boolean isDiscoveryDisabled()
	{
		return this.disableDiscovery;
	}

	// Getters and setters
	public boolean isMaster()
	{
		return this.iAmTheOneTrueMaster;
	}

	public boolean isMasterAlive()
	{
		return this.theMasterIsAlive;
	}

	/**
	 * check secret against sig?!
	 */
	public boolean isMasterAlive(final String password)
	{
		if (!theMasterIsAlive) { return false; }
		if (discoverySignature == null)
		{
			System.err.println("WARNING: discovered dataspace rejected without siganture");
			return false;
		}
		// discoverySignature
		System.out.println("isMasterAlive for password " + password + " and discoverySignature " + discoverySignature);
		final String challenge = this.dataspaceUrl + "*" + this.fullGroupName;
		final String response = equip.data.Challenge.makeResponse(password, challenge);
		if (!(response.equals(discoverySignature)))
		{
			System.err.println("isMasterAlive signature test failed for " + challenge + ": " + response + " vs "
					+ discoverySignature);
			return false;
		}
		System.err.println("Dataspace signature ok");
		return true;
	}

	/**
	 * update whether container is autostart
	 */
	public synchronized void setContainerAutostart(final String container, final boolean autostart)
	{
		if (container == null) { return; }
		if (autostart)
		{
			if (this.autoStart == null || this.autoStart.equals(BootGlobals.NULL_VALUE) || this.autoStart.equals(""))
			{
				this.autoStart = container;
			}
			else
			{
				if (this.autoStart.indexOf(container) < 0)
				{
					this.autoStart = this.autoStart + "," + container;
				}
				else
				{
					// already there
					return;
				}
			}
		}
		else
		{
			// remove
			if (this.autoStart == null || this.autoStart.equals(BootGlobals.NULL_VALUE) || this.autoStart.equals(""))
			{
				// not there
				return;
			}

			final int ix = this.autoStart.indexOf(container);
			if (ix < 0)
			{
				// not there
				return;
			}
			final int cix = this.autoStart.indexOf(',', ix);
			if (cix < 0)
			{
				// end
				this.autoStart = this.autoStart.substring(0, ix);
			}
			else
			{
				// not end
				this.autoStart = this.autoStart.substring(0, ix) + this.autoStart.substring(cix + 1);
			}
		}
		// update and save config
		System.out.println("Saving installation properties with updated autostart: " + this.autoStart);
		instProperties.setProperty(BootGlobals.STARTUP_PROP, this.autoStart);
		BootGlobals.savePropertyFile(new File(this.path + BootGlobals.getSeparator()
				+ BootGlobals.PROP_FILE_NAME), instProperties);
	}

	public void setDataspaceUrl(final String url)
	{
		// make sure you've trimmed that trailing group name
		this.dataspaceUrl = url.substring(0, url.lastIndexOf('*'));
	}

	// TODO ... race hazard here when >2 BootFrames up on same machine?
	public void setMaster()
	{
		this.iAmTheOneTrueMaster = this.haveDataspaceFolder && !this.theMasterIsAlive;
	}

	public void setMasterAlive(final boolean f)
	{
		this.theMasterIsAlive = f;
	}

	/**
	 * start installation.
	 * <ul>
	 * <li>(re)Saves installation properties file (with secret).</li>
	 * <li>Does {@link #coldStart} if requested (typically by property file
	 * {@link BootGlobals#COLD_START_PROP}).</li>
	 * <li>If master, calls {@link #startup()} and {@link #advertise(String)} to start and advertise
	 * dataspace server [this should probably be a separate sub-process -cmg].</li>
	 * <li>If not master, calls {@link #join()} to join dataspace [this is probably unnecessary (at
	 * present) since each container is a separate process and will join itself -cmg].</li>
	 * <li>Creates {@link equip.ect.webstart.Container}s for all known installation containers.</li>
	 * <li>creates {@link equip.ect.webstart.HostManager}.</li>
	 * <li>calls {@link #startContainer(String, HostManager)} which in turn calls {@link equip.ect.webstart.Container#start(HostManager)} on all
	 * auto-start configured {@link equip.ect.webstart.Container}s.</li>
	 * </ul>
	 * 
	 * @param secret
	 *            Shared secret used for within-installation security and trust management.
	 */
	public boolean start(final String secret)
	{
		if (!this.isValid) { return false; }
		if (BootGlobals.DEBUG)
		{
			System.err.println("Installation valid.");
		}
		if (this.createDirectories)
		{
			setupDirectories();
		}
		// mark this as autostart
		try
		{
			final String bootPropertyFile = BootGlobals.getLocalFilestoreRoot().getCanonicalPath()
					+ BootGlobals.getSeparator() + BootGlobals.PROP_FILE_NAME;
			final File temp = new File(bootPropertyFile);
			java.util.Properties bootProperties = null;
			if (!temp.exists())
			{
				if (BootGlobals.DEBUG)
				{
					System.err.println("First Installation detected! Attempting to write boot properties...");
				}
				bootProperties = new java.util.Properties();
			}
			else
			{
				bootProperties = BootGlobals.loadPropertyFile(temp);
			}
			final File installationDir = new File(this.path);
			bootProperties.setProperty(BootGlobals.STARTUP_PROP, installationDir.getName());
			BootGlobals.savePropertyFile(temp, bootProperties);
		}
		catch (final java.io.IOException e)
		{
			System.err.println("ERROR setting this installation to autostart: " + e);
			e.printStackTrace(System.err);
		}
		// increment download epoch (master)
		if (this.isMaster())
		{
			int dlepoch = 0;
			try
			{
				final String sdlepoch = instProperties.getProperty(BootGlobals.DOWNLOAD_EPOCH);
				if (sdlepoch != null)
				{
					dlepoch = Integer.parseInt(sdlepoch);
				}
			}
			catch (final NumberFormatException e)
			{
				System.err.println("ERROR parsing " + BootGlobals.DOWNLOAD_EPOCH + ": "
						+ instProperties.getProperty(BootGlobals.DOWNLOAD_EPOCH));
			}
			instProperties.setProperty(BootGlobals.DOWNLOAD_EPOCH, Integer.toString(dlepoch + 1));
		}
		synchronized (this)
		{
			// Do stuff with the shared secret
			if (this.sharedSecret == null)
			{
				if (secret == null) { return false; }
				this.sharedSecret = secret;
			}
			instProperties.setProperty(BootGlobals.SECRET_PROP, this.sharedSecret);
			BootGlobals.savePropertyFile(new File(this.path + BootGlobals.getSeparator()
					+ BootGlobals.PROP_FILE_NAME), instProperties);
		}
		// else assume installation is good to go...
		if (this.coldStart)
		{
			this.coldStart();
		}

		// refresh dataspace persistence config (if master)
		if (this.isMaster())
		{
			try
			{
				final File configFile = new File(path, BootGlobals.DS_CONFIG_FILE);
				final java.io.FileWriter out = new java.io.FileWriter(configFile);
				out.write("# auto-gen config file for Installation " + this + "\n");
				// guess dataspace URL
				String url = BootGlobals.DEFAULT_DS_URL;
				String host = System.getProperty("LOCALHOST");
				if (host == null)
				{
					host = equip.net.ServerSap.rewriteLocalAddress(java.net.InetAddress.getLocalHost())
							.getHostAddress();
				}
				url = url.substring(0, url.indexOf("//") + 2) + host + url.substring(url.indexOf("//") + 2);
				System.out.println("Guess dataspace URL as " + url);
				final StringBuilder escurl = new StringBuilder(url);
				int i;
				for (i = 0; i < escurl.length(); i++)
				{
					if (escurl.charAt(i) == ':' || escurl.charAt(i) == '=' || escurl.charAt(i) == ' ')
					{
						escurl.setCharAt(i, '_');
					}
				}
				url = escurl.toString();
				out.write(url + ".dataStore1Name: " + BootGlobals.DS_DIR_NAME + "\n");
				out.write(url + ".dataStore1Class: equip.data.FileBackedMemoryDataStore\n");
				out.write(BootGlobals.DS_DIR_NAME + ".path: " + this.path + "\n");
				out.write(BootGlobals.DS_DIR_NAME + ".checkpointEventCount: 100000\n");
				out.write(BootGlobals.DS_DIR_NAME + ".maxFlushIntervalS: 2\n");
				// * STOREID.writeProcessBound: T/F [default T]
				// * STOREID.writePureEvents: T/F [default T]
				// * STOREID.restoreProcessBound: T/F [default F - don't enable this unless you are
				// really sure :-)]
				out.close();
			}
			catch (final Exception e)
			{
				System.err.println("ERROR writing dataspace persistence config file: " + e);
				e.printStackTrace(System.err);
			}
		}

		// really start...
		try
		{
			final File logFile = new File(this.path + File.separator + Boot.LOG_PREFIX + System.currentTimeMillis()
					+ Boot.LOG_SUFFIX);

			final String classPath = Container.removeQuotes(Boot.deriveFullWebStartClassPath());
			String libraryPath = Container.removeQuotes(System.getProperty("java.library.path"));
			final String commonDir = path + File.separator + "java" + File.separator + "common";
			final File common = new File(commonDir);
			if (libraryPath == null)
			{
				libraryPath = common.getCanonicalPath();
			}
			else if (!libraryPath.contains(common.getCanonicalPath()))
			{
				libraryPath = common.getCanonicalPath() + File.pathSeparator + libraryPath;
			}
			System.err.println("libraryPath -common = " + libraryPath);

			final String execStr = "java \"-DEQUIP_PATH="
					+ this.path
					+ "\" "
					+ Boot.getProxyProperties()
					+ "\"-DDataspaceSecret="
					+ this.sharedSecret
					+ "\" "
					+ "-classpath "
					+ "\""
					+ classPath
					+ "\" "
					+ "\"-Djava.library.path="
					+ libraryPath
					+ ";\" "
					+ (System.getProperty("discoveryUrls", null) != null ? " \"-DdiscoveryUrls="
							+ System.getProperty("discoveryUrls") + "\" " : "")
					+ (System.getProperty("externalDataspaceHost", null) != null ? " \"-DexternalDataspaceHost="
							+ System.getProperty("externalDataspaceHost") + "\" " : "")
					+ (System.getProperty("externalDataspacePort", null) != null ? " \"-DexternalDataspacePort="
							+ System.getProperty("externalDataspacePort") + "\" " : "")
					+ " ect.webstart.Installation " + "\"" + this.path + "\" \"" + logFile.getAbsolutePath()
					+ "\"";

			System.out.println("launching Installation: " + execStr);

			if (Boot.isWindows())
			{
				myProcess = ProcessUtils.exec(execStr, null);
			}
			else
			{
				myProcess = Runtime.getRuntime().exec(Boot.parseCommandLine(execStr));
			}
			/*
			 * try { OutputStream ps = new BufferedOutputStream (new FileOutputStream(logFile)); new
			 * StreamPump(myProcess.getErrorStream(), ps, true); new
			 * StreamPump(myProcess.getInputStream(), ps);
			 * 
			 * } catch (Exception e) {
			 * System.err.println("ERROR writing installation output to file: "+e); DebugFrame
			 * debugFrame = new DebugFrame("Installation Output", 400, 400);
			 * debugFrame.setVisible(true);
			 * debugFrame.processInputStream(myProcess.getErrorStream());
			 * debugFrame.processInputStream(myProcess.getInputStream()); }
			 */
			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						myProcess.waitFor();
						System.out.println("joined installation");
					}
					catch (final Exception e)
					{
						System.err.println("ERROR joining Installation process: " + e);
					}
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							// reload property file
							loadProperties(path, false);
							Boot.showBootFrame();
						}
					});
				}
			}.start();
		}
		catch (final Exception e)
		{
			if (BootGlobals.DEBUG)
			{
				e.getMessage();
			}
			return false;
		}
		return true;
	}

	/**
	 * start selected container
	 */
	public boolean startContainer(final String name, final HostManager foo)
	{
		if (BootGlobals.DEBUG)
		{
			System.err.println("Attempting to start container " + name + "...");
		}
		if (!this.containers.containsKey(name))
		{
			if (BootGlobals.DEBUG)
			{
				System.err.println("...unsuccessful.");
			}
			return false;
		}
		if (BootGlobals.DEBUG)
		{
			System.err.println("...successful.");
		}
		foo.startContainer(containers.get(name));
		return true;
	}

	/**
	 * start selected container
	 */
	public boolean stopContainer(final String name, final HostManager foo)
	{
		if (BootGlobals.DEBUG)
		{
			System.err.println("Attempting to stpo container " + name + "...");
		}
		if (!this.containers.containsKey(name))
		{
			if (BootGlobals.DEBUG)
			{
				System.err.println("...unsuccessful.");
			}
			return false;
		}
		if (BootGlobals.DEBUG)
		{
			System.err.println("...successful.");
		}
		foo.stopContainer(containers.get(name));
		return true;
	}

	/**
	 * start selected container
	 */
	public void stopContainers(final HostManager foo)
	{
		if (BootGlobals.DEBUG)
		{
			System.err.println("Attempting to stpo containers...");
		}
		for (String name : containers.keySet())
		{
			stopContainer(name, foo);
		}
		if (BootGlobals.DEBUG)
		{
			System.err.println("...tried.");
		}
	}

	/**
	 * For swing components to use
	 */
	@Override
	public String toString()
	{
		try
		{
			String name = "Install";
			if(fullGroupName != null)
			{
				name = fullGroupName.substring(0, fullGroupName.lastIndexOf('#'));
			}
			//final String prefix = (this.isMaster()) ? "Master: " : "Slave: ";
			// if (this.isMaster()) return
			// "<html><font color=#008800>Master: "+name+"</font></html>";
			// if (this.isMasterAlive()) return
			// "<html><font color=#008800>Slave: "+name+"</font></html>";
			// return "<html><font color=#884444>Slave: "+name+"</font></html>";
			if (this.isMaster()) { return "Master: " + name; }
			if (this.isMasterAlive()) { return "Slave: " + name + " (installation active)"; }
			return "Slave: " + name + " (installation " + (disableDiscovery ? "status unknown - remote" : "inactive")
					+ ")";
		}
		catch (final Exception e)
		{
			System.err.println("ERROR doing Installation.toString: " + e);
			e.printStackTrace(System.err);
			return "Invalid local installation";
		}
	}

	void requestSlaveUpload()
	{
		final int ulepoch = 1;
		final equip.data.TupleImpl ultuple = new equip.data.TupleImpl(new equip.data.StringBoxImpl(
				BootGlobals.UPLOAD_REQUEST_TUPLETYPE), new equip.data.IntBoxImpl(ulepoch),
				new equip.data.StringBoxImpl(httpServer.getBaseURL() + "history/"));
		ultuple.id = guids.getUnique();
		dataspace.addItem(ultuple, equip.data.LockType.LOCK_HARD, true, false, null);
		// processBound, local, itemLease
		System.out.println("Updated dataspace upload info to " + ulepoch + ", " + httpServer.getBaseURL() + "history/");
		hostManager.setStatus("Request slave upload update");
		dataspace.deleteItem(ultuple.id, false);
	}

	/**
	 * slave download flagged/updated from master
	 */
	protected void handleSlaveDownload(final int masterepoch, String masterurl)
	{
		if (disableDiscovery)
		{
			// may be outside NAT firewall - use explicitly configured URL
			System.out.println("Using explicit download url " + configUrl);
			masterurl = this.configUrl;
		}

		// stop??
		hostManager.setStatus("Slave update triggered: stop containers");
		stopContainers(hostManager);

		int epoch = 0;
		final String sepoch = instProperties.getProperty(BootGlobals.DOWNLOAD_EPOCH);
		if (sepoch != null)
		{
			try
			{
				epoch = Integer.parseInt(sepoch);
			}
			catch (final NumberFormatException e)
			{
			}
		}
		if (epoch == masterepoch)
		{
			hostManager.setStatus("Download already up to date");
			System.out.println("Download already up to date (epoch " + epoch + ")");
		}
		else
		{
			hostManager.setStatus("Downloading components and their dependencies");
			// download /update
			download = new DownloadManager(new File(this.path), masterurl);
			final boolean downloadOk = download.update();
			hostManager.setStatus("Download " + (downloadOk ? "succeeded" : "failed"));
			System.out.println("Download " + (downloadOk ? "succeeded" : "failed"));

			// update props?
			if (downloadOk)
			{
				instProperties.setProperty(BootGlobals.DOWNLOAD_EPOCH, Integer.toString(masterepoch));
				instProperties.setProperty(BootGlobals.CONFIG_URL_PROP, masterurl);
				BootGlobals.savePropertyFile(new File(this.path + BootGlobals.getSeparator()
						+ BootGlobals.PROP_FILE_NAME), instProperties);
			}
		}

		// ends in download?! - full migrate!
		String uploadurl = masterurl;
		if (uploadurl.endsWith("/download/"))
		{
			// one of ours?!
			uploadurl = uploadurl.substring(0, uploadurl.length() - 9) + "history/";
		}
		handleUploadRequest(1, uploadurl, false);

		// start??
		// autostart any containers...
		if (BootGlobals.DEBUG)
		{
			System.err.println("Looking for containers to autostart...");
		}
		if (this.autoStart != null && !this.autoStart.equals(BootGlobals.NULL_VALUE) && !this.autoStart.equals(""))
		{
			final String[] bootConts = BootGlobals.parseStringArray(this.autoStart);
			if (BootGlobals.DEBUG)
			{
				System.err.println("Wheelarg! Found..." + bootConts.length);
			}
			for (final String bootCont : bootConts)
			{
				if (containerDirs.contains(bootCont))
				{
					if (BootGlobals.DEBUG)
					{
						System.err.println(bootCont + " exists. Firing it up.");
					}
					hostManager.setStatus("Starting container " + bootCont);
					startContainer(bootCont, hostManager);
				}
				else if (BootGlobals.DEBUG)
				{
					System.err.println("Can't find " + bootCont + ".");
				}
			}
		}
		else if (BootGlobals.DEBUG)
		{
			System.err.println("None found...");
		}
		hostManager.setStatus("Installation slave updated");
	}

	/**
	 * slave upload flagged/updated from master
	 */
	protected void handleUploadRequest(final int request, String masterurl, final boolean updateOnly)
	{
		if (disableDiscovery)
		{
			// may be outside NAT firewall - use explicitly configured URL
			System.out.println("Using explicit upload url based on " + configUrl);
			masterurl = this.configUrl;
			// ends in download?! - full migrate!
			if (masterurl.endsWith("/download/"))
			{
				// one of ours?!
				masterurl = masterurl.substring(0, masterurl.length() - 9) + "history/";
			}
		}

		// client-specific path component
		String clientname = "undefined";
		try
		{
			final java.net.InetAddress host = java.net.InetAddress.getLocalHost();
			clientname = host.getCanonicalHostName();
		}
		catch (final Exception e)
		{
			System.err.println("ERROR getting local host canonical name: " + e);
		}
		masterurl = masterurl + clientname + "/";
		hostManager.setStatus("Upload to " + masterurl);
		try
		{
			// update, migrate
			final boolean ok = UploadManager.upload(new File(this.path), new java.net.URL(masterurl), true,
													!updateOnly);
			hostManager.setStatus("Upload " + (ok ? "succeeded" : "failed"));
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: Uploading: " + e);
			e.printStackTrace(System.err);
			hostManager.setStatus("Upload error: " + e);
		}
	}

	protected void init(final String info, final boolean master, final String dsurl)
	{
		this.createDirectories = true;
		if (master)
		{
			if (BootGlobals.DEBUG)
			{
				System.err.println("Creating new Installation: " + info);
			}
			// starting a mastered installation ... info is readable groupName
			iAmTheOneTrueMaster = true;
			this.fullGroupName = generateGroupName(info);
			this.isValid = true;
		}
		else
		{
			if (BootGlobals.DEBUG)
			{
				System.err.println("Using discovered Installation: " + info);
			}
			if (dsurl != null)
			{
				this.fullGroupName = generateGroupName(info);
				this.dataspaceUrl = dsurl;
				this.disableDiscovery = true;
			}
			else
			{
				this.dataspaceUrl = info.substring(0, info.indexOf('*'));
				final String rest = info.substring(info.indexOf('*') + 1);
				if (rest.indexOf('*') < 0)
				{
					this.fullGroupName = rest;
					this.discoverySignature = null;
				}
				else
				{
					this.fullGroupName = rest.substring(0, rest.indexOf('*'));
					this.discoverySignature = rest.substring(rest.indexOf('*') + 1);
				}
			}
			this.isValid = true;
			this.theMasterIsAlive = true; // like duh (Note to self)
		}
	}

	protected synchronized void loadProperties(String path, final boolean discoverDataspace)
	{
		try
		{
			this.path = new File(path).getCanonicalPath();
			path = this.path;
		}
		catch (final java.io.IOException e)
		{
			System.err.println("ERROR getting canonical path for " + path + ": " + e);
			this.path = path;
		}
		final File propPath = new File(path + BootGlobals.getSeparator() + BootGlobals.PROP_FILE_NAME);
		// validate the installation, and get those group name/secrets out...
		instProperties = BootGlobals.loadPropertyFile(propPath);
		if (BootGlobals.DEBUG)
		{
			System.err.println("Looking for Installation startup props...");
		}
		if (instProperties != null)
		{
			if (instProperties.getProperty(BootGlobals.NO_DISCOVERY_DS_URL) != null)
			{
				disableDiscovery = true;
				dataspaceUrl = instProperties.getProperty(BootGlobals.NO_DISCOVERY_DS_URL);
			}
			if (BootGlobals.DEBUG)
			{
				System.err.println("boot.prop found...");
			}
			this.fullGroupName = instProperties.getProperty(BootGlobals.GROUP_PROP);
			if (BootGlobals.DEBUG)
			{
				System.out.println("Configuring " + this.fullGroupName);
			}
			if (instProperties.getProperty(BootGlobals.CONFIG_URL_PROP) != null)
			{
				this.configUrl = instProperties.getProperty(BootGlobals.CONFIG_URL_PROP);
			}
			else
			{
				this.configUrl = System.getProperty("download", null);
			}
			final String sec = instProperties.getProperty(BootGlobals.SECRET_PROP);
			this.autoStart = instProperties.getProperty(BootGlobals.STARTUP_PROP);
			final String temp = instProperties.getProperty(BootGlobals.COLD_START_PROP);
			if (temp != null && temp.equals(BootGlobals.TRUE))
			{
				this.coldStart = true;
			}
			if (this.fullGroupName != null && sec != null)
			{
				this.sharedSecret = sec;
				// is the dataspacePersistence folder around here somewhere?
				if (BootGlobals.DEBUG)
				{
					System.err.println("Looking for dataspace persistence folder...");
				}
				containerDirs = BootGlobals.listSubDirs(new File(path));
				final int lala = containerDirs.indexOf(BootGlobals.DS_DIR_NAME);
				this.haveDataspaceFolder = (lala > -1);

				// See if this installation is currently up,whether we are master or not...
				if (BootGlobals.DEBUG)
				{
					System.err.print("Checking to see if this installation is alive already...{");
				}
				if (discoverDataspace && !disableDiscovery)
				{
					this.dataspaceUrl = discover(this.fullGroupName);
				}
				if (BootGlobals.DEBUG)
				{
					System.err.println((this.dataspaceUrl != null) + "}");
				}

				if (this.haveDataspaceFolder && this.dataspaceUrl == null)
				{ // restart as the one true master
					if (BootGlobals.DEBUG)
					{
						System.err.println("I Am The One True Master...");
					}
					iAmTheOneTrueMaster = true;
					// remove this folder from the container dir list...
					containerDirs = containerDirs.substring(0, lala)
							+ containerDirs.substring(	lala + BootGlobals.DS_DIR_NAME.length() + 1,
														containerDirs.length());
					if (BootGlobals.DEBUG)
					{
						System.err.println("Container dirs after: " + containerDirs);
					}
					this.isValid = true;
				}
				else
				{ // rediscover/rejoin another installation
					if (BootGlobals.DEBUG)
					{
						System.err.println("I be the slave...");
					}
					if (discoverDataspace)
					{
						this.theMasterIsAlive = (this.dataspaceUrl != null);
					}
					this.isValid = true;
				}

				// drop dirs without prop file
				containerDirs = BootGlobals.listSubDirsWithPropFiles(new File(path));
			}
			// else installation is shafted
			else if (BootGlobals.DEBUG)
			{
				System.err.println("either fullGroupName or secret are shafted...");
			}
		}
		// else installation is shafted
		else if (BootGlobals.DEBUG)
		{
			System.err.println("boot.prop NOT found (" + propPath + "), installation shafted...");
		}
	}

	/**
	 * really start - in child process
	 */
	protected boolean reallyStart()
	{

		// Start the HostManager utility...
		hostManager = new HostManager(this);
		// start this Installation
		if (this.isMaster())
		{
			hostManager.setStatus("Starting dataspace server");
			// start it up
			this.dataspaceUrl = this.startup();
			if (dataspace == null)
			{
				hostManager.setStatus("Failed to start dataspace server");
				return false;
			}
			hostManager.setStatus("Advertising dataspace server");
			// advertise it
			guids = new equip.data.GUIDFactoryImpl();
			advertise(fullGroupName);
			hostManager.setStatus("Starting HTTP server");
			try
			{
				httpServer = new UploadHttpServer(HTTP_PORT, path);
			}
			catch (final Exception e)
			{
				System.err.println("ERROR starting installation web server on port " + HTTP_PORT + ": " + e);
				hostManager.setStatus("Start HTTP server failed");
			}
		}
		else
		{
			hostManager.setStatus("Joining dataspace");
			// let's join in...
			if (this.dataspaceUrl != null && !this.dataspaceUrl.equals(""))
			{
				this.join();
			}
			else
			{
				System.err.println("Start slave with undiscoverable dataspace - giving up!");
				System.exit(-1);
			}
		}

		if (this.isMaster() && configUrl != null)
		{
			hostManager.setStatus("Downloading components and their dependencies");
			// download /update
			download = new DownloadManager(new File(this.path), this.configUrl);
			final boolean downloadOk = download.update();
			hostManager.setStatus("Download " + (downloadOk ? "succeeded" : "failed"));
			System.out.println("Download " + (downloadOk ? "succeeded" : "failed"));
		}
		// publish download epoch
		int dlepoch = 0;
		try
		{
			final String sdlepoch = instProperties.getProperty(BootGlobals.DOWNLOAD_EPOCH);
			if (sdlepoch != null)
			{
				dlepoch = Integer.parseInt(sdlepoch);
			}
		}
		catch (final NumberFormatException e)
		{
			System.err.println("ERROR parsing " + BootGlobals.DOWNLOAD_EPOCH + ": "
					+ instProperties.getProperty(BootGlobals.DOWNLOAD_EPOCH));
		}

		hostManager.setStatus("Setting up containers");
		// scan container subdirectories, create Container objects...
		final String[] conts = BootGlobals.parseStringArray(containerDirs);
		containers.clear();
		for (final String cont : conts)
		{
			containers.put(cont, new Container(new File(this.path + BootGlobals.getSeparator() + cont),
					this.dataspaceUrl));
		}

		for (final String cont : conts)
		{
			hostManager.addContainer(containers.get(cont));
		}

		if (this.isMaster())
		{

			final equip.data.TupleImpl dltuple = new equip.data.TupleImpl(new equip.data.StringBoxImpl(
					BootGlobals.DOWNLOAD_EPOCH_TUPLETYPE), new equip.data.IntBoxImpl(dlepoch),
					new equip.data.StringBoxImpl(httpServer.getBaseURL() + "download/"));
			dltuple.id = guids.getUnique();
			dataspace.addItem(dltuple, equip.data.LockType.LOCK_HARD, true, false, null);
			// processBound, local, itemLease
			System.out.println("Updated dataspace download info to " + dlepoch + ", " + httpServer.getBaseURL()
					+ "download/");

			/*
			 * int ulepoch = 1; equip.data.TupleImpl ultuple = new equip.data.TupleImpl (new
			 * equip.data.StringBoxImpl(BootGlobals.UPLOAD_REQUEST_TUPLETYPE), new
			 * equip.data.IntBoxImpl(ulepoch), new
			 * equip.data.StringBoxImpl(httpServer.getBaseURL()+"history/")); ultuple.id =
			 * guids.getUnique(); dataspace.addItem(ultuple, equip.data.LockType.LOCK_HARD, true,
			 * false, null); //processBound, local, itemLease
			 * System.out.println("Updated dataspace upload info to "
			 * +ulepoch+", "+httpServer.getBaseURL()+"history/");
			 */
			// local migrate of data to history - could be done more efficiency via filesystem!
			final String uploadurl = httpServer.getBaseURL() + "history/";
			handleUploadRequest(1, uploadurl, false);
			new Thread()
			{
				@Override
				public void run()
				{
					while (true)
					{
						try
						{
							Thread.sleep(MASTER_MIGRATE_LOGS_INTERVAL_MS);
						}
						catch (final InterruptedException e)
						{
						}
						System.out.println("Master migrating old logs to history");
						handleUploadRequest(1, uploadurl, false);
					}
				}
			}.start();
		}
		else
		{
			hostManager.setStatus("Checking if download required from master");
			System.out.println("Checking for master download epoch in dataspace...");
			final equip.data.TupleImpl dltuple = new equip.data.TupleImpl(new equip.data.StringBoxImpl(
					BootGlobals.DOWNLOAD_EPOCH_TUPLETYPE), null, null);
			final equip.data.TupleImpl ultuple = new equip.data.TupleImpl(new equip.data.StringBoxImpl(
					BootGlobals.UPLOAD_REQUEST_TUPLETYPE), null, null);
			final equip.data.DataSession session = dataspace.createSession(new equip.data.DataCallback()
			{
				@Override
				public void notify(final equip.data.Event event, final equip.data.EventPattern pattern,
						final boolean patternDeleted, final equip.data.DataSession session,
						final equip.runtime.ValueBase closure)
				{
					System.out.println("Notify dataspace...");
					if (event instanceof equip.data.AddEvent)
					{
						final equip.data.AddEvent add = (equip.data.AddEvent) event;
						if (add.binding != null && add.binding.item instanceof equip.data.Tuple)
						{
							final equip.data.Tuple tuple = (equip.data.Tuple) add.binding.item;
							if (tuple.fields != null
									&& tuple.fields.length >= 3
									&& tuple.fields[0] instanceof equip.data.StringBox
									&& ((equip.data.StringBox) tuple.fields[0]).value
											.equals(BootGlobals.DOWNLOAD_EPOCH_TUPLETYPE)
									&& tuple.fields[1] instanceof equip.data.IntBox
									&& tuple.fields[2] instanceof equip.data.StringBox)
							{
								final int ep = ((equip.data.IntBox) tuple.fields[1]).value;
								final String url = ((equip.data.StringBox) tuple.fields[2]).value;
								System.out.println("Download epoch found to be " + ep + ", " + url);
								handleSlaveDownload(ep, url);
							}
							if (tuple.fields != null
									&& tuple.fields.length >= 3
									&& tuple.fields[0] instanceof equip.data.StringBox
									&& ((equip.data.StringBox) tuple.fields[0]).value
											.equals(BootGlobals.UPLOAD_REQUEST_TUPLETYPE)
									&& tuple.fields[1] instanceof equip.data.IntBox
									&& tuple.fields[2] instanceof equip.data.StringBox)
							{
								final int ep = ((equip.data.IntBox) tuple.fields[1]).value;
								final String url = ((equip.data.StringBox) tuple.fields[2]).value;
								System.out.println("Upload request found for " + ep + ", " + url);
								handleUploadRequest(ep, url, true);
							}
						}
					}
				}
			}, null);
			final equip.data.EventPattern pattern = new equip.data.EventPatternImpl();
			pattern.initAsSimpleItemMonitor(dltuple, false);
			session.addPattern(pattern);
			final equip.data.EventPattern pattern2 = new equip.data.EventPatternImpl();
			pattern2.initAsSimpleItemMonitor(ultuple, false);
			session.addPattern(pattern2);
			// dataspace.waitForEvents(false);
			System.out.println("Checking for master download epoch in dataspace done");
			// ....
		}
		// recheck tools
		// ....
		// autostart any containers...
		if (BootGlobals.DEBUG)
		{
			System.err.println("Looking for containers to autostart...");
		}
		if (this.isMaster() && this.autoStart != null && !this.autoStart.equals(BootGlobals.NULL_VALUE)
				&& !this.autoStart.equals(""))
		{
			final String[] bootConts = BootGlobals.parseStringArray(this.autoStart);
			if (BootGlobals.DEBUG)
			{
				System.err.println("Wheelarg! Found..." + bootConts.length);
			}
			for (final String bootCont : bootConts)
			{
				if (containerDirs.contains(bootCont))
				{
					if (BootGlobals.DEBUG)
					{
						System.err.println(bootCont + " exists. Firing it up.");
					}
					hostManager.setStatus("Starting container " + bootCont);
					startContainer(bootCont, hostManager);
				}
				else if (BootGlobals.DEBUG)
				{
					System.err.println("Can't find " + bootCont + ".");
				}
			}
		}
		else if (BootGlobals.DEBUG)
		{
			System.err.println("None found...");
		}
		hostManager.setStatus("Installation started");
		return true;
	}

	private void advertise(final String groupName)
	{
		if (BootGlobals.DEBUG)
		{
			System.err.println("Advertising: " + groupName);
		}
		// advertise group name and dataspace
		final DiscoveryServerAgent agent = new DiscoveryServerAgentImpl();
		final ServerDiscoveryInfo temp = new ServerDiscoveryInfoImpl();
		temp.groups = new String[] { groupName };
		temp.serviceTypes = new String[] { BootGlobals.DEFAULT_SERVICE };
		temp.urls = new String[] { this.dataspaceUrl + "*" + groupName + "*"
				+ equip.data.Challenge.makeResponse(this.sharedSecret, this.dataspaceUrl + "*" + groupName) };
		agent.startDefault(new ServerDiscoveryInfo[] { temp });
	}

	/**
	 * clear out persistent data TODO long term ... cold start ... remove persistent data
	 */
	private void coldStart()
	{
		// clear out persistent data
		// TODO long term ... cold start ... remove persistent data
	}

	/**
	 * Discovery Management - don't call on the swing thread - takes too long (and will give
	 * exceptions)
	 */
	private String discover(final String groupName)
	{
		if (BootGlobals.DEBUG)
		{
			System.err.println("Discovering: " + groupName);
		}
		// return valid dataspace surl if there is one, null if not...
		// Validates the DS
		// this takes time
		final DataspaceFinder finder = new DataspaceFinder();
		final JProgressBar progress[] = new JProgressBar[1];
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					final JFrame waiting = new JFrame("Discovery");
					waiting.getContentPane().setLayout(new BorderLayout());
					progress[0] = new JProgressBar(0, MAX_DISCOVER_COUNT);// seconds
					waiting.getContentPane().add(new JLabel("Looking for group " + groupName + "..."),
													BorderLayout.NORTH);
					waiting.getContentPane().add(progress[0]);
					waiting.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
					waiting.addWindowListener(new WindowAdapter()
					{
						@Override
						public void windowClosing(final WindowEvent e)
						{
							System.err.println("Try to finish discovery");
							finder.stop();
							// close soon
						}
					});
					waiting.pack();
					waiting.setVisible(true);
					final Timer timer = new Timer(DISCOVER_STEP_MS, new ActionListener()
					{
						@Override
						public void actionPerformed(final ActionEvent ae)
						{
							if (progress[0].getValue() >= MAX_DISCOVER_COUNT)
							{
								((Timer) ae.getSource()).stop();
								waiting.setVisible(false);
								System.out.println("Discover dialog closed");
							}
							else
							{
								progress[0].setValue(progress[0].getValue() + 1);
							}
						}
					});
					timer.start();
				}
			});
		}
		catch (final Exception e)
		{
			System.err.println("Interrupted: " + e);
			e.printStackTrace(System.err);
		}
		final String res = finder.go(groupName);
		System.out.println("Discover " + groupName + " done: " + res);
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					if (progress[0] != null)
					{
						progress[0].setValue(MAX_DISCOVER_COUNT);
					}
				}
			});
		}
		catch (final Exception e)
		{
			System.err.println("Interrupted: " + e);
			e.printStackTrace(System.err);
		}
		return res;
	}

	private String generateGroupName(final String readableBit)
	{
		try
		{
			return readableBit.replace('#', '$') + "#" + java.net.InetAddress.getLocalHost().toString()
					+ System.currentTimeMillis();
		}
		catch (final java.net.UnknownHostException e)
		{
			return readableBit;
		}
	}

	private void join()
	{
		if (BootGlobals.DEBUG)
		{
			System.err.println("Joining: " + this.dataspaceUrl);
		}
		// TODO ... authentication and validation on this.dataspaceUrl
		// if successful, join dataspace
		dataspace = DataManager.getInstance().getDataspace(this.dataspaceUrl, DataManager.DATASPACE_CLIENT, true, true);
	}

	/**
	 * rewrite (if necessary) url for NATing firewall. based on equip.net.ServerURL.getURL
	 */
	private String rewriteMonikerForFirewall(Moniker mon)
	{
		int addr = 0;
		short port = 0;
		String scheme = "equip";
		TraderMoniker tmon = null;
		if (mon instanceof TraderMoniker)
		{
			tmon = (TraderMoniker) mon;
			mon = tmon.trader;
		}
		if (mon instanceof SimpleTCPMoniker)
		{
			final SimpleTCPMoniker tcp = (SimpleTCPMoniker) mon;
			addr = tcp.addr;
			port = tcp.port;
			scheme = "equip";
		}
		else if (mon instanceof SimpleUDPMoniker)
		{
			final SimpleUDPMoniker tcp = (SimpleUDPMoniker) mon;
			addr = tcp.addr;
			port = tcp.port;
			scheme = "equipu";
		}
		else if (mon instanceof MulticastUDPMoniker)
		{
			final MulticastUDPMoniker tcp = (MulticastUDPMoniker) mon;
			addr = tcp.addr;
			port = tcp.port;
			scheme = "equipm";
		}
		else
		{
			System.err.println("Warning: ServerURL::getURL called with " + "unknown moniker type ("
					+ mon.getClass().getName() + ")");
			return null;
		}
		final StringBuilder buf = new StringBuilder();
		buf.append(scheme);
		buf.append("://");
		final StringBuilder abuf = new StringBuilder();
		abuf.append((addr >> 24) & 0xff);
		abuf.append('.');
		abuf.append((addr >> 16) & 0xff);
		abuf.append('.');
		abuf.append((addr >> 8) & 0xff);
		abuf.append('.');
		abuf.append((addr) & 0xff);
		buf.append(System.getProperty("externalDataspaceHost", abuf.toString()));
		buf.append(':');
		buf.append(System.getProperty("externalDataspacePort", Integer.toString(port & 0xffff)));
		buf.append('/');
		if (tmon != null)
		{
			buf.append(tmon.name);
		}

		return buf.toString();
	}

	/**
	 * Setup installation directory stuff for newly [created|discovered] installs
	 */
	private void setupDirectories()
	{
		this.createDirectories = false;
		if (BootGlobals.DEBUG)
		{
			System.err.println("Setting up installation directories for " + this.fullGroupName);
		}
		// create and write out properties file....
		instProperties.setProperty(BootGlobals.GROUP_PROP, this.fullGroupName);
		if (disableDiscovery && dataspaceUrl != null)
		{
			instProperties.setProperty(BootGlobals.NO_DISCOVERY_DS_URL, dataspaceUrl);
		}

		String rootLoc = null;
		try
		{
			rootLoc = BootGlobals.getLocalFilestoreRoot().getCanonicalPath();
		}
		catch (final java.io.IOException e)
		{
			System.err.println("ERROR getting canonical path for filestore root " + BootGlobals.getLocalFilestoreRoot()
					+ ": " + e);
			rootLoc = BootGlobals.getLocalFilestoreRoot().getAbsolutePath();
		}
		this.path = rootLoc + BootGlobals.getSeparator() + BootGlobals.clobberGroupName(this.fullGroupName);
		// prevent group folder name clashes
		final boolean goodName = false;
		final File installationDir = new File(this.path);
		if (installationDir.exists())
		{
			System.err.println("WARNING: over-wriing directory " + installationDir + " with new installation defaults");
		}
		// default download
		this.configUrl = System.getProperty("download", null);
		if (this.configUrl != null && !this.configUrl.startsWith("file:") && !this.configUrl.startsWith("http:"))
		{
			if (this.configUrl.charAt(1) == ':' || this.configUrl.startsWith("/") || this.configUrl.startsWith("\\"))
			{
				// absolute
				this.configUrl = "file://" + this.configUrl;
			}
			else
			{
				try
				{
					// relative
					this.configUrl = new File(this.configUrl).getCanonicalFile().toURI().toString();
				}
				catch (final Exception e)
				{
					System.err.println("ERROR getting URI for file " + this.configUrl + ": " + e);
				}
			}
		}
		if (this.configUrl != null)
		{
			instProperties.setProperty(BootGlobals.CONFIG_URL_PROP, this.configUrl);
		}

		if (BootGlobals.DEBUG)
		{
			System.err.println("Filename chosen for config: " + this.path);
		}
		// Make the new Installation directories...
		try
		{
			if (BootGlobals.DEBUG)
			{
				System.err.println("Creating config file...");
			}
			File temp;
			if (this.isMaster())
			{
				temp = new File(this.path + BootGlobals.getSeparator() + BootGlobals.DS_DIR_NAME);
			}
			else
			{
				temp = new File(this.path);
			}
			temp.mkdirs();
			final String bootPropertyFile = BootGlobals.getLocalFilestoreRoot().getCanonicalPath()
					+ BootGlobals.getSeparator() + BootGlobals.PROP_FILE_NAME;
			temp = new File(bootPropertyFile);
			java.util.Properties bootProperties = null;
			if (!temp.exists())
			{
				if (BootGlobals.DEBUG)
				{
					System.err.println("First Installation detected! Attempting to write boot properties...");
				}
				bootProperties = new java.util.Properties();
			}
			else
			{
				bootProperties = BootGlobals.loadPropertyFile(temp);
			}
			bootProperties.setProperty(BootGlobals.STARTUP_PROP, installationDir.getName());
			BootGlobals.savePropertyFile(temp, bootProperties);
			if (BootGlobals.DEBUG)
			{
				System.err.println("...finished");
			}
		}
		catch (final Exception e)
		{
			// ah well, better luck next time....
			if (BootGlobals.DEBUG)
			{
				System.err.println(e.getMessage());
			}
		}
		// Try to automatically create a Java Container
		if (BootGlobals.DEBUG)
		{
			System.err.println("Attempting to create Java container...");
		}
		String filename = this.path;
		if (Container.createJavaContainer(new File(this.path), this.dataspaceUrl) != null)
		{
			if (BootGlobals.DEBUG)
			{
				System.err.println("Successful...");
			}
			this.autoStart = BootGlobals.JAVA_CONTAINER;
			instProperties.setProperty(BootGlobals.STARTUP_PROP, this.autoStart);
			filename += BootGlobals.getSeparator() + BootGlobals.JAVA_CONTAINER;
			containerDirs = this.autoStart;
		}
		else if (BootGlobals.DEBUG)
		{
			System.err.println("Unsuccessful...");
		}
	}

	/**
	 * Dataspace Management. TODO sometime ... HTF to tell DS where it's persistence folder is at?
	 */
	private String startup()
	{
		// TODO sometime ... HTF to tell DS where it's persistence folder is at?
		dataspace = DataManager.getInstance().getDataspace(BootGlobals.DEFAULT_DS_URL, DataManager.DATASPACE_SERVER,
															true);
		final Moniker mon = ((Server) dataspace).getMoniker();
		// behind NATting firewall??
		final String dsurl = rewriteMonikerForFirewall(mon);
		if (BootGlobals.DEBUG)
		{
			System.err.println("Dataspace started, url = " + dsurl);
		}
		return dsurl;
	}

	// PRIVATE HELPER METHODS
	// Probably should use this method one day...
	private void wipeSecretFromMemory()
	{
		sharedSecret = null;
	}
}
