package com.liudonghua.apps.iconvert.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import com.liudonghua.apps.iconvert.view.TreeViewCellController;

public class FileInfoWrapper {
    private StringProperty filePath = new SimpleStringProperty();
    private StringProperty sourceEncoding = new SimpleStringProperty();
    private StringProperty targetEncoding = new SimpleStringProperty();
    private BooleanProperty isConvert = new SimpleBooleanProperty();
    private BooleanProperty isDirectory = new SimpleBooleanProperty();
    private TreeViewCellController treeViewCellController;

    public FileInfoWrapper() {
        this("", "", "", false, false);
    }

    public FileInfoWrapper(String filePath) {
        this(filePath, "", "", false, false);
    }

    public FileInfoWrapper(String filePath, String sourceEncoding, String targetEncoding,
            boolean isConvert, boolean isDirectory) {
        this.filePath.set(filePath);
        this.sourceEncoding.set(sourceEncoding);
        this.targetEncoding.set(targetEncoding);
        this.isConvert.set(isConvert);
        this.isDirectory.set(isDirectory);
    }

    public final String getFilePath() {
        return filePath.get();
    }

    public final void setFilePath(String value) {
        filePath.set(value);
    }

    public StringProperty getFilePathProperty() {
        return filePath;
    }

    public final String getSourceEncoding() {
        return sourceEncoding.get();
    }

    public final void setSourceEncoding(String value) {
        sourceEncoding.set(value);
    }

    public StringProperty getSourceEncodingProperty() {
        return sourceEncoding;
    }

    public final String getTargetEncoding() {
        return targetEncoding.get();
    }

    public final void setTargetEncoding(String value) {
        targetEncoding.set(value);
    }

    public StringProperty getTargetEncodingProperty() {
        return targetEncoding;
    }

    public final boolean getIsConvert() {
        return isConvert.get();
    }

    public final void setIsConvert(boolean value) {
        isConvert.set(value);
    }

    public BooleanProperty getIsConvertProperty() {
        return isConvert;
    }

    public final boolean getIsDirectory() {
        return isDirectory.get();
    }

    public final void setIsDirectory(boolean value) {
        isDirectory.set(value);
    }

    public BooleanProperty getIsDirectoryProperty() {
        return isDirectory;
    }

    public TreeViewCellController getTreeViewCellController() {
        return treeViewCellController;
    }

    public void setTreeViewCellController(TreeViewCellController treeViewCellController) {
        this.treeViewCellController = treeViewCellController;
    }

    @Override
    public String toString() {
        return filePath.get();
    }

}
