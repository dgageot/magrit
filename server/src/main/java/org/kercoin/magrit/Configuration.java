package org.kercoin.magrit;

import java.io.File;

public class Configuration {

	private static final String DEFAULT_BASE_DIR = System.getProperty("java.io.tmpdir") + "/magrit";

	private int sshPort = 2022;
	
	private File repositoriesHomeDir = new File(DEFAULT_BASE_DIR, "repos");
	
	private File publickeysRepositoryDir = new File(DEFAULT_BASE_DIR, "keys");
	
	private File workHomeDir = new File(DEFAULT_BASE_DIR, "builds");

	private boolean remoteAllowed;
	
	public int getSshPort() {
		return sshPort;
	}

	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}

	public File getRepositoriesHomeDir() {
		return repositoriesHomeDir;
	}

	public void setRepositoriesHomeDir(File repositoriesHomeDir) {
		this.repositoriesHomeDir = repositoriesHomeDir;
	}
	
	public File getWorkHomeDir() {
		return workHomeDir;
	}
	
	public void setWorkHomeDir(File workHomeDir) {
		this.workHomeDir = workHomeDir;
	}

	public File getPublickeyRepositoryDir() {
		return publickeysRepositoryDir;
	}
	
	public void setPublickeysRepositoryDir(File publickeysRepositoryDir) {
		this.publickeysRepositoryDir = publickeysRepositoryDir;
	}

	public void applyStandardLayout(String dir) {
		repositoriesHomeDir = new File(dir, "bares");
		workHomeDir = new File(dir, "builds");
		publickeysRepositoryDir = new File(dir, "keys");
	}

	public boolean isRemoteAllowed() {
		return remoteAllowed;
	}

	public void setRemoteAllowed(boolean remoteAllowed) {
		this.remoteAllowed = remoteAllowed;
	}
	
}
