# Database & ORM Design - Q&A Reference

## 1. ORM Choice: Hibernate

### Q: Why did we choose Hibernate as ORM?
**A:** Hibernate provides several advantages:

| Benefit | Explanation |
|---------|-------------|
| **Object-Oriented** | Work with Java objects, not SQL |
| **Database Agnostic** | Change DB dialect without code changes |
| **Lazy Loading** | Load data only when accessed |
| **Caching** | Level 1 & 2 caches reduce DB queries |
| **Query Language** | HQL similar to SQL but works with objects |
| **Transaction Management** | Automatic ACID compliance |
| **Relationship Mapping** | Automatic handling of @OneToMany, @ManyToOne |
| **Cascade Operations** | Delete parent automatically deletes children |

### Q: What is the Hibernate configuration?
**A:** Configuration in [hibernate.cfg.xml](../src/main/java/bo/dao/hibernate.cfg.xml):

```xml
<hibernate-configuration>
    <session-factory>
        <!-- Database Connection -->
        <property name="hibernate.dialect">org.hibernate.dialect.MySQLDialect</property>
        <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/mydb</property>
        <property name="hibernate.connection.username">root</property>
        <property name="hibernate.connection.password">1234</property>
        
        <!-- Connection Pool -->
        <property name="hibernate.c3p0.min_size">5</property>
        <property name="hibernate.c3p0.max_size">20</property>
        <property name="hibernate.c3p0.timeout">300</property>
        
        <!-- Schema Validation -->
        <property name="hbm2ddl.auto">validate</property>
        <!-- Options:
             validate:   Check schema matches entities
             update:     Modify schema without losing data
             create:     Drop & recreate schema
             create-drop: Create on startup, drop on shutdown
        -->
        
        <!-- SQL Output (Development) -->
        <property name="hibernate.show_sql">false</property>
        <property name="hibernate.format_sql">false</property>
        
        <!-- Entity Mapping -->
        <mapping class="bo.Client"/>
        <mapping class="bo.Commande"/>
        <mapping class="bo.Produit"/>
        <mapping class="bo.Ligne_Commande"/>
    </session-factory>
</hibernate-configuration>
```

---

## 2. Entity Mapping Strategy

### Q: How do entities map to database tables?
**A:** Using JPA annotations:

```java
// SIMPLE ENTITY
@Entity
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nom", nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false)
    private BigDecimal capital;
    
    @Column(length = 200)
    private String address;
}

// GENERATED SQL
CREATE TABLE client (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    capital DECIMAL NOT NULL,
    address VARCHAR(200),
    CONSTRAINT client_pk PRIMARY KEY (id)
);
```

### Q: How are relationships mapped?
**A:**

**One-to-Many Relationship**
```java
// PARENT ENTITY
@Entity
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    // One client has many orders
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Commande> commandes;
}

// CHILD ENTITY
@Entity
@Table(name = "commande")
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Date date;
    private Float total;
    
    // Many orders belong to one client
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
}

// GENERATED SQL
CREATE TABLE client (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ...
);

CREATE TABLE commande (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATETIME,
    total FLOAT,
    client_id BIGINT NOT NULL,
    FOREIGN KEY (client_id) REFERENCES client(id),
    ...
);
```

**Many-to-Many Relationship (via Join Table)**
```java
// EXAMPLE: If we had Users and Roles
@Entity
public class User {
    @Id
    private Long id;
    private String username;
    
    @ManyToMany
    @JoinTable(
        name = "user_roles",  // Join table name
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}

@Entity
public class Role {
    @Id
    private Long id;
    private String name;
    
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
}

// GENERATED SQL
CREATE TABLE user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);
```

---

## 3. Cascade Operations

### Q: What does cascade mean and why is it important?
**A:** Cascade propagates operations from parent to child:

```java
@OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
private List<Commande> commandes;

// CascadeType options:
// - ALL:      Apply all operations
// - PERSIST:  Save parent → saves children
// - MERGE:    Merge parent → merges children
// - REMOVE:   Delete parent → deletes children
// - REFRESH:  Refresh parent → refreshes children
// - DETACH:   Detach parent → detaches children
```

**Example with CASCADE.ALL**
```java
// Scenario: Delete a client
Client client = session.find(Client.class, 1L);

// ✓ With CascadeType.ALL:
session.delete(client);
// → Also deletes all associated Commande records
// → Automatic cleanup

// ✗ Without cascade:
session.delete(client);
// → Error: Foreign key constraint violation
// → Can't delete client if orders exist
// → Must manually delete orders first
```

---

## 4. Fetch Strategy: Eager vs Lazy

### Q: What is Eager vs Lazy loading and when use each?
**A:**

**Lazy Loading (Default)**
```java
@Entity
public class Client {
    private Long id;
    private String name;
    
    @OneToMany(fetch = FetchType.LAZY)  // ← Default
    private List<Commande> commandes;
}

// QUERY 1: Load client
Client client = session.get(Client.class, 1L);
// SQL: SELECT * FROM client WHERE id = 1
// Result: Client loaded, commandes NOT loaded yet

// QUERY 2: Access commandes
List<Commande> orders = client.getCommandes();
// SQL: SELECT * FROM commande WHERE client_id = 1
// Result: Now commandes loaded (lazy loading trigger)
```

**Eager Loading**
```java
@Entity
public class Client {
    private Long id;
    private String name;
    
    @OneToMany(fetch = FetchType.EAGER)  // ← Load immediately
    private List<Commande> commandes;
}

// QUERY 1: Load client
Client client = session.get(Client.class, 1L);
// SQL: SELECT * FROM client WHERE id = 1
// SQL: SELECT * FROM commande WHERE client_id = 1
// Result: Client AND commandes loaded immediately
```

### Q: When should we use Lazy loading?
**A:** Almost always for @OneToMany:

```java
// ✅ Lazy: Efficient for most cases
@Entity
public class Client {
    @OneToMany(fetch = FetchType.LAZY)
    private List<Commande> commandes;  // Only load if accessed
}

// Usage in UI
List<ClientDTO> clients = clientService.findAll();  // Single query
table.setData(clients);                             // Display clients
// If user clicks order button → Load orders (second query)
```

### Q: When should we use Eager loading?
**A:** Rarely, only when data needed immediately:

```java
// ❌ Eager: Loads unnecessary data
@Entity
public class Commande {
    @ManyToOne(fetch = FetchType.EAGER)  // Always load client
    private Client client;
}
// Every order query also loads client
// It's related, so maybe OK

// ✅ Lazy: Load only if needed
@Entity
public class Commande {
    @ManyToOne(fetch = FetchType.LAZY)
    private Client client;
}
// Load client only if accessed: order.getClient()
```

---

## 5. Session Management

### Q: What is a Hibernate Session and how do we manage it?
**A:** Session represents connection to database:

```java
public class ClientDAO {
    public List<Client> findAll() {
        // 1. Get session
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            // 2. Execute query
            List<Client> clients = session
                .createQuery("FROM Client", Client.class)
                .list();
            
            // 3. Return results
            return clients;
            
        } finally {
            // 4. CLOSE session (critical!)
            session.close();
        }
    }
}
```

### Q: What happens if we don't close the session?
**A:** Resource leak - connections never released:

```
⚠️ PROBLEM: Unclosed Sessions
┌─────────────────────┐
│ Session 1 (open)    │
│ Session 2 (open)    │─ Connection pool fills up
│ Session 3 (open)    │
│ Session 4 (open)    │─ App slows down
│ ...                 │
└─────────────────────┘

Result:
- Connection pool exhausted
- New queries fail: "No available connection"
- Application becomes unresponsive
```

### Q: How do we ensure sessions are closed?
**A:** Using try-finally:

```java
// ✅ CORRECT: Session always closed
Session session = HibernateUtil.getSessionFactory().openSession();
try {
    return session.get(Client.class, id);
} finally {
    session.close();  // Called regardless of exception
}

// ✅ CORRECT: Try-with-resources (Java 7+)
try (Session session = HibernateUtil.getSessionFactory().openSession()) {
    return session.get(Client.class, id);
}  // Session auto-closed
```

---

## 6. Transaction Management

### Q: What is a Hibernate transaction and when is it needed?
**A:** Transaction ensures ACID properties:

```java
public void create(Client client) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();
    
    try {
        session.save(client);
        transaction.commit();  // Save to database
    } catch (HibernateException e) {
        transaction.rollback();  // Undo on error
        throw e;
    } finally {
        session.close();
    }
}
```

### Q: When do we need transactions?
**A:**

**Need Transaction:**
- INSERT, UPDATE, DELETE operations
- Multiple operations that must be atomic
- Operations that require rollback on error

**Don't Need Transaction (Read-Only):**
- SELECT queries only
- No data modifications

```java
// ✅ Need transaction
public void update(Client client) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction tx = session.beginTransaction();
    try {
        session.update(client);
        tx.commit();  // Must commit updates
    } catch (Exception e) {
        tx.rollback();
    } finally {
        session.close();
    }
}

// ❌ Transaction not needed (but doesn't hurt)
public List<Client> findAll() {
    Session session = HibernateUtil.getSessionFactory().openSession();
    try {
        return session.createQuery("FROM Client", Client.class).list();
    } finally {
        session.close();  // Just close
    }
}
```

---

## 7. Query Types: HQL vs Native SQL

### Q: What is HQL and when do we use it?
**A:** HQL (Hibernate Query Language) is object-oriented query language:

```java
// HQL: Works with objects
String hql = "FROM Client WHERE capital > :minCapital";
List<Client> richClients = session
    .createQuery(hql, Client.class)
    .setParameter("minCapital", BigDecimal.valueOf(50000))
    .list();

// Parameters:
// - "Client" = Entity class name (not table name)
// - "capital" = Entity field name (not column name)
// - ":minCapital" = Named parameter (prevents SQL injection)

// Native SQL: When HQL not sufficient
String sql = "SELECT * FROM client WHERE datediff(year, created_date, now()) > 5";
List<Client> oldClients = session
    .createNativeQuery(sql, Client.class)
    .list();
```

### Q: How do we prevent SQL injection in HQL?
**A:** Using parameterized queries:

```java
// ❌ WRONG: String concatenation (SQL injection risk)
String name = userInput;  // Could be: " OR 1=1 --"
String hql = "FROM Client WHERE name = '" + name + "'";
List<Client> result = session.createQuery(hql, Client.class).list();

// ✅ CORRECT: Parameterized query
String hql = "FROM Client WHERE name = :name";
List<Client> result = session.createQuery(hql, Client.class)
    .setParameter("name", userInput)
    .list();
// Even if userInput = " OR 1=1 --"
// Parameter treated as literal string, not SQL
```

---

## 8. HQL Query Examples

### Q: What are common HQL query patterns?
**A:**

**SELECT with WHERE**
```java
// Find all clients with capital > 50000
String hql = "FROM Client WHERE capital > :minCapital";
List<Client> clients = session.createQuery(hql, Client.class)
    .setParameter("minCapital", new BigDecimal("50000"))
    .list();
```

**SELECT with IN clause**
```java
// Find clients by IDs
String hql = "FROM Client WHERE id IN (:ids)";
List<Client> clients = session.createQuery(hql, Client.class)
    .setParameterList("ids", Arrays.asList(1L, 2L, 3L))
    .list();
```

**SELECT with JOIN**
```java
// Get all orders with client info
String hql = "SELECT c, COUNT(o) FROM Client c LEFT JOIN c.commandes o GROUP BY c.id";
List<Object[]> results = session.createQuery(hql).list();
// Each Object[] = [Client, Order count]
```

**SELECT with ORDER BY**
```java
// Get top 10 clients by capital (descending)
String hql = "FROM Client ORDER BY capital DESC";
List<Client> topClients = session.createQuery(hql, Client.class)
    .setMaxResults(10)
    .list();
```

**COUNT aggregate**
```java
// Count total clients
String hql = "SELECT COUNT(*) FROM Client";
Long count = session.createQuery(hql, Long.class).uniqueResult();
```

**SUM aggregate**
```java
// Total revenue
String hql = "SELECT SUM(total) FROM Commande";
BigDecimal totalRevenue = session.createQuery(hql, BigDecimal.class).uniqueResult();
```

---

## 9. Query Performance

### Q: How do we optimize Hibernate queries?
**A:**

**Optimization 1: Select Only Needed Fields**
```java
// ❌ SLOW: Load all Client data
String hql = "FROM Client";
List<Client> clients = session.createQuery(hql, Client.class).list();
// Then iterate and access fields
for (Client c : clients) {
    System.out.println(c.getName());
}

// ✅ FAST: Select only needed fields
String hql = "SELECT c.id, c.name FROM Client c";
List<Object[]> results = session.createQuery(hql).list();
// Only name & id loaded
for (Object[] row : results) {
    System.out.println(row[1]);  // name
}
```

**Optimization 2: Use Pagination**
```java
// ❌ SLOW: Load all 10,000 records
String hql = "FROM Client";
List<Client> allClients = session.createQuery(hql, Client.class).list();

// ✅ FAST: Load page by page
int pageSize = 50;
int pageNum = 1;
String hql = "FROM Client";
List<Client> page = session.createQuery(hql, Client.class)
    .setFirstResult((pageNum - 1) * pageSize)
    .setMaxResults(pageSize)
    .list();
```

**Optimization 3: Avoid N+1 Query Problem**
```java
// ❌ SLOW: N+1 queries
List<Client> clients = session.createQuery("FROM Client", Client.class).list();
for (Client c : clients) {
    // Each iteration triggers new query!
    List<Commande> orders = c.getCommandes();  // Query #1, #2, #3...
}
// Total queries: 1 (clients) + N (orders per client)

// ✅ FAST: Single query with JOIN
String hql = "FROM Client c JOIN FETCH c.commandes";
List<Client> clients = session.createQuery(hql, Client.class).list();
for (Client c : clients) {
    List<Commande> orders = c.getCommandes();  // Already loaded
}
// Total queries: 1 (with join)
```

---

## 10. Detached Objects

### Q: What is a detached object?
**A:** Object loaded from database but session closed:

```java
// ATTACHED: Object loaded in active session
Session session1 = HibernateUtil.getSessionFactory().openSession();
Client client = session1.get(Client.class, 1L);  // Attached
session1.close();
// Now DETACHED: session closed

// Trying to access properties
System.out.println(client.getName());  // OK - already loaded
System.out.println(client.getCommandes());  // ⚠️ Error!
// LazyInitializationException: Cannot lazy load collection
// (session closed)

// RE-ATTACH: Use new session
Session session2 = HibernateUtil.getSessionFactory().openSession();
Client attached = session2.merge(client);  // Re-attach
System.out.println(attached.getCommandes());  // OK
session2.close();
```

---

## 11. Caching Strategy

### Q: How does Hibernate caching work?
**A:**

**Level 1 Cache (Session Cache)**
```java
Session session = HibernateUtil.getSessionFactory().openSession();

// Query 1
Client client1 = session.get(Client.class, 1L);  // DB hit

// Query 2 (same object)
Client client2 = session.get(Client.class, 1L);  // Cache hit (no DB)

// Both references point to same object in memory
System.out.println(client1 == client2);  // true

session.close();  // Cache cleared
```

**Benefits of Level 1 Cache**:
- Prevents duplicate object creation
- Improves performance in same transaction
- Automatic, not configurable

---

## Summary: Database & ORM Philosophy

Key principles:

1. **ORM Abstraction**: Work with objects, not SQL
2. **Lazy Loading**: Load data only when needed
3. **Cascading**: Automatic parent-child operations
4. **Parameterized Queries**: Prevent SQL injection
5. **Transaction Safety**: ACID compliance guaranteed
6. **Session Management**: Always close sessions
7. **Query Optimization**: Select only needed fields
8. **N+1 Problem**: Use JOIN FETCH to avoid
9. **Performance**: Cache, pagination, lazy loading
10. **Clean Code**: DTOs isolate persistence from business logic
