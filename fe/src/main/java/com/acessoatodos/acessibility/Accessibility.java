package com.acessoatodos.acessibility;

import lombok.Getter;

/**
 * Code of accessibilities in system
 */
@Getter
public enum Accessibility {

    ACCESS_RAMP(100), ADAPTED_WC(101), ELEVATOR(102),
    PANEL_BRAILE_ELEVATOR(103), ALLOWED_GUIDE_DOG(104), BRAILE_INFO(105);

    private final int value;

    Accessibility(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
