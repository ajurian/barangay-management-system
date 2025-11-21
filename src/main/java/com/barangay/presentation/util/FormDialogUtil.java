package com.barangay.presentation.util;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utility helpers to keep form dialogs open until validation passes.
 */
public final class FormDialogUtil {

    private FormDialogUtil() {
    }

    /**
     * Prevents the dialog from closing when validation fails.
     *
     * @param dialog    dialog to guard
     * @param validator validator returning an error message when invalid
     * @param title     title for the error alert (fallback if null)
     */
    public static void keepOpenOnValidationFailure(Dialog<?> dialog,
            Supplier<Optional<String>> validator,
            String title) {
        if (dialog == null || validator == null) {
            return;
        }
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton == null) {
            return;
        }
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            Optional<String> error = validator.get();
            if (error.isPresent()) {
                event.consume();
                String effectiveTitle = title != null ? title : "Form Validation";
                DialogUtil.showError(effectiveTitle, error.get());
            }
        });
    }
}
