package com.j256.testcheckpublisher.plugin.frameworks;

import org.apache.maven.plugin.logging.Log;

/**
 * Types of generators that we support.
 */
public enum FrameworkCheckGeneratorFactory {
	SUREFIRE(SurefireFrameworkCheckGenerator.class),
	// end
	;

	private final Class<? extends FrameworkCheckGenerator> generatorClass;

	private FrameworkCheckGeneratorFactory(Class<? extends FrameworkCheckGenerator> generatorClass) {
		this.generatorClass = generatorClass;
	}

	public FrameworkCheckGenerator create(Log log) {
		try {
			return generatorClass.newInstance();
		} catch (Exception e) {
			log.error("Could not create instance of: " + generatorClass, e);
			return new SurefireFrameworkCheckGenerator();
		}
	}
}
