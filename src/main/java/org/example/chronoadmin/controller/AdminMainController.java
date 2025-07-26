package org.example.chronoadmin.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.example.chronoadmin.model.AdminLicense;
import org.example.chronoadmin.model.AdminScratchCard;
import org.example.chronoadmin.model.SalesRequest;
import org.example.chronoadmin.service.AdminLicenseService;

import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminMainController {

    // Scratch Card Generation
    @FXML private TextField salesPersonIdField, salesPersonNameField, territoryField;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button generateCardsBtn, exportCardsBtn;
    @FXML private Label generationStatus;
    @FXML private VBox generatedCardsSection;
    @FXML private TextArea generatedCardsArea;

    // Scratch Cards Management
    @FXML private TableView<AdminScratchCard> scratchCardsTable;
    @FXML private TableColumn<AdminScratchCard, String> cardCodeColumn, cardSalesPersonColumn, cardTerritoryColumn, cardStatusColumn, cardCreatedColumn, cardUsedColumn;
    @FXML private TextField searchCardsField;
    @FXML private Button refreshCardsBtn, searchCardsBtn;

    // Sales Requests
    @FXML private TextArea salesKeyInput;
    @FXML private Button processSalesKeyBtn, refreshRequestsBtn;
    @FXML private Label salesKeyStatus;
    @FXML private TableView<SalesRequest> salesRequestsTable;
    @FXML private TableColumn<SalesRequest, String> requestScratchCodeColumn, requestSalesPersonColumn, requestCustomerColumn, requestReceivedColumn, requestActionsColumn;

    // License Management
    @FXML private TableView<AdminLicense> licensesTable;
    @FXML private TableColumn<AdminLicense, String> licenseScratchCodeColumn, licenseSalesPersonColumn, licenseCustomerColumn, licenseIssuedColumn, licenseExpiresColumn, licenseStatusColumn;
    @FXML private TextField searchLicensesField;
    @FXML private Button refreshLicensesBtn, searchLicensesBtn;
    @FXML private VBox licenseDisplaySection;
    @FXML private TextArea generatedLicenseArea;
    @FXML private Button copyLicenseBtn;

    private ObservableList<AdminScratchCard> scratchCardsData = FXCollections.observableArrayList();
    private ObservableList<SalesRequest> salesRequestsData = FXCollections.observableArrayList();
    private ObservableList<AdminLicense> licensesData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupQuantitySpinner();
        setupScratchCardsTable();
        setupSalesRequestsTable();
        setupLicensesTable();
        loadInitialData();
    }

    private void setupQuantitySpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10);
        quantitySpinner.setValueFactory(valueFactory);
    }

    private void setupScratchCardsTable() {
        cardCodeColumn.setCellValueFactory(new PropertyValueFactory<>("scratchCode"));
        cardSalesPersonColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getSalesPersonName() + " (" + cellData.getValue().getSalesPersonId() + ")"));
        cardTerritoryColumn.setCellValueFactory(new PropertyValueFactory<>("territory"));
        cardStatusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isUsed() ? "Used" : "Available"));
        cardCreatedColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        cardUsedColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getUsedAt() != null ?
                cellData.getValue().getUsedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A"));

        scratchCardsTable.setItems(scratchCardsData);
    }

    private void setupSalesRequestsTable() {
        requestScratchCodeColumn.setCellValueFactory(new PropertyValueFactory<>("scratchCode"));
        requestSalesPersonColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getSalesPersonName() + " (" + cellData.getValue().getSalesPersonId() + ")"));
        requestCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerDetails"));
        requestReceivedColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getReceivedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        // Custom cell factory for actions column
        requestActionsColumn.setCellFactory(column -> {
            return new TableCell<SalesRequest, String>() {
                private final Button generateLicenseBtn = new Button("Generate License");

                {
                    generateLicenseBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 12px;");
                    generateLicenseBtn.setOnAction(event -> {
                        SalesRequest request = getTableView().getItems().get(getIndex());
                        generateLicenseForRequest(request);
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(generateLicenseBtn);
                    }
                }
            };
        });

        salesRequestsTable.setItems(salesRequestsData);
    }

    private void setupLicensesTable() {
        licenseScratchCodeColumn.setCellValueFactory(new PropertyValueFactory<>("scratchCode"));
        licenseSalesPersonColumn.setCellValueFactory(new PropertyValueFactory<>("salesPersonId"));
        licenseCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerDetails"));
        licenseIssuedColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getIssuedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        licenseExpiresColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getExpiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        licenseStatusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));

        licensesTable.setItems(licensesData);
    }

    private void loadInitialData() {
        refreshScratchCards();
        refreshSalesRequests();
        refreshLicenses();
    }

    @FXML
    private void generateScratchCards() {
        String salesPersonId = salesPersonIdField.getText().trim();
        String salesPersonName = salesPersonNameField.getText().trim();
        String territory = territoryField.getText().trim();
        int quantity = quantitySpinner.getValue();

        if (salesPersonId.isEmpty() || salesPersonName.isEmpty() || territory.isEmpty()) {
            showError(generationStatus, "Please fill in all fields");
            return;
        }

        try {
            List<AdminScratchCard> generatedCards = AdminLicenseService.generateScratchCards(
                salesPersonId, salesPersonName, territory, quantity);

            // Display generated cards
            StringBuilder cardsText = new StringBuilder();
            cardsText.append("Generated ").append(quantity).append(" scratch cards for ").append(salesPersonName).append(":\n\n");

            for (AdminScratchCard card : generatedCards) {
                cardsText.append("Scratch Code: ").append(card.getScratchCode()).append("\n");
                cardsText.append("Embedded Password: ").append(card.getEmbeddedPassword()).append("\n");
                cardsText.append("Sales Person: ").append(card.getSalesPersonName()).append(" (").append(card.getSalesPersonId()).append(")\n");
                cardsText.append("Territory: ").append(card.getTerritory()).append("\n");
                cardsText.append("Created: ").append(card.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
                cardsText.append("---------------------------------------------------\n");
            }

            generatedCardsArea.setText(cardsText.toString());
            generatedCardsSection.setVisible(true);
            generatedCardsSection.setManaged(true);
            exportCardsBtn.setVisible(true);

            showSuccess(generationStatus, "Successfully generated " + quantity + " scratch cards!");

            // Clear form
            salesPersonIdField.clear();
            salesPersonNameField.clear();
            territoryField.clear();
            quantitySpinner.getValueFactory().setValue(10);

            // Refresh table
            refreshScratchCards();

        } catch (Exception e) {
            showError(generationStatus, "Error generating scratch cards: " + e.getMessage());
        }
    }

    @FXML
    private void exportScratchCards() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Scratch Cards");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("scratch_cards_" + java.time.LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(exportCardsBtn.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Scratch Code,Embedded Password,Sales Person ID,Sales Person Name,Territory,Created Date\n");

                String[] lines = generatedCardsArea.getText().split("---------------------------------------------------");
                for (String line : lines) {
                    if (line.contains("Scratch Code:")) {
                        String[] parts = line.split("\n");
                        String scratchCode = parts[0].replace("Scratch Code: ", "").trim();
                        String embeddedPassword = parts[1].replace("Embedded Password: ", "").trim();
                        String salesPerson = parts[2].replace("Sales Person: ", "").trim();
                        String territory = parts[3].replace("Territory: ", "").trim();
                        String created = parts[4].replace("Created: ", "").trim();

                        writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            scratchCode, embeddedPassword, salesPerson.split(" \\(")[1].replace(")", ""),
                            salesPerson.split(" \\(")[0], territory, created));
                    }
                }

                showSuccess(generationStatus, "Scratch cards exported successfully to: " + file.getAbsolutePath());

            } catch (Exception e) {
                showError(generationStatus, "Error exporting scratch cards: " + e.getMessage());
            }
        }
    }

    @FXML
    private void refreshScratchCards() {
        try {
            List<AdminScratchCard> cards = AdminLicenseService.getAllScratchCards();
            scratchCardsData.clear();
            scratchCardsData.addAll(cards);
        } catch (Exception e) {
            showError(generationStatus, "Error loading scratch cards: " + e.getMessage());
        }
    }

    @FXML
    private void searchScratchCards() {
        String searchTerm = searchCardsField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            refreshScratchCards();
            return;
        }

        try {
            List<AdminScratchCard> allCards = AdminLicenseService.getAllScratchCards();
            List<AdminScratchCard> filteredCards = allCards.stream()
                .filter(card ->
                    card.getScratchCode().toLowerCase().contains(searchTerm) ||
                    card.getSalesPersonName().toLowerCase().contains(searchTerm) ||
                    card.getSalesPersonId().toLowerCase().contains(searchTerm) ||
                    card.getTerritory().toLowerCase().contains(searchTerm))
                .toList();

            scratchCardsData.clear();
            scratchCardsData.addAll(filteredCards);
        } catch (Exception e) {
            showError(generationStatus, "Error searching scratch cards: " + e.getMessage());
        }
    }

    @FXML
    private void processSalesKey() {
        String salesKey = salesKeyInput.getText().trim();
        if (salesKey.isEmpty()) {
            showError(salesKeyStatus, "Please enter a sales person key");
            return;
        }

        try {
            AdminLicenseService.processSalesPersonKey(salesKey);
            showSuccess(salesKeyStatus, "Sales key processed successfully!");
            salesKeyInput.clear();
            refreshSalesRequests();
            refreshScratchCards();
        } catch (Exception e) {
            showError(salesKeyStatus, "Error processing sales key: " + e.getMessage());
        }
    }

    @FXML
    private void refreshSalesRequests() {
        try {
            List<SalesRequest> requests = AdminLicenseService.getPendingSalesRequests();
            salesRequestsData.clear();
            salesRequestsData.addAll(requests);
        } catch (Exception e) {
            showError(salesKeyStatus, "Error loading sales requests: " + e.getMessage());
        }
    }

    private void generateLicenseForRequest(SalesRequest request) {
        try {
            String licenseKey = AdminLicenseService.generateLicense(request);

            generatedLicenseArea.setText(licenseKey);
            licenseDisplaySection.setVisible(true);
            licenseDisplaySection.setManaged(true);

            refreshSalesRequests();
            refreshLicenses();

            // Switch to license management tab to show the generated license
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("License Generated");
            alert.setHeaderText("License generated successfully!");
            alert.setContentText("License key has been generated for " + request.getSalesPersonName() +
                               ". You can copy it from the License Management tab.");
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to generate license");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void refreshLicenses() {
        try {
            List<AdminLicense> licenses = AdminLicenseService.getAllLicenses();
            licensesData.clear();
            licensesData.addAll(licenses);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Error loading licenses: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void searchLicenses() {
        String searchTerm = searchLicensesField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            refreshLicenses();
            return;
        }

        try {
            List<AdminLicense> allLicenses = AdminLicenseService.getAllLicenses();
            List<AdminLicense> filteredLicenses = allLicenses.stream()
                .filter(license ->
                    license.getScratchCode().toLowerCase().contains(searchTerm) ||
                    license.getSalesPersonId().toLowerCase().contains(searchTerm) ||
                    license.getCustomerDetails().toLowerCase().contains(searchTerm))
                .toList();

            licensesData.clear();
            licensesData.addAll(filteredLicenses);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error searching licenses: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void copyLicenseKey() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(generatedLicenseArea.getText());
        clipboard.setContent(content);

        copyLicenseBtn.setText("Copied!");
        copyLicenseBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> {
                    copyLicenseBtn.setText("Copy License Key");
                    copyLicenseBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void showError(Label statusLabel, String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    private void showSuccess(Label statusLabel, String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #27ae60;");
    }
}
