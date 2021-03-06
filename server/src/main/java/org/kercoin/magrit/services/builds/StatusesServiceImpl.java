package org.kercoin.magrit.services.builds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kercoin.magrit.services.builds.Pipeline.Key;
import org.kercoin.magrit.services.builds.Pipeline.Task;
import org.kercoin.magrit.services.dao.BuildDAO;
import org.kercoin.magrit.utils.GitUtils;
import org.kercoin.magrit.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Thead-safe
 * @author ptitfred
 *
 */
public class StatusesServiceImpl implements StatusesService {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final GitUtils gitUtils;
	private final BuildDAO dao;
	private final Pipeline pipeline;

	@Inject
	public StatusesServiceImpl(GitUtils gitUtils, BuildDAO dao, Pipeline pipeline) {
		super();
		this.gitUtils = gitUtils;
		this.dao = dao;
		this.pipeline = pipeline;
	}

	@Override
	public List<Status> getStatus(Repository repository, String sha1) {
		if (repository == null) {
			throw new NullPointerException("Repository can't be null");
		}
		if (sha1 == null) {
			throw new NullPointerException("SHA1 can't be null");
		}
		log.info("Checking status for {} @ {}", repository.getDirectory(), sha1);
		boolean running = false;
		boolean pending = false;
		List<Status> statuses = new ArrayList<Status>();
		try {
			RevCommit commit = gitUtils.getCommit(repository, sha1);
			if (commit == null) {
				return Arrays.asList(Status.UNKNOWN);
			}
			for (Key k : pipeline.list(PipelineImpl.running())) {
				Task<BuildResult> task = pipeline.get(k);
				if (task instanceof BuildTask) {
					Pair<Repository, String> target = ((BuildTask) task).getTarget();
					if (sha1.equals(target.getU())) {
						running = true;
						break;
					}
				}
			}
			for(Key k : pipeline.list(PipelineImpl.pending())) {
				Task<BuildResult> task = pipeline.get(k);
				if (task instanceof BuildTask) {
					Pair<Repository, String> target = ((BuildTask) task).getTarget();
					if (sha1.equals(target.getU())) {
						pending = true;
						break;
					}
				}
			}

			List<BuildResult> results = dao.getAll(repository, sha1);
			if (results.size() == 0) {
				if (!running && !pending) {
					statuses.add(Status.NEW);
				}
			} else {
				for (BuildResult result: results) {
					statuses.add(result.getExitCode() == 0 ? Status.OK : Status.ERROR);
				}
			}
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		if (running) {
			statuses.add(Status.RUNNING);
		}
		if (pending) {
			statuses.add(Status.PENDING);
		}
		if (statuses.isEmpty()) {
			statuses.add(Status.UNKNOWN);
		}
		return statuses;
	}

}
