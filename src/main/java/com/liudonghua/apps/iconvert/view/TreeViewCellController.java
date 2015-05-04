package com.liudonghua.apps.iconvert.view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

import com.liudonghua.apps.iconvert.MainApp;

public class TreeViewCellController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private HBox treeViewCellContainer;

    @FXML
    private CheckBox treeViewCellCheckBox;

    @FXML
    private HBox treeViewCellDetailContainer;

    @FXML
    private ComboBox<String> treeViewCellComboSource;

    @FXML
    private ComboBox<String> treeViewCellComboTarget;

    @FXML
    private ProgressBar treeViewCellProgress;

    public TreeViewCellController(MainApp mainApp) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TreeViewCell.fxml"),
                mainApp.getI18nBundle());
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ComboBox<String> getTreeViewCellComboSource() {
        return treeViewCellComboSource;
    }

    public ProgressBar getTreeViewCellProgress() {
        return treeViewCellProgress;
    }

    public ComboBox<String> getTreeViewCellComboTarget() {
        return treeViewCellComboTarget;
    }

    public CheckBox getTreeViewCellCheckBox() {
        return treeViewCellCheckBox;
    }

    public HBox getContainer() {
        return treeViewCellContainer;
    }

    public HBox getDetailContainer() {
        return treeViewCellDetailContainer;
    }

    @FXML
    void initialize() {

    }
}
