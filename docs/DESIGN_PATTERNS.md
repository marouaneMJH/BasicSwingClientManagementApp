# Design Patterns & Implementation - Q&A Reference

## 1. Design Patterns Overview

### Q: What design patterns are used in this project?
**A:** We use several GoF (Gang of Four) and architectural patterns:

| Pattern | Location | Purpose |
|---------|----------|---------|
| **DAO Pattern** | dao/ | Encapsulate database access |
| **DTO Pattern** | dto/ | Transfer data between layers |
| **Service Locator** | view/panels | Find appropriate service for operations |
| **Singleton** | HibernateUtil.java | Single SessionFactory instance |
| **Factory** | HibernateUtil, Controllers | Create/initialize objects |
| **MVC** | Overall architecture | Separate concerns of Model/View/Controller |
| **Strategy** | AdvancedSearchPanel | Multiple search strategies per entity type |
| **Observer/Listener** | Swing components | React to user events |
| **Template Method** | Import dialogs | Common import workflow |

---

## 2. DAO Pattern (Data Access Object)

### Q: What is the DAO pattern and how do we implement it?
**A:** DAO pattern abstracts database access from business logic:

**Purpose**: 
- Hide SQL/HQL queries from service layer
- Provide consistent interface for CRUD operations
- Handle Hibernate session management
- Enable easy testing with mock DAOs

**Structure**:
```java
public class ClientDAO {
    // CREATE
    public void create(Client client) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            session.save(client);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
    
    // READ
    public Client findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.get(Client.class, id);
        } finally {
            session.close();
        }
    }
    
    // READ ALL
    public List<Client> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM Client", Client.class).list();
        } finally {
            session.close();
        }
    }
    
    // UPDATE
    public void update(Client client) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            session.update(client);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
    
    // DELETE
    public void delete(Client client) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            session.delete(client);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
    
    // SEARCH/QUERY
    public List<Client> searchByName(String name) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            String hql = "FROM Client WHERE name LIKE :name";
            return session.createQuery(hql, Client.class)
                .setParameter("name", "%" + name + "%")
                .list();
        } finally {
            session.close();
        }
    }
}
```

### Q: How does DAO pattern benefit our system?
**A:**
```
Without DAO Pattern:
View → Service → [HQL queries scattered everywhere]

With DAO Pattern:
View → Service → DAO → [Centralized database queries]

Benefits:
1. Service layer remains clean (no SQL/HQL)
2. Multiple services can share same DAO
3. Easy to mock/test service without real database
4. Query changes localized to DAO
5. Testable: Can test Service with MockDAO
```

---

## 3. DTO Pattern (Data Transfer Object)

### Q: How does DTO pattern work in our system?
**A:** DTOs are lightweight value objects that transfer data between layers:

**Why Separate Entity and DTO?**

```java
// ENTITY (Persistence Layer) - Represents database record
@Entity
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private BigDecimal capital;
    private String address;
    
    @OneToMany(mappedBy = "client")
    private List<Commande> commandes;  // ← Heavy: hundreds of records
    
    @Transient
    private String calculatedField;    // ← Only for database use
}

// DTO (Data Transfer) - Clean, lightweight
public class ClientDTO {
    private Long id;
    private String name;
    private BigDecimal capital;
    private String address;
    // NO commandes list
    // NO transient fields
}
```

**Conversion Pattern**:
```java
// Entity → DTO (for sending to View)
public ClientDTO entityToDTO(Client entity) {
    return new ClientDTO(
        entity.getId(),
        entity.getName(),
        entity.getCapital(),
        entity.getAddress()
    );
}

// DTO → Entity (for saving to database)
public Client dtoToEntity(ClientDTO dto) {
    Client client = new Client();
    client.setId(dto.getId());
    client.setName(dto.getName());
    client.setCapital(dto.getCapital());
    client.setAddress(dto.getAddress());
    return client;
}
```

### Q: What problems does DTO pattern solve?
**A:**

| Problem | Solution | Example |
|---------|----------|---------|
| **Lazy loading issues** | Only include loaded data in DTO | Don't serialize commandes collection |
| **Security** | Exclude sensitive fields | Don't expose password in DTO |
| **Version mismatch** | DTO independent of Entity | Add new Entity field without breaking View |
| **Performance** | Select only needed fields | Query only name/capital, not commandes |
| **API consistency** | Stable DTO vs changing Entity | Entity refactored without View changes |

---

## 4. Service Layer Pattern

### Q: How does the Service layer implement business logic?
**A:** Service layer acts as orchestrator and enforcer of business rules:

**Example: Order Creation Service**
```java
public class CommandeService {
    private CommandeDAO commandeDAO;
    private LigneCommandeDAO ligneDAO;
    private ProduitDAO produitDAO;
    private CommandeDTOConverter converter;
    
    public void create(CommandeDTO dto) {
        // 1. VALIDATION - Business rules
        if (dto.getClient() == null) {
            throw new IllegalArgumentException("Client required");
        }
        if (dto.getLineItems() == null || dto.getLineItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have line items");
        }
        if (dto.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total must be greater than 0");
        }
        
        // 2. ORCHESTRATION - Multiple DAOs
        Commande commande = converter.toEntity(dto);
        Long commandeId = commandeDAO.create(commande);
        
        // 3. SIDE EFFECTS - Stock management
        for (LigneCommandeDTO lineDTO : dto.getLineItems()) {
            Produit produit = produitDAO.findById(lineDTO.getProduitId());
            
            // 3a. Validate stock available
            if (produit.getQtstock() < lineDTO.getQuantite()) {
                throw new InsufficientStockException(
                    "Product " + produit.getNom() + " has only " + 
                    produit.getQtstock() + " available"
                );
            }
            
            // 3b. Create line item
            LigneCommande ligne = new LigneCommande();
            ligne.setCommande(commande);
            ligne.setProduit(produit);
            ligne.setQuantite(lineDTO.getQuantite());
            ligneDAO.create(ligne);
            
            // 3c. Update inventory
            produit.setQtstock(produit.getQtstock() - lineDTO.getQuantite());
            produitDAO.update(produit);
        }
        
        // 4. RETURN - Success
        return commandeId;
    }
}
```

### Q: What responsibilities does Service layer have?
**A:**

```
❌ Service does NOT:
- Execute SQL/HQL queries (that's DAO)
- Manage Hibernate sessions (that's DAO/HibernateUtil)
- Format data for UI (that's Controller/View)
- Handle HTTP requests (that's web layer, not applicable here)

✅ Service DOES:
- Validate business rules
- Coordinate multiple DAOs
- Perform calculations
- Enforce constraints
- Manage complex workflows
- Ensure data consistency
- Handle transactions (delegate to DAO)
```

---

## 5. Controller - Adapter Pattern

### Q: How do Controllers act as adapters?
**A:** Controllers translate between View (UI) and Service (business logic):

```java
public class ClientController {
    private ClientService service = new ClientService();
    
    // View → Controller (data comes in as DTO or raw values)
    public void ajouterClient(ClientDTO dto) {
        try {
            service.create(dto);
            // Success - return to View
            return true;
        } catch (Exception e) {
            // Failure - translate to user-friendly message
            throw new RuntimeException("Failed to add client: " + e.getMessage());
        }
    }
    
    // Service → View (data goes out formatted for UI)
    public List<ClientDTO> listerClients() {
        List<ClientDTO> clients = service.findAll();
        // Could apply View-specific transformations here
        return clients;
    }
    
    // Error handling translation
    public ClientDTO rechercherClient(Long id) {
        try {
            return service.findById(id);
        } catch (HibernateException e) {
            throw new RuntimeException("Database error retrieving client");
        } catch (Exception e) {
            throw new RuntimeException("Error searching for client");
        }
    }
}
```

**Controller Responsibilities**:
1. **Input validation**: Check basic constraints
2. **Service invocation**: Call appropriate service methods
3. **Error translation**: Convert technical errors to user messages
4. **Data formatting**: Prepare data for View display
5. **Request routing**: Direct requests to correct service

---

## 6. Strategy Pattern - Advanced Search

### Q: How does AdvancedSearchPanel implement Strategy pattern?
**A:** Different search strategies for different entity types:

```java
public class AdvancedSearchPanel extends JPanel {
    public enum SearchMode {
        CLIENTS,      // Search: Name, Capital range, Address
        PRODUCTS,     // Search: Name, Price range, Stock range
        ORDERS        // Search: Date range, Client
    }
    
    private SearchMode currentMode;
    
    // Strategy 1: Client search
    public void setModeClients() {
        currentMode = SearchMode.CLIENTS;
        // Show: nameField, minCapitalField, maxCapitalField, addressField
        // Hide: priceFields, stockFields, dateFields
    }
    
    // Strategy 2: Product search
    public void setModeProducts() {
        currentMode = SearchMode.PRODUCTS;
        // Show: nameField, minPriceField, maxPriceField, minStockField, maxStockField
        // Hide: capitalFields, addressField, dateFields
    }
    
    // Strategy 3: Order search
    public void setModeOrders() {
        currentMode = SearchMode.ORDERS;
        // Show: dateFromField, dateToField, clientField
        // Hide: capitalFields, priceFields, stockFields
    }
    
    // Unified search interface
    public Map<String, Object> getFilters() {
        Map<String, Object> filters = new HashMap<>();
        
        switch (currentMode) {
            case CLIENTS:
                filters.put("name", nameField.getText());
                filters.put("minCapital", minCapitalField.getValue());
                filters.put("maxCapital", maxCapitalField.getValue());
                break;
            case PRODUCTS:
                filters.put("name", nameField.getText());
                filters.put("minPrice", minPriceField.getValue());
                filters.put("maxPrice", maxPriceField.getValue());
                break;
            case ORDERS:
                filters.put("dateFrom", dateFromField.getDate());
                filters.put("dateTo", dateToField.getDate());
                break;
        }
        
        return filters;
    }
}
```

**Strategy Pattern Benefit**:
- Same UI component, different filtering logic per entity
- Easy to add new search strategies
- Encapsulates strategy-specific code

---

## 7. Factory Pattern - HibernateUtil

### Q: How does HibernateUtil implement Factory and Singleton?
**A:**

```java
public class HibernateUtil {
    // SINGLETON: Single instance
    private static SessionFactory sessionFactory = null;
    
    // FACTORY: Create SessionFactory
    static {
        try {
            Configuration config = new Configuration();
            config.configure("dao/hibernate.cfg.xml");
            // Auto-detect @Entity classes
            config.addAnnotatedClass(Client.class);
            config.addAnnotatedClass(Commande.class);
            config.addAnnotatedClass(Produit.class);
            config.addAnnotatedClass(Ligne_Commande.class);
            
            sessionFactory = config.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    // ACCESS POINT: Get SessionFactory instance
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    // FACTORY METHOD: Create new session
    public static Session getNewSession() {
        return sessionFactory.openSession();
    }
    
    // CLEANUP: Shutdown
    public static void shutdown() {
        sessionFactory.close();
    }
}
```

**Combined Patterns**:
1. **Singleton** - Only one SessionFactory per JVM
2. **Factory** - Produces Session objects on demand
3. **Lazy Initialization** - SessionFactory created on first access
4. **Resource Management** - Centralized connection pooling

---

## 8. Observer/Listener Pattern - UI Events

### Q: How does the application handle user interactions?
**A:** Using Swing's built-in Observer pattern:

```java
public class ClientPanel extends JPanel {
    private JButton addButton, deleteButton, refreshButton, importButton;
    private ClientController controller;
    
    private void initializeListeners() {
        // OBSERVER PATTERN: Button → Event Listener → Handler
        
        // Add button listener
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show form dialog
                ClientFormDialog dialog = new ClientFormDialog(null);
                if (dialog.showDialog()) {
                    ClientDTO newClient = dialog.getClientData();
                    controller.ajouterClient(newClient);
                    refresh();
                }
            }
        });
        
        // Delete button listener
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Long clientId = (Long) tableModel.getValueAt(selectedRow, 0);
                controller.supprimerClient(clientId);
                refresh();
            }
        });
        
        // Table selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() >= 0;
            deleteButton.setEnabled(hasSelection);
            editButton.setEnabled(hasSelection);
        });
    }
}
```

**Event Flow**:
```
User clicks button
        ↓
ActionEvent fires
        ↓
Listener's actionPerformed() called
        ↓
Controller method invoked
        ↓
Business logic executed
        ↓
UI updated
```

---

## 9. Template Method Pattern - Import Dialogs

### Q: How do import dialogs follow Template Method pattern?
**A:** Common structure, entity-specific details:

```java
public class CsvImportDialog extends JDialog {
    public enum ImportType {
        CLIENTS, PRODUCTS, ORDERS
    }
    
    private ImportType importType;
    
    // TEMPLATE METHOD: Common workflow
    public void validateAndImport() {
        if (selectedFile == null) {
            showError("Please select a file");
            return;
        }
        
        // 1. Load preview (common)
        loadPreview();
        
        // 2. Validate (specific to entity type)
        ImportResult<?> result = validateImportData();
        
        if (result.hasErrors()) {
            // 3a. Display errors and allow partial import (specific)
            displayValidationErrors(result.getErrors());
        } else {
            // 3b. Import all records (specific)
            importRecords(result.getSuccessfulRecords());
            showSuccess("Imported " + result.getSuccessfulRecords().size() + " records");
        }
    }
    
    private ImportResult<?> validateImportData() {
        // Query-specific validation
        switch (importType) {
            case CLIENTS:
                return ImportUtil.importClientsFromCSV(selectedFile);
            case PRODUCTS:
                return ImportUtil.importProductsFromCSV(selectedFile);
            case ORDERS:
                return ImportUtil.importOrdersFromCSV(selectedFile);
        }
    }
    
    private void importRecords(List<?> records) {
        // Entity-specific save logic
        switch (importType) {
            case CLIENTS:
                for (ClientDTO client : (List<ClientDTO>) records) {
                    controller.ajouterClient(client);
                }
                break;
            case PRODUCTS:
                for (ProduitDTO product : (List<ProduitDTO>) records) {
                    controller.ajouterProduit(product);
                }
                break;
            case ORDERS:
                for (CommandeDTO order : (List<CommandeDTO>) records) {
                    controller.ajouterCommande(order);
                }
                break;
        }
    }
}
```

**Template Method Structure**:
```
validateAndImport() {       ← Template method (fixed)
    loadPreview()           ← Common step
    switch(type) {          ← Specific step
        CLIENTS: ...
        PRODUCTS: ...
        ORDERS: ...
    }
    displayResult()         ← Common step
}
```

---

## 10. Currency and Number Formatting

### Q: How do we handle number and currency formatting?
**A:** Using Java's NumberFormat for internationalization:

```java
// GLOBAL FORMATTING
private static final NumberFormat currencyFormat = 
    NumberFormat.getCurrencyInstance(Locale.US);  // MAD
private static final NumberFormat percentFormat = 
    NumberFormat.getPercentInstance();             // %
private static final DecimalFormat decimalFormat = 
    new DecimalFormat("0.00");                     // 2 decimal places

// USAGE IN DASHBOARD
String formattedRevenue = currencyFormat.format(totalRevenue);
    // Input: 45678.50
    // Output: "MAD45,678.50"

String formattedPercent = percentFormat.format(highValuePercent);
    // Input: 0.25
    // Output: "25%"

String formattedPrice = decimalFormat.format(productPrice);
    // Input: 99.5
    // Output: "99.50"

// CONFIG: Easy to change locale
Locale locale = Locale.FRANCE;  // → "45 678,50 €"
Locale locale = Locale.GERMANY; // → "45.678,50 €"
```

---

## 11. Exception Handling Strategy

### Q: How do we model custom exceptions?
**A:** Specific exception types for different error scenarios:

```java
// BASE CUSTOM EXCEPTION
public class ApplicationException extends Exception {
    public ApplicationException(String message) {
        super(message);
    }
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}

// SPECIFIC EXCEPTIONS
public class ClientNotFoundException extends ApplicationException {
    public ClientNotFoundException(Long clientId) {
        super("Client with ID " + clientId + " not found");
    }
}

public class InsufficientStockException extends ApplicationException {
    public InsufficientStockException(String productName, int required, int available) {
        super("Product '" + productName + "' requires " + required + 
              " units but only " + available + " available");
    }
}

public class DataValidationException extends ApplicationException {
    public DataValidationException(String field, String message) {
        super("Validation error in field '" + field + "': " + message);
    }
}

// USAGE IN SERVICE
public class CommandeService {
    public CommandeDTO findById(Long id) {
        Commande commande = commandeDAO.findById(id);
        if (commande == null) {
            throw new ClientNotFoundException(id);  // ← Specific exception
        }
        return converter.toDTO(commande);
    }
}

// USAGE IN VIEW
try {
    commandeDTO = controller.rechercherCommande(selectedId);
} catch (ClientNotFoundException e) {
    JOptionPane.showMessageDialog(this, 
        "Order not found", "Error", JOptionPane.ERROR_MESSAGE);
} catch (ApplicationException e) {
    JOptionPane.showMessageDialog(this, 
        e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
}
```

---

## Summary: Design Pattern Philosophy

Our design patterns create:

1. **Clean Boundaries**: Each pattern creates clear separation
2. **Reusability**: Patterns enable code reuse across modules
3. **Testability**: Patterns like DAO, Service make mocking easy
4. **Maintainability**: Standard patterns = predictable code
5. **Flexibility**: Strategy pattern supports multiple implementations
6. **Scalability**: Patterns scale to new features without major refactoring
7. **Consistency**: Same patterns used throughout application
8. **Readability**: Developers recognize standard patterns immediately
