package controller;

import bo.Produit;
import java.util.List;

import dao.ProduitDAO;
import dto.ProduitDTO;
import service.ProduitService;

public class ProduitController {
    
    public void ajouterProduit(ProduitDTO produitDTO) {
        ProduitService produitservice = new ProduitService();
        produitservice.create(produitDTO);
    }

    public List<ProduitDTO> getAllProduit() {
        ProduitService produitservice = new ProduitService();
        return produitservice.retreive();
    }

    public List<ProduitDTO> getAllProduits() {
        ProduitService service = new ProduitService();
        return service.getAllProduits();
    }

    public List<ProduitDTO> searchProduits(String searchTerm) {
        ProduitService service = new ProduitService();
        return service.searchProduits(searchTerm);
    }

    public void decrease_stock() {
        ProduitService produitservice = new ProduitService();
        produitservice.decrease_stock();
    }

    public void saveProduit(Produit produit) {
        ProduitDAO dao = new ProduitDAO();
        if (produit.getId() == 0 || produit.getId() < 1) {
            // Create new product
            dao.create(produit);
        } else {
            // Update existing product
            dao.update(produit);
        }
    }

    public boolean deleteProduit(int id) {
        ProduitService produitService = new ProduitService();
        return produitService.delete(id);
    }

    public ProduitDTO getProduitDTO(int id) {
        ProduitService produitService = new ProduitService();
        return produitService.getProduitDTO(id);
    }
}
