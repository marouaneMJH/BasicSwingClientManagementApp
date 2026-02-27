package service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import bo.Client;
import dao.ClientDAO;
import dto.ClientDTO;
import exception.ClientNotFoundException;

public class ClientService implements ClientServiceInterface {

	@Override
	public List<ClientDTO> retreive() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get all clients as DTOs.
	 */
	public List<ClientDTO> getAllClients() {
		ClientDAO dao = new ClientDAO();
		List<Client> clients = dao.getAllClients();
		return clients.stream()
			.map(this::fromClient)
			.collect(Collectors.toList());
	}

	/**
	 * Search clients by name or address.
	 */
	public List<ClientDTO> searchClients(String searchTerm) {
		if (searchTerm == null || searchTerm.trim().isEmpty()) {
			return getAllClients();
		}
		ClientDAO dao = new ClientDAO();
		List<Client> clients = dao.search(searchTerm);
		return clients.stream()
			.map(this::fromClient)
			.collect(Collectors.toList());
	}

	@Override
	public void create(ClientDTO clientdto) {
		ClientDAO dao=new ClientDAO();
		dao.create(this.toClient(clientdto));
		
	}

	@Override
	public void update(ClientDTO clientdto, int id) {
		ClientDAO dao=new ClientDAO();

		Optional<Client> client=Optional.ofNullable(dao.findById(id));
		if (client.isPresent())
		 {
			Client C=this.toClient(clientdto);
			C.setId(id);
			dao.update(C);
		}
		
	}

	@Override
	public boolean delete(int id) {
		ClientDAO dao=new ClientDAO();

		
		
		 	return dao.delete(dao.findById(id));
		
		
		
	}

	@Override
	public ClientDTO getClientDTO(int id) {
		
		ClientDAO dao=new ClientDAO();
		Optional<Client> client=Optional.ofNullable(dao.findById(id));
		if (client.isPresent()) return this.fromClient(client.get());
		else 
			throw new ClientNotFoundException("Client introuvable! Il faut choisir un autre Id.");
	}
public Client toClient(ClientDTO clientdto) {
	Client client=new Client();
	client.setId(clientdto.getId());
	client.setNom(clientdto.getNom());
	client.setCapital(clientdto.getCapital());
	client.setAdresse(clientdto.getAdresse());
	return client;
}
public ClientDTO fromClient(Client client) {
	ClientDTO clientdto=new ClientDTO();
	clientdto.setId(client.getId());
	clientdto.setNom(client.getNom());
	clientdto.setCapital(client.getCapital());
	clientdto.setAdresse(client.getAdresse());
	
	return clientdto;
	
	
}


}
