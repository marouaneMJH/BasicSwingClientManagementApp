package dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import bo.Commande;
import bo.Ligne_Commande;
import java.util.List;

public class CommandeDAO {
public void create(Commande commande) {
	Transaction tx=null;
	try {
	Session session=HibernateUtil.getSessionFactory().openSession();
	tx=session.beginTransaction();
	session.save(commande);
	for(Ligne_Commande l:commande.getLignes()) {
		l.setCommande(commande);
		session.saveOrUpdate(l);
		
	}
	
	tx.commit();
	session.close();
}
catch(HibernateException e) {
	tx.rollback();
	e.printStackTrace();
}
	
}

/**
 * Get all commands from database.
 */
public List<Commande> getAllCommandes() {
	Transaction tx = null;
	List<Commande> commandes = null;
	try {
		Session session = HibernateUtil.getSessionFactory().openSession();
		tx = session.beginTransaction();
		Query<Commande> query = session.createQuery("FROM Commande ORDER BY datecmd DESC", Commande.class);
		commandes = query.list();
		tx.commit();
		session.close();
		return commandes;
	} catch (HibernateException e) {
		if (tx != null) tx.rollback();
		e.printStackTrace();
		return List.of();
	}
}

/**
 * Search commands by client name.
 */
public List<Commande> search(String searchTerm) {
	Transaction tx = null;
	List<Commande> commandes = null;
	try {
		Session session = HibernateUtil.getSessionFactory().openSession();
		tx = session.beginTransaction();
		String hql = "FROM Commande c WHERE LOWER(c.client.nom) LIKE LOWER(:term) ORDER BY c.datecmd DESC";
		Query<Commande> query = session.createQuery(hql, Commande.class);
		query.setParameter("term", "%" + searchTerm + "%");
		commandes = query.list();
		tx.commit();
		session.close();
		return commandes;
	} catch (HibernateException e) {
		if (tx != null) tx.rollback();
		e.printStackTrace();
		return List.of();
	}
}

/**
 * Find command by ID.
 */
public Commande findById(int id) {
	Transaction tx = null;
	Commande commande = null;
	try {
		Session session = HibernateUtil.getSessionFactory().openSession();
		tx = session.beginTransaction();
		commande = session.find(Commande.class, id);
		tx.commit();
		session.close();
		return commande;
	} catch (HibernateException e) {
		if (tx != null) tx.rollback();
		e.printStackTrace();
		return null;
	}
}

/**
 * Update command.
 */
public void update(Commande commande) {
	Transaction tx = null;
	try {
		Session session = HibernateUtil.getSessionFactory().openSession();
		tx = session.beginTransaction();
		session.merge(commande);
		tx.commit();
		session.close();
	} catch (HibernateException e) {
		if (tx != null) tx.rollback();
		e.printStackTrace();
	}
}

/**
 * Delete command.
 */
public boolean delete(Commande commande) {
	Transaction tx = null;
	try {
		Session session = HibernateUtil.getSessionFactory().openSession();
		tx = session.beginTransaction();
		session.remove(commande);
		tx.commit();
		session.close();
		return true;
	} catch (HibernateException e) {
		if (tx != null) tx.rollback();
		e.printStackTrace();
		return false;
	}
}
}
