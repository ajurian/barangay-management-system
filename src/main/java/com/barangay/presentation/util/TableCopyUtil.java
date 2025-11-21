package com.barangay.presentation.util;

import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * Utility helpers for adding clipboard copy context menus to tables.
 */
public final class TableCopyUtil {

    private TableCopyUtil() {
    }

    public static <T> void attachCopyContextMenu(TableView<T> table, Function<T, String> valueProvider,
            String menuLabel) {
        if (table == null || valueProvider == null || menuLabel == null) {
            return;
        }

        MenuItem copyItem = new MenuItem(menuLabel);
        copyItem.disableProperty().bind(Bindings.isNull(table.getSelectionModel().selectedItemProperty()));
        copyItem.setOnAction(event -> {
            T selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            String value = valueProvider.apply(selected);
            if (value == null || value.isBlank()) {
                return;
            }
            ClipboardContent content = new ClipboardContent();
            content.putString(value);
            Clipboard.getSystemClipboard().setContent(content);
        });

        table.setContextMenu(new ContextMenu(copyItem));
    }
}
