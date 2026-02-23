package controller;

import bo.Commande;
import java.util.Date;
import java.util.List;

import dao.CommandeDAO;
import dto.ClientDTO;
import dto.CommandeDTO;
import dto.Ligne_CommandeDTO;
import service.CommandeService;

public class CommandeController {

    public void ajouterCommande(CommandeDTO commandeDTO) {
        new CommandeService().save(commandeDTO);
    }

    public CommandeDTO createCommande() {
        return CommandeService.createCommandDTO();
    }

    public CommandeDTO getCommande() {
        return new CommandeService().getCommandeDTO();
    }

    public void ajouterLigne(Ligne_CommandeDTO ligne) {
        new CommandeService().addLigne(ligne);
    }

    public void associerClient(ClientDTO dto) {
        new CommandeService().addClient(dto);
    }

    public void saveCommande(CommandeDTO commande) {
        new CommandeService().save(commande);
    }

    public void initialiserCommande() {
        new CommandeService().initialiserCommande();
    }

    public List<CommandeDTO> getAllCommandes() {
        CommandeService service = new CommandeService();
        return service.getAllCommandes();
    }

    public List<CommandeDTO> searchCommandes(String searchTerm) {
        CommandeService service = new CommandeService();
        return service.searchCommandes(searchTerm);
    }

    public void saveCommandeEntity(Commande commande) {
        CommandeDAO dao = new CommandeDAO();
        if (commande.getIdcmd() == 0 || commande.getIdcmd() < 1) {
            // Create new order
            dao.create(commande);
        } else {
            // Update existing order
            dao.update(commande);
        }
    }

    public boolean deleteCommande(int id) {
        CommandeService service = new CommandeService();
        return service.delete(id);
    }

    public CommandeDTO getCommandeDTO(int id) {
        CommandeService service = new CommandeService();
        return service.getCommandeDTO(id);
    }
}
