# CSV Import Test Files

## Files Created

### 1. test_data_clients.csv
- **10 client records** ready to import
- Includes various capital amounts ($38,500 - $250,000)
- Mix of simple and complex addresses
- Tests comma handling in address field (quoted)
- All records are valid

**Expected Result:** 10 successful imports

---

### 2. test_data_products.csv
- **20 product records** ready to import
- Price range: $9.99 - $299.00
- Stock levels: 45 - 1,000 units
- Mix of low stock (< 100) and well-stocked items
- All records are valid

**Expected Result:** 20 successful imports

---

### 3. test_data_orders.csv
- **10 order records** with dates only
- Date range: January 15 - February 22, 2026
- Simple format: just dates (client/products added via UI)
- All records are valid

**Expected Result:** 10 successful imports

---

### 4. test_data_clients_with_errors.csv
- **8 records with intentional errors** for testing validation
- Tests:
  - Missing capital value
  - Invalid capital (text instead of number)
  - Missing address
  - Negative capital
  - Empty rows

**Expected Result:** 
- 3 successful imports (Valid Company, Valid Company 2, Another Valid)
- 5 errors shown in validation

---

## How to Test

1. **Launch Application:**
   ```bash
   make run
   ```

2. **Test Client Import:**
   - Go to "Clients" tab
   - Click green "Import" button
   - Select `test_data_clients.csv`
   - Check "First row is header" ✓
   - Preview should show 10 rows
   - Click "Import"
   - Should see: "Successful: 10, Failed: 0"

3. **Test Product Import:**
   - Go to "Products" tab
   - Click "Import" button
   - Select `test_data_products.csv`
   - Enable header checkbox
   - Should import 20 products

4. **Test Error Handling:**
   - Go to "Clients" tab
   - Click "Import"
   - Select `test_data_clients_with_errors.csv`
   - Preview shows 7 data rows (+ header)
   - Validation errors panel shows 5 errors:
     - Row 2: Missing capital
     - Row 3: Invalid number format
     - Row 4: Missing address
     - Row 6: Negative capital
     - Row 7: Insufficient fields
   - Can still import 3 valid records

5. **Test Order Import:**
   - Go to "Orders" tab
   - Click "Import"
   - Select `test_data_orders.csv`
   - Should import 10 orders
   - Note: Orders need client association via Edit dialog

---

## CSV Format Requirements

### Clients
```csv
Name,Capital,Address
Company Name,50000.00,123 Street Address
```

### Products
```csv
Name,Price,Stock
Product Name,99.99,100
```

### Orders
```csv
Date
2026-02-23
```

---

## Special Cases Tested

✓ Commas in fields (quoted: "Address with, comma")
✓ Decimal numbers (123.45)
✓ Large numbers (250000.00)
✓ Date format (yyyy-MM-dd)
✓ Empty fields
✓ Invalid data types
✓ Negative numbers
✓ Special characters in names

---

## Dashboard Impact

After importing test data:
- **Dashboard** will show updated statistics
- **Client Segmentation** will categorize by capital ranges
- **Low Stock Alert** will show products with stock < 10
- **Top Clients** will rank by capital

---

## Notes

- Import is **additive** (doesn't delete existing records)
- Duplicate detection is **not implemented** (same name can be imported twice)
- Stock levels are validated (must be > 0)
- Capital must be positive
- All fields are required except as noted
