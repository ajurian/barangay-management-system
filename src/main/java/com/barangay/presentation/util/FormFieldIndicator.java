package com.barangay.presentation.util;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;

/**
 * Helper for consistently tagging form field labels as required or optional.
 */
public final class FormFieldIndicator {

    private static final String BASE_TEXT_KEY = "formFieldIndicator.baseText";
    private static final String ASTERISK_NODE_KEY = "formFieldIndicator.asteriskNode";
    private static final String ASTERISK_TEXT = "*";
    private static final String OPTIONAL_SUFFIX = " (optional)";
    private static final String REQUIRED_STYLE = "required-field-label";
    private static final String OPTIONAL_STYLE = "optional-field-label";

    private FormFieldIndicator() {
    }

    public static Label requiredLabel(String text) {
        return createLabel(text, FieldState.REQUIRED);
    }

    public static Label optionalLabel(String text) {
        return createLabel(text, FieldState.OPTIONAL);
    }

    public static void markRequired(Label label) {
        if (label != null) {
            updateState(label, FieldState.REQUIRED);
        }
    }

    public static void markOptional(Label label) {
        if (label != null) {
            updateState(label, FieldState.OPTIONAL);
        }
    }

    private static Label createLabel(String text, FieldState state) {
        Label label = new Label(text);
        label.getProperties().put(BASE_TEXT_KEY, text);
        updateState(label, state);
        return label;
    }

    private static void updateState(Label label, FieldState state) {
        Object base = label.getProperties().get(BASE_TEXT_KEY);
        String baseText = base instanceof String ? (String) base : label.getText();
        label.setGraphic(null);
        label.setContentDisplay(ContentDisplay.LEFT);
        label.setGraphicTextGap(4);
        label.getStyleClass().removeAll(REQUIRED_STYLE, OPTIONAL_STYLE);
        if (state == FieldState.REQUIRED) {
            label.setText(baseText);
            label.setGraphic(getOrCreateAsterisk(label));
            label.setContentDisplay(ContentDisplay.RIGHT);
            label.getStyleClass().add(REQUIRED_STYLE);
        } else {
            label.setText(baseText + OPTIONAL_SUFFIX);
            label.getStyleClass().add(OPTIONAL_STYLE);
        }
    }

    private static Label getOrCreateAsterisk(Label owner) {
        Object existing = owner.getProperties().get(ASTERISK_NODE_KEY);
        if (existing instanceof Label) {
            return (Label) existing;
        }
        Label asterisk = new Label(ASTERISK_TEXT);
        asterisk.getStyleClass().add("required-asterisk");
        asterisk.setFocusTraversable(false);
        owner.getProperties().put(ASTERISK_NODE_KEY, asterisk);
        return asterisk;
    }

    private enum FieldState {
        REQUIRED,
        OPTIONAL
    }
}
