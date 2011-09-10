package org.kercoin.magrit.commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.kercoin.magrit.Context;
import org.kercoin.magrit.utils.LoggerInputStream;
import org.kercoin.magrit.utils.LoggerOutputStream;

import com.google.inject.Provider;

public abstract class AbstractCommand<C extends AbstractCommand<C>> implements Command, Runnable {

	public static interface CommandProvider<C extends AbstractCommand<C>> extends Provider<C> {
		boolean accept(String command);
	}
	
	protected InputStream in;
	protected OutputStream out;
	protected OutputStream err;
	protected ExitCallback callback;
	protected final Context ctx;
	protected Environment env;

	public AbstractCommand(Context ctx) {
		this.ctx = ctx;
	}
	
	public abstract C command(String command) throws IOException;
	
	protected Repository createRepository(String repoPath) throws IOException {
		if (repoPath.charAt(0) == '/') {
			repoPath = repoPath.substring(1);
		}
		
		RepositoryBuilder builder = new RepositoryBuilder();
		builder.setGitDir(new File(ctx.configuration().getRepositoriesHomeDir(), repoPath));
		return builder.build();
	}
	
	private boolean logStreams = false;
	
	@Override
	public void setInputStream(InputStream in) {
		if (logStreams && !(in instanceof LoggerInputStream)) {
			this.in = new LoggerInputStream(in);
		} else {
			this.in = in;
		}
	}

	@Override
	public void setOutputStream(OutputStream out) {
		if (logStreams && !(out instanceof LoggerOutputStream)) {
			this.out = new LoggerOutputStream(out);
		} else {
			this.out = out;
		}
	}

	@Override
	public void setErrorStream(OutputStream err) {
		if (logStreams && !(err instanceof LoggerOutputStream)) {
			this.err = new LoggerOutputStream(err);
		} else {
			this.err = err;
		}
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		this.callback = callback;
	}

	@Override
	public void start(Environment env) throws IOException {
		this.env = env;
		new Thread(this, getName()).start();
	}
	
	protected abstract String getName();
	
	public static interface EndCallback<C> {
		void onEnd(C command);
	}
	
	private Set<EndCallback<C>> callbacks = new HashSet<EndCallback<C>>();
	
	public void addEndCallback(EndCallback<C> callback) {
		callbacks.add(callback);
	}

	@Override
	public void destroy() {
		this.out = null;
		this.in = null;
		this.err = null;
		this.callback = null;
		if (this.callbacks != null && this.callbacks.size()>0) {
			for(EndCallback<C> endCallback : this.callbacks) {
				endCallback.onEnd(getType().cast(this));
			}
		}
		this.callbacks = null;
	}
	
	protected abstract Class<C> getType();

}
