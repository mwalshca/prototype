package com.fmax.prototype.services;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class PersistenceHelper {
	private @PersistenceContext EntityManager em;
	
	public synchronized void persist(Object object) {
		em.persist(object);
	}	
}
