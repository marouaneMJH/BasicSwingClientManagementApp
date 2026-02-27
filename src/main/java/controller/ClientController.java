package controller;

import bo.Client;
import dto.ClientDTO;
import service.ClientService;
import java.util.List;

public class ClientController {
    
    public void ajouterClient(ClientDTO clientdto) {
        ClientService clientservice = new ClientService();
        clientservice.create(clientdto);
    }

    public void modifierClient(ClientDTO clientdto, int id) {
        ClientService clientservice = new ClientService();
        clientservice.update(clientdto, id);
    }

    public boolean supprimerClient(int id) {
        ClientService clientservice = new ClientService();
        return clientservice.delete(id);
    }

    public ClientDTO getClientDTO(int id) {
        ClientService clientservice = new ClientService();
        return clientservice.getClientDTO(id);
    }

    public List<ClientDTO> getAllClients() {
        ClientService clientservice = new ClientService();
        return clientservice.getAllClients();
    }

    public List<ClientDTO> searchClients(String searchTerm) {
        ClientService clientservice = new ClientService();
        return clientservice.searchClients(searchTerm);
    }

    public void saveClient(Client client) {
        ClientService clientservice = new ClientService();
        if (client.getId() <= 0) {
            // Create new client
            ClientDTO dto = new ClientDTO();
            dto.setNom(client.getNom());
            dto.setCapital(client.getCapital());
            dto.setAdresse(client.getAdresse());
            clientservice.create(dto);
        } else {
            // Update existing client
            ClientDTO dto = new ClientDTO();
            dto.setId(client.getId());
            dto.setNom(client.getNom());
            dto.setCapital(client.getCapital());
            dto.setAdresse(client.getAdresse());
            clientservice.update(dto, client.getId());
        }
    }

    public boolean deleteClient(int id) {
        ClientService clientservice = new ClientService();
        return clientservice.delete(id);
    }
}
