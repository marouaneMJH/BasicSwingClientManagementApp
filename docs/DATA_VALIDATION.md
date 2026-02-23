# Data Validation & Business Logic - Q&A Reference

## 1. Validation Philosophy

### Q: Where should validation occur?
**A:** **Multi-tier validation** strategy:

```
Tier 1: UI Level (Client-side)
├─ Basic input checks (empty fields)
├─ Format checks (email, date format)
└─ Prevent invalid data early

Tier 2: Controller Level
├─ Type validation
├─ Range validation
└─ Cross-field validation

Tier 3: Service Level (CRITICAL)
├─ Business rule validation
├─ Database constraint checks
├─ Complex logic validation

Tier 4: Database Level
├─ Unique constraints
├─ Foreign keys
├─ Data type constraints
```

### Q: What validation should NOT happen in UI?
**A:** Security-critical and business-logic validation must happen in Service layer:

```java
// ❌ WRONG: Only UI validation
public void saveOrder() {
    if (totalField.getValue() > 0) {  // UI check
        service.create(order);        // Service trusts input!
    }
}

// ✅ CORRECT: UI + Service validation
public void saveOrder() {
    if (totalField.getValue() > 0) {  // UI check (UX)
        try {
            service.create(order);    // Service re-validates!
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }
}

// Service layer
public void create(CommandeDTO dto) {
    // RE-VALIDATE: Never trust UI validation
    if (dto.getTotal() == null || dto.getTotal() <= 0) {
        throw new IllegalArgumentException("Invalid total");
    }
    // Proceed with creation
}
```

---

## 2. Entity-Level Validation

### Q: What validation exists at the Entity level?
**A:** Hibernate annotations enforce constraints:

```java
@Entity
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;  // Database constraint: NOT NULL
    
    @Column(nullable = false)
    @Positive  // Hibernate validation: > 0
    private BigDecimal capital;
    
    @Column(length = 200)
    private String address;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Commande> commandes;
}
```

**Validation Annotations**:
```java
@NotNull           // Value must not be null
@Positive          // Number > 0
@PositiveOrZero    // Number >= 0
@Min(value)        // Number >= value
@Max(value)        // Number <= value
@Size(min, max)    // String length or collection size
@Pattern(regex)    // String matches regex
@Email             // Valid email format
@Temporal          // Date/time format
```

---

## 3. Service Layer Validation

### Q: What validation patterns does Service layer use?
**A:**

**Pattern 1: Required Field Validation**
```java
public class ClientService {
    public void create(ClientDTO dto) {
        // Check required fields
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Client name is required");
        }
        if (dto.getCapital() == null) {
            throw new IllegalArgumentException("Capital is required");
        }
        if (dto.getAddress() == null || dto.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Address is required");
        }
    }
}
```

**Pattern 2: Business Rule Validation**
```java
public class ProduitService {
    public void create(ProduitDTO dto) {
        // Business rule: Price must be positive
        if (dto.getPrix().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        
        // Business rule: Stock must be non-negative
        if (dto.getQtstock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        
        // Business rule: Some products require minimum stock
        if (dto.getName().contains("Premium") && dto.getQtstock() < 10) {
            throw new IllegalArgumentException("Premium products must have at least 10 units in stock");
        }
    }
}
```

**Pattern 3: Referential Integrity Validation**
```java
public class CommandeService {
    public void create(CommandeDTO dto) {
        // Validate client exists
        Client client = clientDAO.findById(dto.getClientId());
        if (client == null) {
            throw new ClientNotFoundException(dto.getClientId());
        }
        
        // Validate products exist and validate stock
        for (LigneCommandeDTO ligne : dto.getLineItems()) {
            Produit produit = produitDAO.findById(ligne.getProduitId());
            if (produit == null) {
                throw new IllegalArgumentException(
                    "Product with ID " + ligne.getProduitId() + " not found");
            }
            
            // Validate stock availability
            if (produit.getQtstock() < ligne.getQuantite()) {
                throw new InsufficientStockException(
                    produit.getNom(), ligne.getQuantite(), produit.getQtstock());
            }
        }
    }
}
```

**Pattern 4: Cross-Field Validation**
```java
public class CommandeService {
    public void create(CommandeDTO dto) {
        // Validation: Date must not be in future
        if (dto.getDate().after(new Date())) {
            throw new IllegalArgumentException("Order date cannot be in the future");
        }
        
        // Validation: Total must match sum of line items
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (LigneCommandeDTO ligne : dto.getLineItems()) {
            calculatedTotal = calculatedTotal.add(
                ligne.getPrice().multiply(new BigDecimal(ligne.getQuantite()))
            );
        }
        
        if (!calculatedTotal.equals(dto.getTotal())) {
            throw new IllegalArgumentException(
                "Total mismatch: expected " + calculatedTotal + 
                " but got " + dto.getTotal());
        }
    }
}
```

---

## 4. CSV Import Validation

### Q: How does ImportUtil validate CSV data?
**A:**

```java
public class ImportUtil {
    
    // VALIDATION FOR CLIENTS
    public static ImportResult<ClientDTO> importClientsFromCSV(File file) {
        ImportResult<ClientDTO> result = new ImportResult<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int rowNumber = 0;
            
            // Skip header
            reader.readLine();
            rowNumber++;
            
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                
                try {
                    // Parse CSV line
                    List<String> fields = parseCSVLine(line);
                    
                    if (fields.size() < 3) {
                        result.addError(new ImportError(
                            rowNumber, "Format", "", 
                            "Insufficient fields (expected 3, got " + fields.size() + ")"));
                        continue;
                    }
                    
                    String name = fields.get(0).trim();
                    String capitalStr = fields.get(1).trim();
                    String address = fields.get(2).trim();
                    
                    // VALIDATE: Name required
                    if (name.isEmpty()) {
                        result.addError(new ImportError(
                            rowNumber, "Name", "", "Name is required"));
                        continue;
                    }
                    
                    // VALIDATE: Capital required and numeric
                    if (capitalStr.isEmpty()) {
                        result.addError(new ImportError(
                            rowNumber, "Capital", capitalStr, "Capital is required"));
                        continue;
                    }
                    
                    BigDecimal capital;
                    try {
                        // Handle European format: "92000,00" → "92000.00"
                        capitalStr = capitalStr.replace(",", ".");
                        capital = new BigDecimal(capitalStr);
                    } catch (NumberFormatException e) {
                        result.addError(new ImportError(
                            rowNumber, "Capital", capitalStr, 
                            "Invalid number format"));
                        continue;
                    }
                    
                    // VALIDATE: Capital non-negative
                    if (capital.compareTo(BigDecimal.ZERO) < 0) {
                        result.addError(new ImportError(
                            rowNumber, "Capital", capitalStr, 
                            "Capital cannot be negative"));
                        continue;
                    }
                    
                    // VALIDATE: Address required
                    if (address.isEmpty()) {
                        result.addError(new ImportError(
                            rowNumber, "Address", "", "Address is required"));
                        continue;
                    }
                    
                    // SUCCESS: Create DTO
                    ClientDTO dto = new ClientDTO();
                    dto.setName(name);
                    dto.setCapital(capital);
                    dto.setAddress(address);
                    
                    result.addSuccessfulRecord(dto);
                    
                } catch (Exception e) {
                    result.addError(new ImportError(
                        rowNumber, "General", line, 
                        "Error parsing row: " + e.getMessage()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file: " + e.getMessage());
        }
        
        return result;
    }
    
    // VALIDATION FOR PRODUCTS
    public static ImportResult<ProduitDTO> importProductsFromCSV(File file) {
        ImportResult<ProduitDTO> result = new ImportResult<>();
        
        // Similar pattern...
        // - Name required, non-empty
        // - Price required, positive number
        // - Stock required, non-negative integer
        
        return result;
    }
    
    // VALIDATION FOR ORDERS
    public static ImportResult<CommandeDTO> importOrdersFromCSV(File file) {
        ImportResult<CommandeDTO> result = new ImportResult<>();
        
        // Similar pattern...
        // - Date required, valid date format (yyyy-MM-dd)
        
        return result;
    }
}
```

### Q: What is the ImportResult class?
**A:**

```java
public class ImportResult<T> {
    private List<T> successfulRecords = new ArrayList<>();
    private List<ImportError> errors = new ArrayList<>();
    private int totalRows;
    
    public void addSuccessfulRecord(T record) {
        successfulRecords.add(record);
    }
    
    public void addError(ImportError error) {
        errors.add(error);
    }
    
    public List<T> getSuccessfulRecords() {
        return successfulRecords;
    }
    
    public List<ImportError> getErrors() {
        return errors;
    }
    
    public int getSuccessCount() {
        return successfulRecords.size();
    }
    
    public int getErrorCount() {
        return errors.size();
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public String getErrorSummary() {
        StringBuilder sb = new StringBuilder();
        for (ImportError error : errors) {
            sb.append(String.format("Row %d [%s]: %s\n",
                error.getRowNumber(),
                error.getField(),
                error.getMessage()));
        }
        return sb.toString();
    }
}

public class ImportError {
    private int rowNumber;
    private String field;
    private String value;
    private String message;
    
    public ImportError(int rowNumber, String field, String value, String message) {
        this.rowNumber = rowNumber;
        this.field = field;
        this.value = value;
        this.message = message;
    }
    
    // Getters...
}
```

---

## 5. Stock Management & Business Logic

### Q: How does stock management work?
**A:**

**Step 1: Stock Validation When Adding Product to Order**
```java
public class CommandeFormDialog extends JDialog {
    private void addLineItem() {
        // Show dialog with product selection
        JComboBox<ProduitDTO> productCombo = new JComboBox<>();
        productCombo.addItem(/* filtered to stock > 0 only */);
        
        // When product selected, update available quantity
        productCombo.addActionListener(e -> {
            ProduitDTO selected = (ProduitDTO) productCombo.getSelectedItem();
            availableStockLabel.setText("Available: " + selected.getQtstock());
        });
        
        JSpinner quantitySpinner = new JSpinner(
            new SpinnerNumberModel(1, 1, selected.getQtstock(), 1));
        // ← Max limited by available stock
    }
}
```

**Step 2: Stock Re-validation Before Save**
```java
private void saveCommande() {
    // Before saving, re-validate all stock levels
    // (prevents race conditions if another user bought inventory)
    
    for (LineItem item : orderItems) {
        Produit currentProduit = produitController.lire(item.getProduit().getId());
        
        if (currentProduit.getQtstock() < item.getQuantity()) {
            throw new InsufficientStockException(
                "Stock changed! Product " + currentProduit.getNom() + 
                " has only " + currentProduit.getQtstock() + 
                " available (need " + item.getQuantity() + ")");
        }
    }
    
    // Proceed with save if all validations pass
    commandeController.ajouterCommande(dto);
}
```

**Step 3: Stock Decrease After Successful Save**
```java
public class CommandeService {
    public void create(CommandeDTO dto) {
        // 1. Validate
        validateOrder(dto);
        
        // 2. Save order
        Long commandeId = commandeDAO.create(entity);
        
        // 3. Decrease stock for each line item
        for (LigneCommandeDTO ligne : dto.getLineItems()) {
            Produit produit = produitDAO.findById(ligne.getProduitId());
            
            // Calculate new stock
            int newStock = produit.getQtstock() - ligne.getQuantite();
            produit.setQtstock(newStock);
            
            // Update in database
            produitDAO.update(produit);
        }
    }
}
```

### Q: What happens if stock update fails?
**A:** Transaction ensures atomicity:

```java
public void create(CommandeDTO dto) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction tx = session.beginTransaction();
    
    try {
        // 1. Save order
        Commande commande = converter.toEntity(dto);
        session.save(commande);
        
        // 2. Create line items and update stock
        for (LigneCommandeDTO ligne : dto.getLineItems()) {
            Produit produit = session.get(Produit.class, ligne.getProduitId());
            produit.setQtstock(produit.getQtstock() - ligne.getQuantite());
            session.update(produit);
        }
        
        // 3. Commit all changes
        tx.commit();
        
    } catch (Exception e) {
        // Rollback on ANY error
        // Order not saved, stock not decreased
        tx.rollback();
        throw e;
    }
}
```

**ACID Property**: 
- **Atomic**: All updates or none
- **Consistent**: Stock never decreases without matching order
- **Isolated**: Other users don't see partial updates
- **Durable**: No data loss after commit

---

## 6. DTO Validation vs Entity Validation

### Q: What's the difference between DTO and Entity validation?
**A:**

```java
// DTO VALIDATION (Service layer)
public class ClientDTO {
    private String name;
    private BigDecimal capital;
    private String address;
    
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name required");
        }
        if (capital == null || capital.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Invalid capital");
        }
    }
}

// ENTITY VALIDATION (Hibernate)
@Entity
public class Client {
    @NotNull
    @Column(nullable = false)
    private String name;
    
    @NotNull
    @Positive
    @Column(nullable = false)
    private BigDecimal capital;
    
    @NotNull
    @Column(nullable = false)
    private String address;
}

// USAGE COMPARISON
// DTO validation (business logic level)
ClientDTO dto = new ClientDTO("Acme", new BigDecimal("50000"), "123 Main St");
dto.validate();  // Throws if invalid
service.create(dto);

// Entity validation (database level)
Client entity = new Client();
entity.setName("Acme");
entity.setCapital(new BigDecimal("50000"));
session.save(entity);  // Throws if validation fails
```

**Key Difference**:
- **DTO validation** = business rules that can change
- **Entity validation** = database constraints that are permanent

---

## 7. Error Handling in Import

### Q: How does CsvImportDialog handle validation errors?
**A:**

```java
public class CsvImportDialog extends JDialog {
    private void performImport() {
        // 1. Call ImportUtil
        ImportResult<?> result = validateFile();
        
        // 2. Display errors (if any)
        if (result.hasErrors()) {
            errorTextArea.setText(result.getErrorSummary());
            
            // Error display format:
            // Row 2 [Capital]: Capital is required
            // Row 3 [Capital]: Invalid number format
            // Row 4 [Address]: Address is required
            
            // User can still import successful records
            JOptionPane.showMessageDialog(this,
                result.getSuccessCount() + " records valid, " +
                result.getErrorCount() + " errors found.\n" +
                "Import valid records?",
                "Validation Results",
                JOptionPane.WARNING_MESSAGE);
        }
        
        // 3. Import successful records
        if (result.getSuccessCount() > 0) {
            importRecords(result.getSuccessfulRecords());
        }
    }
}
```

---

## 8. Dashboard Validation

### Q: How does Dashboard validate business metrics?
**A:**

```java
public class DashboardPanel extends JPanel {
    private void updateStats() {
        // GET DATA
        List<ClientDTO> clients = clientController.listerClients();
        List<CommandeDTO> orders = commandeController.listerCommandes();
        List<ProduitDTO> products = produitController.listerProduits();
        
        // VALIDATE & CALCULATE
        int totalClients = clients == null ? 0 : clients.size();
        int totalOrders = orders == null ? 0 : orders.size();
        int totalProducts = products == null ? 0 : products.size();
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (CommandeDTO order : orders) {
            // Validate order has valid total
            if (order.getTotal() != null && order.getTotal().compareTo(ZERO) > 0) {
                totalRevenue = totalRevenue.add(order.getTotal());
            }
        }
        
        int lowStockCount = 0;
        for (ProduitDTO product : products) {
            // Validate product has valid stock
            if (product.getQtstock() != null && product.getQtstock() < 10) {
                lowStockCount++;
            }
        }
        
        // UPDATE UI (only if data valid)
        totalClientsLabel.setText(String.valueOf(totalClients));
        totalRevenueLabel.setText(currencyFormat.format(totalRevenue));
        lowStockLabel.setText(String.valueOf(lowStockCount));
    }
}
```

---

## 9. Validation Performance

### Q: How do we avoid validation performance issues?
**A:**

**Pattern 1: Lazy Validation (Only When Needed)**
```java
// ❌ SLOW: Validate all records upfront
public class ProduitPanel {
    public void refresh() {
        List<ProduitDTO> products = controller.listerProduits();
        
        // Validate every product (expensive)
        for (ProduitDTO p : products) {
            validateProduct(p);  // Calls DB lookups
        }
        
        // Then display
        updateTable(products);
    }
}

// ✅ FAST: Validate only on display/edit
public class ProduitPanel {
    public void refresh() {
        List<ProduitDTO> products = controller.listerProduits();
        updateTable(products);
        // Validation happens only if user edits a record
    }
    
    public void edit(ProduitDTO product) {
        validateProduct(product);  // Validate only selected record
        showEditDialog(product);
    }
}
```

**Pattern 2: Bulk Validation in Import**
```java
public class ImportUtil {
    // Single pass through file for all validations
    while ((line = reader.readLine()) != null) {
        // Parse once, validate all fields at once
        String name = fields.get(0);
        String price = fields.get(1);
        String stock = fields.get(2);
        
        // All validations in single row pass
        if (valid) {
            result.addSuccessfulRecord(dto);
        } else {
            result.addError(error);
        }
    }
    // Much faster than individual queries per row
}
```

---

## Summary: Validation Philosophy

Key principles:

1. **Multi-tier**: Validation at UI, Controller, Service, DAO levels
2. **Defense in Depth**: Never trust input from any layer
3. **Business Rules in Service**: Critical validation in one place
4. **Fail Fast**: Validate early, throw clear exceptions
5. **Atomic Transactions**: All-or-nothing database operations
6. **Error Tracking**: Import tracks multiple errors, not just first
7. **User Feedback**: Clear, specific error messages
8. **Performance**: Validate only when necessary
9. **Consistency**: Same validation pattern across all entities
10. **Security**: Don't expose database errors to users
