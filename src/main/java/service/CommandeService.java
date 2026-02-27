package service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import bo.Client;
import bo.Commande;
import dao.CommandeDAO;
import dto.ClientDTO;
import dto.CommandeDTO;
import dto.Ligne_CommandeDTO;

public class CommandeService {
	private static CommandeDTO cmd;
	public static CommandeDTO createCommandDTO() {
		if (cmd==null) cmd=new CommandeDTO();
		return cmd;
		
	}
	public CommandeDTO getCommandeDTO() {
		
		return cmd;
		
	}
	public void addLigne(Ligne_CommandeDTO ligne) {
		
		
		cmd.getLignes().add(ligne);
		float total=0;
		for(Ligne_CommandeDTO l:cmd.getLignes()) {
			total+=l.getSous_total();
		}
		cmd.setTotal(total);
			
		
	}
	public void addClient(ClientDTO dto) {
		
		cmd.setClient(dto);
		
		
	}
	public void save(CommandeDTO commandedto) {
		new CommandeDAO().create(this.toCommande(commandedto));
		
	}
	public Commande toCommande(CommandeDTO commandedto) {
		Commande commande=new Commande();
		commande.setDatecmd(commandedto.getDatecmd());
		
		commande.setClient(new ClientService().toClient(commandedto.getClient()));
		
		commande.setLignes(commandedto.getLignes().stream().map(ldto->new Ligne_CommandeService().toLigne(ldto)).collect(Collectors.toList()));
		commande.setTotal(commandedto.getTotal());
		return commande;
	}
	public void initialiserCommande() {
		cmd=null;
		createCommandDTO();
		
	}

	/**
	 * Get all commands as DTOs.
	 */
	public List<CommandeDTO> getAllCommandes() {
		CommandeDAO dao = new CommandeDAO();
		List<Commande> commandes = dao.getAllCommandes();
		if (commandes == null) {
			commandes = List.of();
		}
		return commandes.stream()
			.map(this::fromCommandeEntity)
			.collect(Collectors.toList());
	}

	/**
	 * Search commands by client name.
	 */
	public List<CommandeDTO> searchCommandes(String searchTerm) {
		if (searchTerm == null || searchTerm.trim().isEmpty()) {
			return getAllCommandes();
		}
		CommandeDAO dao = new CommandeDAO();
		List<Commande> commandes = dao.search(searchTerm);
		if (commandes == null) {
			commandes = List.of();
		}
		return commandes.stream()
			.map(this::fromCommandeEntity)
			.collect(Collectors.toList());
	}

	/**
	 * Get command by ID.
	 */
	public CommandeDTO getCommandeDTO(int id) {
		CommandeDAO dao = new CommandeDAO();
		Commande commande = dao.findById(id);
		if (commande != null) {
			return this.fromCommandeEntity(commande);
		}
		return null;
	}

	/**
	 * Delete command.
	 */
	public boolean delete(int id) {
		CommandeDAO dao = new CommandeDAO();
		Commande commande = dao.findById(id);
		if (commande != null) {
			return dao.delete(commande);
		}
		return false;
	}

	/**
	 * Convert Commande entity to DTO (new method to avoid confusion with existing toCommande).
	 */
	private CommandeDTO fromCommandeEntity(Commande commande) {
		CommandeDTO dto = new CommandeDTO();
		dto.setIdcmd(commande.getIdcmd());
		dto.setDatecmd(commande.getDatecmd());
		
		if (commande.getClient() != null) {
			ClientDTO clientDTO = new ClientDTO();
			clientDTO.setId(commande.getClient().getId());
			clientDTO.setNom(commande.getClient().getNom());
			clientDTO.setCapital(commande.getClient().getCapital());
			clientDTO.setAdresse(commande.getClient().getAdresse());
			dto.setClient(clientDTO);
		}
		
		dto.setTotal(commande.getTotal());
		return dto;
	}

}
