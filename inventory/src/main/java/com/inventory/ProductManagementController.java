package com.inventory;

import java.io.IOException;
import java.io.PrintWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;
import java.util.stream.Collectors;


public class ProductManagementController {
    @FXML private TableView<Product> productTableView;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TextField nameField, priceField, quantityField, descriptionField;
    
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadProductData();
        setupTableSelection();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }

    private void loadProductData() {
        try {
            productList.setAll(ProductDAO.getAllProducts());
            productTableView.setItems(productList);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void setupTableSelection() {
        productTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    populateFieldsWithProduct(newSelection);
                }
            });
    }

    @FXML
    private void handleAddProduct() {
        if (nameField.getText().isEmpty() || priceField.getText().isEmpty()) {
    showErrorAlert("Error", "Name and price are required!");
    return;
}
        try {
            Product product = createProductFromFields();
            ProductDAO.createProduct(product);
            productList.add(product);
            clearFields();
        } catch (NumberFormatException e) {
            showErrorAlert("Input Error", "Please enter valid numbers for price and quantity");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add product: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateProduct() {
        Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showErrorAlert("Selection Error", "No product selected");
            return;
        }

        try {
            Product updatedProduct = createProductFromFields();
            updatedProduct.setId(selectedProduct.getId());
            ProductDAO.updateProduct(updatedProduct);
            loadProductData(); // Refresh table
            clearFields();
        } catch (NumberFormatException e) {
            showErrorAlert("Input Error", "Please enter valid numbers for price and quantity");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update product: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showErrorAlert("Selection Error", "No product selected");
            return;
        }

        try {
            ProductDAO.deleteProduct(selectedProduct.getId());
            productList.remove(selectedProduct);
            clearFields();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to delete product: " + e.getMessage());
        }
    }
    @FXML
private void handleExport() throws IOException {
    try (PrintWriter writer = new PrintWriter("products_export.csv")) {
        writer.println("ID,Name,Price,Quantity");
        productList.forEach(p -> writer.println(
            p.getId() + "," + p.getName() + "," + p.getPrice() + "," + p.getQuantity()));
       
    }
}

    private Product createProductFromFields() throws NumberFormatException {
        return new Product(
            0, // ID will be set by database
            nameField.getText(),
            Double.parseDouble(priceField.getText()),
            Integer.parseInt(quantityField.getText()),
            descriptionField.getText()
        );
    }

    private void populateFieldsWithProduct(Product product) {
        nameField.setText(product.getName());
        priceField.setText(String.valueOf(product.getPrice()));
        quantityField.setText(String.valueOf(product.getQuantity()));
        descriptionField.setText(product.getDescription());
    }

    private void clearFields() {
        nameField.clear();
        priceField.clear();
        quantityField.clear();
        descriptionField.clear();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}