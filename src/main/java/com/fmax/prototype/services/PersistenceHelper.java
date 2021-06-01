package com.fmax.prototype.services;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
@Transactional
public class PersistenceHelper {
	private @PersistenceContext EntityManager em;
	
	public void persist(Object object) {
		em.persist(object);
	}
}
