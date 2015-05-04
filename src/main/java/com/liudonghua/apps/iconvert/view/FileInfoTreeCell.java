package com.liudonghua.apps.iconvert.view;

import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

import com.liudonghua.apps.iconvert.model.FileInfoWrapper;

public class FileInfoTreeCell extends TreeCell<FileInfoWrapper> {

    protected void updateItem(FileInfoWrapper item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            TreeViewCellController treeViewCellController = item.getTreeViewCellController();
            treeViewCellController.getTreeViewCellCheckBox().setOnAction(
                    (event) -> updateState(event));
            setGraphic(treeViewCellController.getContainer());
        }
        setText(null);
    }

    // The following code refered to the implementation of CheckBoxTreeItem
    private boolean updateLock = false;

    private void updateState(ActionEvent event) {
        TreeItem<FileInfoWrapper> treeItem = getTreeItem();
        if (treeItem.getValue().getTreeViewCellController().getTreeViewCellCheckBox()
                .indeterminateProperty().get())
            return;

        boolean firstLock = !updateLock;

        // toggle parent (recursively up to root)
        updateLock = true;
        updateUpwards(treeItem);

        if (firstLock)
            updateLock = false;

        // toggle children (recursively down to leaf)
        if (updateLock)
            return;
        updateDownwards(treeItem);
    }

    private void updateUpwards(TreeItem<FileInfoWrapper> treeItem) {
        TreeItem<FileInfoWrapper> parent = (TreeItem<FileInfoWrapper>) treeItem.getParent();
        if (parent != null) {
            CheckBox parentCheckBox = parent.getValue().getTreeViewCellController()
                    .getTreeViewCellCheckBox();
            int selectCount = 0;
            int indeterminateCount = 0;
            for (TreeItem<FileInfoWrapper> child : parent.getChildren()) {
                CheckBox cbti = child.getValue().getTreeViewCellController()
                        .getTreeViewCellCheckBox();
                selectCount += cbti.isSelected() && !cbti.isIndeterminate() ? 1 : 0;
                indeterminateCount += cbti.isIndeterminate() ? 1 : 0;
            }

            if (selectCount == parent.getChildren().size()) {
                parentCheckBox.setSelected(true);
                parentCheckBox.setIndeterminate(false);
            } else if (selectCount == 0 && indeterminateCount == 0) {
                parentCheckBox.setSelected(false);
                parentCheckBox.setIndeterminate(false);
            } else {
                parentCheckBox.setIndeterminate(true);
            }
            // need to recursively up to root
            updateUpwards(parent);
        }
    }

    private void updateDownwards(TreeItem<FileInfoWrapper> treeItem) {
        if (!treeItem.isLeaf()) {
            for (TreeItem<FileInfoWrapper> child : treeItem.getChildren()) {
                CheckBox cbti = child.getValue().getTreeViewCellController()
                        .getTreeViewCellCheckBox();
                cbti.setSelected(treeItem.getValue().getTreeViewCellController()
                        .getTreeViewCellCheckBox().isSelected());
                // need to recursively down to leaf
                updateDownwards(child);
            }
        }
    }
}
