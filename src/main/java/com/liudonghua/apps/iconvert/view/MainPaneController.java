package com.liudonghua.apps.iconvert.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.mozilla.universalchardet.UniversalDetector;

import com.liudonghua.apps.iconvert.MainApp;
import com.liudonghua.apps.iconvert.model.FileInfoWrapper;
import com.liudonghua.apps.iconvert.utils.Utils;

public class MainPaneController extends Stage implements Initializable {

    @FXML
    private MenuItem menuExit;

    @FXML
    private Menu menuLanguage;

    @FXML
    private Menu menuTheme;

    @FXML
    private ToggleGroup toggleGroupLanguage;

    @FXML
    private ToggleGroup toggleGroupTheme;

    @FXML
    private MenuItem menuAbout;

    @FXML
    private TextField textFieldInputDir;

    @FXML
    private Button buttonInputDir;

    @FXML
    private CheckBox checkBoxCustomOutput;

    @FXML
    private TextField textFieldOutputDir;

    @FXML
    private Button buttonOutputDir;

    @FXML
    private ComboBox<String> comboBoxOutputEncoding;

    @FXML
    private Button buttonStart;

    @FXML
    private CheckBox checkBoxFilter1;

    @FXML
    private CheckBox checkBoxFilter2;

    @FXML
    private CheckBox checkBoxFilter3;

    @FXML
    private CheckBox checkBoxFilter4;

    @FXML
    private CheckBox checkBoxCustomFilter;

    @FXML
    private TextField textFieldCustomFilter;

    @FXML
    private Button buttonFilter;

    @FXML
    private VBox vBoxMainContainer;

    @FXML
    private TitledPane titledPaneInput;

    @FXML
    private TitledPane titledPaneFilter;

    @FXML
    private TitledPane titledPanePreview;

    @FXML
    private TreeView<FileInfoWrapper> treeViewPreview;

    @FXML
    private ProgressBar progressBarStatus;

    @FXML
    private Label labelStatus;

    private Scene scene;
    private MainApp mainApp;
    private ResourceBundle i18nBundle;
    private List<FileInfoWrapper> fileInfoWrappers;
    private TreeItem<FileInfoWrapper> rootNode;
    private ObservableList<String> commonEncodingObservableList = FXCollections
            .observableArrayList("UTF-8", "Unicode", "GBK");
    private BooleanProperty customOutputEnable = new SimpleBooleanProperty();
    private BooleanProperty filterAllEnable = new SimpleBooleanProperty();
    private BooleanProperty filterJavaEnable = new SimpleBooleanProperty();
    private BooleanProperty filterPropertiesEnable = new SimpleBooleanProperty();
    private BooleanProperty filterXMLEnable = new SimpleBooleanProperty();
    private BooleanProperty customFilterEnable = new SimpleBooleanProperty();
    private String[] filterExtensions;

    public MainPaneController(MainApp mainApp) {
        this.mainApp = mainApp;
        this.i18nBundle = mainApp.getI18nBundle();
        setTitle(i18nBundle.getString("app.title"));
        getIcons().add(new Image("file:resources/images/art-icon-32.png"));
        setMinHeight(600);
        setMinWidth(700);
        initModality(Modality.WINDOW_MODAL);
        initOwner(mainApp.getPrimaryStage());
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainPane.fxml"),
                mainApp.getI18nBundle());
        fxmlLoader.setController(this);
        try {
            scene = new Scene((Parent) fxmlLoader.load());
            scene.getStylesheets().add(mainApp.getTheme());
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // initialize the menu items
        initializeMenu();
        // initialize the input pane area
        initializeInputPane();
        // initialize the filter pane area
        initializeFilterPane();
        // initialize the tree view pane area
        initializeTreeViewPane();

    }

    private void initializeMenu() {
        menuLanguage.getItems().clear();
        Locale localInPref = mainApp.getLocale();
        for (Entry<String, Locale> localeEntry : mainApp.locales.entrySet()) {
            RadioMenuItem languageMenuItem = new RadioMenuItem(localeEntry.getKey());
            if (localeEntry.getValue().equals(localInPref)) {
                languageMenuItem.setSelected(true);
            }
            languageMenuItem.setToggleGroup(toggleGroupLanguage);
            menuLanguage.getItems().add(languageMenuItem);
            languageMenuItem.setOnAction(event -> handleLanguageChange(event));
        }

        menuTheme.getItems().clear();
        String themeInPref = mainApp.getTheme();
        for (Entry<String, String> themeEntry : mainApp.themes.entrySet()) {
            RadioMenuItem themeMenuItem = new RadioMenuItem(themeEntry.getKey());
            if (themeEntry.getValue().equals(themeInPref)) {
                themeMenuItem.setSelected(true);
            }
            themeMenuItem.setToggleGroup(toggleGroupTheme);
            menuTheme.getItems().add(themeMenuItem);
            themeMenuItem.setOnAction(event -> handleThemeChange(event));
        }
    }

    private void initializeInputPane() {
        customOutputEnable.bind(checkBoxCustomOutput.selectedProperty());
        comboBoxOutputEncoding.itemsProperty().set(commonEncodingObservableList);
        comboBoxOutputEncoding.setOnAction(event -> {
            String newValue = comboBoxOutputEncoding.getSelectionModel().getSelectedItem();
            fileInfoWrappers.stream().forEachOrdered((fileInfo) -> {
                fileInfo.setTargetEncoding(newValue);
            });
        });
    }

    private void initializeFilterPane() {
        filterAllEnable.bind(checkBoxFilter1.selectedProperty());
        filterJavaEnable.bind(checkBoxFilter2.selectedProperty());
        filterPropertiesEnable.bind(checkBoxFilter3.selectedProperty());
        filterXMLEnable.bind(checkBoxFilter4.selectedProperty());
        customFilterEnable.bind(checkBoxCustomFilter.selectedProperty());
    }

    private void initializeTreeViewPane() {
        DoubleBinding prefHeightProperty = vBoxMainContainer.heightProperty().subtract(
                titledPaneInput.heightProperty().add(titledPaneFilter.heightProperty()));
        titledPanePreview.prefHeightProperty().bind(prefHeightProperty.subtract(0));
        treeViewPreview.setCellFactory((para) -> {
            return new FileInfoTreeCell();
        });
    }

    @FXML
    void handleInputDirAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        String initialOpenLocation = null;
        try {
            String executingFilePath = MainApp.class.getProtectionDomain().getCodeSource()
                    .getLocation().getPath();
            String executingDirPath = new File(executingFilePath).getParent();
            initialOpenLocation = URLDecoder.decode(executingDirPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (initialOpenLocation == null) {
            initialOpenLocation = System.getProperty("user.home");
        }
        directoryChooser.setInitialDirectory(new File(initialOpenLocation));
        File selectedDirectory = directoryChooser.showDialog(this);
        if (selectedDirectory != null) {
            textFieldInputDir.setText(selectedDirectory.getAbsolutePath());
            initTreeView(selectedDirectory);
            setupInputProcessEnable(true);
        }
    }

    private void initTreeView(File directory) {
        Path directoryPath = FileSystems.getDefault().getPath(directory.getPath());
        FileInfoWrapper rootFileInfo = new FileInfoWrapper(directory.getAbsolutePath(), "", "",
                false, true);
        rootNode = new TreeItem<FileInfoWrapper>(rootFileInfo);
        rootFileInfo.setTreeViewCellController(setupTreeViewController(rootFileInfo));
        rootNode.setExpanded(true);
        fileInfoWrappers = new ArrayList<>();
        fileInfoWrappers.add(rootFileInfo);
        filterExtensions = customFilterEnable.get() ? Utils
                .getFilterExtensions(textFieldCustomFilter.getText().split("\\s*;\\s*"))
                : getDefaultFilterExtensions();
        recusiveIterateDirectory(rootNode, fileInfoWrappers, directoryPath);
        treeViewPreview.setRoot(rootNode);
        // auto detect the encoding of each file in fileInfoWrappers
        new Thread(() -> {
            byte[] buf = new byte[4096];
            UniversalDetector detector = new UniversalDetector(null);
            fileInfoWrappers.stream().filter(fileInfo -> !fileInfo.getIsDirectory())
                    .forEachOrdered(fileInfo -> {
                        FileInputStream fis = null;
                        int nread = 0;
                        try {
                            fis = new FileInputStream(fileInfo.getFilePath());
                            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                                detector.handleData(buf, 0, nread);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (fis != null) {
                                    fis.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        detector.dataEnd();
                        String preEncoding = detector.getDetectedCharset();
                        final String encoding = preEncoding != null ? preEncoding : "UTF-8";
                        // Fix issue "Not on FX application thread"
                            Platform.runLater(() -> {
                                fileInfo.setSourceEncoding(encoding);
                            });
                            detector.reset();
                        });
        }).start();
    }

    private List<FileInfoWrapper> recusiveIterateDirectory(TreeItem<FileInfoWrapper> node,
            List<FileInfoWrapper> fileInfos, Path rootPath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    FileInfoWrapper directoryInfo = new FileInfoWrapper(path.toFile()
                            .getAbsolutePath(), "", "", false, true);
                    directoryInfo.setTreeViewCellController(setupTreeViewController(directoryInfo));
                    fileInfos.add(directoryInfo);
                    TreeItem<FileInfoWrapper> subRootNode = new TreeItem<FileInfoWrapper>(
                            directoryInfo);
                    subRootNode.setExpanded(true);
                    recusiveIterateDirectory(subRootNode, fileInfos, path);
                    node.getChildren().add(subRootNode);
                } else {
                    boolean filterMatch = filterMatch(path.toFile().getAbsolutePath());
                    FileInfoWrapper fileInfo = new FileInfoWrapper(path.toFile().getAbsolutePath(),
                            "", "", filterMatch, false);
                    fileInfo.setTreeViewCellController(setupTreeViewController(fileInfo));
                    fileInfos.add(fileInfo);
                    node.getChildren().add(new TreeItem<FileInfoWrapper>(fileInfo));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileInfos;
    }

    private boolean filterMatch(String filePath) {
        String extension = Utils.getFileNameExtension(filePath);
        boolean matchExtension = false;
        for (String filterExtension : filterExtensions) {
            if (filterExtension.equals("*") || extension.equalsIgnoreCase(filterExtension)) {
                matchExtension = true;
                break;
            }
        }
        return matchExtension;
    }

    private String[] getDefaultFilterExtensions() {
        List<String> defaultFilterExtension = new ArrayList<>();
        if (filterAllEnable.get()) {
            defaultFilterExtension.add("*");
        }
        if (filterJavaEnable.get()) {
            defaultFilterExtension.add("java");
        }
        if (filterPropertiesEnable.get()) {
            defaultFilterExtension.add("properties");
        }
        if (filterXMLEnable.get()) {
            defaultFilterExtension.add("xml");
        }
        return defaultFilterExtension.toArray(new String[defaultFilterExtension.size()]);
    }

    private TreeViewCellController setupTreeViewController(FileInfoWrapper fileInfo) {
        TreeViewCellController treeViewCellController = new TreeViewCellController(mainApp);
        treeViewCellController.getTreeViewCellCheckBox().textProperty()
                .bind(Bindings.createStringBinding(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return fileInfo.getFilePath().substring(
                                fileInfo.getFilePath().lastIndexOf(File.separator) + 1);
                    }

                }));
        treeViewCellController.getTreeViewCellCheckBox().selectedProperty()
                .bindBidirectional(fileInfo.getIsConvertProperty());
        if (!fileInfo.getIsDirectory()) {
            treeViewCellController.getTreeViewCellComboSource().itemsProperty()
                    .set(commonEncodingObservableList);
            treeViewCellController.getTreeViewCellComboTarget().itemsProperty()
                    .set(commonEncodingObservableList);
            treeViewCellController.getTreeViewCellComboSource().valueProperty()
                    .bindBidirectional(fileInfo.getSourceEncodingProperty());
            treeViewCellController.getTreeViewCellComboTarget().valueProperty()
                    .bindBidirectional(fileInfo.getTargetEncodingProperty());
        }
        treeViewCellController.getDetailContainer().visibleProperty()
                .bind(fileInfo.getIsDirectoryProperty().not());
        return treeViewCellController;
    }

    @FXML
    void handleCustomOutputAction(ActionEvent event) {
        setupCustomOutputEnable(customOutputEnable.get());
    }

    @FXML
    void handleOutputDirAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        String initialOpenLocation = null;
        try {
            String executingFilePath = MainApp.class.getProtectionDomain().getCodeSource()
                    .getLocation().getPath();
            String executingDirPath = new File(executingFilePath).getParent();
            initialOpenLocation = URLDecoder.decode(executingDirPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (initialOpenLocation == null) {
            initialOpenLocation = System.getProperty("user.home");
        }
        directoryChooser.setInitialDirectory(new File(initialOpenLocation));
        File selectedDirectory = directoryChooser.showDialog(this);
        if (selectedDirectory != null) {
            textFieldOutputDir.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    void handleStartAction(ActionEvent event) {
        new Thread(
                () -> {
                    // Start convert execution
                    Platform.runLater(() -> {
                        updateInternalStatue(RUNNING_STATUS.CONVERTION_EXECUTING);
                    });
                    fileInfoWrappers
                            .stream()
                            .filter(fileInfo -> {
                                return !fileInfo.getIsDirectory()
                                        && fileInfo.getIsConvert()
                                        && !fileInfo.getSourceEncoding().equals(
                                                fileInfo.getTargetEncoding())
                                        && fileInfo.getTargetEncoding().length() > 0;
                            })
                            .forEachOrdered(
                                    fileInfo -> {
                                        String sourceFilePath, targetFilePath;
                                        if (customOutputEnable.get()) {
                                            sourceFilePath = fileInfo.getFilePath();
                                            targetFilePath = Utils.getCustomConvertTargetFilePath(
                                                    textFieldInputDir.getText(),
                                                    textFieldOutputDir.getText(),
                                                    fileInfo.getFilePath());
                                            Utils.createDirIfNeeded(targetFilePath);
                                        } else {
                                            sourceFilePath = Utils.getDefaultConvertTargetFilePath(
                                                    textFieldInputDir.getText(),
                                                    fileInfo.getFilePath());
                                            targetFilePath = fileInfo.getFilePath();
                                            Utils.createDirIfNeeded(sourceFilePath);
                                            Utils.copyFile(targetFilePath, sourceFilePath);
                                        }
                                        Utils.convertEncoding(sourceFilePath, targetFilePath,
                                                fileInfo.getSourceEncoding(),
                                                fileInfo.getTargetEncoding());
                                    });
                    // End convert execution
                    Platform.runLater(() -> {
                        updateInternalStatue(RUNNING_STATUS.CONVERTION_COMPLETE);
                    });

                }).start();

    }

    @FXML
    void handleFilterAction(ActionEvent event) {
        if (fileInfoWrappers != null) {
            fileInfoWrappers
                    .stream()
                    .filter(fileInfo -> !fileInfo.getIsDirectory())
                    .forEachOrdered(
                            fileInfo -> {
                                String extension = Utils.getFileNameExtension(fileInfo
                                        .getFilePath());
                                if ((filterAllEnable.get())
                                        || (filterJavaEnable.get() && extension.equals("java"))
                                        || (filterPropertiesEnable.get() && extension
                                                .equals("properties"))
                                        || (filterXMLEnable.get() && extension.equals("xml"))) {
                                    fileInfo.setIsConvert(true);
                                } else {
                                    fileInfo.setIsConvert(false);
                                }
                            });
        }

    }

    @FXML
    void handleCustomFilterAction(ActionEvent event) {
        setupCustomFilterEnable(customFilterEnable.get());
    }

    @FXML
    void handleDoCustomFilter(ActionEvent event) {
        String[] splittedFilter = textFieldCustomFilter.getText().split("\\s*;\\s*");
        String[] filterExtensions = Utils.getFilterExtensions(splittedFilter);
        if (fileInfoWrappers != null && filterExtensions.length > 0) {
            fileInfoWrappers
                    .stream()
                    .filter(fileInfo -> !fileInfo.getIsDirectory())
                    .forEachOrdered(
                            fileInfo -> {
                                String extension = Utils.getFileNameExtension(fileInfo
                                        .getFilePath());
                                boolean matchExtension = false;
                                for (String filterExtension : filterExtensions) {
                                    if (filterExtension.equals("*")
                                            || extension.equalsIgnoreCase(filterExtension)) {
                                        matchExtension = true;
                                        break;
                                    }
                                }
                                fileInfo.setIsConvert(matchExtension);
                            });
        }
    }

    @FXML
    void handleExit(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    void handleLanguageChange(ActionEvent event) {
        RadioMenuItem item = (RadioMenuItem) event.getSource();
        Locale localeSelected = mainApp.locales.get(item.getText());
        if (!mainApp.getCurrentInUseLocale().equals(localeSelected)) {
            mainApp.setLocale(localeSelected);
            close();
            mainApp.initialize();
            mainApp.showMainPane();
        }
    }

    @FXML
    void handleThemeChange(ActionEvent event) {
        RadioMenuItem item = (RadioMenuItem) event.getSource();
        String themeSelected = mainApp.themes.get(item.getText());
        scene.getStylesheets().clear();
        scene.getStylesheets().add(themeSelected);
        mainApp.setTheme(themeSelected);
    }

    @FXML
    void handleAbout(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(i18nBundle.getString("app.dialog.about.title"));
        alert.setHeaderText(null);
        alert.setContentText(i18nBundle.getString("app.dialog.about.content_text"));

        alert.showAndWait();
    }

    private void setupInputProcessEnable(boolean enable) {
        comboBoxOutputEncoding.disableProperty().set(!enable);
        buttonStart.disableProperty().set(!enable);
    }

    private void setupCustomOutputEnable(boolean enable) {
        textFieldOutputDir.disableProperty().set(!enable);
        buttonOutputDir.disableProperty().set(!enable);
    }

    private void setupCustomFilterEnable(boolean enable) {
        // set default filter enable property
        checkBoxFilter1.disableProperty().set(enable);
        checkBoxFilter2.disableProperty().set(enable);
        checkBoxFilter3.disableProperty().set(enable);
        checkBoxFilter4.disableProperty().set(enable);
        // set custom filter enable property
        textFieldCustomFilter.disableProperty().set(!enable);
        buttonFilter.disableProperty().set(!enable);
    }

    enum RUNNING_STATUS {
        INITIALIZE_COMPLETE, CONVERTION_EXECUTING, CONVERTION_FAILED, CONVERTION_COMPLETE;
    }

    private void updateInternalStatue(RUNNING_STATUS status) {
        switch (status) {
        case INITIALIZE_COMPLETE:
            progressBarStatus.setProgress(0);
            labelStatus.setText(i18nBundle.getString("app.status.initialize_complete"));
            break;
        case CONVERTION_EXECUTING:
            progressBarStatus.setProgress(-1);
            labelStatus.setText(i18nBundle.getString("app.status.convertion_executing"));
            break;
        case CONVERTION_FAILED:
            progressBarStatus.setProgress(0);
            labelStatus.setText(i18nBundle.getString("app.status.convertion_failed"));
            break;
        case CONVERTION_COMPLETE:
            progressBarStatus.setProgress(1);
            labelStatus.setText(i18nBundle.getString("app.status.convertion_complete"));
            break;
        }
    }
}
