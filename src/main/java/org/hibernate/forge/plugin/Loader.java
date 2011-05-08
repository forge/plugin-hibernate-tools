package org.hibernate.forge.plugin;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

public class Loader implements Extension {

	public void observes(@Observes BeforeBeanDiscovery event, BeanManager manager) {
		event.addAnnotatedType(manager.createAnnotatedType(GenerateEntities.class));
	}
}
