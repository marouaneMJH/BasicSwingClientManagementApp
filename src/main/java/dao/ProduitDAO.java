package dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import bo.Produit;

public class ProduitDAO {
public List<Produit> retreive(){
	Transaction tx=null;
List<Produit> liste=new ArrayList<Produit>();
		try {
		Session session=HibernateUtil.getSessionFactory().openSession();
		tx=session.beginTransaction();
		liste=session.createQuery("From Produit P where P.qtstock > 0").list();
		tx.commit();
		session.close();
		return liste;
		
		
	}
	catch(HibernateException e) {
		tx.rollback();
		e.printStackTrace();
		return liste;
	
	}

	
}
public void update(Produit produit){
	Transaction tx=null;

		try {
		Session session=HibernateUtil.getSessionFactory().openSession();
		tx=session.beginTransaction();
		session.update(produit);
		
		tx.commit();
		session.close();
		System.out.println("====================Produit modifié"+produit.getId() );
		
		
		
	}
	catch(HibernateException e) {
		tx.rollback();
		e.printStackTrace();
		
	
	}

	
}

public void create(Produit produit) {
	Transaction tx = null;
	try {
		Session session = HibernateUtil.getSessionFactory().openSession();
		tx = session.beginTransaction();
		session.persist(produit);
		tx.commit();
		session.close();
		System.out.println("====================Produit créé: " + produit.getId());
	} catch (HibernateException e) {
		if (tx != null) tx.rollback();
		e.printStackTrace();
	}
}

/**
 * Get all products from database.
 */
public List<Produit> getAllProduits() {
	Transaction tx = null;
	List<Produit> produits = null;
	try {
		Session session = HibernateUtil.getSessionFactory().openSession();
		tx = session.beginTransaction();
		Query<Produit> query = session.createQuery("FROM Produit ORDER BY libelle", Produit.class);
		produits = query.list();
		tx.commit();
		session.close();
		return produits;
	} catch (HibernateException e) {
		if (tx != null) tx.rollback();
		e.printStackTrace();
		return List.of();
	}
}

/**
 * Search products by name or libelle.
 */
public List<Produit> search(String searchTerm) {
	Transaction tx = null;
	List<Produit> produits = null;
	try {
		Session session = HibernateUtil.getSessionFactory().openSession();
		tx = session.beginTransaction();
		String hql = "FROM Produit WHERE LOWER(libelle) LIKE LOWER(:term) ORDER BY libelle";
		Query<Produit> query = session.createQuery(hql, Produit.class);
		query.setParameter("term", "%" + searchTerm + "%");
		produits = query.list();
		tx.commit();
		session.close();
		return produits;
	} catch (HibernateException e) {
		if (tx != null) tx.rollback();
		e.printStackTrace();
		return List.of();
	}
}

/**
 * Find product by ID.
 */
public Produit findById(int id) {
	Transaction tx = null;
	Produit produit = null;
	try {
		Session session = HibernateUtil.getSessionFactory().openSession();
		tx = session.beginTransaction();
		produit = session.find(Produit.class, id);
		tx.commit();
		session.close();
		return produit;
	} catch (HibernateException e) {
		if (tx != null) tx.rollback();
		e.printStackTrace();
		return null;
	}
}

/**
 * Delete product.
 */
public boolean delete(Produit produit) {
	Transaction tx = null;
	try {
		Session session = HibernateUtil.getSessionFactory().openSession();
		tx = session.beginTransaction();
		session.remove(produit);
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
