package dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import bo.Client;
import org.hibernate.Transaction;
import java.util.List;

public class ClientDAO {

	
public void create(Client client) {
	Transaction tx=null;
		try {
		Session session=HibernateUtil.getSessionFactory().openSession();
		tx=session.beginTransaction();
		session.save(client);
		tx.commit();
		session.close();
	}
	catch(HibernateException e) {
		tx.rollback();
		e.printStackTrace();
	}
}

public void update(Client client) {
Transaction tx=null;
	try {
	Session session=HibernateUtil.getSessionFactory().openSession();
	tx=session.beginTransaction();
	session.saveOrUpdate(client);
	tx.commit();
	session.close();
}
catch(HibernateException e) {
	tx.rollback();
	e.printStackTrace();
}
}
public boolean delete(Client client) {
Transaction tx=null;
	try {
	Session session=HibernateUtil.getSessionFactory().openSession();
	tx=session.beginTransaction();
	session.delete(client);
	tx.commit();
	session.close();
	return true;
}
catch(HibernateException e) {
	tx.rollback();
	return false;
}
}
public Client findById(int id) {
		Transaction tx=null;
		Client C=null;
			try {
			Session session=HibernateUtil.getSessionFactory().openSession();
			tx=session.beginTransaction();
			C=session.find(Client.class, id);
			tx.commit();
			session.close();
			return C;
			
			
		}
		catch(HibernateException e) {
			tx.rollback();
			e.printStackTrace();
			return C;
		
		}

	}

	/**
	 * Get all clients from database.
	 */
	public List<Client> getAllClients() {
		Transaction tx = null;
		List<Client> clients = null;
		try {
			Session session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();
			Query<Client> query = session.createQuery("FROM Client", Client.class);
			clients = query.list();
			tx.commit();
			session.close();
			return clients;
		} catch (HibernateException e) {
			if (tx != null) tx.rollback();
			e.printStackTrace();
			return List.of();
		}
	}

	/**
	 * Search clients by name containing the search term.
	 */
	public List<Client> searchByName(String searchTerm) {
		Transaction tx = null;
		List<Client> clients = null;
		try {
			Session session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();
			String hql = "FROM Client WHERE LOWER(nom) LIKE LOWER(:searchTerm) ORDER BY nom";
			Query<Client> query = session.createQuery(hql, Client.class);
			query.setParameter("searchTerm", "%" + searchTerm + "%");
			clients = query.list();
			tx.commit();
			session.close();
			return clients;
		} catch (HibernateException e) {
			if (tx != null) tx.rollback();
			e.printStackTrace();
			return List.of();
		}
	}

	/**
	 * Search clients by name or address.
	 */
	public List<Client> search(String searchTerm) {
		Transaction tx = null;
		List<Client> clients = null;
		try {
			Session session = HibernateUtil.getSessionFactory().openSession();
			tx = session.beginTransaction();
			String hql = "FROM Client WHERE LOWER(nom) LIKE LOWER(:term) OR LOWER(adresse) LIKE LOWER(:term) ORDER BY nom";
			Query<Client> query = session.createQuery(hql, Client.class);
			query.setParameter("term", "%" + searchTerm + "%");
			clients = query.list();
			tx.commit();
			session.close();
			return clients;
		} catch (HibernateException e) {
			if (tx != null) tx.rollback();
			e.printStackTrace();
			return List.of();
		}
	}
	
	public static void main(String[] args) {
		System.out.println(new ClientDAO().findById(1));
	}
}
