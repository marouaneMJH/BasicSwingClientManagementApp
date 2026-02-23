package service;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import bo.Client;
import bo.Produit;
import dao.ProduitDAO;
import dto.ClientDTO;
import dto.CommandeDTO;
import dto.Ligne_CommandeDTO;
import dto.ProduitDTO;

public class ProduitService implements ProduitServiceInterface{

	@Override
	public List<ProduitDTO> retreive() {
		
		 List<ProduitDTO> produitdtos=new ProduitDAO().retreive().stream().map(p->fromProduit(p)).collect(Collectors.toList());
		 return produitdtos;
	}
	public ProduitDTO fromProduit(Produit produit) {
		ProduitDTO produitdto=new ProduitDTO();
		produitdto.setId(produit.getId());
		produitdto.setQtstock(produit.getQtstock());
		produitdto.setLibelle(produit.getLibelle());
		produitdto.setPrix(produit.getPrix());
		
		return produitdto;
		
		
	}
	public Produit toProduit(ProduitDTO produitdto) {
		Produit produit=new Produit();
		produit.setId(produitdto.getId());
		produit.setQtstock(produitdto.getQtstock());
		produit.setLibelle(produitdto.getLibelle());
		produit.setPrix(produitdto.getPrix());
		
		return produit;
		
		
	}
	public void decrease_stock() {
		CommandeDTO cmd=new CommandeService().getCommandeDTO();
		for(Ligne_CommandeDTO l:cmd.getLignes()) {
			ProduitDTO p=l.getProduit();
			p.setQtstock(p.getQtstock()-l.getQuantite());
			new ProduitDAO().update(this.toProduit(p));
		}
		
	}

	/**
	 * Get all products as DTOs.
	 */
	public List<ProduitDTO> getAllProduits() {
		ProduitDAO dao = new ProduitDAO();
		List<Produit> produits = dao.getAllProduits();
		if (produits == null) {
			produits = List.of();
		}
		return produits.stream()
			.map(this::fromProduit)
			.collect(Collectors.toList());
	}

	/**
	 * Search products by name (libelle).
	 */
	public List<ProduitDTO> searchProduits(String searchTerm) {
		if (searchTerm == null || searchTerm.trim().isEmpty()) {
			return getAllProduits();
		}
		ProduitDAO dao = new ProduitDAO();
		List<Produit> produits = dao.search(searchTerm);
		if (produits == null) {
			produits = List.of();
		}
		return produits.stream()
			.map(this::fromProduit)
			.collect(Collectors.toList());
	}

	/**
	 * Get product by ID.
	 */
	public ProduitDTO getProduitDTO(int id) {
		ProduitDAO dao = new ProduitDAO();
		Produit produit = dao.findById(id);
		if (produit != null) {
			return this.fromProduit(produit);
		}
		return null;
	}

	/**
	 * Delete product.
	 */
	public boolean delete(int id) {
		ProduitDAO dao = new ProduitDAO();
		Produit produit = dao.findById(id);
		if (produit != null) {
			return dao.delete(produit);
		}
		return false;
	}

	/**
	 * Create a new product from DTO.
	 */
	public void create(ProduitDTO produitDTO) {
		ProduitDAO dao = new ProduitDAO();
		Produit produit = toProduit(produitDTO);
		dao.create(produit);
	}
}
