package org.kercoin.magrit.services;

import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyBuildQueueService implements BuildQueueService {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	@Override
	public void enqueueBuild(Repository repository, String sha1) {
		log.info("New build on {} @ {}", repository.getDirectory(), sha1);
	}

}
