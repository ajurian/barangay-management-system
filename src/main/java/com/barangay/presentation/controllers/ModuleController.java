package com.barangay.presentation.controllers;

import com.barangay.infrastructure.config.DIContainer;

/**
 * Contract for module screens within the main workspace.
 */
public interface ModuleController {
    /**
     * Provide dependencies shared across modules.
     */
    void init(DIContainer container, MainLayoutController mainLayoutController);

    /**
     * Refresh data when the module becomes visible.
     */
    void refresh();
}
